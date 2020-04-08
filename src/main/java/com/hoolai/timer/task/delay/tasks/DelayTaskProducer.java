package com.hoolai.timer.task.delay.tasks;

import com.hoolai.timer.task.delay.core.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: Ksssss(chenlin @ hoolai.com)
 * @time: 2020-03-31 11:47
 */

@Component
public class DelayTaskProducer {
    @Autowired
    private JedisProvider client;
    @Autowired
    private RedisConfig config;


    @PostConstruct
    public void start() {
        ExecutorService executor = Executors.newFixedThreadPool(6);
        for (int i = 0; i < 6; i++) {
            executor.execute(new TaskProducer());
        }
    }

    class TaskProducer implements Runnable {
        private final Jedis jedis;
        private final String[] keys;
        private final long timeStamp = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(111);
        private final AtomicInteger i = new AtomicInteger(0);

        public TaskProducer() {
            this.jedis = client.provider();
            keys = config.keys();
        }

        @Override
        public void run() {
            int value = i.get();
            try {
                while (value < 3000) {
                    int pos = value % 2;
                    String key = keys[pos];
                    jedis.zadd(key, System.currentTimeMillis() + value, "" + i);
                    value = i.incrementAndGet();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                jedis.close();
            }
        }
    }
}
