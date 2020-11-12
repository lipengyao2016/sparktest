package com.bigdata.utils;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @ClassName: RedisUtil 
 * @Description: RedisUtil.java
SerializableUtil.java
ShiroUtil.java
StringUtils.java
 * @author tianpengw 
 * @date 2017年4月26日 上午10:09:05 
 *
 */
public class RedisUtil {

//	private static Logger logger = LogManager.getLogger(RedisUtil.class);

	/** 默认缓存时间 */
	private static final int DEFAULT_CACHE_SECONDS = 60 * 60 * 1;// 单位秒 设置成一个钟

	/** 连接池 **/
	private static JedisPool jedisPool;
	
	static {
		if (jedisPool == null) {  
            JedisPoolConfig config = new JedisPoolConfig();  
            //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；  
            //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。  
            config.setMaxIdle(8);  
            //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。  
            config.setMaxTotal(8);  
            //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；  
            config.setMaxWaitMillis(1000 * 100);  
            //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；  
            config.setTestOnBorrow(true);  
            config.setMinEvictableIdleTimeMillis(60000);  
            config.setTimeBetweenEvictionRunsMillis(30000);  
            config.setNumTestsPerEvictionRun(-1);  
            config.setMinIdle(0);

            jedisPool = new JedisPool(config,"47.107.239.163", 7006
					,2000,"xfz178");
        }  
		
		
	}

	/**
	 * 释放redis资源
	 * 
	 * @param jedis
	 */
	private static void releaseResource(Jedis jedis) {
		if (jedis != null) {
			jedisPool.returnResource(jedis);
		}
	}





	public static Boolean set(String key, String val, int seconds) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.set(key, val);
			if (seconds > 0)
			{
				jedis.expire(key, seconds);
			}

			return true;
		} catch (Exception e) {
			return false;
		} finally {
			releaseResource(jedis);
		}
	}





	/**
	 * 添加一个内容到指定key的hash中
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public static Boolean addHash(String key, String field, String value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.hset(key, field, value);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			releaseResource(jedis);
		}
	}
	
	public void setjedisPool(JedisPool jedisPool) {
		RedisUtil.jedisPool = jedisPool;
	}

	public static JedisPool getjedisPool() {
        return jedisPool;  
	}
	public static void main(String[] args) {
	}
}