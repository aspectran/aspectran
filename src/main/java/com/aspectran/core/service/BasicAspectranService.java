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

/**
 * The Class BasicAspectranService.
 */
public class BasicAspectranService extends AbstractAspectranService {

	private final boolean derivedService;

	private AspectranServiceLifeCycleListener aspectranServiceLifeCycleListener;
	
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
		this.derivedService = false;
	}

	public BasicAspectranService(AspectranService parentAspectranService) {
		super(parentAspectranService);
		this.derivedService = true;
	}

	public void setAspectranServiceLifeCycleListener(AspectranServiceLifeCycleListener aspectranServiceLifeCycleListener) {
		this.aspectranServiceLifeCycleListener = aspectranServiceLifeCycleListener;
	}

	protected void afterStartup() {
	}

	protected void beforeShutdown() {
	}

	@Override
	public void startup() throws AspectranServiceException {
		if (!this.derivedService) {
			synchronized (this.startupShutdownMonitor) {
				if (this.closed.get()) {
					throw new AspectranServiceException("Could not start AspectranService because it has already been destroyed.");
				}
				if (this.active.get()) {
					throw new AspectranServiceException("Could not start AspectranService because it has already been started.");
				}

				loadActivityContext();
				registerShutdownHook();
				afterStartup();

				this.closed.set(false);
				this.active.set(true);

				log.info("AspectranService has been started successfully.");

				if (aspectranServiceLifeCycleListener != null) {
					aspectranServiceLifeCycleListener.started();
				}
			}
		}
	}

	@Override
	public void restart() throws AspectranServiceException {
		if (!this.derivedService) {
			synchronized (this.startupShutdownMonitor) {
				if (this.closed.get()) {
					log.warn("Could not restart AspectranService because it has already been destroyed.");
					return;
				}
				if (!this.active.get()) {
					log.warn("Could not restart AspectranService because it is currently stopped.");
					return;
				}

				beforeShutdown();
				doDestroy();

				log.info("AspectranService has been stopped.");

				reloadActivityContext();
				afterStartup();

				this.closed.set(false);
				this.active.set(true);

				log.info("AspectranService has been restarted successfully.");

				if (aspectranServiceLifeCycleListener != null) {
					aspectranServiceLifeCycleListener.restarted(isHardReload());
				}
			}
		}
	}

	@Override
	public void pause() {
		if (!this.derivedService) {
			synchronized (this.startupShutdownMonitor) {
				if (this.closed.get()) {
					log.warn("Could not pause AspectranService because it has already been destroyed.");
					return;
				}

				pauseSchedulerService();

				if (aspectranServiceLifeCycleListener != null) {
					aspectranServiceLifeCycleListener.paused();
				}

				log.info("AspectranService has been paused.");
			}
		}
	}

	@Override
	public void pause(long timeout) {
		if (!this.derivedService) {
			synchronized (this.startupShutdownMonitor) {
				if (this.closed.get()) {
					log.warn("Could not pause AspectranService because it has already been destroyed.");
					return;
				}

				if (aspectranServiceLifeCycleListener != null) {
					aspectranServiceLifeCycleListener.paused(timeout);
				}
			}
		}
	}

	@Override
	public void resume() {
		if (!this.derivedService) {
			synchronized (this.startupShutdownMonitor) {
				if (this.closed.get()) {
					log.warn("Could not resume AspectranService because it has already been destroyed.");
					return;
				}

				resumeSchedulerService();

				if (aspectranServiceLifeCycleListener != null) {
					aspectranServiceLifeCycleListener.resumed();
				}

				log.info("AspectranService has been resumed.");
			}
		}
	}

	@Override
	public void shutdown() {
		if (!this.derivedService) {
			synchronized (this.startupShutdownMonitor) {
				beforeShutdown();
				doDestroy();
				removeShutdownHook();

				log.info("AspectranService has been shut down successfully.");
			}
		}
	}

	/**
	 * Actually performs destroys the singletons in the bean registry.
	 * Called by both {@code shutdown()} and a JVM shutdown hook, if any.
	 */
	private void doDestroy() {
		if (this.active.get() && this.closed.compareAndSet(false, true)) {
			if (aspectranServiceLifeCycleListener != null) {
				aspectranServiceLifeCycleListener.paused();
			}

			destroyActivityContext();

			this.active.set(false);

			if (aspectranServiceLifeCycleListener != null) {
				aspectranServiceLifeCycleListener.stopped();
			}
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
					synchronized (startupShutdownMonitor) {
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
		if (this.shutdownHook != null) {
			try {
				Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
			} catch (IllegalStateException ex) {
				// ignore - VM is already shutting down
			}
		}
	}

	@Override
	public boolean isActive() {
		return this.active.get();
	}

}