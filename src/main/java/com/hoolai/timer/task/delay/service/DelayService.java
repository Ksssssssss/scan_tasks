package com.hoolai.timer.task.delay.service;

import java.util.List;

/**
 * @description:
 * @author: Ksssss(chenlin @ hoolai.com)
 * @time: 2020-03-30 21:01
 */

public interface DelayService {
    List<String> zRangeByScore();

    void zadd(long timeStamp, String udid);

    void zrem(String key, double min, double max, int offset, int count);
}
