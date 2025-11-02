package com.smart.controller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.smart.config.MyConfig;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import net.bytebuddy.utility.RandomString;

@Controller
@RequestMapping("/user")
public class UserController {

    private final BCryptPasswordEncoder passwordEncoder;

	private final MyConfig myConfig;
	private final HomeController homeController;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;

	UserController(HomeController homeController, MyConfig myConfig, BCryptPasswordEncoder passwordEncoder) {
		this.homeController = homeController;
		this.myConfig = myConfig;
		this.passwordEncoder = passwordEncoder;
	}

	// method for adding common data to response (ab yeh method har handle k liye chalega)
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		//System.out.println("common data handler run first");
		String username = principal.getName();
		User user = userRepository.getUserByEmail(username);
		model.addAttribute("user", user);
	}
	
	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "Dashboard - Smart Contact Manager");
		return "user/user_dashboard";
	}

	// showing particular contact details
	@RequestMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") int id, Model model, Principal principal) {
		Optional<Contact> contactOptional = contactRepository.findById(id);
		Contact contact = contactOptional.get();
		
		String username = principal.getName();
		User user = userRepository.getUserByEmail(username); // login wala user nikala
		
		if (user.getId() == contact.getUser().getId()) { // Restriction on user to get other contacts
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "user/contact_details";
	}

	// show contacts handler (per page hme 5 contacts dikhane h and current
	// page-page)
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") int page, Model model, Principal principal) {
//		 contact ki puri list bhejni hai

//		 one way
//		 String username = principal.getName();
//		 User user = userRepository.getUserByEmail(username);
//		 List<Contact> contacts=user.getContacts();

		// second way (using contact repo)
		String username = principal.getName();
		User user = userRepository.getUserByEmail(username);

		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(), pageable);
//			System.out.println(user.getContacts());
//			System.out.println(contacts.getContent());//the actual list of Contact objects for the current page.
//			System.out.println(contacts.getNumber());//the current page number (0-based).
//			System.out.println(contacts.getSize());//the size of each page (how many items per page).
//			System.out.println(contacts.getTotalElements());//total number of items(sab page k milakr)
//			System.out.println(contacts.getTotalPages());//total number of pages.(kitne page m divide hue h contacts)
//			System.out.println(contacts.hasNext());
//			System.out.println(contacts.hasPrevious());
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		model.addAttribute("title", "ShowContacts- Smart Contact Manager");
		return "user/show_contacts";
	}

	// Delete Contact Handler
	@RequestMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") int id, Principal principal, HttpSession session) {
		Optional<Contact> contactOptional = contactRepository.findById(id);
		Contact contact = contactOptional.get();

		String username = principal.getName();
		User user = userRepository.getUserByEmail(username);

		if (user.getId() == contact.getUser().getId()) { // check kra vo hi user h naa
		    // delete the image from the folder
			//don't delete default contact.png
			String imageName = contact.getImageUrl();
			if (imageName != null && !imageName.isBlank() && !imageName.contentEquals("contact.png")) {
				try {
					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imageName);
					Files.deleteIfExists(path);
					System.out.println("Image deleted: " + imageName);

				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Failed to delete image: " + imageName);
				}
			}
			// Now delete the contact from the database
			contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact Deleted Succesfully!!", "success"));
		} else {
			session.setAttribute("message", new Message("Not Allow to Delete this Contact!!", "danger"));
		}
		return "redirect:/user/show-contacts/0";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "ContactForm - Smart Contact Manager");
		model.addAttribute("contact", new Contact());
		return "user/add_contacts";
	}
	
	// process add form handler
	@PostMapping("/process-contact")
	public String processAddContactForm(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			String username = principal.getName();
			User user = userRepository.getUserByEmail(username); // user nikala

			// processing and uploading file..
			if (file.isEmpty()) {
				// default photo set krdenge
				System.out.println("Default Image is uploaded!!");
				contact.setImageUrl("contact.png");

			} else {
				// upload the file to folder and update the name to contact db
				Random random=new Random();
				int randomInt=random.nextInt(1000);
				contact.setImageUrl(randomInt+file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + randomInt+file.getOriginalFilename());
				try (InputStream inputStream = file.getInputStream()) {
					Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
				}
				System.out.println("Image is uploaded!!");
			}
			contact.setUser(user); // contact m user set krdiya
			user.getContacts().add(contact); // user m contact add krdiya
			userRepository.save(user); // update krdega user ko
			System.out.println("Added to Database!!");
			// message success........
			session.setAttribute("message", new Message("Your Contact is added!! Add more....", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			// message error........
			session.setAttribute("message", new Message("Something went Wrong!! Try again....", "danger"));
		}
		return "user/add_contacts";
	}

	// open update form handler
	@RequestMapping("/update/{cId}")
	public String openUpdateContactForm(@PathVariable("cId") int id, Principal principal, Model model,
			HttpSession session) {

		Optional<Contact> contactOptional = contactRepository.findById(id);
		Contact contact = contactOptional.get();

		String username = principal.getName();
		User user = userRepository.getUserByEmail(username);

		if (user.getId() == contact.getUser().getId()) { // check kra vo hi user h naa
			model.addAttribute("title", "Update ContactForm - Smart Contact Manager");
			model.addAttribute("contact", contact);
		}
		return "user/update_contact";
	}

	// process update form handler
	@PostMapping("/update-contact")
	public String UpdateContactForm(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session, Model model) {

		try {
			Contact oldcontact = contactRepository.findById(contact.getcId()).get();

			if (!file.isEmpty()) {
				// delete old photo
				String imageName = oldcontact.getImageUrl(); // old photo k url
				if (imageName != null && !imageName.isBlank() && !imageName.contentEquals("contact.png")) {
					try {
						File saveFile = new ClassPathResource("static/img").getFile();
						Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imageName);
						Files.deleteIfExists(path);
						System.out.println("Image deleted: " + imageName);

					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("Failed to delete image: " + imageName);
					}
				}

				// upload new photo
				Random random = new Random();
				int randomInt = random.nextInt(1000);
				contact.setImageUrl(randomInt + file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator + randomInt + file.getOriginalFilename());
				try (InputStream inputStream = file.getInputStream()) {
					Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
				}
				System.out.println("Image is updated!!");
			} else {
				contact.setImageUrl(oldcontact.getImageUrl());
			}

			String username = principal.getName();
			User user = userRepository.getUserByEmail(username);
			contact.setUser(user);
			contactRepository.save(contact); // yeh update krdega contact ko

			// message success........
			session.setAttribute("message", new Message("Your Contact is Updated!! Add more....", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			// message error........
			session.setAttribute("message", new Message("Something went Wrong!! Try again....", "danger"));
		}
		return "redirect:/user/contact/" + contact.getcId();
	}
	
	//view profile page handler
	@RequestMapping("/profile")
	public String viewProfilePage(Model model) {
		model.addAttribute("title", "Profile Page - Smart Contact Manager");
		return "user/profile";
	}
	
	// view setting page handler
	@RequestMapping("/setting")
	public String viewSettingPage(Model model) {
		model.addAttribute("title", "setting Page - Smart Contact Manager");
		return "user/setting";
	}

	// change password handler
	@PostMapping("/change")
	public String changePassword(@RequestParam("oldp") String oldpassword, @RequestParam("newp") String newpassword,Principal principal,HttpSession session) {
		System.out.println("old password: "+oldpassword);
		User user=userRepository.getUserByEmail(principal.getName());
		System.out.println("current password: "+user.getPassword());
	
		if(passwordEncoder.matches(oldpassword,user.getPassword())) {
			System.out.println("password matches");
			//change the password
			user.setPassword(passwordEncoder.encode(newpassword));
			userRepository.save(user);
			System.out.println("new password: "+newpassword);
			session.setAttribute("message", new Message("Your Password is successfully changed!!", "success"));
			
			
		}else {
			System.out.println("password not matches");
			//wrong old password
			session.setAttribute("message", new Message("Please Enter correct old Password!!", "danger"));
			return "redirect:/user/setting";
		}
	
	
		return "redirect:/user/index";
	}
	
	
	
	
	

}
