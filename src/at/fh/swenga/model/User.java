package at.fh.swenga.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import at.fh.swenga.model.UserRole;

@Entity
@Table(name = "User")
public class User implements java.io.Serializable {
	
	private static final long serialVersionUID = 7206044165580863029L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Version
	private long version;
	
	@Column(nullable = false, unique = true, length = 60)
	private String username;
	
	@Column(nullable = false, length = 300)
	private String email;
	
	@Column(nullable = false, length = 300)
	private String occupation;
	
	@Column(name = "password", nullable = false, length = 60)
	private String password;
 
	@Column(name = "enabled", nullable = false)
	private boolean enabled;
 
	@ManyToMany(fetch=FetchType.LAZY,cascade=CascadeType.PERSIST)
	@JoinTable(
			  name = "UsersUserRole", 
			  joinColumns = @JoinColumn (name = "user_id"), 
			  inverseJoinColumns = @JoinColumn (name = "userRole_id"))
	private Set<UserRole> userRoles;
	
	@ManyToMany(mappedBy = "userAnswer")
	private Set<Answer> submittedAnswers;
	
	@ManyToMany(mappedBy = "users")
	private Set<Group> groups;
	
	@OneToMany(mappedBy="groupOwner",fetch=FetchType.LAZY)
	private List<Group> groupOwnership;
	
	@ElementCollection
	private Set<String> tagList;
	
	public User() {
		super();
	}

	public User(String username) {
		super();
		this.username = username;
	}
	
	public User(String username, String password, boolean enabled) {
		super();
		this.username = username;
		this.password = password;
		this.enabled = enabled;
	}
	
	

	public User(String username, String email, String occupation, String password, boolean enabled) {
		super();
		this.username = username;
		this.email = email;
		this.occupation = occupation;
		this.password = password;
		this.enabled = enabled;
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
		User other = (User) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public void encryptPassword() {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		password = passwordEncoder.encode(password);		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<Answer> getSubmitedAnswers() {
		return submittedAnswers;
	}

	public void setSubmitedAnswers(Set<Answer> submitedAnswers) {
		this.submittedAnswers = submitedAnswers;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public List<Group> getGroupOwnership() {
		return groupOwnership;
	}

	public void setGroupOwnership(List<Group> groupOwnership) {
		this.groupOwnership = groupOwnership;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	public Set<Answer> getSubmittedAnswers() {
		return submittedAnswers;
	}

	public void setSubmittedAnswers(Set<Answer> submittedAnswers) {
		this.submittedAnswers = submittedAnswers;
	}
	
	public void addUserRole(UserRole userRole) {
		if (userRoles==null) userRoles = new HashSet<UserRole>();
		userRoles.add(userRole);
	}
	
	public void removeUserRole(UserRole userRole) {
		if (userRoles==null) userRoles = new HashSet<UserRole>();
		userRoles.remove(userRole);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public Set<String> getTagList() {
		return tagList;
	}

	public void setTagList(Set<String> tagList) {
		this.tagList = tagList;
	}
	
	public void removeUserTag(String tag) {
		if(tagList != null) 
			this.tagList.remove(tag.trim().toLowerCase());
	}
	
	public void addUserTag(String tag) {
		if(tagList == null) tagList = new HashSet<>();
		if(tag.trim().length() >0)
		this.tagList.add(tag.trim().toLowerCase());
	}
	
	
}
