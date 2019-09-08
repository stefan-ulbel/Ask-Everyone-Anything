package at.fh.swenga.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import at.fh.swenga.bl.PermissionHelper;
import at.fh.swenga.dao.GroupRepository;
import at.fh.swenga.dao.UserRepository;
import at.fh.swenga.dao.UserRoleRepository;
import at.fh.swenga.model.Group;
import at.fh.swenga.model.User;


/**
 * GroupController	 Handles all group actions and pages
 * 
 * @author CortexLab
 * @version 1.0
 */
@Controller
@SessionAttributes("username")
public class GroupController {
	
	@Autowired
	GroupRepository groupRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserRoleRepository userRoleRepository;
	
	@Autowired
	PermissionHelper permissionHelper;

	/**
	 * Shows All Groups which are public and validated
	 * ...
	 */
	@RequestMapping(value = {"listAllGroups" })
	public String listAllGroups(Model model) {
		// Get current user:
		User user = permissionHelper.getCurrentUser();
		if(user == null) return "redirect:login";
		
		// get all public and validated groups, split up into groups where user is member and is not member.
		// Groups where user is not member will have a join link on the page. Groups where user is member
		// will have a leave option:
		List<Group> groups_member = groupRepository.getAllPublicAndValidatedGroupsWhereUserIsMember(user.getId());
		List<Group> groups_not_member = groupRepository.getAllPublicAndValidatedGroupsWhereUserIsNotMember(user.getId());
		model.addAttribute("groups_member", groups_member);
		model.addAttribute("groups_not_member", groups_not_member);
		model.addAttribute("userid", user.getId()); // id is needed to compare userid with ownerId
		
		return "public_groups";
	}	
	
	/**
	 * Shows Groups of which user is member.
	 * ...
	 */
	@RequestMapping(value = {"listMyGroups" })
	public String listMyGroups(Model model) {

		User user = permissionHelper.getCurrentUser();
		if(user == null) return "redirect:login";
		model.addAttribute("username", user.getUsername());
		model.addAttribute("userid", user.getId()); // id is needed to compare userid with ownerId
		
		List<Group> groups = groupRepository.findGroupsForUser(user.getId()); 
		model.addAttribute("groups", groups);
		
		return "my_groups";
	}
	
	/**
	 * Join a group
	 * 
	 * @param id Id of the group to join
	 * @param page Name of the page to redirect after joining. Either public groups or my groups page  
	 */
	@RequestMapping(value = {"joinGroup" })
	public String joinGroup(Model model, RedirectAttributes redirectAttributes, 
			@RequestParam int id, @RequestParam String page) {
		
		User user = permissionHelper.getCurrentUser();
		if(user == null) return "redirect:login";
		model.addAttribute("username", user.getUsername());
				
		Optional<Group> groupToJoin = groupRepository.findById(id);
		if(groupToJoin.isPresent())
		{
			Group group = groupToJoin.get();
			if(group.isValidated() && !group.isIs_private())
			{
				group.setUsers(groupRepository.findGroupMembers(group.getId()));
				group.addMember(user);
				groupRepository.save(group);
				redirectAttributes.addAttribute("message", "You are now a member of " + group.getGroupName());
			}
				
		}
		else
			redirectAttributes.addAttribute("errorMsg", "Group does not exist");
			
		if(page.equals("public"))
			return "redirect:listAllGroups";
		else
			return "redirect:listMyGroups";
	}
	
	/**
	 * Leave a group
	 *
	 * @param id Id of the group to leave
	 * @param page Name of the page to redirect after joining.
	 * Either public groups or my groups page  
	 */
	@RequestMapping(value = {"leaveGroup" })
	public String leaveGroup(Model model, RedirectAttributes redirectAttributes, 
			@RequestParam int id, @RequestParam String page) {
		
		User user = permissionHelper.getCurrentUser();
		if(user == null) return "redirect:login";
		model.addAttribute("username", user.getUsername());
		
		Optional<Group> groupToLeave = groupRepository.findById(id);
		if(groupToLeave.isPresent())
		{
			Group group = groupToLeave.get();
			if(group.getGroupOwner().getId() == user.getId()) // the owner may not leave a group
			{
				redirectAttributes.addAttribute("errorMsg", "Owner can't "
						+ "leave group. Change group owner first.");
				if(page.equals("public"))
					return "redirect:listAllGroups";
				else
					return "redirect:listMyGroups";
			}
			
			// Set group members since lazy loading does not work
			group.setUsers(groupRepository.findGroupMembers(group.getId()));			
			group.removeMember(user);
			groupRepository.save(group);
			redirectAttributes.addAttribute("message", "Successfully left " + group.getGroupName());
		}
		else
			redirectAttributes.addAttribute("errorMsg", "Group does not exist");
			
		
		if(page.equals("public"))
			return "redirect:listAllGroups";
		else
			return "redirect:listMyGroups";
	}
	
	/**
	 * Build the page to edit or create a group
	 * 
 	 * @param id If this parameter is present, it represents the id 
 	 * of the group to edit. If not, the user wants to create a new
 	 * group.  
	 */
	@GetMapping("/editGroup")
	public String editGroup(Model model, RedirectAttributes redirectAttributes, 
			@RequestParam(value="id", required=false) Integer id) {
		
		User user = permissionHelper.getCurrentUser();
		if(user == null) return "redirect:login";
		model.addAttribute("username", user.getUsername());
		
		if(id == null)		// No id specified means the user wants to create a new group					
			return "edit_group"; // Go directly to the edit/create-group page	
			
		
		Optional<Group> groupToEdit = groupRepository.findById(id);
		if(groupToEdit.isPresent())
		{
			Group group = groupToEdit.get();
			// if user is not owner of group and not admin:
			if(group.getGroupOwner().getId() != user.getId() && !permissionHelper.isAdmin(user)) 
			{
				redirectAttributes.addAttribute("errorMsg", "Must be owner of group!");
				return "redirect:listMyGroups";
			}
			
			model.addAttribute("group", group);		
		}
		else
		{	redirectAttributes.addAttribute("errorMsg", "Group does not exist");
			return "redirect:listMyGroups";
		}
						
		
		return "edit_group";
	}
	
	/**
	 * Processes the Post-Request of an edited group.
	 * 
 	 * @param changedGroup The changed group.
 	 * @param bindingResult Holds the result of the validation and
 	 * binding and contains errors that may have occurred
 	 * @param newOwnerName Username of the (new) owner.
 	 * @param checkboxValue If this varaible is not null, the private checkbox was checked in the form
	 */
	@PostMapping("/editGroup")
	public String editGroup(@Valid Group changedGroup, RedirectAttributes redirectAttributes, 
			BindingResult bindingResult, Model model, @RequestParam("newOwner") String newOwnerName, 
			@RequestParam(value = "is_private_cb", required = false) String checkboxValue) {

		// Any errors? -> Create a String out of all errors and return to the page
		if (bindingResult.hasErrors()) {
			String errorMessage = "";
			for (FieldError fieldError : bindingResult.getFieldErrors()) {
				errorMessage += fieldError.getField() + " is invalid: "+fieldError.getCode()+". ";
			}
			redirectAttributes.addAttribute("errorMsg", errorMessage);
			return "redirect:listMyGroups";
		}

		User user = permissionHelper.getCurrentUser();
		if(user == null) return "redirect:login";
		
		// Retrieve the group we want to change:	
		Optional<Group> groupToEdit = groupRepository.findById(changedGroup.getId());
		if(groupToEdit.isPresent())
		{
			Group group = groupToEdit.get();
			// if user is not owner of group and not admin:
			if(group.getGroupOwner().getId() != user.getId() && !permissionHelper.isAdmin(user)) 
			{
				redirectAttributes.addAttribute("errorMsg", "Must be owner of group!");
				return "redirect:listMyGroups";
			}
			// Change the attributes:			
			// Set new Owner:
			User newOwner = userRepository.findByUsername(newOwnerName);
			if(newOwner != null)
				group.setGroupOwner(newOwner);
			else
			{
				redirectAttributes.addAttribute("errorMsg", "Invalid owner");
				return "redirect:listMyGroups";
			}
			
			
			group.setGroupName(changedGroup.getGroupName());
			group.setGroupDescription(changedGroup.getGroupDescription());			
			
			//Check if private checkbock was checked in form:
			if(checkboxValue != null)
				group.setIs_private(true);
			else
				group.setIs_private(false);
			
			//save the group
			groupRepository.save(group);
			redirectAttributes.addAttribute("message", "Changed group " + changedGroup.getGroupName());
			
			} else
				redirectAttributes.addAttribute("errorMsg", "Group does not exist!");			
		
		//admin gets forwarded to manage groups page
		if(permissionHelper.isAdmin(user))
			return "redirect:adminGroups";
		else
			return "redirect:listMyGroups";		
		
	}
	
	/**
	 * Request GET mapping to create a group
	 * ...
	 */
	@GetMapping("/addGroup")
	public String showAddGroupForm(Model model) {
		return "redirect:editGroup";
	}
	
	/**
	 * Processes the Post-Request of a new group.
	 * 
 	 * @param newGroup The new group.
 	 * @param bindingResult Holds the result of the validation
 	 * and binding and contains errors that may have occurred
 	 * @param newOwnerName Username of the owner of the new group.
 	 * @param checkboxValue If this varaible is not null, the private checkbox was checked in the form
	 */
	@PostMapping("/addGroup")
	public String addGroup(@Valid Group newGroup, RedirectAttributes redirectAttributes, BindingResult bindingResult,
			Model model, @RequestParam(value="newOwner") String newOwnerName,
			@RequestParam(value = "is_private_cb", required = false) String checkboxValue) {

		// Any errors? -> Create a String out of all errors and return to the page
		if (bindingResult.hasErrors()) {
			String errorMessage = "";
			for (FieldError fieldError : bindingResult.getFieldErrors()) {
				errorMessage += fieldError.getField() + " is invalid: " + fieldError.getCode() + ". ";
			}
			redirectAttributes.addAttribute("errorMsg", errorMessage);

			return "redirect:listMyGroups";
		}

		// Retrieve current user:
		User user = permissionHelper.getCurrentUser();
		if(user == null) return "redirect:login";		
		
		// Look for group with same name in the list. One available -> Abort and redirect
		Group existingGroup = groupRepository.findByGroupName(newGroup.getGroupName());		
		if (existingGroup != null)
		{
			redirectAttributes.addAttribute("errorMsg", "Group already exists!");
			return "redirect:listMyGroups";
		}
		else
		{
			// Retrieve owner user-object
			User newOwner = userRepository.findByUsername(newOwnerName);
			if(newOwner == null)			
			{
				redirectAttributes.addAttribute("errorMsg", "Invalid owner");
				return "redirect:listMyGroups";
			}
			Group group = new Group();
			// Set attributes of new group:
			group.setGroupName(newGroup.getGroupName());
			group.setGroupDescription(newGroup.getGroupDescription());
			group.setUsers(groupRepository.findGroupMembers(group.getId()));
			group.addMember(user);
			group.addMember(newOwner);
			group.setGroupOwner(newOwner);
			
			if(checkboxValue != null)
				group.setIs_private(true);
			else
				group.setIs_private(false);
			
			// Save new group
			groupRepository.save(group);
			redirectAttributes.addAttribute("message", "New group " + newGroup.getGroupName() + " added."
					+ " An Admin will have to approve this group before other people can join.");
		}
		
		// Redirect to my groups page
		return "redirect:listMyGroups";
	}
	
	/**
	 * Delete a group
	 * 
	 * @param id Id of the group to delete
	 */
	@GetMapping("/deleteGroup")
	public String deleteGroup(Model model, RedirectAttributes redirectAttributes,
			@RequestParam(value="id") Integer id) {
		
		// Get current user:
		User user = permissionHelper.getCurrentUser();
		if(user == null) return "redirect:login";
		// Find group to delete:
		Optional<Group> groupToDelete = groupRepository.findById(id);

		if(groupToDelete.isPresent())
		{
			Group group = groupToDelete.get();
			// if user is not owner of group and not admin
			if(group.getGroupOwner().getId() != user.getId() && !permissionHelper.isAdmin(user)) 
			{
				redirectAttributes.addAttribute("errorMsg", "Must be owner of group!");
				return "redirect:listMyGroups";
			}
			
			// Delete the group:
			groupRepository.deleteById(id);
			redirectAttributes.addAttribute("message", "Successfully deleted " + group.getGroupName());
			
		}
		else
			redirectAttributes.addAttribute("errorMsg", "Invalid group");		
		
		// Admin will be redirected to manage groups page
		if(permissionHelper.isAdmin(user))
			return "redirect:adminGroups";
		else
			return "redirect:listMyGroups";		
	}
	
	/**
	 * Approve a group. Admins only.
	 * 
	 * @param id Id of the group to approve
	 */
	@Secured("ROLE_ADMIN")
	@GetMapping("/approveGroup")
	public String approveGroup(Model model, RedirectAttributes redirectAttributes, 
			@RequestParam(value="id") Integer id) {		
	
		// Retrieve group by id:
		Optional<Group> groupToApprove = groupRepository.findById(id);
		if(groupToApprove.isPresent())
		{
			Group group = groupToApprove.get();
			// Approve group:
			group.setValidated(true);
			// Save group:
			groupRepository.save(group);
			redirectAttributes.addAttribute("message", "Successfully approved " + group.getGroupName());
			
		}
		else
			redirectAttributes.addAttribute("errorMsg", "Invalid group");
		
		return "redirect:adminGroups";
	}
	
	/**
	 * Shows all groups for admin page. Admins only.
	 * 
	 */
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = {"adminGroups" })
	public String adminGroups(Model model) {
		
		// Find all groups:
		List<Group> groups = groupRepository.findAll(); 
		model.addAttribute("groups", groups);
		
		return "admin_groups";
	}
	
	/**
	 * Add a member to a group
	 * @param id Id of group to modify.
	 * @param userToAdd Username of user to add.
	 */
	@RequestMapping("/addMember")
	public String addMember(Model model, RedirectAttributes redirectAttributes, @RequestParam(value="id") Integer id, 
			@RequestParam(value="userToAdd") String userToAdd) {
		
		// Get current user:
		User user = permissionHelper.getCurrentUser();
		if(user == null) return "redirect:login";
		// Find group to modify:
		Optional<Group> groupToModify = groupRepository.findById(id);

		if(groupToModify.isPresent())
		{
			Group group = groupToModify.get();
			// if user is not owner of group and not admin
			if(group.getGroupOwner().getId() != user.getId() && !permissionHelper.isAdmin(user)) 
			{
				redirectAttributes.addAttribute("errorMsg", "Must be owner of group!");
				return "redirect:listMyGroups";
			}
			
			// Find new member
			User newMember = userRepository.findByUsername(userToAdd);			
			if(newMember != null)
			{	//add Member to group and save group
				group.setUsers(groupRepository.findGroupMembers(group.getId()));
				group.addMember(newMember);
				groupRepository.save(group);
			}
			else
				redirectAttributes.addAttribute("errorMsg", "Could not find user");
			
			// Redirect to same page so user can add more groups
			return "redirect:/editGroup?id=" + group.getId();
			
		}
		else
			redirectAttributes.addAttribute("errorMsg", "Invalid group");
		
		return "redirect:listMyGroups";
	}
	
}
