package com.hoolai.timer.task.delay.tasks;

import com.hoolai.timer.task.delay.core.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: Ksssss(chenlin @ hoolai.com)
 * @time: 2020-03-28 18:22
 */

@Component
public class RedisClient {
    @Autowired
    private JedisProvider jedisProvider;
    @Autowired
    private RedisConfig config;
    /**
     * 分片数量
     */
    private static int SEGMENT_COUNT;
    private String[] keys;

    @PostConstruct
    public void init() {
        keys = config.keys();
        SEGMENT_COUNT = keys.length;
    }

//    public void zadd(long timeStamp, String udid) {
//        try {
//            int pos = udid.hashCode() % SEGMENT_COUNT;
//            String key = keys[pos];
//            client.zadd(key, timeStamp, udid);
//        } finally {
//        }
//    }

    public Set<String> zrangeByScore(String key, int count) {
        Jedis client = jedisProvider.provider();
        Set<String> ids = client.zrangeByScore(key, 0, System.currentTimeMillis(), 0, count);
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_SET;
        }
        return ids;
    }

    public void zrem(String key, String... udids) {
        Jedis jedis = jedisProvider.provider();
        jedis.zrem(key, udids);
    }

    public static void main(String[] args) {

    }
}
