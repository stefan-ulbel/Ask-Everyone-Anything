package at.fh.swenga.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import at.fh.swenga.model.Question;
import at.fh.swenga.model.Quiz;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer>{

	/**
	 * Get all the questions for a quiz
	 * @param quiz
	 * @return List of questions
	 */
	List<Question> findByQuiz(Quiz quiz);
	
	/**
	 * Count how often a question has been answered
	 * @param questionId
	 * @return int amount of answers
	 */
	@Query("SELECT COUNT(ua) FROM Question q JOIN q.answerList a JOIN a.userAnswer ua WHERE q.id = :questionId")
	int countQuestionAnswered(@Param("questionId") int questionId);
	
	/**
	 * Count how often a question has been answered by a group
	 * @param questionId
	 * @param groupId
	 * @return int amount of answers
	 */
	@Query("SELECT COUNT(ua) FROM Question q JOIN q.answerList a "+
	"JOIN a.userAnswer ua WHERE q.id = :questionId AND ua.id IN " +
	"(SELECT u.id FROM Group g JOIN g.users u WHERE g.id = :groupId)")
	int countQuestionAnsweredByGroup(@Param("questionId") int questionId, @Param("groupId") int groupId);
}
