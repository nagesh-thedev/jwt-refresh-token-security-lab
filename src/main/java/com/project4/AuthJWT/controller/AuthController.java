package com.project4.AuthJWT.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project4.AuthJWT.DTO.RefreshRequest;
import com.project4.AuthJWT.DTO.RefreshResponse;
import com.project4.AuthJWT.DTO.UserRequest;
import com.project4.AuthJWT.DTO.UserResponse;
import com.project4.AuthJWT.entity.AuthUser;
import com.project4.AuthJWT.exception.RegisterationFailedException;
import com.project4.AuthJWT.service.JWTService;
import com.project4.AuthJWT.service.RefreshTokenService;
import com.project4.AuthJWT.service.UserService;

import jakarta.validation.Valid;

@RestController
public class AuthController {
	
	private UserService userService;
	private AuthenticationManager authMan;
	private JWTService jwtService;
	private RefreshTokenService refService;
	
	

	

	public AuthController(UserService userService, AuthenticationManager authMan, JWTService jwtService,
			RefreshTokenService refService) {
		super();
		this.userService = userService;
		this.authMan = authMan;
		this.jwtService = jwtService;
		this.refService = refService;
	}

	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) throws RegisterationFailedException{
		return userService.register(request);
	}
	
	@PostMapping("/login")
	public ResponseEntity<UserResponse> login(@Valid @RequestBody UserRequest request){
		var authentication = authMan.authenticate(new UsernamePasswordAuthenticationToken
				(request.getUsername(),request.getPassword()));
		String access_token = jwtService.generateToken(authentication.getName());
		AuthUser user = userService.getUser(authentication.getName());
		String refresh_token = refService.createRefreshToken(user
			 , request.getDeviceId(), request.getDeviceName());
		UserResponse res = new UserResponse();
		res.setAccess_token(access_token);
		res.setRefresh_token(refresh_token);
		res.setUsername(authentication.getName());
		res.setMessage("LOGGED IN SUCCESSFULLY");
		return ResponseEntity.status(HttpStatus.OK).body(res);
	}
	
	
	@GetMapping("/wish")
	public ResponseEntity<String> wish(){
		return ResponseEntity.status(HttpStatus.OK).body("HI, HOW ARE YOU ??");
	}
	
	
	@PostMapping("/refresh")
	public ResponseEntity<RefreshResponse> refresh(
	        @RequestBody RefreshRequest request) {

	    RefreshResponse response =
	            refService.rotateRefreshToken(request.getRefreshToken());

	    return ResponseEntity
	            .status(HttpStatus.OK)
	            .body(response);
	}
	
	@PostMapping("/logout")
	public ResponseEntity<String> logout(
	        @RequestBody RefreshRequest request) {

	    refService.revokeRefreshToken(request.getRefreshToken());

	    return ResponseEntity.ok("LOGGED OUT SUCCESSFULLY");
	}
	
	@PostMapping("/logout-all")
	public ResponseEntity<String> logoutAll(
	        Authentication authentication) {

	    AuthUser user = userService.getUser(authentication.getName());

	    refService.revokeAllUserTokens(user.getId());

	    return ResponseEntity.ok("LOGGED OUT FROM ALL DEVICES");
	}
	
	

}
