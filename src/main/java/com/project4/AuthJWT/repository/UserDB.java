package com.project4.AuthJWT.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project4.AuthJWT.entity.AuthUser;

public interface UserDB extends JpaRepository<AuthUser, Long> {
   
	Optional<AuthUser> findByUsername(String username);
	
}
