/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.service;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class CoreAspectranService.
 */
public class CoreAspectranService extends AbstractAspectranService {

	private static final Log log = LogFactory.getLog(CoreAspectranService.class);
	
	private static final long DEFAULT_PAUSE_TIMEOUT = 321L;
	
	private AspectranServiceControllerListener aspectranServiceControllerListener;
	
	/** Flag that indicates whether this context is currently active */
	private final AtomicBoolean active = new AtomicBoolean();

	/** Flag that indicates whether this context has been closed already */
	private final AtomicBoolean closed = new AtomicBoolean();

	/** Synchronization monitor for the "refresh" and "destroy" */
	private final Object startupShutdownMonitor = new Object();

	/** Reference to the JVM shutdown hook, if registered */
	private Thread shutdownHook;

	public void setAspectranServiceControllerListener(AspectranServiceControllerListener aspectranServiceControllerListener) {
		this.aspectranServiceControllerListener = aspectranServiceControllerListener;
	}

	@Override
	public ActivityContext startup() throws AspectranServiceException {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				throw new AspectranServiceException("Cannot start AspectranService, because it was already destroyed.");
			}
			if(this.active.get()) {
				throw new AspectranServiceException("Cannot start AspectranService, because it was already started.");
			}

			loadActivityContext();
			registerShutdownHook();

			this.closed.set(false);
			this.active.set(true);

			log.info("AspectranService was started successfully.");

			if(aspectranServiceControllerListener != null) aspectranServiceControllerListener.started();

			return activityContext;
		}
	}

	@Override
	public synchronized void restart() throws AspectranServiceException {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				log.warn("Cannot restart AspectranService, because it was already destroyed.");
				return;
			}
			if(!this.active.get()) {
				log.warn("Cannot restart AspectranService, because it is currently stopped.");
				return;
			}

			stop();

			reloadActivityContext();

			this.closed.set(false);
			this.active.set(true);

			log.info("AspectranService was restarted.");

			if(aspectranServiceControllerListener != null)
				aspectranServiceControllerListener.restarted();
		}
	}

	@Override
	public synchronized void reload() throws AspectranServiceException {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				log.warn("Cannot restart AspectranService, because it was already destroyed.");
				return;
			}
			if(!this.active.get()) {
				log.debug("Cannot restart AspectranService, because it is currently stopped.");
				return;
			}

			if(aspectranServiceControllerListener != null)
				aspectranServiceControllerListener.paused(DEFAULT_PAUSE_TIMEOUT);

			reloadActivityContext();

			if(aspectranServiceControllerListener != null)
				aspectranServiceControllerListener.resumed();

			log.info("AspectranService was reloaded.");

			if(aspectranServiceControllerListener != null)
				aspectranServiceControllerListener.reloaded();
		}
	}

	@Override
	public synchronized void refresh() throws AspectranServiceException {
		if(isHardReload())
			restart();
		else
			reload();
	}

	@Override
	public synchronized void pause() {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				log.warn("Cannot restart AspectranService, because it was already destroyed.");
				return;
			}

			if(aspectranServiceControllerListener != null)
				aspectranServiceControllerListener.paused(-1L);

			log.info("AspectranService was paused.");
		}
	}

	@Override
	public synchronized void pause(long timeout) {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				log.warn("Cannot restart AspectranService, because it was already destroyed.");
				return;
			}

			if(aspectranServiceControllerListener != null)
				aspectranServiceControllerListener.paused(timeout);
		}
	}

	@Override
	public synchronized void resume() {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				log.warn("Cannot restart AspectranService, because it was already destroyed.");
				return;
			}

			if(aspectranServiceControllerListener != null)
				aspectranServiceControllerListener.resumed();

			log.info("AspectranService was resumed.");
		}
	}

	private void stop() {
		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.paused(DEFAULT_PAUSE_TIMEOUT);

		destroyApplicationScope();
		destroyActivityContext();

		this.active.set(false);

		log.info("AspectranService was stopped.");

		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.stopped();
	}

	@Override
	public void destroy() {
		synchronized(this.startupShutdownMonitor) {
			doDestroy();
			removeShutdownHook();

			log.info("AspectranService has been shut down successfully.");
		}
	}

	/**
	 * Actually performs destroys the singletons in the bean factory.
	 * Called by both {@code destroy()} and a JVM shutdown hook, if any.
	 */
	private void doDestroy() {
		if(this.active.get() && this.closed.compareAndSet(false, true)) {
			stop();
		}
	}

	private void destroyApplicationScope() {
		ApplicationAdapter applicationAdapter = getApplicationAdapter();
		if(applicationAdapter != null) {
			Scope scope = applicationAdapter.getApplicationScope();
			if(scope != null)
				scope.destroy();
		}
	}

	/**
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 */
	private void registerShutdownHook() {
		if (this.shutdownHook == null) {
			// No shutdown hook registered yet.
			this.shutdownHook = new Thread() {
				@Override
				public void run() {
					synchronized(startupShutdownMonitor) {
						doDestroy();
					}
				}
			};
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		}
	}

	/**
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 */
	private void removeShutdownHook() {
		// If we registered a JVM shutdown hook, we don't need it anymore now:
		// We've already explicitly closed the context.
		if(this.shutdownHook != null) {
			try {
				Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
			} catch(IllegalStateException ex) {
				// ignore - VM is already shutting down
			}
		}
	}

	@Override
	public boolean isActive() {
		return this.active.get();
	}

}