package com.contact.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.contact.entities.*;

public interface UserRepository extends JpaRepository<User, Integer> {

	@Query("SELECT u FROM User u WHERE u.email = :email")
	User getUserByUserName(@Param("email") String email);

	@Query("SELECT u FROM User u WHERE u.name = :name")
	User getUserByName(@Param("name") String name);
}
