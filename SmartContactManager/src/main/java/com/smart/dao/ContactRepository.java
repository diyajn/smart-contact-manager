package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;


public interface ContactRepository extends JpaRepository<Contact, Integer>{

	
	//Page-sublist of a list of objects
	@Query("from Contact as c where c.user.id=:x")
	//pageable contains:
	//currentPage-page
	//Contact per page-5
	public Page<Contact> findContactsByUser(@Param("x") int userId, Pageable pageable);
	
	//jo is string ko contain kre rhe honge vo sare naam aajayenge
	//jo bnda login h usi k contacts show hone chahiye
	public List<Contact> findByNameContainingAndUser(String keywords,User user);  
	
	
}
