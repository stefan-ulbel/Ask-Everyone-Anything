package at.fh.swenga.bl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import at.fh.swenga.dao.GroupRepository;
import at.fh.swenga.dao.UserRepository;
import at.fh.swenga.dao.UserRoleRepository;
import at.fh.swenga.model.Quiz;
import at.fh.swenga.model.User;

/**
 * Helper class used for various permission checks
 * 
 * @author CortexLab
 * @version 1.0
 */
@Component("PermissionHelper")
public class PermissionHelper {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserRoleRepository userRoleRepository;
	
	@Autowired
	GroupRepository groupRepository;
	
	private final String adminRole = "ROLE_ADMIN";
	
	/**
	 * Gets the current username from the logged in user
	 * @return current username from the logged in user as a String
	 */
	public String getCurrentUserUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	/**
	 * Gets the current user object from the logged in user and loads the tag list from the user
	 * @return current logged in user as User object with the tag list loaded
	 */
	public User getCurrentUserWithTags() {
		User user = getCurrentUser();
		if (user != null)
			user.setTagList(userRepository.findUserTagsFromUser(user.getId()));
		return user;
	}

	/**
	 * Gets the current user object from the logged in user
	 * @return current logged in user as User object
	 */
	public User getCurrentUser() {
		return userRepository.findByUsername(getCurrentUserUsername());
	}

	/**
	 * Gets the current user object from the logged in user and with the roles of the user
	 * @return current logged in user as User object with the role list loaded
	 */
	public User getCurrentUserWithRoles() {
		User user = getCurrentUser();
		return getCurrentUserWithRoles(user);
	}
	
	/**
	 * Loads the roles of the user
	 * @param user User object for which the roles should be loaded
	 * @return user with the role list loaded
	 */
	public User getCurrentUserWithRoles(User user) {
		if (user != null)
			user.setUserRoles(userRoleRepository.getUserRoles(user));
		return user;
	}

	/**
	 * Checks if a user is allowed to edit a certain quiz
	 * @param user	User who tires to edit the quiz
	 * @param quiz	Quiz which should get edited
	 * @return boolean if the user is allowed to edit the quiz
	 */
	public boolean checkPermission(User user, Quiz quiz) {

		if (quiz == null || user == null)
			return false;

		if (user.getUserRoles().contains(userRoleRepository.getRole("ROLE_ADMIN")))
			return true;

		if ((quiz.getOwner() != null && quiz.getOwner().getId() == user.getId())) {
			return true;
		}

		return false;

	}
	
	/**
	 * Checks if the user is allowed to participate in a quiz
	 * @param user	User who tries to participate
	 * @param quiz	Quiz the user tries to participate in
	 * @return	boolean	if the user is allowed to participate in the quiz
	 */
	public boolean checkGroupMembershipAndOwnerShip(User user, Quiz quiz) {
		
		if(user == null) return false;
		if(quiz == null) return false;
		
		if(quiz.getOwner() != null && quiz.getOwner().getId() == user.getId()) return true;
		
		// Check if not a member
		if (quiz.getGroup() != null && groupRepository.checkGroupMembers(user.getId(), quiz.getGroup().getId()) == 0) {
			return false;
		}
		return true;
	}

	/**
	 * Helper function which checks if a quiz is open to participate in (compares start and end date with the current date)
	 * @param quiz	Quiz which should get checked
	 * @return	boolean	true => the quiz is open
	 */
	public boolean checkIfQuizIsOpen(Quiz quiz) {
		if ((quiz.getStartOn() != null && quiz.getStartOn().after(new Date()))
				|| (quiz.getEndOn() != null && quiz.getEndOn().before(new Date()))) {
			return false;
		}
		return true;
	}
	
	// 
	/**
	 * Checks if a user is allowed to edit a certain quiz, gets the current user from spring security
	 * Don´t use this method in loops, get the user first
	 * @param user	User who tires to edit the quiz
	 * @param quiz	Quiz which should get edited
	 * @return boolean if the user is allowed to edit the quiz
	 */
	public boolean checkPermission(Quiz quiz) {
		User user = getCurrentUserWithRoles();
		return checkPermission(user, quiz);
	}
	
	/**
	 * Checks if a user is allowed to edit a certain user, gets the current user from spring security
	 * Don´t use this method in loops, get the user first
	 * @param editedUser	User which should get edited
	 * @return boolean if the user is allowed to edit the user
	 */
	public boolean checkPermission(User editedUser) {
		User editingUser = getCurrentUserWithRoles();
		return checkPermission(editingUser, editedUser);
	}

	/**
	 * Checks if a user is allowed to edit a certain user
	 * @param editingUser	User which tries to edit the other user
	 * @param editedUser	User which should get edited
	 * @return boolean if the user is allowed to edit the user
	 */
	public boolean checkPermission(User editingUser, User editedUser) {

		if (editingUser == null || editedUser == null)
			return false;

		if (editingUser.getUserRoles().contains(userRoleRepository.getRole(adminRole)))
			return true;

		// You can always edit yourself
		if (editingUser.getId() == editedUser.getId()) {
			return true;
		}

		return false;
	}
	
	/**
	 * @param user	the current user which should get checked
	 * @return	Returns if the current user has the admin role
	 */
	public boolean isAdmin(User user)
	{
		user.setUserRoles(userRoleRepository.getUserRoles(user));
		if(user.getUserRoles().contains(userRoleRepository.getRole(adminRole)))
			return true;
		else
			return false;
	}
}
