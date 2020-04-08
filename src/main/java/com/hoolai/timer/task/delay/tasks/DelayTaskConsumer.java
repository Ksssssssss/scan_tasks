package com.hoolai.timer.task.delay.tasks;

import com.hoolai.timer.task.delay.core.ConsumerThreadPoolExecutor;
import com.hoolai.timer.task.delay.core.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: Ksssss(chenlin @ hoolai.com)
 * @time: 2020-03-28 17:57
 */

@Component
public class DelayTaskConsumer {

    @Autowired
    private JedisProvider provider;
    @Autowired
    private RedisConfig config;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private String[] keys;
    private ConsumerThreadPoolExecutor executorEvent;
    private ConsumerThreadPoolExecutor executor;
    private AtomicInteger pos = new AtomicInteger(-1);

    @PostConstruct
    public void start() {

        this.keys = config.keys();
        executorEvent = new ConsumerThreadPoolExecutor("handler", keys.length);
        executor = new ConsumerThreadPoolExecutor("consumer", keys.length);
        scheduledExecutorService.scheduleWithFixedDelay(new DelayTaskTrigger(), 1, 1, TimeUnit.SECONDS);
    }

    class DelayTaskTrigger implements Runnable {

        private final Jedis client;

        public DelayTaskTrigger() {
            client = provider.provider();
        }

        @Override
        public void run() {
            CountDownLatch countDownLatch = new CountDownLatch(keys.length);
            for (int i = 0; i < keys.length; i++) {
                executorEvent.submit(new DelayTaskEvent(keys[i], countDownLatch));
            }

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class DelayTaskEvent implements Runnable {
        private final String key;
        private final Jedis client;
        private final CountDownLatch countDownLatch;

        public DelayTaskEvent(String key, CountDownLatch countDownLatch) {
            this.key = key;
            client = provider.provider();
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            int retry = 0;
            Set<String> ids;
            while (retry < 3) {
                try {
                    while (true) {
                        ids = client.zrangeByScore(key, 0, System.currentTimeMillis(), 0, 100);
                        if (CollectionUtils.isEmpty(ids)) {
                            break;
                        }
                        client.zrem(key, ids.toArray(new String[ids.size()]));
                        executor.submit(new DelayTaskHandler(ids));
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    retry++;
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException subE) {
                        subE.printStackTrace();
                    }
                }
            }
            client.close();
            if (client.isConnected()) {
                client.disconnect();
            }
            countDownLatch.countDown();
        }
    }

    class DelayTaskHandler implements Runnable {
        private final Set<String> udids;

        public DelayTaskHandler(Set<String> ids) {
            this.udids = ids;
        }

        @Override
        public void run() {
            for (String id : udids) {

                //todo
                System.out.println(id + "升级完成");
            }
        }
    }

    private String next() {
        if (pos.incrementAndGet() == keys.length) {
            pos.set(0);
        }
        return keys[pos.get()];
    }
}
