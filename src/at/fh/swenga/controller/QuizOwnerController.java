package at.fh.swenga.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import at.fh.swenga.bl.PermissionHelper;
import at.fh.swenga.dao.AnswerRepository;
import at.fh.swenga.dao.GroupRepository;
import at.fh.swenga.dao.QuestionRepository;
import at.fh.swenga.dao.QuizRepository;
import at.fh.swenga.dao.UserRepository;
import at.fh.swenga.dao.UserRoleRepository;
import at.fh.swenga.enums.QuizType;
import at.fh.swenga.model.Answer;
import at.fh.swenga.model.Group;
import at.fh.swenga.model.Question;
import at.fh.swenga.model.Quiz;
import at.fh.swenga.model.User;

/**
 * QuizOwnerController Handles all quiz owner actions (modify, delete and
 * other...)
 * 
 * @author CortexLab
 * @version 1.0
 */
@Controller
@SessionAttributes("username")
public class QuizOwnerController {

	@Autowired
	QuizRepository quizRepository;

	@Autowired
	QuestionRepository questionRepository;

	@Autowired
	AnswerRepository answerRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserRoleRepository userRoleRepository;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	PermissionHelper permissionHelper;

	/**
	 * Prepares the new_quiz page for a new quiz, sets the current user as owner and
	 * the start date to the current date
	 */
	@RequestMapping(value = { "newQuiz" }, method = RequestMethod.GET)
	public String newQuizSave(Model model) {

		User user = permissionHelper.getCurrentUser();

		model.addAttribute("pageTitle", "New quiz");
		
		// The current user will become the owner
		model.addAttribute("newOwner", user.getUsername());

		model.addAttribute("isQuiz", 1);

		// By default: The start date is the current date
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		model.addAttribute("newStartDate", df.format(new Date()));

		model.addAttribute("groups", groupRepository.findAll());

		return "new_quiz";
	}

	/**
	 * Prepares the new_quiz page for a new survey, sets the current user as owner
	 * and the start date to the current date
	 */
	@RequestMapping(value = { "newSurvey" }, method = RequestMethod.GET)
	public String newSurveySave(Model model) {

		User user = permissionHelper.getCurrentUser();

		model.addAttribute("pageTitle", "New survey");
		
		// The current user will become the owner
		model.addAttribute("newOwner", user.getUsername());
		model.addAttribute("isSurvey", 1);

		// By default: The start date is the current date
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		model.addAttribute("newStartDate", df.format(new Date()));

		model.addAttribute("groups", groupRepository.findAll());

		return "new_quiz";
	}

	/**
	 * Saves a quiz to the database
	 * 
	 * @param quizId       Id of the quiz, if the quiz is edited, a new quiz is
	 *                     created if this id is not provided
	 * @param newName      Name of the quiz, this is used for the search
	 *                     functionality
	 * @param newDesc      Description of the quiz, this is displayed on the user
	 *                     answer page/ result page and on the PDF
	 * @param newStartDate Start date of the quiz, users cannot participate in the
	 *                     quiz if this date has not passed
	 * @param newEndDate   End date of the quiz, users cannot participate in the
	 *                     quiz if this date has passed
	 * @param newGroup     Group for the quiz, the quiz is public if no valid group
	 *                     is set
	 * @return
	 */
	@RequestMapping(value = { "newQuiz" }, method = RequestMethod.POST)
	public String newQuiz(Model model, RedirectAttributes redirectAttributes,
			@RequestParam(required = false, defaultValue = "0") int quizId, @RequestParam String newName,
			@RequestParam String newDesc, @RequestParam String newStartDate, @RequestParam String newEndDate,
			@RequestParam int newGroup) {

		User user = permissionHelper.getCurrentUser();

		Quiz quiz = null;

		if (quizId <= 0) {
			// If no id is provided => create a new object
			quiz = new Quiz();
			quiz.setQuizType(QuizType.Quiz);
		} else {
			// If a id is provided => try to edit the quiz
			Optional<Quiz> quizOpt = quizRepository.findById(quizId);

			if (quizOpt.isPresent()) {

				quiz = quizOpt.get();

				// Check if the user is allowed to edit the quiz
				if (!permissionHelper.checkPermission(quiz)) {
					redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this quiz!");
					return "redirect:dashboard";
				}

			}
			else
			{
				// If the quizId is not in the database => display an error
				redirectAttributes.addAttribute("errorMsg", "Quiz not found!");
				return "redirect:dashboard";
			}
		}

		// Set a default name if no name is provided
		if (newName.trim().length() == 0) {
			redirectAttributes.addAttribute("warningMsg", "Name empty!");
			quiz.setName("Quiz with no name");
		} else {
			quiz.setName(newName);
		}

		quiz.setDescription(newDesc);
		quiz.setOwner(user);
		if (newGroup == -1)
			quiz.setGroup(null);
		else {
			// Try to get the group and set it to the quiz
			Optional<Group> groupOpt = groupRepository.findById(newGroup);
			if (groupOpt.isPresent()) {
				Group group = groupOpt.get();
				quiz.setGroup(group);
				model.addAttribute("newGroup", group);

			}
		}

		Date dateStart = null;
		Date dateEnd = null;
		
		// Try to parse the start and end date => save them to the quiz if successful
		if (!newStartDate.trim().equals("")) {
			dateStart = tryToParseDate(newStartDate);
		}

		if (!newEndDate.trim().equals("")) {
			dateEnd = tryToParseDate(newEndDate);
		}
		
		if (dateStart != null) {
			quiz.setStartOn(dateStart);
		} else {
			quiz.setStartOn(null);
		}
		
		if (dateEnd != null) {
			// Prevent that the end date is before the start date
			if (dateStart == null || !dateStart.after(dateEnd)) {
				quiz.setEndOn(dateEnd);
			} else {
				redirectAttributes.addAttribute("errorMsg", "The end date cannot be before the start date");
				quiz.setEndOn(null);
			}
		} else {
			quiz.setEndOn(null);
		}


		quiz = quizRepository.save(quiz);

		redirectAttributes.addAttribute("quizId", quiz.getId());
		return "redirect:editQuiz";
	}

	/**
	 * Saves a new survey to the database
	 * 
	 * @param quizId       Id of the survey, if the survey is edited, a new survey
	 *                     is created if this id is not provided
	 * @param newName      Name of the survey, this is used for the search
	 *                     functionality
	 * @param newDesc      Description of the survey, this is displayed on the user
	 *                     answer page/ result page and on the PDF
	 * @param newStartDate Start date of the survey, users cannot participate in the
	 *                     quiz if this date has not passed
	 * @param newEndDate   End date of the survey, users cannot participate in the
	 *                     quiz if this date has passed
	 * @param newGroup     Group for the quiz, the survey is public if no valid
	 *                     group is set
	 * @return
	 */
	@RequestMapping(value = { "newSurvey" }, method = RequestMethod.POST)
	public String newSurvey(Model model, RedirectAttributes redirectAttributes,
			@RequestParam(required = false, defaultValue = "0") int quizId, @RequestParam String newName,
			@RequestParam String newDesc, @RequestParam String newStartDate, @RequestParam String newEndDate,
			@RequestParam int newGroup) {

		User user = permissionHelper.getCurrentUser();

		Quiz quiz = null;

		if (quizId <= 0) {
			// If no id is provided => create a new object
			quiz = new Quiz();
			quiz.setQuizType(QuizType.Survey);
		} else {
			// If a id is provided => try to edit the quiz
			Optional<Quiz> quizOpt = quizRepository.findById(quizId);

			if (quizOpt.isPresent()) {
				quiz = quizOpt.get();

				// Check if the user is allowed to edit the quiz
				if (!permissionHelper.checkPermission(quiz)) {
					redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this survey!");
					return "redirect:dashboard";
				}

			}
			else
			{
				// If the quizId is not in the database => display an error
				redirectAttributes.addAttribute("errorMsg", "Survey not found!");
				return "redirect:dashboard";
			}
		}

		// Set a default name if no name is provided
		if (newName.trim().length() == 0) {
			redirectAttributes.addAttribute("warningMsg", "Name empty!");
			quiz.setName("Survey with no name");
		} else {
			quiz.setName(newName);
		}

		quiz.setDescription(newDesc);
		quiz.setOwner(user);
		if (newGroup == -1)
			quiz.setGroup(null);
		else {
			Optional<Group> groupOpt = groupRepository.findById(newGroup);
			if (groupOpt.isPresent()) {
				Group group = groupOpt.get();
				quiz.setGroup(group);
			}
		}

		// Try to parse the date
		Date dateStart = null;
		Date dateEnd = null;
		
		if (!newStartDate.trim().equals("")) {
			dateStart = tryToParseDate(newStartDate);
		}

		if (!newEndDate.trim().equals("")) {
			dateEnd = tryToParseDate(newEndDate);
		}
		
		if (dateStart != null) {
			quiz.setStartOn(dateStart);
		} else {
			quiz.setStartOn(null);
		}
		
		if (dateEnd != null) {
			// Prevent that the end date is before the end date
			if (dateStart == null || !dateStart.after(dateEnd)) {
				quiz.setEndOn(dateEnd);
			} else {
				redirectAttributes.addAttribute("errorMsg", "The end date cannot be before the start date");
				quiz.setEndOn(null);
			}
		} else {
			quiz.setEndOn(null);
		}

		quiz = quizRepository.save(quiz);

		redirectAttributes.addAttribute("quizId", quiz.getId());
		return "redirect:editQuiz";
	}

	/**
	 * Add a question to an existing quiz
	 * 
	 * @param quizId          Id from the quiz to which the question should get
	 *                        added
	 * @param newQuestionText Text of the question
	 */
	@RequestMapping(value = { "addQuestion" })
	public String addQuestion(Model model, RedirectAttributes redirectAttributes, @RequestParam int quizId,
			@RequestParam String newQuestionText) {

		// Prevent empty question texts
		if (newQuestionText.trim().length() == 0) {
			redirectAttributes.addAttribute("quizId", quizId);
			return "redirect:editQuiz";
		}

		Optional<Quiz> quizOpt = quizRepository.findById(quizId);

		if (quizOpt.isPresent()) {
			Quiz quiz = quizOpt.get();

			// Check if the user is allowed to edit the quiz
			if (!permissionHelper.checkPermission(quiz)) {
				redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this quiz!");
				return "redirect:dashboard";
			}

			quiz.setQuestionList(questionRepository.findByQuiz(quiz));

			Question question = new Question();
			question.setQuestionText(newQuestionText);

			question.setQuiz(quiz);
			question = questionRepository.save(question);

			redirectAttributes.addAttribute("quizId", quiz.getId());
		}

		return "redirect:editQuiz";
	}

	/**
	 * Add a answer to an existing question
	 * 
	 * @param newQuestionId      Id of the question the answer should get added to
	 * @param newAnswerText      Answer text
	 * @param newAnswerIsCorrect This is set if the answer is correct
	 */
	@RequestMapping(value = { "addAnswer" })
	public String addAnswer(Model model, RedirectAttributes redirectAttributes, @RequestParam int newQuestionId,
			@RequestParam String newAnswerText, @RequestParam(required = false) String newAnswerIsCorrect) {

		Optional<Question> questionOpt = questionRepository.findById(newQuestionId);

		if (questionOpt.isPresent()) {

			Question question = questionOpt.get();
			redirectAttributes.addAttribute("quizId", question.getQuiz().getId());

			// Prevent empty answers
			if (newAnswerText.trim().length() == 0)
				return "redirect:editQuiz";

			// Check if the user is allowed to edit the quiz
			if (!permissionHelper.checkPermission(question.getQuiz())) {
				redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this quiz!");
				return "redirect:dashboard";
			}

			question.setAnswerList(answerRepository.findByQuestion(question));

			Answer answer = new Answer();
			answer.setAnswerText(newAnswerText);
			answer.setCorrect(newAnswerIsCorrect != null);

			answer.setQuestion(question);
			answer = answerRepository.save(answer);

		}

		return "redirect:editQuiz";
	}

	/**
	 * Delete an answer from a question
	 * 
	 * @param newAnswerId Answer id which should get deleted
	 * @param quizId      Used to redirect back to the edit quiz page
	 * @return
	 */
	@RequestMapping(value = { "deleteAnswer" })
	public String deleteAnswer(Model model, RedirectAttributes redirectAttributes, @RequestParam int newAnswerId,
			@RequestParam int quizId) {

		Optional<Answer> answerOpt = answerRepository.findById(newAnswerId);

		if (answerOpt.isPresent()) {
			Answer answer = answerOpt.get();

			// Check if the user is allowed to edit the quiz
			if (answer.getQuestion() != null && !permissionHelper.checkPermission(answer.getQuestion().getQuiz())) {
				redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this quiz!");
				return "redirect:dashboard";
			}

			answerRepository.delete(answer);

			redirectAttributes.addAttribute("quizId", quizId);

		}

		return "redirect:editQuiz";
	}

	/**
	 * Changes if an answer is correct
	 * 
	 * @param newAnswerId Id of the answer which should get modified
	 * @param quizId      Used to redirect back to the edit quiz page
	 */
	@RequestMapping(value = { "toggleAnswerCorrectness" })
	public String toggleAnswerCorrectness(Model model, RedirectAttributes redirectAttributes,
			@RequestParam int newAnswerId, @RequestParam int quizId) {

		Optional<Answer> answerOpt = answerRepository.findById(newAnswerId);

		if (answerOpt.isPresent()) {
			Answer answer = answerOpt.get();

			// Check if the user is allowed to edit the quiz
			if (answer.getQuestion() != null && !permissionHelper.checkPermission(answer.getQuestion().getQuiz())) {
				redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this quiz!");
				return "redirect:dashboard";
			}

			answer.setCorrect(!answer.isCorrect());
			answerRepository.save(answer);

			redirectAttributes.addAttribute("quizId", quizId);
		}

		return "redirect:editQuiz";
	}

	/**
	 * Adds all the data from one quiz object to the model Called when the user
	 * tries to edit a quiz or if a new quiz has been created
	 * 
	 * @param quizId Id of the quiz which should get loaded to the editQuiz page
	 */
	@RequestMapping(value = { "editQuiz" })
	public String editQuiz(Model model, RedirectAttributes redirectAttributes, @RequestParam(required = false, defaultValue = "-1") int quizId) {

		Optional<Quiz> quizOpt = quizRepository.findById(quizId);
		if (quizOpt.isPresent()) {
			Quiz quiz = quizOpt.get();

			// Check if the user is allowed to edit the quiz
			if (!permissionHelper.checkPermission(quiz)) {
				redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this quiz!");
				return "redirect:dashboard";
			}

			// Set the matching page information for the view
			if (quiz.getQuizType() == QuizType.Quiz) {
				model.addAttribute("pageTitle", "Edit quiz");
			} else if (quiz.getQuizType() == QuizType.Survey) {
				model.addAttribute("pageTitle", "Edit survey");
			} else {
				model.addAttribute("pageTitle", "Edit");
			}

			quiz.setQuestionList(questionRepository.findByQuiz(quiz));

			model.addAttribute("quizId", quiz.getId());
			model.addAttribute("newOwner", quiz.getOwner().getUsername());

			DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

			if (quiz.getStartOn() != null)
				model.addAttribute("newStartDate", df.format(quiz.getStartOn()));

			if (quiz.getEndOn() != null)
				model.addAttribute("newEndDate", df.format(quiz.getEndOn()));

			model.addAttribute("newName", quiz.getName());
			model.addAttribute("newDesc", quiz.getDescription());

			// Load the tag list manually since lazy loading doesnot work
			quiz.setTagList(new HashSet<>(quizRepository.findQuizTagsFromQuiz(quiz.getId())));
			model.addAttribute("quizTags", quiz.getTagList());

			model.addAttribute("groups", groupRepository.findAll());

			// Add a special flag for the view, if the quiz object is a quiz or a survey
			if (quiz.getQuizType() == QuizType.Quiz)
				model.addAttribute("isQuiz", 1);
			else if (quiz.getQuizType() == QuizType.Survey)
				model.addAttribute("isSurvey", 1);

			// Load the groups for the drop down
			model.addAttribute("newGroup", quiz.getGroup());
			List<Question> questions = quiz.getQuestionList();
			for (Question question : questions) {
				question.setAnswerList(answerRepository.findByQuestion(question));
			}
			model.addAttribute("questions", questions);

		}
		return "new_quiz";
	}

	/**
	 * Add a tag to an existing quiz
	 * 
	 * @param quizId     Quiz id which should get the new tag
	 * @param newQuizTag The new tag, can also be multiple tags comma separated
	 */
	@RequestMapping(value = { "addQuizTag" })
	public String addQuizTag(Model model, RedirectAttributes redirectAttributes, @RequestParam int quizId,
			@RequestParam String newQuizTag) {

		Optional<Quiz> quizOpt = quizRepository.findById(quizId);

		if (quizOpt.isPresent()) {
			Quiz quiz = quizOpt.get();

			// Check if the user is allowed to edit the quiz
			if (!permissionHelper.checkPermission(quiz)) {
				redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this quiz!");
				return "redirect:dashboard";
			}

			quiz.setTagList(new HashSet<>(quizRepository.findQuizTagsFromQuiz(quiz.getId())));

			// Multiple comma separated tags can be added 
			String prepNewQuizTag = newQuizTag.replaceAll(",", ";");
			String[] newTags = prepNewQuizTag.split(";");

			for (String newTag : newTags) {
				quiz.addTagtoList(newTag);
			}

			quiz = quizRepository.save(quiz);

			redirectAttributes.addAttribute("quizId", quiz.getId());

		}

		return "redirect:editQuiz";
	}

	/**
	 * Removes tags from a quiz
	 * 
	 * @param quizId     Quiz id from which the tag(s) should be removed
	 * @param newQuizTag The tag which should get removed, can also be multiple tags
	 *                   comma separated
	 */
	@RequestMapping(value = { "deleteQuizTag" })
	public String deleteQuizTag(Model model, RedirectAttributes redirectAttributes, @RequestParam int quizId,
			@RequestParam String newQuizTag) {

		Optional<Quiz> quizOpt = quizRepository.findById(quizId);

		if (quizOpt.isPresent()) {
			Quiz quiz = quizOpt.get();

			// Check if the user is allowed to edit the quiz
			if (!permissionHelper.checkPermission(quiz)) {
				redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this quiz!");
				return "redirect:dashboard";
			}

			quiz.setTagList(new HashSet<>(quizRepository.findQuizTagsFromQuiz(quiz.getId())));
			quiz.removeTagfromList(newQuizTag);
			quiz = quizRepository.save(quiz);

			redirectAttributes.addAttribute("quizId", quiz.getId());

		}

		return "redirect:editQuiz";
	}

	/**
	 * Set the deleted flag for a quiz, this quiz wont be shown for users who
	 * have not answered the quiz
	 * 
	 * @param quizId Quiz which should get deleted
	 */
	@RequestMapping(value = { "deleteQuiz" })
	public String deleteQuiz(Model model,RedirectAttributes redirectAttributes, @RequestParam int quizId) {

		Optional<Quiz> quizOpt = quizRepository.findById(quizId);
		if (quizOpt.isPresent()) {
			Quiz quiz = quizOpt.get();

			// Check if the user is allowed to edit the quiz
			if (!permissionHelper.checkPermission(quiz)) {
				redirectAttributes.addAttribute("errorMsg", "You dont have the permission to modify this quiz!");
				return "redirect:dashboard";
			}

			try {
				
				// Logical deletion
				quiz.setDeleted(true);
				quizRepository.save(quiz);
				redirectAttributes.addAttribute("errorMsg", "Quiz deleted");
			} catch (Exception e) {
				redirectAttributes.addAttribute("errorMsg", "Couldnot delete quiz: " + e.getMessage());
			}

		}

		return "redirect:list";
	}

	/**
	 * Try to parse a given date string to a date object
	 * 
	 * @param dateString in the format dd.mm.yyyy or yyyy-mm-dd
	 * @return date object or null if the parse failed
	 */
	private Date tryToParseDate(String dateString) {
		Date date = null;
		try {
			DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
			date = format.parse(dateString);
			return date;
		} catch (Exception e) {

			try {
				// Try a second date format
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				date = format.parse(dateString);
				return date;

			} catch (Exception e2) {
				return null;
			}

		}

		
	}

}
