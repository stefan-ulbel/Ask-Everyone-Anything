package at.fh.swenga.dao;
 
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import at.fh.swenga.model.User;
import at.fh.swenga.model.UserRole;
 
@Repository
@Transactional
public class UserRoleRepository{
 
	@PersistenceContext
	protected EntityManager entityManager;
 
	public UserRole getRole(String role) {
		try {
			TypedQuery<UserRole> typedQuery = entityManager
					.createQuery("select ur from UserRole ur where ur.role= :role", UserRole.class);
			typedQuery.setParameter("role", role);
			return typedQuery.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public Set<UserRole> getUserRoles(User user) {
		try {
			TypedQuery<UserRole> typedQuery = entityManager.createQuery("select ur from User u JOIN u.userRoles ur where u.id = :userId", UserRole.class);
			typedQuery.setParameter("userId", user.getId());
			return new HashSet<UserRole>(typedQuery.getResultList());
		} catch (NoResultException e) {
			return new HashSet<UserRole>();
		}
	}
 
	public void persist(UserRole userRole) {
		entityManager.persist(userRole);
	}
	
	public void merge(UserRole userRole) {
		entityManager.merge(userRole);
	}
}
 