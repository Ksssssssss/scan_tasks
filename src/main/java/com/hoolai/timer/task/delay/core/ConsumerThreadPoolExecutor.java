package com.hoolai.timer.task.delay.core;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: Ksssss(chenlin @ hoolai.com)
 * @time: 2020-04-07 10:30
 */

public class ConsumerThreadPoolExecutor {
    private final ThreadPoolExecutor executor;
    private static final int DEFAULT_POOL_SIZE = 3;
    private static final int DEFAULT_CAPACITY_SIZE = 100;

    public ConsumerThreadPoolExecutor(String name) {
        this(name,DEFAULT_POOL_SIZE);
    }

    public ConsumerThreadPoolExecutor(String name,int poolSize) {
        this(name,poolSize, DEFAULT_CAPACITY_SIZE);
    }

    public ConsumerThreadPoolExecutor(String name,int poolSize, int capacitySize) {
        executor = new ThreadPoolExecutor(poolSize, poolSize, 0l, TimeUnit.MICROSECONDS,new LinkedBlockingQueue(capacitySize)
                ,new SystemThreadFactory(name),new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(task, result);
    }

    public static class SystemThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public SystemThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            this.group = s!=null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
        }


        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }

            if (t.getPriority() != 5) {
                t.setPriority(5);
            }

            return t;
        }
    }

}
