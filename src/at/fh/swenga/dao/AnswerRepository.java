package at.fh.swenga.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import at.fh.swenga.model.Answer;
import at.fh.swenga.model.Question;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer>{

	List<Answer> findByQuestion(Question question);
	
	@EntityGraph(value = "Answer.userAnswer", type = EntityGraphType.LOAD)
	Optional<Answer> findFirstWithUserAnswerById(int id);
	
	/**
	 * Get all the answers a user has selected for a certain quiz
	 * @param quizId	Id from the quiz
	 * @param userId
	 * @return	List of answers
	 */
	@Query("SELECT a "
			+ "FROM Answer a "
			+ "JOIN a.userAnswer ua "
			+ "JOIN a.question quest "
			+ "JOIN quest.quiz quiz "
			+ "WHERE quiz.id = :quizId AND ua.id = :userId")
	List<Answer> findSelectedAnswers(@Param("quizId") int quizId, @Param("userId") int userId);
	
	/**
	 * List of answers from a group to a certain quiz
	 * @param quizId	Id from the quiz
	 * @param groupId
	 * @return List of answers
	 */
	@Query("SELECT a "
			+ "FROM Answer a "
			+ "JOIN a.userAnswer ua "
			+ "JOIN a.question quest "
			+ "JOIN quest.quiz quiz "
			+ "JOIN Group g ON g.id = :groupId "
			+ "WHERE quiz.id = :quizId AND g.id = :groupId AND ua.id IN "
			+ "(SELECT u.id FROM Group g2 JOIN g2.users u WHERE g2.id = :groupId)")
	List<Answer> findAnswersforGroup(@Param("quizId") int quizId, @Param("groupId") int groupId); 
	
	/**
	 * Find all the correct answers from a quiz
	 * @param quizId Id from the quiz
	 * @return List of correct answers
	 */
	@Query("SELECT a "
			+ "FROM Answer a "
			+ "JOIN a.question quest "
			+ "JOIN quest.quiz quiz "
			+ "WHERE quiz.id = :quizId AND a.correct = 1")
	List<Answer> findAllCorrectAnswers(@Param("quizId") int quizId);
	
	/**
	 * Count all the answers a user has submitted
	 * @param userId
	 * @return	int
	 */
	@Query("SELECT COUNT(a) "
			+ "FROM Answer a "
			+ "JOIN a.userAnswer ua "
			+ "WHERE ua.id = :userId")
	int countAnswersFromUser(@Param("userId") int userId);
	
	/**
	 * Count how often a answer has been picked
	 * @param answerId
	 * @return	int how often a answer has been picked
	 */
	@Query("SELECT COUNT(ua) FROM Answer a JOIN a.userAnswer ua WHERE a.id = :answerId")
	int countAnswerPicked(@Param("answerId") int answerId);
	
	/**
	 * Count how often a group has picked a certain answer
	 * @param answerId
	 * @param groupId
	 * @return int
	 */
	@Query("SELECT COUNT(ua) FROM Answer a JOIN a.userAnswer ua WHERE a.id = :answerId AND ua.id IN " + 
			"(SELECT u.id FROM Group g JOIN g.users u WHERE g.id = :groupId)")
	int countAnswerPickedByGroup(@Param("answerId") int answerId, @Param("groupId") int groupId);
	
	
}
