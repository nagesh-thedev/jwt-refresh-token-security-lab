package com.project4.AuthJWT.DTO;


import lombok.Data;
@Data
public class UserResponse {
		
		private String message;

		private String username;
        
		private String refresh_token;
		
		private String access_token;
	
}
