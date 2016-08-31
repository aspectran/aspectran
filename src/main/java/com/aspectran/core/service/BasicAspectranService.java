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
import com.aspectran.core.context.bean.scope.Scope;

/**
 * The Class BasicAspectranService.
 */
public class BasicAspectranService extends AbstractAspectranService {

	private static final long DEFAULT_PAUSE_TIMEOUT = 321L;
	
	private AspectranServiceControllerListener aspectranServiceControllerListener;
	
	/** Flag that indicates whether this context is currently active */
	private final AtomicBoolean active = new AtomicBoolean();

	/** Flag that indicates whether this context has been closed already */
	private final AtomicBoolean closed = new AtomicBoolean();

	/** Synchronization monitor for the "restart" and "shutdown" */
	private final Object startupShutdownMonitor = new Object();

	/** Reference to the JVM shutdown hook, if registered */
	private Thread shutdownHook;

	/**
	 * Instantiates a new Basic aspectran service.
	 *
	 * @param applicationAdapter the application adapter
	 */
	public BasicAspectranService(ApplicationAdapter applicationAdapter) {
		super(applicationAdapter);
		applicationAdapter.setAspectranServiceController(this);
	}

	@Override
	public void setAspectranServiceControllerListener(AspectranServiceControllerListener aspectranServiceControllerListener) {
		this.aspectranServiceControllerListener = aspectranServiceControllerListener;
	}

	@Override
	public void startup() throws AspectranServiceException {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				throw new AspectranServiceException("Cannot start Aspectran Service, because it was already destroyed.");
			}
			if(this.active.get()) {
				throw new AspectranServiceException("Cannot start Aspectran Service, because it was already started.");
			}

			loadActivityContext();
			registerShutdownHook();

			this.closed.set(false);
			this.active.set(true);

			log.info("Aspectran Service has been started successfully.");

			if(aspectranServiceControllerListener != null) {
				aspectranServiceControllerListener.started();
			}
		}
	}

	@Override
	public void restart() throws AspectranServiceException {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				log.warn("Cannot restart Aspectran Service, because it was already destroyed.");
				return;
			}
			if(!this.active.get()) {
				log.warn("Cannot restart Aspectran Service, because it is currently stopped.");
				return;
			}

			doDestroy();

			log.info("Aspectran Service has been stopped.");

			reloadActivityContext();

			this.closed.set(false);
			this.active.set(true);

			log.info("Aspectran Service has been restarted.");

			if(aspectranServiceControllerListener != null) {
				aspectranServiceControllerListener.restarted(isHardReload());
			}
		}
	}

	@Override
	public void pause() {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				log.warn("Cannot restart Aspectran Service, because it was already destroyed.");
				return;
			}

			if(aspectranServiceControllerListener != null) {
				aspectranServiceControllerListener.paused(-1L);
			}

			log.info("Aspectran Service has been paused.");
		}
	}

	@Override
	public void pause(long timeout) {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
				log.warn("Cannot restart Aspectran Service, because it was already destroyed.");
				return;
			}

			if(aspectranServiceControllerListener != null) {
				aspectranServiceControllerListener.paused(timeout);
			}
		}
	}

	@Override
	public void resume() {
		synchronized(this.startupShutdownMonitor) {
			if(this.closed.get()) {
					log.warn("Cannot resume Aspectran Service, because it was already destroyed.");
				return;
			}

			if(aspectranServiceControllerListener != null) {
				aspectranServiceControllerListener.resumed();
			}

			log.info("Aspectran Service has been resumed.");
		}
	}

	@Override
	public void shutdown() {
		synchronized(this.startupShutdownMonitor) {
			doDestroy();
			removeShutdownHook();

			log.info("Aspectran Service has been shut down successfully.");
		}
	}

	/**
	 * Actually performs destroys the singletons in the bean registry.
	 * Called by both {@code shutdown()} and a JVM shutdown hook, if any.
	 */
	private void doDestroy() {
		if(this.active.get() && this.closed.compareAndSet(false, true)) {
			if(aspectranServiceControllerListener != null) {
				aspectranServiceControllerListener.paused(DEFAULT_PAUSE_TIMEOUT);
			}

			destroyApplicationScope();
			destroyActivityContext();

			this.active.set(false);

			if(aspectranServiceControllerListener != null) {
				aspectranServiceControllerListener.stopped();
			}
		}
	}

	/**
	 * Destroys an application scope.
	 */
	private void destroyApplicationScope() {
		ApplicationAdapter applicationAdapter = getApplicationAdapter();
		if(applicationAdapter != null) {
			Scope scope = applicationAdapter.getApplicationScope();
			if(scope != null) {
				scope.destroy();
			}
		}
	}

	/**
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 */
	private void registerShutdownHook() {
		if(this.shutdownHook == null) {
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