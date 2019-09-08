package at.fh.swenga.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.persistence.JoinColumn;


@Entity
@Table(name = "Answer")
@NamedEntityGraph(name = "Answer.userAnswer", attributeNodes = @NamedAttributeNode("userAnswer"))
public class Answer {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Version
	private long version;
	
	@Column(nullable = false, length = 300)
	private String answerText;
	
	private boolean correct;
	
	@ManyToOne //(cascade = CascadeType.PERSIST)
	private Question question;
	
	@ManyToMany
	@JoinTable(
	  name = "UserAnswer", 
	  joinColumns = @JoinColumn (name = "answer_id"), 
	  inverseJoinColumns = @JoinColumn (name = "user_id"))
	private Set<User> userAnswer;
	
	private transient int countPicked;

	public Answer() {
		super();
	}
	
	

	public Answer(String answerText) {
		super();
		this.answerText = answerText;
		this.correct = true;
	}



	public Answer(String answerText, boolean correct) {
		super();
		this.answerText = answerText;
		this.correct = correct;
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
		Answer other = (Answer) obj;
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

	public String getAnswerText() {
		return answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

	public boolean isCorrect() {
		return correct;
	}

	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Set<User> getUserAnswer() {
		return userAnswer;
	}

	public void setUserAnswer(Set<User> userAnswer) {
		this.userAnswer = userAnswer;
	}
	
	public void addUserAnswer(User user) {
		if(userAnswer == null) userAnswer = new HashSet<User>();
		this.userAnswer.add(user);
	}
	
	public void removeUserAnswer(User user) {
		if(userAnswer == null) userAnswer = new HashSet<User>();
		else this.userAnswer.remove(user);
	}



	public int getCountPicked() {
		return countPicked;
	}



	public void setCountPicked(int countPicked) {
		this.countPicked = countPicked;
	}
	
	
}
