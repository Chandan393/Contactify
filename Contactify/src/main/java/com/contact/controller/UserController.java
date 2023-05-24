package com.contact.controller;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.dao.ContactRepository;
import com.contact.dao.UserRepository;
import com.contact.entities.Contact;
import com.contact.entities.User;
import com.contact.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	//method for adding common data to Response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
String userName = principal.getName();
		System.out.println("Username "+userName);
		//get the user using username(Email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER "+user);
		model.addAttribute("user", user);
	}
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title", "User DashBoard");
		 return "normal/user-dashboard";
	}
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactFome(Model model) {
		  model.addAttribute("title", "Add Contact");
		  model.addAttribute("contact", new Contact());
		return "normal/add_contact";
	}
	//Processing Add Contact Form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user =this.userRepository.getUserByUserName(name);
			//processing and uploading file
			if(file.isEmpty() ) {
				//if the file is empty the give error message
				System.out.println("File is empty-ERROR");
				contact.setImage("contact.png");
			}
			else {
				//uploading the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is successfully uploaded");
			}
			contact.setuser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("DATA "+contact);
			System.out.println("Added to Database");
			//message success.....
			session.setAttribute("message", new Message("Your Contact is Added", "success"));
		} catch (Exception e) {
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			//error message if contact not added successfully
			session.setAttribute("message", new Message("Something went wrong!! Try again", "danger"));
		}
		return "normal/add_contact";
	}
	//Show contacts Handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		m.addAttribute("title", "View Contacts");
		//contacts list 
           String userName =  principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
    //Pageable Object contains- currentPage-page and Contact per Page-5
	Pageable pageable =	PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	//showing Specific contact details
	@RequestMapping("/{cID}/contact")
	public String showContactDetail(@PathVariable("cID") Integer cID,Model model, Principal principal) {
		System.out.println("cID "+cID);
		Optional<Contact> contactOptional =this.contactRepository.findById(cID);
		Contact contact = contactOptional.get();
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
	if(user.getId() == contact.getuser().getId()) {
		model.addAttribute("title", contact.getName());
		model.addAttribute("contact", contact);
	}
		return "normal/contact_details";
	}
	//delete contact handler	
	@GetMapping("/delete/{cID}")
	public String deleteContact(@PathVariable("cID") Integer cID, Model model,HttpSession session,
			Principal principal) {
		Contact contact = this.contactRepository.findById(cID).get();
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Contact Deleted Successfully", "success"));
		return "redirect:/user/show-contacts/0";
	}
	//Open Update Form handler
	@PostMapping("/update-contact/{cID}")
	public String updateForm(@PathVariable("cID") Integer cID, Model m) {
		m.addAttribute("title","Update Contact");
	      Contact contact = this.contactRepository.findById(cID).get();
	      m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	//Update contact Handler
	@RequestMapping(value= "/process-update", method= RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") 
	MultipartFile file, Model m, HttpSession session, Principal principal) {
		try {
			//fetching old contact details
		Contact oldContactDetail = this.contactRepository.findById(contact.getcID()).get();
			//if new image is selected or not
			if(!file.isEmpty()) {
				    //deleting old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
			    File file1 = new File(deleteFile, oldContactDetail.getImage());
			    file1.delete();
				 //updating new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setuser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact Updated Successfully", "success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Contact Name "+contact.getName());
		return "redirect:/user/"+contact.getcID()+"/contact";
	}
	//Your profile Handler
	@GetMapping("/profile")
	public String yourProfileHandler(Model m) {
		m.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	//open settings handler
	@GetMapping("/settings")
	public String openSettings(Model m) {
		m.addAttribute("title","Settings");
		return "normal/settings";
	}
	//change  password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, 
			@RequestParam("newPassword") String newPassword,Principal principal, HttpSession session) {
		System.out.println("Old Password "+oldPassword);
		System.out.println("New Password "+newPassword);
	   String userName  = principal.getName();
	   User currentUser = this.userRepository.getUserByUserName(userName);
	   if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
		   //change password
		   currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		   this.userRepository.save(currentUser);
		   session.setAttribute("message", new Message("Password Changed Successfully", "success"));
	   } else {
		   //error
		   session.setAttribute("message", new Message("Please Enter Correct Old Password", "danger"));
		   return "redirect:/user/index";
	   }
	   return "redirect:/user/index";
	}
}

