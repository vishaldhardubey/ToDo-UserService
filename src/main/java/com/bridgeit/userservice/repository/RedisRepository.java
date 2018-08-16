package com.bridgeit.userservice.repository;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.bridgeit.userservice.securityservice.security.TokenGenerator;

@Repository
public class RedisRepository implements IRedisRepository {
	private final static Logger LOGGER = LoggerFactory.getLogger(RedisRepository.class);
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	TokenGenerator tokenGenerator;
	
	private HashOperations<String,String,String> hashOperation;

	public RedisRepository(RedisTemplate<String, Object> redisTemplate) {
		super();
		this.redisTemplate = redisTemplate;
	}
	@PostConstruct
	private void init() {
		hashOperation=redisTemplate.opsForHash();
	}
	@Override
	public void save(String token) {
		LOGGER.info(token);
		String userId=tokenGenerator.parseJWT(token);
		hashOperation.putIfAbsent("TOKEN",userId,token);
		LOGGER.info("Succesfully Stored in Redis Repository");
	}
	@Override
	public String getToken(String userId) {
		return hashOperation.get("TOKEN", userId);
	}
	
}
