/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.github.svegon.utils.multithreading;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class MinPriorityThreadFactory implements ThreadFactory {
	private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;
	
	public MinPriorityThreadFactory() {
		SecurityManager s = System.getSecurityManager();
		group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		namePrefix = "pool-min-" + POOL_NUMBER.getAndIncrement() + "-thread-#";
	}
	
	@Override
	public Thread newThread(@NotNull Runnable r) {
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);

		if(t.isDaemon()) {
			t.setDaemon(false);
		}

		if(t.getPriority() != Thread.MIN_PRIORITY) {
			t.setPriority(Thread.MIN_PRIORITY);
		}

		return t;
	}
	
	public static ExecutorService newFixedThreadPool() {
		return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new MinPriorityThreadFactory());
	}
}
