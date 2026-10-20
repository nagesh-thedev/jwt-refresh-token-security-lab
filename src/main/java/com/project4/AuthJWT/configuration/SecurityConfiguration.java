package com.project4.AuthJWT.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.project4.AuthJWT.filter.JWTAuthFilter;
import com.project4.AuthJWT.service.CustomUserDetailservice;



@Configuration
public class SecurityConfiguration {
	
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,DaoAuthenticationProvider authenticationProvider
			,JWTAuthFilter jwtAuthFilter) {
		http.authenticationProvider(authenticationProvider);
		http.authorizeHttpRequests(auth -> auth.requestMatchers("/register","/login").permitAll().anyRequest().authenticated());
		http.formLogin(form-> form.disable());
		http.csrf(csrf -> csrf.disable());
		http.logout(logout -> logout.disable());
		http.sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.addFilterBefore(jwtAuthFilter,  UsernamePasswordAuthenticationFilter.class);
		return http.build();
		}
	
	
	@Bean
	public DaoAuthenticationProvider getDaoAuthProvider(CustomUserDetailservice UserDetails,PasswordEncoder encoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(UserDetails);
		provider.setPasswordEncoder(encoder);
		return provider;
	}
	
	@Bean
	public AuthenticationManager getAuthManager(AuthenticationConfiguration config) {
		return config.getAuthenticationManager();
	} 

	
}
