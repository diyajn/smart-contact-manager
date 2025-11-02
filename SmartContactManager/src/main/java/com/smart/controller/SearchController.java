package com.smart.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@RestController  //(yeh json return krega)
public class SearchController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	//search handler
	@GetMapping("/search/{keyword}")
	public ResponseEntity<?> search(@PathVariable("keyword") String keyword,Principal principal ){
		System.out.println(keyword);
		User user=userRepository.getUserByEmail(principal.getName());
		List<Contact> contacts=contactRepository.findByNameContainingAndUser(keyword, user);
		return ResponseEntity.ok(contacts);
	}
}
