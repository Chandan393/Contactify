package com.contact.dao;

import com.contact.entities.Contact;

import com.contact.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	@Query("from Contact as c where c.user.id =:userId")
	Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);

	// Search contact
	List<Contact> findByNameContainingAndUser(String keywords, User user);
}
