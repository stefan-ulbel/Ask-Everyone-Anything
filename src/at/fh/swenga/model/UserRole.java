package at.fh.swenga.model;
 
import java.util.Set;
 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.persistence.GenerationType;
 
@Entity
@Table(name = "UserRole")
public class UserRole implements java.io.Serializable {
	
	private static final long serialVersionUID = 8098173157518993615L;
 
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Version
	private long version;
 
	@ManyToMany(mappedBy = "userRoles", fetch = FetchType.LAZY)
	private Set<User> users;
 
	@Column(name = "role", nullable = false, length = 45)
	private String role;
 
	public UserRole() {
		super();
	}
 
	public UserRole(String role) {
		super();
		this.role = role;
	}
 
	public int getId() {
		return id;
	}
 
	public void setId(int id) {
		this.id = id;
	}
 
	public Set<User> getUsers() {
		return users;
	}
 
	public void setUsers(Set<User> users) {
		this.users = users;
	}
	
	public void addUser(User user) {
		this.users.add(user);
	}
	
	public void removeUser(User user) {
		this.users.remove(user);
	}
 
	public String getRole() {
		return role;
	}
 
	public void setRole(String role) {
		this.role = role;
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
		UserRole other = (UserRole) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
 
}