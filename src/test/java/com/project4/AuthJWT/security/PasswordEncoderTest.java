package com.project4.AuthJWT.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest {
	
	@Test
	public void passwordEncryptionCheck() {
		PasswordEncoder pwec = new BCryptPasswordEncoder();
		String password = "12345";
		String encPwd = pwec.encode(password);
		assertNotEquals(password, encPwd);
	}
	
	@Test
	public void passwordEncryptionSameHash() {
		PasswordEncoder pwec = new BCryptPasswordEncoder();
		String password = "12345";
		String encPwd = pwec.encode(password);
		String reencPwd = pwec.encode(password);
		assertNotEquals(reencPwd, encPwd);
	}
	
	@Test
	public void passwordMatchesCheck() {
		PasswordEncoder pwec = new BCryptPasswordEncoder();
		String password = "12345";
		String encPwd = pwec.encode(password);
		assertFalse(pwec.matches("123456", encPwd));
	}
	

}
