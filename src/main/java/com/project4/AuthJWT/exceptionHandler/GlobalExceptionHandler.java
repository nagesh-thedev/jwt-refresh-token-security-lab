package com.project4.AuthJWT.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.project4.AuthJWT.DTO.UserResponse;
import com.project4.AuthJWT.exception.InvalidRefreshTokenException;
import com.project4.AuthJWT.exception.RegisterationFailedException;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(RegisterationFailedException.class)
	public ResponseEntity<UserResponse> Registerationfailed(Exception e){
		UserResponse response = new UserResponse();
		response.setUsername(null);
		response.setMessage(e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
	
	  @ExceptionHandler(InvalidRefreshTokenException.class)
	    public ResponseEntity<String> handleInvalidRefreshToken(
	            InvalidRefreshTokenException ex) {

	        return ResponseEntity
	                .status(HttpStatus.UNAUTHORIZED)
	                .body(ex.getMessage());
	    }
	  
	  @ExceptionHandler(BadCredentialsException.class)
	  public ResponseEntity<String> handleBadCredentials(
	          BadCredentialsException ex) {

	      return ResponseEntity
	              .status(HttpStatus.UNAUTHORIZED)
	              .body("Invalid username or password");
	  }
	  
	  @ExceptionHandler(MethodArgumentNotValidException.class)
	  public ResponseEntity<String> handleValidationErrors(
	          MethodArgumentNotValidException ex) {

	      String message = ex.getBindingResult()
	              .getFieldErrors()
	              .stream()
	              .map(error -> error.getDefaultMessage())
	              .filter(java.util.Objects::nonNull)
	              .findFirst()
	              .orElse("Invalid request");

	      return ResponseEntity
	              .status(HttpStatus.BAD_REQUEST)
	              .body(message);
	  }

}
