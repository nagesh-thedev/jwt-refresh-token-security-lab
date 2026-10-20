package com.project4.AuthJWT.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest  {
	
	@NotBlank(message = "Username is required")
	private String username;
	
	@NotBlank(message = "password is required")
	private String password;
	
	@NotNull @NotEmpty
	private String deviceId;
	
	@NotNull @NotEmpty
	private String deviceName;

}
