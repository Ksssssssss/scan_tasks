package com.hoolai.timer.task.delay.tasks;

import com.hoolai.timer.task.delay.core.RedisConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;

/**
 *
 *@description: 
 *@author: Ksssss(chenlin@hoolai.com)
 *@time: 2020-03-30 10:16
 * 
 */

@Component
public class JedisProvider {
    @Autowired
    private RedisConfig config;
    private JedisPool jedisPool;

    @PostConstruct
    public void afterPropertiesSet() {
        //默认含8个redis
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        jedisPool = new JedisPool(poolConfig,config.getHost(),config.getPort());
    }

    public Jedis provider(){
        return jedisPool.getResource();
    }
}
