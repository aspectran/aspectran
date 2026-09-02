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

import com.aspectran.netty.server.handler.NettyAccessLogHandler;
import com.aspectran.netty.server.handler.NettyChannelInitializer;
import com.aspectran.netty.server.handler.NettyResourceHandler;
import com.aspectran.netty.server.handler.logging.PathBasedLoggingGroupHandler;
import com.aspectran.netty.service.NettyService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.lifecycle.AbstractLifeCycle;
import io.netty.bootstrap.ServerBootstrap;
import com.aspectran.netty.service.DefaultNettyService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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

    private NettyService nettyService;

    private boolean autoStart = true;

    private boolean virtualThreads = true;

    private String threadNamePrefix = "netty-task-";

    private int bossThreads = 1;

    private int workerThreads = 0;

    private int shutdownTimeoutSecs = 5;

    private int maxContentLength = 10 * 1024 * 1024; // 10MB

    private boolean contentCompression;

    private NettyResourceHandler resourceHandler;

    private NettyAccessLogHandler accessLogHandler;

    private PathBasedLoggingGroupHandler loggingGroupHandler;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private ExecutorService requestExecutor;

    public List<NettyListenerConfig> getListeners() {
        return listeners;
    }

    public void setListeners(NettyListenerConfig... listeners) {
        this.listeners.clear();
        if (listeners != null) {
            Collections.addAll(this.listeners, listeners);
        }
    }

    public void setListeners(List<NettyListenerConfig> listeners) {
        this.listeners.clear();
        if (listeners != null) {
            this.listeners.addAll(listeners);
        }
    }

    public void addListener(NettyListenerConfig listenerConfig) {
        Assert.notNull(listenerConfig, "listenerConfig must not be null");
        this.listeners.add(listenerConfig);
    }

    public NettyContextRouter getContextRouter() {
        return contextRouter;
    }

    public List<NettyContext> getContexts() {
        return contextRouter.getContexts();
    }

    public void setContexts(NettyContext... contexts) {
        contextRouter.setContexts(contexts);
    }

    public void setContexts(List<NettyContext> contexts) {
        contextRouter.setContexts(contexts);
    }

    public void addContext(NettyContext context) {
        contextRouter.addContext(context);
    }

    public NettyService getNettyService() {
        return nettyService;
    }

    public void setNettyService(NettyService nettyService) {
        this.nettyService = nettyService;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean isVirtualThreads() {
        return virtualThreads;
    }

    public void setVirtualThreads(boolean virtualThreads) {
        this.virtualThreads = virtualThreads;
    }

    public int getBossThreads() {
        return bossThreads;
    }

    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getShutdownTimeoutSecs() {
        return shutdownTimeoutSecs;
    }

    public void setShutdownTimeoutSecs(int shutdownTimeoutSecs) {
        this.shutdownTimeoutSecs = shutdownTimeoutSecs;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public boolean isContentCompression() {
        return contentCompression;
    }

    public void setContentCompression(boolean contentCompression) {
        this.contentCompression = contentCompression;
    }

    public NettyResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    public void setResourceHandler(NettyResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;
    }

    public NettyAccessLogHandler getAccessLogHandler() {
        return accessLogHandler;
    }

    public void setAccessLogHandler(NettyAccessLogHandler accessLogHandler) {
        this.accessLogHandler = accessLogHandler;
    }

    public PathBasedLoggingGroupHandler getLoggingGroupHandler() {
        return loggingGroupHandler;
    }

    public void setLoggingGroupHandler(PathBasedLoggingGroupHandler loggingGroupHandler) {
        this.loggingGroupHandler = loggingGroupHandler;
    }

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

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    public void setWorkerName(String workerName) {
        if (StringUtils.hasText(workerName)) {
            this.threadNamePrefix = workerName.trim() + "-task-";
        }
    }

    public ExecutorService getRequestExecutor() {
        return requestExecutor;
    }

    public void setRequestExecutor(ExecutorService requestExecutor) {
        this.requestExecutor = requestExecutor;
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
    @SuppressWarnings("deprecation")
    protected void doStart() throws Exception {
        if (contextRouter.isEmpty()) {
            if (nettyService != null) {
                if (nettyService instanceof DefaultNettyService defaultNettyService) {
                    contextRouter.addContext(new NettyContext(nettyService.getContextPath(), defaultNettyService));
                }
            } else {
                throw new IllegalStateException("Neither nettyService nor contexts are configured on NettyServer");
            }
        }

        for (NettyContext context : contextRouter.getContexts()) {
            if (!context.isStarted()) {
                context.start();
            }
        }

        if (listeners.isEmpty()) {
            listeners.add(new NettyListenerConfig(8080));
        }

        ThreadFactory bossThreadFactory = new DefaultThreadFactory("netty-boss", true);
        ThreadFactory workerThreadFactory = new DefaultThreadFactory("netty-worker", true);

        Class<? extends ServerSocketChannel> channelClass;
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(bossThreads, bossThreadFactory);
            workerGroup = new EpollEventLoopGroup(workerThreads, workerThreadFactory);
            channelClass = EpollServerSocketChannel.class;
            logger.info("Netty native Epoll transport is available and active");
        } else if (KQueue.isAvailable()) {
            bossGroup = new KQueueEventLoopGroup(bossThreads, bossThreadFactory);
            workerGroup = new KQueueEventLoopGroup(workerThreads, workerThreadFactory);
            channelClass = KQueueServerSocketChannel.class;
            logger.info("Netty native KQueue transport is available and active");
        } else {
            bossGroup = new NioEventLoopGroup(bossThreads, bossThreadFactory);
            workerGroup = new NioEventLoopGroup(workerThreads, workerThreadFactory);
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
                requestExecutor = Executors.newThreadPerTaskExecutor(threadFactory);
                logger.info("Java 21 Virtual Threads enabled for Netty request dispatching (prefix: '{}')", prefix);
            } else {
                String poolName = (prefix.endsWith("-") ? prefix.substring(0, prefix.length() - 1) : prefix);
                requestExecutor = Executors.newCachedThreadPool(new DefaultThreadFactory(poolName, true));
            }
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

    protected ChannelInitializer<SocketChannel> createChannelInitializer(NettyListenerConfig listenerConfig) {
        return new NettyChannelInitializer(
                listenerConfig,
                contextRouter,
                requestExecutor,
                resourceHandler,
                accessLogHandler,
                loggingGroupHandler,
                maxContentLength,
                contentCompression
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

}
