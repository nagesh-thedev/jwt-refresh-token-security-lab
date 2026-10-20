package com.project4.AuthJWT.DTO;

import lombok.Data;

@Data
public class RefreshResponse {
	private String access_token;
	
	private String refresh_token;
	
	private String message;
	
	private String username;
}
