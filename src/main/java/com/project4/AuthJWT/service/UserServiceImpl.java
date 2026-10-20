package com.project4.AuthJWT.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project4.AuthJWT.DTO.UserRequest;
import com.project4.AuthJWT.DTO.UserResponse;
import com.project4.AuthJWT.constants.Role;
import com.project4.AuthJWT.entity.AuthUser;
import com.project4.AuthJWT.exception.RegisterationFailedException;
import com.project4.AuthJWT.exception.UserNotFoundException;
import com.project4.AuthJWT.repository.UserDB;

@Service
public class UserServiceImpl  implements UserService{
	
	private UserDB userrepo;
	private PasswordEncoder pwdEnc;
	
	
	

	public UserServiceImpl(UserDB userrepo, PasswordEncoder pwdEnc) {
		super();
		this.userrepo = userrepo;
		this.pwdEnc = pwdEnc;
	}


     @Override
     public ResponseEntity<UserResponse> register(UserRequest request)
        throws RegisterationFailedException {

        if (userrepo.findByUsername(request.getUsername()).isPresent()) {
            throw new RegisterationFailedException(
                  "Username already exists"
           );
         }

        AuthUser user = requestMapper(request);

        List<Role> userRoles = new ArrayList<>();
        userRoles.add(Role.USER);
        user.setRole(userRoles);

        try {
            AuthUser savedUser = userrepo.save(user);
            UserResponse response = responseMapper(savedUser);

             return ResponseEntity.status(HttpStatus.CREATED).body(response);

         } catch (DataIntegrityViolationException ex) {
            throw new RegisterationFailedException(
                "Username already exists"
          );
      }
}

	
	    
	
	@Override
	public AuthUser getUser(String userName) {
		AuthUser user = userrepo.findByUsername(userName).orElseThrow(()-> new UserNotFoundException(""
				+ "USER WITH :"+ userName+" NOT EXIST"));
		return user;
	}

	private UserResponse responseMapper(AuthUser savedUser) throws RegisterationFailedException {
		UserResponse response = new UserResponse();
		if(savedUser!=null) {
			response.setUsername(savedUser.getUsername());
			response.setMessage("USER REGISTERED SUCCESSFULLY");
		}else {
			throw new RegisterationFailedException("REGISTERATION FAILED TRY AGAIN !!");
		}
		return response;
	}

	private AuthUser requestMapper(UserRequest request) {
		AuthUser response = new AuthUser();
		response.setUsername(request.getUsername());
		response.setPassword(pwdEnc.encode(request.getPassword()));
		return response;
	}
	
	

}
