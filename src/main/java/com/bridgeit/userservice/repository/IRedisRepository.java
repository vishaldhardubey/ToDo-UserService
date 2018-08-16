package com.bridgeit.userservice.repository;

public interface IRedisRepository {
	
	public void save(String token);
	public String getToken(String userId);
}
