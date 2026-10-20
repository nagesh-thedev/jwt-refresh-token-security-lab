package com.project4.AuthJWT.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project4.AuthJWT.entity.AuthUser;
import com.project4.AuthJWT.exception.UserNotFoundException;
import com.project4.AuthJWT.repository.UserDB;



@Service
public class CustomUserDetailservice implements UserDetailsService{
	
	private UserDB userrepo;
	
	

	public CustomUserDetailservice(UserDB userrepo) {
		super();
		this.userrepo = userrepo;
	}



	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AuthUser authuser = userrepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User Not found with Username :"+username));
	    return User.withUsername(authuser.getUsername())
	    		.password(authuser.getPassword())
	    		.roles(authuser.getRole().stream().map(Enum::name).toArray(String[]::new)).build();
	}

}
