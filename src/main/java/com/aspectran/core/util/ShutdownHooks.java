/**
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.util;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * Manages the shutdown-hook thread and tasks to execute on shutdown.
 *
 * @since 4.0.0
 */
public class ShutdownHooks {

	private static final Log log = LogFactory.getLog(ShutdownHooks.class);

	private static final List<Task> tasks = new ArrayList<>();

	private static Thread hook;

	public static synchronized <T extends Task> T add(final T task) {
		if (task == null) {
			throw new IllegalArgumentException("The task argument must not be null.");
		}

		if (hook == null) {
			hook = addHook(new Thread("Aspectran Shutdown Hook") {
				@Override
				public void run() {
					runTasks();
				}
			});
		}

		if (log.isDebugEnabled()) {
			log.debug("Adding shutdown-hook task: " + task);
		}

		tasks.add(task);

		return task;
	}

	private static synchronized void runTasks() {
		if (log.isDebugEnabled()) {
			log.debug("Running all shutdown-hook tasks.");
		}

		// Iterate through copy of tasks list
		for (Task task : tasks.toArray(new Task[tasks.size()])) {
			if (log.isDebugEnabled()) {
				log.debug("Running task: " + task);
			}
			try {
				task.run();
			} catch (Throwable e) {
				log.warn("Task failed", e);
			}
		}

		tasks.clear();
	}

	private static Thread addHook(final Thread thread) {
		if (log.isDebugEnabled()) {
			log.debug("Registering shutdown-hook: " + thread);
		}
		try {
			Runtime.getRuntime().addShutdownHook(thread);
		} catch (AbstractMethodError e) {
			// JDK 1.3+ only method. Bummer.
			if (log.isDebugEnabled()) {
				log.debug("Failed to register shutdown-hook" + e);
			}
		}
		return thread;
	}

	public static synchronized void remove(final Task task) {
		if (task == null) {
			throw new IllegalArgumentException("The task argument must not be null.");
		}

		// ignore if hook never installed
		if (hook == null) {
			return;
		}

		// Drop the task
		tasks.remove(task);

		// If there are no more tasks, then remove the hook thread
		if (tasks.isEmpty()) {
			removeHook(hook);
			hook = null;
		}
	}

	private static void removeHook(final Thread thread) {
		if (log.isDebugEnabled()) {
			log.debug("Removing shutdown-hook: " + thread);
		}

		try {
			Runtime.getRuntime().removeShutdownHook(thread);
		} catch (AbstractMethodError e) {
			// JDK 1.3+ only method. Bummer.
			if (log.isDebugEnabled()) {
				log.debug("Failed to remove shutdown-hook" + e);
			}
		} catch (IllegalStateException e) {
			// The VM is shutting down, not a big deal; ignore
		}
	}

	/**
	 * Essentially a {@link Runnable} which allows running to throw an exception.
	 */
	public interface Task {

		void run() throws Exception;

	}

}