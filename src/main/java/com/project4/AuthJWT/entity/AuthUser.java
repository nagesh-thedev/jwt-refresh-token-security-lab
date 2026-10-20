package com.project4.AuthJWT.entity;

import java.util.List;

import com.project4.AuthJWT.constants.Role;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Entity
@Data
public class AuthUser {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false , unique= true)
	private String username;
	
	@Column(nullable=false )
	private String password;
	
	@ElementCollection
	@CollectionTable(
			name= "user_roles",
			joinColumns= @JoinColumn(name ="user_id")
			)
	@Enumerated(EnumType.STRING)
	private List<Role> role;

}
