package at.fh.swenga.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import at.fh.swenga.enums.QuizType;

@Entity
@Table(name = "Quiz")
public class Quiz {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Version
	private long version;

	@Column(nullable = false, length = 1000)
	private String description;

	@ManyToOne
	private Group group;

	@ManyToOne
	private User owner;

	@Column(nullable = false)
	private QuizType quizType;

	@OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
	private List<Question> questionList;

	@ElementCollection
	private Set<String> tagList;

	@Temporal(TemporalType.DATE)
	private Date createdOn;

	@Temporal(TemporalType.DATE)
	private Date startOn;

	@Temporal(TemporalType.DATE)
	private Date endOn;

	private int viewCount;
	private int attemptCount;

	private String name;
	
	private boolean deleted;

	private boolean onlyOneAttempt = true;
	private transient Set<String> matchigTags; 
	private transient boolean hasEditPerm;
	
	private transient boolean quizIsOpen = true;
	private transient boolean userIsMember = true;
	private transient boolean userHasAnswered = false;

	public Quiz() {
		super();
	}

	public Quiz(String description, QuizType quizType, Date createdOn) {
		super();
		this.description = description;
		this.quizType = quizType;
		this.createdOn = createdOn;
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
		Quiz other = (Quiz) obj;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public QuizType getQuizType() {
		return quizType;
	}

	public void setQuizType(QuizType quizType) {
		this.quizType = quizType;
	}

	public List<Question> getQuestionList() {
		return questionList;
	}

	public void setQuestionList(List<Question> questionList) {
		this.questionList = questionList;
	}

	public void addQuestion(Question question) {
		if (questionList == null)
			questionList = new ArrayList<Question>();
		this.questionList.add(question);
	}

	public Set<String> getTagList() {
		return tagList;
	}

	public void setTagList(Set<String> tagList) {

		this.tagList = tagList;
	}
	
	public void addTagtoList(String tag) {
		if(tagList == null) tagList = new HashSet<>();
		
		if(tag.trim().length() > 0)
			this.tagList.add(tag.trim().toLowerCase());
	}
	
	public boolean removeTagfromList(String tag) {
		if(tagList == null) tagList = new HashSet<>();
		return this.tagList.remove(tag.trim().toLowerCase());
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getEndOn() {
		return endOn;
	}

	public void setEndOn(Date endOn) {
		this.endOn = endOn;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public void setAttemptCount(int attemptCount) {
		this.attemptCount = attemptCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOnlyOneAttempt() {
		return onlyOneAttempt;
	}

	public void setOnlyOneAttempt(boolean onlyOneAttempt) {
		this.onlyOneAttempt = onlyOneAttempt;
	}

	public Date getStartOn() {
		return startOn;
	}

	public void setStartOn(Date startOn) {
		this.startOn = startOn;
	}

	public Set<String> getMatchigTags() {
		return matchigTags;
	}

	public void setMatchigTags(Set<String> matchigTags) {
		this.matchigTags = matchigTags;
	}
	

	public String matchingTagsToString(int maxCount) {
		if(matchigTags == null || maxCount<=0 || matchigTags.isEmpty()) return "";
		
		String matchigTagsString = "";
		List<String> matchigTagsList = new ArrayList<>();
		matchigTagsList.addAll(matchigTags);
		for(int i=0;i<Math.min(maxCount, matchigTagsList.size());i++)
		{
			matchigTagsString += matchigTagsList.get(i);
			if(i+1<Math.min(maxCount, matchigTagsList.size())) matchigTagsString += ", ";
		}
		
		if(maxCount < matchigTagsList.size()) matchigTagsString+=", ...";
		
		return matchigTagsString;
	}

	public boolean isHasEditPerm() {
		return hasEditPerm;
	}

	public void setHasEditPerm(boolean hasEditPerm) {
		this.hasEditPerm = hasEditPerm;
	}

	public boolean isQuizIsOpen() {
		return quizIsOpen;
	}

	public void setQuizIsOpen(boolean quizIsOpen) {
		this.quizIsOpen = quizIsOpen;
	}

	public boolean isUserIsMember() {
		return userIsMember;
	}

	public void setUserIsMember(boolean userIsMember) {
		this.userIsMember = userIsMember;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isUserHasAnswered() {
		return userHasAnswered;
	}

	public void setUserHasAnswered(boolean userHasAnswered) {
		this.userHasAnswered = userHasAnswered;
	}

	
	
}
