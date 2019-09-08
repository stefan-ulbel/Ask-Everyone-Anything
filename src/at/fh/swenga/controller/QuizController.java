package at.fh.swenga.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import at.fh.swenga.bl.PermissionHelper;
import at.fh.swenga.dao.AnswerRepository;
import at.fh.swenga.dao.GroupRepository;
import at.fh.swenga.dao.QuestionRepository;
import at.fh.swenga.dao.QuizRepository;
import at.fh.swenga.enums.QuizType;
import at.fh.swenga.model.Answer;
import at.fh.swenga.model.Group;
import at.fh.swenga.model.Question;
import at.fh.swenga.model.Quiz;
import at.fh.swenga.model.User;

/**
 * QuizController Handles all quiz user actions (list, participate, listResults,
 * ...)
 * 
 * @author CortexLab
 * @version 1.0
 */
@Controller
@SessionAttributes("username")
public class QuizController {

	@Autowired
	QuizRepository quizRepository;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	QuestionRepository questionRepository;

	@Autowired
	AnswerRepository answerRepository;

	@Autowired
	PermissionHelper permissionHelper;

	/**
	 * List open quizzes and surveys and shows them in the list quizzes page
	 */
	@RequestMapping(value = { "list" })
	public String list(Model model) {
		listQuizData(model, "List all quizzes and surveys", quizRepository.findByGroupAndDeleted(null, false));
		return "list_data";
	}

	/**
	 * List open quizzes and shows them in the list quizzes page
	 */
	@RequestMapping(value = { "listQuizzes" })
	public String listQuizzes(Model model) {

		listQuizData(model, "List all quizzes",
				quizRepository.findByQuizTypeAndGroupAndDeleted(QuizType.Quiz, null, false));
		return "list_data";
	}

	/**
	 * List open surveys and shows them in the list quizzes page
	 */
	@RequestMapping(value = { "listSurveys" })
	public String listSurveys(Model model) {

		listQuizData(model, "List all surveys",
				quizRepository.findByQuizTypeAndGroupAndDeleted(QuizType.Survey, null, false));
		return "list_data";
	}

	/**
	 * List open quizzes and surveys from the groups where the user is a member and
	 * shows them in the list quizzes page
	 */
	@RequestMapping(value = { "listGroupQuiz" })
	public String listGroupQuiz(Model model) {
		User user = permissionHelper.getCurrentUser();
		listQuizData(model, "List all your group quizzes and surveys",
				quizRepository.findUserGroupQuizzes(user.getId()));
		return "list_data";
	}

	/**
	 * List all quizzes which were answered from the user, also deleted quizzes!
	 */
	@RequestMapping(value = { "listPrevious" })
	public String listPrevious(Model model) {

		User user = permissionHelper.getCurrentUser();

		if (permissionHelper.getCurrentUser() != null)
			listQuizData(model, "Previous quizzes and surveys", quizRepository.findPrevQuizzesFromUser(user.getId()));
		return "list_data";
	}

	/**
	 * Search all quizzes and surveys
	 * 
	 * @param searchString Search string can be part of the quiz name or be a list
	 *                     of tags (comma seperated)
	 */
	@RequestMapping(value = { "searchAll" })
	public String searchAll(Model model, HttpServletRequest request, @RequestParam String searchString) {

		// Search by name
		Set<Quiz> resultSet = new HashSet<Quiz>(quizRepository.findByNameContainingAndDeleted(searchString, false));

		// Search by tags -> tags could be comma seperated
		String prepSearchString = searchString.replaceAll(",", ";");
		String[] searchTags = prepSearchString.split(";");

		// Search each tag individually and add the result to the result set
		for (String searchTag : searchTags) {
			resultSet.addAll(quizRepository.findByTag(searchTag.trim()));
		}

		List<Quiz> resultList = new ArrayList<>();
		resultList.addAll(resultSet);

		listQuizData(model, "Search for " + searchString, resultList);
		return "list_data";
	}

	/**
	 * Helper function which prepares the quiz/survey objects for the list, checks
	 * matching tags and permissions for the frontend
	 * 
	 * @param pageTitle Title for the list quiz page
	 * @param quizzes   List of quizzes/surveys which should get displayed
	 */
	private void listQuizData(Model model, String pageTitle, List<Quiz> quizzes) {
		User user = permissionHelper.getCurrentUserWithTags();
		user = permissionHelper.getCurrentUserWithRoles(user);
		boolean tagsFound = false;
		for (Quiz q : quizzes) {

			// Check if the user has submitted answers for the quiz => will be used in the view
			if (!answerRepository.findSelectedAnswers(q.getId(), user.getId()).isEmpty()) {
				q.setUserHasAnswered(true);
			}

			// Find matching tags (intersection user tags and quiz tags) => displayed in the list view
			if (user != null && user.getTagList() != null && !user.getTagList().isEmpty()) {

				q.setTagList(new HashSet<>(quizRepository.findQuizTagsFromQuiz(q.getId())));
				Set<String> tagIntersect = new HashSet<>(
						CollectionUtils.intersection(q.getTagList(), user.getTagList()));
				q.setMatchigTags(tagIntersect);

				if (tagIntersect != null && !tagIntersect.isEmpty())
					tagsFound = true;
			}
			
			// Check the permissions from the user for the quiz => This will effect the action buttons in the list view
			q.setHasEditPerm(permissionHelper.checkPermission(user, q));
			q.setQuizIsOpen(permissionHelper.checkIfQuizIsOpen(q));
			q.setUserIsMember(permissionHelper.checkGroupMembershipAndOwnerShip(user, q));
		}

		// Set the tagsFound Flag => If no tags are found => a column in the list view will be hidden
		if (tagsFound)
			model.addAttribute("tagsFound", "1");

		// Display a warning if the user has no tags set
		if (!(user != null && user.getTagList() != null && !user.getTagList().isEmpty())) {
			model.addAttribute("infoMsg",
					"You can add tags in your profile to find quizzes and surveys for your interests.");
		}

		model.addAttribute("quizzes", quizzes);
		model.addAttribute("pageTitle", pageTitle);
	}

	/**
	 * Saves the user answers in the database and redirects to the result page
	 * 
	 * @param request The request contains all the user answers, the parameters are
	 *                handled manually since the amount of parameters is unknown
	 * @param quizId  The quiz id of the answered quiz, this acts as a check and is
	 *                commpared with the quizId in the answer/question to prevent
	 *                accidental answer submissions
	 */
	@RequestMapping(value = { "sendAnswer" })
	public String answerQuiz(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes,
			@RequestParam int quizId) {

		// Get all the parameters from the request manually
		Map<String, String[]> paramMap = request.getParameterMap();

		Optional<Quiz> quiz = quizRepository.findById(quizId);
		if (quiz.isPresent()) {
			Quiz quizObj = quiz.get();

			// Check if the quiz is open
			if (!permissionHelper.checkIfQuizIsOpen(quizObj)) {
				redirectAttributes.addAttribute("errorMsg", "This quiz is closed!");
				return "redirect:list";
			}

			// Check if the user is allowed to participate in the quiz
			User user = permissionHelper.getCurrentUser();
			if (!permissionHelper.checkGroupMembershipAndOwnerShip(user, quizObj)) {
				redirectAttributes.addAttribute("errorMsg",
						"This quiz is only for group members of the group " + quizObj.getGroup().getGroupName() + "!");
				return "redirect:list";
			}

			if (!answerRepository.findSelectedAnswers(quizId, user.getId()).isEmpty()) {
				// User has already answered this quiz
				return "redirect:showResult";

			}

			// Loop through the submitted answers and save them to the db
			for (String key : paramMap.keySet()) {
				try {
					if (key.startsWith("question")) {
						int questionId = Integer.parseInt(key.replace("question", ""));
						int answerId = Integer.parseInt(paramMap.get(key)[0]);

						Optional<Answer> answerO = answerRepository.findFirstWithUserAnswerById(answerId);
						if (answerO.isPresent()) {

							Answer answer = answerO.get();

							// Check for irregularities
							if (answer.getQuestion() == null || answer.getQuestion().getQuiz() == null
									|| quizObj.getId() != answer.getQuestion().getQuiz().getId()) {
								redirectAttributes.addAttribute("errorMsg",
										"This request includes wrong answer ids, try to answer the quiz again!");
								return "redirect:dashboard";
							}

							answer.addUserAnswer(user);
							answerRepository.save(answer);
						}
					}
				} catch (Exception e) {
					redirectAttributes.addAttribute("errorMsg", "Error while parsing the answers");
					return "redirect:dashboard";
				}

			}

			redirectAttributes.addAttribute("quizId", quizId);

			return "redirect:showResult";
		}

		return "redirect:list";
	}

	/**
	 * Shows the results of a quiz or survey, the quiz owner gets a special view for
	 * Quizzes
	 * 
	 * @param quizId      Id from the quiz id from which the results should get
	 *                    calculated
	 * @param groupViewId This variable is set if the user only wants the results
	 *                    from one group
	 */
	@RequestMapping(value = { "showResult" })
	public String showResult(Model model, RedirectAttributes redirectAttributes, @RequestParam int quizId,
			@RequestParam(value = "groupViewId", required = false) Integer groupViewId) {

		User user = permissionHelper.getCurrentUser();

		Optional<Quiz> quizOpt = quizRepository.findById(quizId);

		if (quizOpt.isPresent()) {
			Quiz quiz = quizOpt.get();
			// Add quiz information to the attributes
			model.addAttribute("quiz", quiz);

			if (quiz.getQuizType() == QuizType.Quiz && quiz.getOwner().getId() != user.getId()) {
				List<Answer> selectedAnswers = answerRepository.findSelectedAnswers(quizId, user.getId());

				if (selectedAnswers == null || selectedAnswers.size() == 0) {
					// Prevent that the user can access the result page if he hasnot answered the
					// quiz and isnot the owner
					redirectAttributes.addAttribute("quizId", quizId);
					return "redirect:participateQuiz";

				}

				// Get the list of all the correct answers
				List<Answer> correctAnswers = answerRepository.findAllCorrectAnswers(quizId);

				List<Question> questions = questionRepository.findByQuiz(quiz);
				model.addAttribute("questions", questions);

				// Add the selected answers and correct answers to the question object, this makes it easy to access them in the view
				for (Question q : questions) {
					for (Answer selAns : selectedAnswers) {
						if (selAns.getQuestion().getId() == q.getId())
							q.setUserAnwser(selAns);
					}

					for (Answer corAns : correctAnswers) {
						if (corAns.getQuestion().getId() == q.getId())
							q.addCorrectAnswer(corAns);
					}
				}

				model.addAttribute("isQuiz", "1");
				
				// Count the correct and total answers for the result overview
				model.addAttribute("answersCorrect",
						selectedAnswers.stream().filter(a -> a.isCorrect()).toArray().length);
				model.addAttribute("answersTotal", questions.size());
			}
			// The quiz owner gets all answers
			else if (quiz.getQuizType() == QuizType.Survey || quiz.getOwner().getId() == user.getId()) {

				// The owner will get a special result view
				if (quiz.getOwner().getId() == user.getId()) {
					model.addAttribute("isOwner", "1");
				}

				List<Question> questions = questionRepository.findByQuiz(quiz);

				// Get groups which participated in the quiz
				Set<Group> groups = groupRepository.findGroupsForQuiz(quizId);
				model.addAttribute("groups", groups);

				if (groupViewId == null) // show Results for answers from all groups
				{
					// Get all the selected answers and add them to the question object
					List<Answer> selectedAnswers = answerRepository.findSelectedAnswers(quizId, user.getId());
					for (Question q : questions) {
						for (Answer selAns : selectedAnswers) {
							if (selAns.getQuestion().getId() == q.getId())
								q.setUserAnwser(selAns);
						}

						// Count how often a question has been answered
						q.setCountAnswered(questionRepository.countQuestionAnswered(q.getId()));
						q.setAnswerList(answerRepository.findByQuestion(q));

						for (Answer answer : q.getAnswerList()) {
							// Count how often a answer has been selected
							answer.setCountPicked(answerRepository.countAnswerPicked(answer.getId()));
						}

					}

					// If the quiz owner has answered the quiz -> also show his answers
					if (quiz.getQuizType() == QuizType.Quiz) {
						List<Answer> correctAnswers = answerRepository.findAllCorrectAnswers(quizId);

						model.addAttribute("questions", questions);

						for (Question q : questions) {

							for (Answer corAns : correctAnswers) {
								if (corAns.getQuestion().getId() == q.getId())
									q.addCorrectAnswer(corAns);
							}
						}

						model.addAttribute("isQuiz", "1");
						model.addAttribute("answersCorrect",
								selectedAnswers.stream().filter(a -> a.isCorrect()).toArray().length);
						
					}

					model.addAttribute("answersTotal", questions.size());
				} 
				// show only answers from a certain group
				else 
				{
					// get only the answers for the group:
					List<Answer> groupAnswers = answerRepository.findAnswersforGroup(quizId, groupViewId);
					// get the group:
					Optional<Group> group_o = groupRepository.findById(groupViewId);
					Group group;
					if (group_o.isPresent()) {
						group = group_o.get();
					} else {
						model.addAttribute("errorMsg", "Could not find group");
						model.addAttribute("group", null);
						return "show_result";
					}
					model.addAttribute("group", group);

					int aCounter = 0;
					int countQuestionAnswered; // number of times question was answered by group members
					int numAnswers = 0; // calculate number of all possible answers:
					for (Question q : questions) {
						q.setAnswerList(answerRepository.findByQuestion(q));
						numAnswers += q.getAnswerList().size();
					}
					// group who picked answer
					int countAnswerPicked; // number of times answer was picked by group members

					for (Question q : questions) {
						countQuestionAnswered = questionRepository.countQuestionAnsweredByGroup(q.getId(),
								group.getId());
						q.setAnswerList(answerRepository.findByQuestion(q));
						q.setCountAnswered(countQuestionAnswered);

						for (Answer answer : q.getAnswerList()) {
							countAnswerPicked = answerRepository.countAnswerPickedByGroup(answer.getId(),
									group.getId());
							answer.setCountPicked(countAnswerPicked);
				
						}
					}
				}

				model.addAttribute("questions", questions);

			}
		} else {
			return "list_data";
		}

		return "show_result";
	}

	/**
	 * Loads all the questions and answers for a user to participating in quizzes or Surveys, also does a permission check (group membership) and if the quiz is open
	 * @param quizId	Id from the quiz the user wants to participate in
	 */
	@RequestMapping(value = { "participateQuiz" })
	public String participateQuiz(Model model, RedirectAttributes redirectAttributes, @RequestParam int quizId) {
		Optional<Quiz> quiz = quizRepository.findById(quizId);
		if (quiz.isPresent()) {
			Quiz quizObj = quiz.get();

			// Check if the quiz is open
			if (!permissionHelper.checkIfQuizIsOpen(quizObj)) {
				redirectAttributes.addAttribute("errorMsg", "This quiz is closed!");
				return "redirect:list";
			}

			User user = permissionHelper.getCurrentUser();

			// Check if the user is allowed to participate
			if (!permissionHelper.checkGroupMembershipAndOwnerShip(user, quizObj)) {
				redirectAttributes.addAttribute("errorMsg",
						"This quiz is only for group members of the group " + quizObj.getGroup().getGroupName() + "!");
				return "redirect:list";
			}

			// Check if the user already answered this quiz
			if (!answerRepository.findSelectedAnswers(quizId, user.getId()).isEmpty()) {
				// User has already answered this quiz
				if (quizObj.isOnlyOneAttempt()) {
					redirectAttributes.addAttribute("quizId", quizId);

					return "redirect:showResult";
				} else {
					model.addAttribute("warningMsg",
							"You already participated in this quiz, your answers will be overridden!");
					model.addAttribute("notFirstAttemed", "1");
				}
			}

			// Show the name and description from the quiz in the view
			model.addAttribute("quizName", quizObj.getName());
			model.addAttribute("quizId", quizObj.getId());
			model.addAttribute("quizDescription", quizObj.getDescription());

			List<Question> questions = questionRepository.findByQuiz(quizObj);
			for (Question q : questions) {
				q.setAnswerList(answerRepository.findByQuestion(q));
			}
			model.addAttribute("quizQuestions", questions);

			return "answer_question";
		}

		return "redirect:list";
	}

	/**
	 * Export a quiz or survey to PDF so users can answer it with pen and paper
	 * @param quizId	Id from the quiz which should get exported to PDF
	 * @return
	 */
	@RequestMapping(value = { "/exportQuizPDF" })
	public String report(Model model, @RequestParam(name = "quizId", required = false) int quizId) {

		Optional<Quiz> quizOpt = quizRepository.findById(quizId);

		if (quizOpt.isPresent()) {
			Quiz quiz = quizOpt.get();
			model.addAttribute("quiz", quiz);

			List<Question> questions = questionRepository.findByQuiz(quiz);

			// Manually load the answers for a question since lazy loading doesnot work
			for (Question q : questions) {
				q.setAnswerList(answerRepository.findByQuestion(q));
			}

			// Forward the information to the pdfReport-Resolver
			model.addAttribute("questions", questions);
			return "pdfReportV5";
		} else {
			model.addAttribute("errorMsg", "Couldn't find quiz.");
			return "forward:/list";
		}

	}

}
