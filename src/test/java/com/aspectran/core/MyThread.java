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
package com.aspectran.core;

/**
 * <p>Created: 2016. 3. 20.</p>
 */

public class MyThread implements Runnable {

	private Monitor monitor;

	public MyThread(Monitor monitor) {
		super();
		this.monitor = monitor;
	}

	@Override
	public void run() {
	printMessage("1. Entered run method...trying to lock monitor object");
		final Object lock = monitor.getLock();
		synchronized (lock) {
			printMessage("2. Locked monitor object");
			try{
				Thread.sleep(1);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			printMessage("3. Releasing lock");
			lock.notifyAll();
		}
		printMessage("4. End of run method");
	}

	private void printMessage(String msg){
		System.out.println(Thread.currentThread().getName()+" : "+msg);
	}

	public static void main(String[] args) {
		System.out.println("=========Intrinsic Test=======");

		String[] myThreads = {"Therad ONE","Thread TWO","Thread THREE","Thread FOUR"};

		Monitor monitor = new Monitor();

		for(String threadName:myThreads) {
			new Thread(new MyThread(monitor), threadName).start();
		}
	}

	static class Monitor {
		Object lock = new Object();

		public Object getLock() {
			return lock;
		}
	}

}
