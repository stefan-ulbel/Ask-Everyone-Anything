package at.fh.swenga.dao;
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
 
import at.fh.swenga.model.User;
 
@Repository
@Transactional
public class UserRepository{
 
	@PersistenceContext
	protected EntityManager entityManager;
 
	public User findById(int userId) {
		TypedQuery<User> typedQuery = entityManager.createQuery("select u from User u where u.id = :userId",
				User.class);
		typedQuery.setParameter("userId", userId);
		List<User> typedResultList = typedQuery.getResultList();
		
		if(typedResultList != null && !typedResultList.isEmpty())
			return typedResultList.get(0);
		
		return null;
	}
	
	public User findByUsername(String userName) {
		TypedQuery<User> typedQuery = entityManager.createQuery("select u from User u where u.username = :name",
				User.class);
		typedQuery.setParameter("name", userName);
		List<User> typedResultList = typedQuery.getResultList();
		
		if(typedResultList != null && !typedResultList.isEmpty())
			return typedResultList.get(0);
		
		return null;
	}
 
	public Set<String> findUserTagsFromUser(int userId) {
		List<String> resultList = entityManager
				.createQuery("SELECT ut FROM User u JOIN u.tagList ut WHERE u.id=:userId")
				.setParameter("userId", userId)
				.getResultList();
		
		if(resultList != null && !resultList.isEmpty())
			return new HashSet<>(resultList);
		
		return new HashSet<>();
	}
	
	public List<User> findAll() {
		TypedQuery<User> typedQuery = entityManager.createQuery("select u from User u",
				User.class);
		List<User> typedResultList = typedQuery.getResultList();
		
		if(typedResultList != null && !typedResultList.isEmpty())
			return typedResultList;
		
		return new ArrayList<>();
	}
	
	public void persist(User user) {
		entityManager.persist(user);
	}
	
	public User merge(User user) {
		return entityManager.merge(user);
	}
}