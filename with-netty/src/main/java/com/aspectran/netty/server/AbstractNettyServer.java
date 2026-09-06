/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.netty.server;

import com.aspectran.core.component.session.SessionManager;
import com.aspectran.netty.server.handler.NettyChannelInitializer;
import com.aspectran.netty.server.handler.accesslog.NettyAccessLogHandler;
import com.aspectran.netty.server.handler.encoding.NettyEncodingHandler;
import com.aspectran.netty.server.handler.logging.PathBasedLoggingGroupHandler;
import com.aspectran.netty.server.handler.resource.NettyResourceHandler;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.lifecycle.AbstractLifeCycle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Abstract base implementation of the {@link NettyServer} interface.
 * <p>Manages the lifecycle of Netty event loop groups, native transport detection
 * (Epoll on Linux, KQueue on macOS, NIO fallback), Java 21 Virtual Threads dispatching,
 * server bootstrapping, and listener channels.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public abstract class AbstractNettyServer extends AbstractLifeCycle implements NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyServer.class);

    private final List<NettyListenerConfig> listeners = new ArrayList<>();

    private final List<Channel> activeChannels = new CopyOnWriteArrayList<>();

    private final NettyContextRouter contextRouter = new NettyContextRouter();

    private boolean autoStart = true;

    private boolean virtualThreads = true;

    private boolean nativeTransport = true;

    private String threadNamePrefix = "netty-task-";

    private String workerName;

    private int bossThreads = 1;

    private int workerThreads = 0;

    private int shutdownTimeoutSecs = 5;

    private int maxContentLength = 10 * 1024 * 1024; // 10MB

    private boolean contentCompression;

    private NettyEncodingHandler encodingHandler;

    private NettyResourceHandler resourceHandler;

    private NettyAccessLogHandler accessLogHandler;

    private PathBasedLoggingGroupHandler loggingGroupHandler;

    private int idleTimeout;

    private boolean proxyAddressForwarding;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private ExecutorService requestExecutor;

    /**
     * Returns the list of configured listener configurations.
     * @return the list of listener configurations
     */
    public List<NettyListenerConfig> getListeners() {
        return listeners;
    }

    /**
     * Sets the listener configurations for this server.
     * @param listeners the listener configurations
     */
    public void setListeners(NettyListenerConfig... listeners) {
        this.listeners.clear();
        if (listeners != null) {
            Collections.addAll(this.listeners, listeners);
        }
    }

    /**
     * Sets the list of listener configurations for this server.
     * @param listeners the list of listener configurations
     */
    public void setListeners(List<NettyListenerConfig> listeners) {
        this.listeners.clear();
        if (listeners != null) {
            this.listeners.addAll(listeners);
        }
    }

    /**
     * Adds a listener configuration to this server.
     * @param listenerConfig the listener configuration to add
     */
    public void addListener(NettyListenerConfig listenerConfig) {
        Assert.notNull(listenerConfig, "listenerConfig must not be null");
        this.listeners.add(listenerConfig);
    }

    @Override
    public NettyContextRouter getContextRouter() {
        return contextRouter;
    }

    /**
     * Returns the list of registered {@link NettyContext}s.
     * @return the list of Netty contexts
     */
    public List<NettyContext> getContexts() {
        return contextRouter.getContexts();
    }

    /**
     * Sets the {@link NettyContext}s deployed on this server.
     * @param contexts the Netty contexts
     */
    public void setContexts(NettyContext... contexts) {
        contextRouter.setContexts(contexts);
    }

    /**
     * Sets the list of {@link NettyContext}s deployed on this server.
     * @param contexts the list of Netty contexts
     */
    public void setContexts(List<NettyContext> contexts) {
        contextRouter.setContexts(contexts);
    }

    /**
     * Adds a {@link NettyContext} to be deployed on this server.
     * @param context the Netty context to add
     */
    public void addContext(NettyContext context) {
        contextRouter.addContext(context);
    }

    /**
     * Returns whether the server should automatically start when initialized.
     * @return {@code true} if auto-start is enabled, {@code false} otherwise
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Sets whether the server should automatically start when initialized.
     * @param autoStart {@code true} to enable auto-start, {@code false} otherwise
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    @Override
    public boolean isVirtualThreads() {
        return virtualThreads;
    }

    /**
     * Sets whether Java 21 Virtual Threads should be used for request dispatching.
     * @param virtualThreads {@code true} to use virtual threads, {@code false} for standard thread pool
     */
    public void setVirtualThreads(boolean virtualThreads) {
        this.virtualThreads = virtualThreads;
    }

    /**
     * Returns whether native transport (Epoll on Linux, KQueue on macOS) is enabled.
     * @return {@code true} if native transport is enabled, {@code false} otherwise
     */
    public boolean isNativeTransport() {
        return nativeTransport;
    }

    /**
     * Sets whether native transport (Epoll on Linux, KQueue on macOS) should be enabled.
     * @param nativeTransport {@code true} to enable native transport, {@code false} for standard NIO
     */
    public void setNativeTransport(boolean nativeTransport) {
        this.nativeTransport = nativeTransport;
    }

    @Override
    public int getBossThreads() {
        return bossThreads;
    }

    /**
     * Sets the number of boss threads to accept incoming connections.
     * @param bossThreads the number of boss threads
     */
    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    @Override
    public int getWorkerThreads() {
        return workerThreads;
    }

    /**
     * Sets the number of worker threads for I/O event processing.
     * <p>A value of {@code 0} uses Netty's default (2 * available processor count).</p>
     * @param workerThreads the number of worker threads
     */
    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    /**
     * Returns the graceful shutdown timeout in seconds.
     * @return the shutdown timeout in seconds
     */
    public int getShutdownTimeoutSecs() {
        return shutdownTimeoutSecs;
    }

    /**
     * Sets the graceful shutdown timeout in seconds.
     * @param shutdownTimeoutSecs the shutdown timeout in seconds
     */
    public void setShutdownTimeoutSecs(int shutdownTimeoutSecs) {
        this.shutdownTimeoutSecs = shutdownTimeoutSecs;
    }

    /**
     * Returns the maximum content length for aggregated HTTP requests in bytes.
     * @return the maximum content length in bytes
     */
    public int getMaxContentLength() {
        return maxContentLength;
    }

    /**
     * Sets the maximum content length for aggregated HTTP requests in bytes.
     * @param maxContentLength the maximum content length in bytes
     */
    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    /**
     * Returns whether HTTP response content compression (gzip/deflate) is enabled.
     * @return {@code true} if compression is enabled, {@code false} otherwise
     */
    public boolean isContentCompression() {
        return contentCompression;
    }

    /**
     * Sets whether HTTP response content compression (gzip/deflate) should be enabled.
     * @param contentCompression {@code true} to enable compression, {@code false} otherwise
     */
    public void setContentCompression(boolean contentCompression) {
        this.contentCompression = contentCompression;
    }

    /**
     * Returns the encoding handler for setting request/response character encoding.
     * @return the encoding handler, or {@code null} if not configured
     */
    public NettyEncodingHandler getEncodingHandler() {
        return encodingHandler;
    }

    /**
     * Sets the encoding handler for setting request/response character encoding.
     * @param encodingHandler the encoding handler
     */
    public void setEncodingHandler(NettyEncodingHandler encodingHandler) {
        this.encodingHandler = encodingHandler;
    }

    /**
     * Returns the server-level fallback static resource handler.
     * @return the resource handler, or {@code null} if not configured
     */
    public NettyResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    /**
     * Sets the server-level fallback static resource handler.
     * @param resourceHandler the resource handler
     */
    public void setResourceHandler(NettyResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;
    }

    /**
     * Returns the access log handler for recording HTTP requests.
     * @return the access log handler, or {@code null} if not configured
     */
    public NettyAccessLogHandler getAccessLogHandler() {
        return accessLogHandler;
    }

    /**
     * Sets the access log handler for recording HTTP requests.
     * @param accessLogHandler the access log handler
     */
    public void setAccessLogHandler(NettyAccessLogHandler accessLogHandler) {
        this.accessLogHandler = accessLogHandler;
    }

    /**
     * Returns the path-based logging group handler for MDC-based request logging segregation.
     * @return the logging group handler, or {@code null} if not configured
     */
    public PathBasedLoggingGroupHandler getLoggingGroupHandler() {
        return loggingGroupHandler;
    }

    /**
     * Sets the path-based logging group handler for MDC-based request logging segregation.
     * @param loggingGroupHandler the logging group handler
     */
    public void setLoggingGroupHandler(PathBasedLoggingGroupHandler loggingGroupHandler) {
        this.loggingGroupHandler = loggingGroupHandler;
    }

    /**
     * Returns the HTTP connection idle timeout in milliseconds.
     * @return the idle timeout in milliseconds
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets the HTTP connection idle timeout in milliseconds.
     * <p>If set to a positive value, an {@link io.netty.handler.timeout.IdleStateHandler}
     * is registered in the HTTP pipeline to close idle connections when no incoming request
     * is received within this duration.</p>
     * @param idleTimeout the idle timeout in milliseconds
     */
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = Math.max(0, idleTimeout);
    }

    /**
     * Returns the HTTP connection idle timeout in seconds.
     * @return the idle timeout in seconds
     */
    public int getIdleTimeoutSecs() {
        return idleTimeout / 1000;
    }

    /**
     * Sets the HTTP connection idle timeout in seconds.
     * @param idleTimeoutSecs the idle timeout in seconds
     */
    public void setIdleTimeoutSecs(int idleTimeoutSecs) {
        setIdleTimeout(idleTimeoutSecs * 1000);
    }

    /**
     * Returns whether proxy address forwarding (honoring X-Forwarded-* headers) is enabled.
     * @return true if proxy address forwarding is enabled; false otherwise
     */
    public boolean isProxyAddressForwarding() {
        return proxyAddressForwarding;
    }

    /**
     * Sets whether proxy address forwarding (honoring X-Forwarded-* headers) is enabled.
     * @param proxyAddressForwarding true to enable proxy address forwarding; false otherwise
     */
    public void setProxyAddressForwarding(boolean proxyAddressForwarding) {
        this.proxyAddressForwarding = proxyAddressForwarding;
    }

    /**
     * Configures URL path patterns mapped to logging group names for request segregation.
     * @param pathPatternsByGroupName a map where keys are group names and values are comma-separated path patterns
     */
    public void setPathPatternsByGroupName(Map<String, String> pathPatternsByGroupName) {
        if (pathPatternsByGroupName != null) {
            if (this.loggingGroupHandler == null) {
                this.loggingGroupHandler = new PathBasedLoggingGroupHandler();
            }
            this.loggingGroupHandler.setPathPatternsByGroupName(pathPatternsByGroupName);
        } else if (this.loggingGroupHandler != null) {
            this.loggingGroupHandler.setPathPatternsByGroupName(null);
        }
    }

    /**
     * Returns the thread name prefix used for request dispatching threads.
     * @return the thread name prefix
     */
    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    /**
     * Sets the thread name prefix used for request dispatching threads.
     * @param threadNamePrefix the thread name prefix
     */
    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public String getWorkerName() {
        if (workerName != null) {
            return workerName;
        }
        if (StringUtils.hasText(threadNamePrefix)) {
            String prefix = threadNamePrefix.trim();
            if (prefix.endsWith("-task-")) {
                return prefix.substring(0, prefix.length() - "-task-".length());
            }
            if (prefix.endsWith("-")) {
                return prefix.substring(0, prefix.length() - 1);
            }
            return prefix;
        }
        return "netty";
    }

    /**
     * Sets the worker name and updates the thread name prefix accordingly.
     * @param workerName the worker name
     */
    public void setWorkerName(String workerName) {
        this.workerName = workerName;
        if (StringUtils.hasText(workerName)) {
            this.threadNamePrefix = workerName.trim() + "-task-";
        }
    }

    @Override
    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    @Override
    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    @Override
    public ExecutorService getRequestExecutor() {
        return requestExecutor;
    }

    /**
     * Sets a custom {@link ExecutorService} for request dispatching.
     * @param requestExecutor the executor service
     */
    public void setRequestExecutor(ExecutorService requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() {
        if (requestExecutor instanceof ThreadPoolExecutor tpe) {
            return tpe;
        }
        if (requestExecutor instanceof TrackingExecutor tracking) {
            if (tracking.getDelegate() instanceof ThreadPoolExecutor tpe) {
                return tpe;
            }
        }
        return null;
    }

    @Override
    public int getActiveRequests() {
        if (requestExecutor instanceof TrackingExecutor tracking) {
            return tracking.getActiveCount();
        }
        if (requestExecutor instanceof ThreadPoolExecutor tpe) {
            return tpe.getActiveCount();
        }
        return 0;
    }

    @Override
    public int getPeakRequests() {
        if (requestExecutor instanceof TrackingExecutor tracking) {
            return tracking.getPeakCount();
        }
        if (requestExecutor instanceof ThreadPoolExecutor tpe) {
            return tpe.getLargestPoolSize();
        }
        return 0;
    }

    @Override
    public long getTotalRequests() {
        if (requestExecutor instanceof TrackingExecutor tracking) {
            return tracking.getTotalCount();
        }
        if (requestExecutor instanceof ThreadPoolExecutor tpe) {
            return tpe.getTaskCount();
        }
        return 0;
    }

    @Override
    public List<Channel> getActiveChannels() {
        return Collections.unmodifiableList(activeChannels);
    }

    @Override
    public int getActivePort() {
        return getActivePort(0);
    }

    @Override
    public int getActivePort(int index) {
        if (index >= 0 && index < listeners.size()) {
            return listeners.get(index).getActualPort();
        }
        return -1;
    }

    @Override
    public SessionManager getSessionManager() {
        return getSessionManager(null);
    }

    @Override
    public SessionManager getSessionManager(String contextPath) {
        NettyContext context = contextRouter.getContext(contextPath);
        return (context != null ? context.getSessionManager() : null);
    }

    @Override
    public SessionManager getSessionManagerByPath(String path) {
        return getSessionManager(path);
    }

    @Override
    protected void doStart() throws Exception {
        if (contextRouter.isEmpty()) {
            throw new IllegalStateException("No NettyContext configured on " + this);
        }

        for (NettyContext context : contextRouter.getContexts()) {
            if (!context.isStarted()) {
                if (proxyAddressForwarding && !context.isProxyAddressForwarding()) {
                    context.setProxyAddressForwarding(true);
                }
                context.start();
            }
        }

        if (listeners.isEmpty()) {
            listeners.add(new NettyListenerConfig(8080));
        }

        ThreadFactory bossThreadFactory = new DefaultThreadFactory("netty-boss", true);
        ThreadFactory workerThreadFactory = new DefaultThreadFactory("netty-worker", true);

        Class<? extends ServerSocketChannel> channelClass;
        if (nativeTransport && isEpollAvailable()) {
            bossGroup = EpollSupport.createEventLoopGroup(bossThreads, bossThreadFactory);
            workerGroup = EpollSupport.createEventLoopGroup(workerThreads, workerThreadFactory);
            channelClass = EpollSupport.getServerSocketChannelClass();
            logger.info("Netty native Epoll transport is active");
        } else if (nativeTransport && isKQueueAvailable()) {
            bossGroup = KQueueSupport.createEventLoopGroup(bossThreads, bossThreadFactory);
            workerGroup = KQueueSupport.createEventLoopGroup(workerThreads, workerThreadFactory);
            channelClass = KQueueSupport.getServerSocketChannelClass();
            logger.info("Netty native KQueue transport is active");
        } else {
            bossGroup = new MultiThreadIoEventLoopGroup(bossThreads, bossThreadFactory, NioIoHandler.newFactory());
            workerGroup = new MultiThreadIoEventLoopGroup(workerThreads, workerThreadFactory, NioIoHandler.newFactory());
            channelClass = NioServerSocketChannel.class;
            logger.info("Netty NIO transport is active");
        }

        if (requestExecutor == null) {
            String prefix = (StringUtils.hasText(threadNamePrefix) ? threadNamePrefix : "netty-task-");
            if (!prefix.endsWith("-") && !prefix.endsWith(" ")) {
                prefix += "-";
            }
            if (virtualThreads) {
                ThreadFactory threadFactory = Thread.ofVirtual()
                        .name(prefix, 1)
                        .factory();
                requestExecutor = new TrackingExecutor(Executors.newThreadPerTaskExecutor(threadFactory));
                logger.info("Java 21 Virtual Threads enabled for Netty request dispatching (prefix: '{}')", prefix);
            } else {
                String poolName = (prefix.endsWith("-") ? prefix.substring(0, prefix.length() - 1) : prefix);
                requestExecutor = new TrackingExecutor(Executors.newCachedThreadPool(new DefaultThreadFactory(poolName, true)));
            }
        } else if (!(requestExecutor instanceof TrackingExecutor)) {
            requestExecutor = new TrackingExecutor(requestExecutor);
        }

        activeChannels.clear();
        try {
            for (NettyListenerConfig listenerConfig : listeners) {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(channelClass)
                        .option(ChannelOption.SO_BACKLOG, listenerConfig.getBacklog())
                        .option(ChannelOption.SO_REUSEADDR, listenerConfig.isReuseAddress())
                        .childOption(ChannelOption.TCP_NODELAY, listenerConfig.isTcpNoDelay())
                        .childOption(ChannelOption.SO_KEEPALIVE, listenerConfig.isKeepAlive())
                        .childHandler(createChannelInitializer(listenerConfig));

                ChannelFuture future = bootstrap.bind(listenerConfig.getHost(), listenerConfig.getPort()).sync();
                Channel channel = future.channel();
                activeChannels.add(channel);

                if (channel.localAddress() instanceof InetSocketAddress inetSocketAddress) {
                    listenerConfig.setActualPort(inetSocketAddress.getPort());
                    logger.info("Netty listener bound to {}:{}", listenerConfig.getHost(), listenerConfig.getActualPort());
                }
            }
            logger.info("Netty server started successfully (version: {})", NettyServer.getVersion());
        } catch (Exception e) {
            doStop();
            throw e;
        }
    }

    /**
     * Creates a {@link ChannelInitializer} configured with the HTTP pipeline handlers for the specified listener.
     * @param listenerConfig the listener configuration
     * @return the configured channel initializer
     */
    protected ChannelInitializer<SocketChannel> createChannelInitializer(NettyListenerConfig listenerConfig) {
        return new NettyChannelInitializer(
                listenerConfig,
                contextRouter,
                requestExecutor,
                resourceHandler,
                accessLogHandler,
                loggingGroupHandler,
                encodingHandler,
                maxContentLength,
                contentCompression,
                idleTimeout,
                proxyAddressForwarding
        );
    }

    @Override
    protected void doStop() {
        for (NettyContext context : contextRouter.getContexts()) {
            if (context.isStarted()) {
                try {
                    context.stop();
                } catch (Exception e) {
                    logger.warn("Failed to stop NettyContext: {}", context, e);
                }
            }
        }

        for (Channel channel : activeChannels) {
            try {
                if (channel.isOpen()) {
                    channel.close().syncUninterruptibly();
                }
            } catch (Exception e) {
                logger.warn("Failed to close channel: {}", channel, e);
            }
        }
        activeChannels.clear();

        if (requestExecutor != null) {
            requestExecutor.shutdown();
            try {
                if (!requestExecutor.awaitTermination(shutdownTimeoutSecs, TimeUnit.SECONDS)) {
                    requestExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                requestExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            requestExecutor = null;
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully(0, shutdownTimeoutSecs, TimeUnit.SECONDS);
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(0, shutdownTimeoutSecs, TimeUnit.SECONDS);
            workerGroup = null;
        }

        logger.info("Netty server stopped");
    }

    private boolean isEpollAvailable() {
        try {
            if (EpollSupport.isAvailable()) {
                return true;
            }
            if (logger.isDebugEnabled()) {
                Throwable cause = EpollSupport.unavailabilityCause();
                logger.debug("Netty native Epoll transport is not available: {}",
                        cause != null ? cause.getMessage() : "unknown reason");
            }
        } catch (Throwable t) {
            logger.debug("Netty native Epoll transport is not available: {}", t.getMessage());
        }
        return false;
    }

    private boolean isKQueueAvailable() {
        try {
            if (KQueueSupport.isAvailable()) {
                return true;
            }
            if (logger.isDebugEnabled()) {
                Throwable cause = KQueueSupport.unavailabilityCause();
                logger.debug("Netty native KQueue transport is not available: {}",
                        cause != null ? cause.getMessage() : "unknown reason");
            }
        } catch (Throwable t) {
            logger.debug("Netty native KQueue transport is not available: {}", t.getMessage());
        }
        return false;
    }

    private static class EpollSupport {

        static boolean isAvailable() {
            try {
                return Epoll.isAvailable();
            } catch (Throwable t) {
                return false;
            }
        }

        static Throwable unavailabilityCause() {
            try {
                return Epoll.unavailabilityCause();
            } catch (Throwable t) {
                return t;
            }
        }

        @NonNull
        static EventLoopGroup createEventLoopGroup(int threads, ThreadFactory threadFactory) {
            return new MultiThreadIoEventLoopGroup(threads, threadFactory, EpollIoHandler.newFactory());
        }

        static Class<? extends ServerSocketChannel> getServerSocketChannelClass() {
            return EpollServerSocketChannel.class;
        }

    }

    private static class KQueueSupport {

        static boolean isAvailable() {
            try {
                return KQueue.isAvailable();
            } catch (Throwable t) {
                return false;
            }
        }

        static Throwable unavailabilityCause() {
            try {
                return KQueue.unavailabilityCause();
            } catch (Throwable t) {
                return t;
            }
        }

        @NonNull
        static EventLoopGroup createEventLoopGroup(int threads, ThreadFactory threadFactory) {
            return new MultiThreadIoEventLoopGroup(threads, threadFactory, KQueueIoHandler.newFactory());
        }

        static Class<? extends ServerSocketChannel> getServerSocketChannelClass() {
            return KQueueServerSocketChannel.class;
        }

    }

    /**
     * An {@link ExecutorService} wrapper that tracks active, peak, and total request counts.
     */
    public static class TrackingExecutor extends AbstractExecutorService {

        private final ExecutorService delegate;

        private final AtomicInteger activeCount = new AtomicInteger();

        private final AtomicInteger peakCount = new AtomicInteger();

        private final LongAdder totalCount = new LongAdder();

        /**
         * Constructs a new {@code TrackingExecutor} wrapping the specified delegate executor.
         * @param delegate the underlying executor service
         */
        public TrackingExecutor(@NonNull ExecutorService delegate) {
            this.delegate = delegate;
        }

        /**
         * Returns the underlying delegate {@link ExecutorService}.
         * @return the delegate executor service
         */
        public ExecutorService getDelegate() {
            return delegate;
        }

        /**
         * Returns the number of currently active tasks.
         * @return the active task count
         */
        public int getActiveCount() {
            return activeCount.get();
        }

        /**
         * Returns the peak number of concurrently active tasks observed.
         * @return the peak active task count
         */
        public int getPeakCount() {
            return peakCount.get();
        }

        /**
         * Returns the total number of tasks submitted since initialization.
         * @return the total task count
         */
        public long getTotalCount() {
            return totalCount.sum();
        }

        @Override
        public void execute(@NonNull Runnable command) {
            int current = activeCount.incrementAndGet();
            peakCount.accumulateAndGet(current, Math::max);
            totalCount.increment();
            try {
                delegate.execute(() -> {
                    try {
                        command.run();
                    } finally {
                        activeCount.decrementAndGet();
                    }
                });
            } catch (Throwable t) {
                activeCount.decrementAndGet();
                throw t;
            }
        }

        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @NonNull
        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

    }

}
