package at.fh.swenga.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "UserGroup")
public class Group {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Version
	private long version;
	
	// The user which manages the group:
	@ManyToOne
	private User groupOwner;
	
	private String groupName;
	private String groupDescription;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			  name = "GroupMember", 
			  joinColumns = @JoinColumn (name = "group_id"), 
			  inverseJoinColumns = @JoinColumn (name = "user_id"))
	private Set<User> users;
	
	// Group can be public or private.
	private boolean is_private;
	
	// Not validated groups will not be shown in public group page.
	// An Admin has to approve each group.
	private boolean validated;
	
	public Group() {
		super();
	}
	
	

	public Group(User groupOwner, String groupName, String groupDescription, boolean is_private, boolean validated) {
		super();
		this.groupOwner = groupOwner;
		this.groupName = groupName;
		this.groupDescription = groupDescription;		
		this.is_private = is_private;
		this.validated = validated;
		// owner automatically becomes member of new group:
		addMember(groupOwner);		
	}
	
	
	/**
	 * Add user to group
	 * 
	 * @param newmember User-object of new member
	 */
	public void addMember(User newMember)
	{
		if(users == null) 
			users = new HashSet<>();
		if(!users.contains(newMember))
			users.add(newMember);
	}
	
	/**
	 * Remove user from group
	 * 
	 * @param member User-object of member to remove
	 */
	public void removeMember(User member)
	{
		if(users == null) 
			users = new HashSet<>();
		else
			users.remove(member);
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (id != other.id)
			return false;
		return true;
	}

	// Getter and setter:
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getGroupOwner() {
		return groupOwner;
	}

	public void setGroupOwner(User groupOwner) {
		this.groupOwner = groupOwner;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public boolean isInviteReq() {
		return is_private;
	}

	public void setInviteReq(boolean inviteReq) {
		this.is_private = inviteReq;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupDescription() {
		return groupDescription;
	}

	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public boolean isIs_private() {
		return is_private;
	}

	public void setIs_private(boolean is_private) {
		this.is_private = is_private;
	}
	

	
	
}
