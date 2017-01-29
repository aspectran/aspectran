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
package com.aspectran.core;

import java.util.concurrent.locks.StampedLock;

/**
 * <p>Created: 2016. 3. 20.</p>
 */

public class StampedLockTestThread implements Runnable {

	private Monitor monitor;

	private static int count;

	public StampedLockTestThread(Monitor monitor) {
		super();
		this.monitor = monitor;
	}

	@Override
	public void run() {
		printMessage("1. Entered run method");

		StampedLock lock = monitor.getStampedLock();
		long stamp = lock.readLock();

		printMessage("2. readLock: " + stamp);

		try {
			long tryStamp = lock.tryConvertToWriteLock(stamp);

			if (tryStamp != 0L) {
				stamp = tryStamp;
				printMessage("3. tryConvertToWriteLock: " + stamp);
			} else {
				lock.unlockRead(stamp);
				printMessage("3-1. unlockRead: " + stamp);

				stamp = lock.writeLock();
				printMessage("3-2. writeLock: " + stamp);
			}

			try {
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			count++;

			printMessage("==== count: " + count + " ====");
		} finally {
			lock.unlock(stamp);
			printMessage("4. Releasing lock: " + stamp);
		}

		printMessage("5. End of run method");
	}

	private void printMessage(String msg){
		System.out.println(Thread.currentThread().getName() + " - " + msg);
	}

	static class Monitor {
		final StampedLock lock = new StampedLock();

		public StampedLock getStampedLock() {
			return lock;
		}
	}

	public static void main(String[] args) {
		String[] myThreads = { "Therad ONE", "Thread TWO", "Thread THREE", "Thread FOUR" };

		Monitor monitor = new Monitor();

		for (String threadName : myThreads) {
			new Thread(new StampedLockTestThread(monitor), threadName).start();
		}
	}

}
