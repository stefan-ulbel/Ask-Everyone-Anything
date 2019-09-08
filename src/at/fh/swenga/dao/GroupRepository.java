package at.fh.swenga.dao;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import at.fh.swenga.model.Group;
import at.fh.swenga.model.User;

@Repository
@Transactional
public interface GroupRepository extends JpaRepository<Group, Integer>{
	
	// Find all public groups:
	@Query("SELECT g FROM Group g WHERE is_private = FALSE")
	public List<Group> getAllPublicGroups();	
	
	//Find all public and validated groups:
	@Query("SELECT g FROM Group g WHERE g.validated = TRUE AND is_private = FALSE")
	public List<Group> getAllPublicAndValidatedGroups();	
	
	// Find groups where user is member
	@Query("SELECT g FROM Group g JOIN g.users u ON :userId = u.id")
	public List<Group> findGroupsForUser(@Param("userId") int userId);
	
	// Find members of group
	@Query("SELECT u FROM Group g JOIN g.users u WHERE g.id = :groupId")
	public Set<User> findGroupMembers(@Param("groupId") int groupId);
	
	// Verify that user is member of group
	@Query("SELECT COUNT(g) FROM Group g JOIN g.users u WHERE g.id = :groupId AND u.id = :userId")
	public int checkGroupMembers(@Param("userId") int userId, @Param("groupId") int groupId);
	
	// Find group by name
	public Group findByGroupName(@Param("searchString") String searchString);
	
	// Find all public and validated groups where user is member:
	@Query("SELECT g FROM Group g JOIN g.users u ON u.id = :userId WHERE g.validated = TRUE AND is_private = FALSE")
	public List<Group> getAllPublicAndValidatedGroupsWhereUserIsMember(@Param("userId") int userId);

	// Find all public and validated groups where user is not member:	
	@Query("SELECT g FROM Group g LEFT JOIN g.users u ON u.id = :userId WHERE u.id IS NULL AND g.validated = TRUE AND g.is_private = FALSE")
	public List<Group> getAllPublicAndValidatedGroupsWhereUserIsNotMember(@Param("userId") int userId);
	
	// Find all groups of all users who participated in quiz
	@Query("SELECT DISTINCT g "
			+ "FROM Group g JOIN g.users u WHERE u.id IN "
			+ "(SELECT ua.id " 
			+ "FROM Answer a " 
			+ "JOIN a.userAnswer ua " 
			+ "JOIN a.question quest " 
			+ "JOIN quest.quiz quiz "			 
			+ "WHERE quiz.id = :quizId)")
	Set<Group> findGroupsForQuiz(@Param("quizId") int quizId);


}











