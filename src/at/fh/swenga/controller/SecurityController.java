package at.fh.swenga.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import at.fh.swenga.dao.UserRepository;
import at.fh.swenga.dao.UserRoleRepository;
import at.fh.swenga.model.User;
import at.fh.swenga.model.UserRole;

/**
 * SecurityController Handles all security relevant actions
 * (login,logout,register)
 * 
 * @author CortexLab
 * @version 1.0
 */
@Controller
@SessionAttributes("username")
public class SecurityController {

	@Autowired
	UserRepository userDao;

	@Autowired
	UserRoleRepository userRoleDao;

	/**
	 * Display the login page for the user
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(Model model) {

		return "login";
	}

	/**
	 * Redirect the user to the dashboard if the login was successful
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String loginPost(Model model) {
		return "redirect:dashboard";
	}

	/**
	 * Display the create new user page
	 */
	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String signin(Model model) {

		return "new_account";
	}

	/**
	 * Create a new user in the database with the given information
	 * 
	 * @param newUsername       Username for the new user
	 * @param newOccupation     occupation form the new user
	 * @param newEmail          email form the new user
	 * @param newPassword       password for the new user to login
	 * @param newRepeatPassword repeat the password, userNewPassword and
	 *                          userNewPasswordRepeat must match, this should
	 *                          prevent typos in passwords
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String signinPost(Model model, @RequestParam String newUsername, @RequestParam String newOccupation,
			@RequestParam String newEmail, @RequestParam String newPassword, @RequestParam String newRepeatPassword) {

		String errorMsg = "";
		int missingValues = 0;
		if (newUsername.equals("")) {
			errorMsg += "username";
			missingValues++;
		}

		if (newOccupation.equals("")) {
			if (!errorMsg.equals(""))
				errorMsg += ", ";
			errorMsg += "occupation";
			missingValues++;
		}

		if (newEmail.equals("")) {
			if (!errorMsg.equals(""))
				errorMsg += ", ";
			errorMsg += "email";
			missingValues++;
		}

		if (newPassword.equals("")) {
			if (!errorMsg.equals(""))
				errorMsg += ", ";
			errorMsg += "password";
			missingValues++;
		}

		// If values are missing => display a error message
		if (missingValues > 0) {
			model.addAttribute("errorMsg", errorMsg + (missingValues == 1 ? " is" : " are") + " required");

			model.addAttribute("newUsername", newUsername);
			model.addAttribute("newOccupation", newOccupation);
			model.addAttribute("newEmail", newEmail);

			return "new_account";
		}

		// Also check serverside if the passwords match
		if (!newPassword.equals(newRepeatPassword)) {
			model.addAttribute("errorMsg", "passwords don't match");

			model.addAttribute("newUsername", newUsername);
			model.addAttribute("newOccupation", newOccupation);
			model.addAttribute("newEmail", newEmail);

			return "new_account";
		}

		// checks for existing username
		if (userDao.findByUsername(newUsername) != null) {
			model.addAttribute("errorMsg", "username is already taken");

			model.addAttribute("newUsername", "");
			model.addAttribute("newOccupation", newOccupation);
			model.addAttribute("newEmail", newEmail);

			return "new_account";
		}

		// Create a new userobject and try to save it in the db
		User user = new User(newUsername, newEmail, newOccupation, newPassword, true);
		user.encryptPassword();
		userDao.persist(user);

		try {
			// userDao.persist(user);

			UserRole userRole = userRoleDao.getRole("ROLE_USER");
			if (userRole == null) {
				userRole = new UserRole("ROLE_USER");
				userRoleDao.persist(userRole);
			}

			user.addUserRole(userRole);
			userDao.merge(user);

			// On success
			model.addAttribute("newUsername", newUsername);
			return "login";
		} catch (Exception e) {
			model.addAttribute("errorMsg", "Couldn't create account: " + e.getMessage());
		}
		// on Fail
		return "new_account";
	}

	/**
	 * Log the current user out and redirect to the login page, the logout itself is
	 * handled by spring security
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(Model model) {

		return "login";
	}
}