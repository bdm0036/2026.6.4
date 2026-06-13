package com.bookstore.order.service;

import com.bookstore.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class StockService {

    private final StringRedisTemplate redisTemplate;

    public StockService(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private boolean hasRedis() { return redisTemplate != null; }

    private static final String STOCK_KEY = "book:stock:";
    private static final String LOCK_KEY = "book:stock:locked:";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(30);

    public void lockStock(Long bookId, int quantity, String orderNo) {
        if (!hasRedis()) return;
        String stockKey = STOCK_KEY + bookId;
        String lockKey = LOCK_KEY + orderNo;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
            redisTemplate.opsForValue().set(stockKey, "100");
        }

        String lua = "local stock = redis.call('GET', KEYS[1]) " +
                     "if not stock then return -1 end " +
                     "if tonumber(stock) < tonumber(ARGV[1]) then return 0 end " +
                     "redis.call('DECRBY', KEYS[1], ARGV[1]) " +
                     "redis.call('SET', KEYS[2], ARGV[1], 'EX', ARGV[2]) " +
                     "return 1";

        Long result = redisTemplate.execute(
            new org.springframework.data.redis.core.script.DefaultRedisScript<>(lua, Long.class),
            Collections.singletonList(stockKey),
            String.valueOf(quantity), lockKey, String.valueOf(LOCK_TIMEOUT.getSeconds()));

        if (result == null || result == -1) throw new BusinessException("库存系统异常");
        if (result == 0) throw new BusinessException("库存不足");
        redisTemplate.opsForHash().put(lockKey, String.valueOf(bookId), String.valueOf(quantity));
        redisTemplate.expire(lockKey, LOCK_TIMEOUT);
    }

    public void confirmStock(String orderNo) {
        if (!hasRedis()) return;
        redisTemplate.delete(LOCK_KEY + orderNo);
    }

    public void releaseStock(String orderNo) {
        if (!hasRedis()) return;
        String lockKey = LOCK_KEY + orderNo;
        Map<Object, Object> locks = redisTemplate.opsForHash().entries(lockKey);
        for (Map.Entry<Object, Object> entry : locks.entrySet()) {
            String bookId = (String) entry.getKey();
            int quantity = Integer.parseInt((String) entry.getValue());
            redisTemplate.opsForValue().increment(STOCK_KEY + bookId, quantity);
        }
        redisTemplate.delete(lockKey);
    }

    public int getStock(Long bookId) {
        if (!hasRedis()) return 0;
        String stockStr = redisTemplate.opsForValue().get(STOCK_KEY + bookId);
        return stockStr != null ? Integer.parseInt(stockStr) : 0;
    }
}
