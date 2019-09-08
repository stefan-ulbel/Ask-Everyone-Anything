package at.fh.swenga.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "Question")
public class Question {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Version
	private long version;
	
	private String questionText;
	
	@OneToMany(mappedBy="question",fetch=FetchType.LAZY)
	private List<Answer> answerList;
	
	@ManyToOne //(cascade = CascadeType.PERSIST)
	private Quiz quiz;
	
	
	private transient List<Answer> correctAnswers;
	private transient Answer userAnwser;
	
	private transient int countAnswered;
	
	public Question() {
		super();
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
		Question other = (Question) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public List<Answer> getAnswerList() {
		return answerList;
	}

	public void setAnswerList(List<Answer> answerList) {
		this.answerList = answerList;
	}

	public Quiz getQuiz() {
		return quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	
	public List<Answer> getCorrectAnswers() {
		if(correctAnswers == null) correctAnswers = new ArrayList<>();
		
		return correctAnswers;
	}

	public void setCorrectAnswers(List<Answer> correctAnswers) {
		this.correctAnswers = correctAnswers;
	}
	
	public void addCorrectAnswer(Answer correctAnswer) {
		if(correctAnswers == null) correctAnswers = new ArrayList<>();
		this.correctAnswers.add(correctAnswer);
	}

	public Answer getUserAnwser() {
		return userAnwser;
	}

	public void setUserAnwser(Answer userAnwser) {
		this.userAnwser = userAnwser;
	}

	public int getCountAnswered() {
		return countAnswered;
	}

	public void setCountAnswered(int countAnswered) {
		this.countAnswered = countAnswered;
	}

	
	
}
