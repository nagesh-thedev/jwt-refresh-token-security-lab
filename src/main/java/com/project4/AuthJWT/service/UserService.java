package com.project4.AuthJWT.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project4.AuthJWT.DTO.UserRequest;
import com.project4.AuthJWT.DTO.UserResponse;
import com.project4.AuthJWT.entity.AuthUser;
import com.project4.AuthJWT.exception.RegisterationFailedException;

@Service
public interface UserService {
    
	public ResponseEntity<UserResponse> register(UserRequest request) throws RegisterationFailedException;

	AuthUser getUser(String userName);
	
}
