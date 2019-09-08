package at.fh.swenga.controller;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import at.fh.swenga.bl.PermissionHelper;
import at.fh.swenga.dao.UserRepository;
import at.fh.swenga.dao.UserRoleRepository;
import at.fh.swenga.model.User;

/**
 * UserController Handles all user object specific actions (edit user, list
 * users, ...)
 * 
 * @author CortexLab
 * @version 1.0
 */
@Controller
@SessionAttributes("username")
public class UserController {
	@Autowired
	UserRepository userRepository;
	@Autowired
	UserRoleRepository userRoleRepository;

	@Autowired
	PermissionHelper permissionHelper;

	/**
	 * Prepare the user edit page and fill them with the user info, also do
	 * permission checks
	 * 
	 * @param userId user id from the user which should get edited
	 */
	@RequestMapping(value = { "editUser" })
	public String editUser(Model model, @RequestParam(required = false, defaultValue = "-1") int userId) {

		User user;
		if (userId == -1) {
			// Edit current user
			user = permissionHelper.getCurrentUser();
		} else {
			// Edit the user with the given userId
			user = userRepository.findById(userId);

		}

		if (user != null) {

			if (!permissionHelper.checkPermission(user)) {
				model.addAttribute("errorMsg", "You dont have the permission to modify this user!");
				return "redirect:dashboard";
			}

			model.addAttribute("userId", user.getId());

			model.addAttribute("userEmail", user.getEmail());
			model.addAttribute("userName", user.getUsername());
			model.addAttribute("userOccupation", user.getOccupation());
			model.addAttribute("userEnabled", user.isEnabled());

			user.setTagList(userRepository.findUserTagsFromUser(user.getId()));
			model.addAttribute("userTags", user.getTagList());

		} else {
			// ERROR user not found
			model.addAttribute("errorMsg", "User not found!");
			return "redirect:dashboard";
		}

		return "edit_user";
	}

	/**
	 * Save changes to a user object
	 * 
	 * @param userId         user id of the changed user
	 * @param userEmail      new email address of the changed user
	 * @param userOccupation new occupation of the changed user
	 */
	@RequestMapping(value = { "saveUser" })
	public String saveUser(Model model, @RequestParam int userId, @RequestParam String userEmail,
			@RequestParam String userOccupation) {

		User user = userRepository.findById(userId);

		if (user != null) {

			if (!permissionHelper.checkPermission(user)) {
				model.addAttribute("errorMsg", "You dont have the permission to modify this user!");
				return "redirect:dashboard";
			}

			// empty checks
			if (userEmail.isEmpty() || userOccupation.isEmpty()) {
				model.addAttribute("errorMsg", "All fields must be filled out.");
				// Prefill old valid values
				model.addAttribute("userId", user.getId());
				model.addAttribute("userEmail", user.getEmail());
				model.addAttribute("userName", user.getUsername());
				model.addAttribute("userOccupation", user.getOccupation());
				model.addAttribute("userTags", userRepository.findUserTagsFromUser(user.getId()));
				user.setEnabled(true);
				return "edit_user";
			}

			user.setEmail(userEmail);
			user.setOccupation(userOccupation);
			user.setEnabled(true);

			userRepository.merge(user);

			model.addAttribute("userId", userId);
			model.addAttribute("userEmail", user.getEmail());
			model.addAttribute("userName", user.getUsername());
			model.addAttribute("userOccupation", user.getOccupation());
			model.addAttribute("userEnabled", user.isEnabled());
			model.addAttribute("userTags", userRepository.findUserTagsFromUser(user.getId()));

			model.addAttribute("successMsg", "Changes saved.");

		} else {
			model.addAttribute("errorMsg", "User not found!");
			return "redirect:dashboard";
		}

		return "edit_user";
	}

	/**
	 * Add a new user tag to a user object
	 * 
	 * @param userId     user id from the user which should get the new tag
	 * @param newUserTag new tag which should get added to the user, can also be
	 *                   multible comma seperated tags
	 * @return
	 */
	@RequestMapping(value = { "addUserTag" })
	public String addUserTag(Model model, RedirectAttributes redirectAttributes, @RequestParam int userId,
			@RequestParam String newUserTag) {

		User user = userRepository.findById(userId);

		if (user != null) {

			// Check if the user has permissions to edit the user
			if (!permissionHelper.checkPermission(user)) {
				model.addAttribute("errorMsg", "You dont have the permission to modify this user!");
				return "redirect:dashboard";
			}

			user.setTagList(userRepository.findUserTagsFromUser(user.getId()));

			// Multible tags can be added comma seperated
			String prepNewUserTag = newUserTag.replaceAll(",", ";");
			String[] newTags = prepNewUserTag.split(";");

			for (String newTag : newTags) {
				if (newTag.trim().length() > 0)
					user.addUserTag(newTag.trim());
			}

			userRepository.merge(user);

			redirectAttributes.addAttribute("userId", user.getId());

			return "redirect:editUser";
		} else {
			redirectAttributes.addAttribute("errorMsg", "User ID invalid!");
			return "redirect:dashboard";
		}

	}

	/**
	 * Delete a user tag from a user
	 * 
	 * @param userId     id from the user from which the tags should get removed
	 * @param newUserTag Tags which should get removed, can also be multible comma
	 *                   seperated tags
	 */
	@RequestMapping(value = { "deleteUserTag" })
	public String deleteUserTag(Model model, RedirectAttributes redirectAttributes, @RequestParam int userId,
			@RequestParam String newUserTag) {

		User user = userRepository.findById(userId);

		if (user != null) {

			// Check if the user has permissions to edit the user
			if (!permissionHelper.checkPermission(user)) {
				model.addAttribute("errorMsg", "You dont have the permission to modify this user!");
				return "redirect:dashboard";
			}

			user.setTagList(new HashSet<>(userRepository.findUserTagsFromUser(user.getId())));

			user.removeUserTag(newUserTag);

			userRepository.merge(user);

			redirectAttributes.addAttribute("userId", user.getId());

			return "redirect:editUser";
		} else {
			redirectAttributes.addAttribute("errorMsg", "User ID invalid!");
			return "redirect:dashboard";
		}
	}

	/**
	 * Enable or disable a user, a disabled user can not log in
	 * 
	 * @param userId       id from the user
	 * @param continueEdit If this variable is set, the current editing user will
	 *                     get redirected back to the user edit page, this exists
	 *                     because a user can also be disabled/enabled from the list
	 *                     user page (admin only)
	 * @return
	 */
	@RequestMapping(value = { "toggleDisableUser" })
	public String toggleDisableUser(Model model, RedirectAttributes redirectAttributes, @RequestParam int userId,
			@RequestParam(required = false) String continueEdit) {

		User user = userRepository.findById(userId);

		if (user != null) {

			// Check if the user has permissions to edit the user
			if (!permissionHelper.checkPermission(user)) {
				model.addAttribute("errorMsg", "You dont have the permission to modify this user!");
				return "redirect:dashboard";
			}

			user.setEnabled(!user.isEnabled());
			userRepository.merge(user);

			// Redirect back to the editUser page if the request has been fired from the editUser page
			if (continueEdit != null) {
				redirectAttributes.addAttribute("userId", user.getId());
				return "redirect:editUser";
			}
			return "redirect:listUsers";
		} else {
			redirectAttributes.addAttribute("errorMsg", "User ID invalid!");
			return "redirect:dashboard";
		}
	}

	/**
	 * List all users from the database
	 */
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = { "listUsers" })
	public String list(Model model) {
		model.addAttribute("users", userRepository.findAll());
		return "list_users";
	}

	/**
	 * Change the password from a user
	 * 
	 * @param userId                user id from which the password should be
	 *                              changed
	 * @param userNewPassword       new password for the user
	 * @param userNewPasswordRepeat repeat the password, userNewPassword and
	 *                              userNewPasswordRepeat must match, this should
	 *                              prevent typos in passwords
	 * @return
	 */
	@RequestMapping(value = { "changePassword" })
	public String changePassword(Model model, RedirectAttributes redirectAttributes, @RequestParam int userId,
			@RequestParam String userNewPassword, @RequestParam String userNewPasswordRepeat) {

		User user = userRepository.findById(userId);

		if (user != null) {

			redirectAttributes.addAttribute("userId", user.getId());

			// Check if the user has permissions to edit the user
			if (!permissionHelper.checkPermission(user)) {
				redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this user!");
				return "redirect:dashboard";
			}

			// Check if the passwords match
			if (userNewPassword.equals(userNewPasswordRepeat)) {
				// Prevent empty passwords
				if (userNewPassword.trim().length() > 0) {
					user.setPassword(userNewPassword);
					user.encryptPassword();
					userRepository.merge(user);
					redirectAttributes.addAttribute("successMsg", "Password changed!");
				} else {
					redirectAttributes.addAttribute("errorMsg", "Passwords cannot be empty or only spaces!");
					return "redirect:editUser";
				}
			} else {
				redirectAttributes.addAttribute("errorMsg", "Passwords do not match!");
				return "redirect:editUser";
			}

			return "redirect:editUser";
		} else {
			redirectAttributes.addAttribute("errorMsg", "User ID invalid!");
			return "redirect:dashboard";
		}

	}
}
