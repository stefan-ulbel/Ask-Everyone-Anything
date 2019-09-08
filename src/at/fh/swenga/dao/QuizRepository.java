package at.fh.swenga.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import at.fh.swenga.enums.QuizType;
import at.fh.swenga.model.Group;
import at.fh.swenga.model.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer>{

	/**
	 * Get all the quizzes or surveys
	 * @param quizType Quiz or Survey
	 * @param deleted
	 * @return List of quizzes or surveys
	 */
	List<Quiz> findByQuizTypeAndDeleted(QuizType quizType,boolean deleted);
	
	/**
	 * Find the quizzes or surveys from a certain group
	 * @param group
	 * @param deleted
	 * @return List of quizzes from the group
	 */
	List<Quiz> findByQuizTypeAndGroupAndDeleted(QuizType quizType,Group group,boolean deleted);
	
	/**
	 * Find the quizzes and surveys from a certain group
	 * @param group
	 * @param deleted
	 * @return List of quizzes from the group
	 */
	List<Quiz> findByGroupAndDeleted(Group group,boolean deleted);
	
	/**
	 * Count the quizzes or surveys from a user
	 * @param quizType
	 * @param userId
	 * @return int quiz or survey count
	 */
	@Query("SELECT COUNT(q) FROM Quiz q WHERE q.quizType=:quizType AND q.owner.id=:userId AND q.deleted = 0")
	int countByQuizTypeFromUser(@Param("quizType") QuizType quizType, @Param("userId") int userId);
	
	/**
	 * Find the tags from a quiz
	 * @param quizId
	 * @return List of quiz tags (Strings)
	 */
	@Query("SELECT qt FROM Quiz q JOIN q.tagList qt WHERE q.id=:quizId AND q.deleted = 0")
	List<String> findQuizTagsFromQuiz( @Param("quizId") int quizId);
	
	/**
	 * Find all quizzes containing a search string
	 * @param name
	 * @param deleted
	 * @return List of Quizzes with the search string
	 */
	List<Quiz> findByNameContainingAndDeleted(String name,boolean deleted);
	
	/**
	 * Find all the quizzes of a certain tag
	 * @param tag
	 * @return List of quizzes with a certain tag
	 */
	@Query("SELECT q FROM Quiz q JOIN q.tagList qTag WHERE qTag = :tag AND q.deleted = 0")
	List<Quiz> findByTag(@Param("tag") String tag);
	
	/**
	 * Find all the quizzes which a user owns
	 * @param userId
	 * @return List of all the quizzes which a user owns
	 */
	@Query("SELECT q FROM Quiz q JOIN q.owner o WHERE o.id = :userId AND q.deleted = 0")
	List<Quiz> findUserQuizzes(@Param("userId") int userId);
	
	/**
	 * Find all the quizzes from the groups of the user
	 * @param userId
	 * @return List of quizzes
	 */
	@Query("SELECT q FROM Quiz q JOIN q.group g JOIN g.users u WHERE u.id = :userId AND q.deleted = 0")
	List<Quiz> findUserGroupQuizzes(@Param("userId") int userId);
	
	/**
	 * Show all quizzes a user has participated in, also show deleted quizzes
	 * @param userId
	 * @return List of participated quizzes
	 */
	@Query("SELECT DISTINCT q FROM Quiz q JOIN q.questionList quest JOIN quest.answerList a JOIN a.userAnswer ua WHERE ua.id = :userId")
	List<Quiz> findPrevQuizzesFromUser(@Param("userId") int userId);
	
	/**
	 * Count the amount of participants of a quiz
	 * @param quizId
	 * @return int amount of participants
	 */
	@Query("SELECT COUNT(DISTINCT ua.id) FROM Quiz q JOIN q.questionList quest JOIN quest.answerList a JOIN a.userAnswer ua WHERE q.id = :quizId")
	int countParticipants(@Param("quizId") int quizId);
	
	/**
	 * Count the total amount of unique visitors of one user
	 * @param userId
	 * @return in total amount of unique visitors
	 */
	@Query("SELECT COUNT(DISTINCT ua.id) FROM Quiz q JOIN q.questionList quest JOIN quest.answerList a JOIN a.userAnswer ua WHERE q.owner.id = :userId")
	int countTotalParticipants(@Param("userId") int userId);

}

