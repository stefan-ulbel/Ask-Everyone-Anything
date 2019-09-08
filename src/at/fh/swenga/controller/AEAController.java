package at.fh.swenga.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.fluttercode.datafactory.impl.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import at.fh.swenga.dao.QuizRepository;
import at.fh.swenga.dao.UserRepository;
import at.fh.swenga.dao.UserRoleRepository;
import at.fh.swenga.enums.QuizType;
import at.fh.swenga.model.Answer;
import at.fh.swenga.model.Group;
import at.fh.swenga.model.Question;
import at.fh.swenga.model.Quiz;
import at.fh.swenga.model.User;
import at.fh.swenga.model.UserRole;
import at.fh.swenga.bl.PermissionHelper;
import at.fh.swenga.dao.AnswerRepository;
import at.fh.swenga.dao.GroupRepository;
import at.fh.swenga.dao.QuestionRepository;

/**
 * AEAController	Handels the dashboard, error handling(404 and exceptions) and
 * the generation of test data
 * 
 * @author CortexLab
 * @version 1.0
 */
@Controller
@SessionAttributes("username")
public class AEAController {
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
	 * Prepares the dashboard: Overview for the user, list the quizzes the user has
	 * created and relevant group quizzes for the user
	 */
	@RequestMapping(value = { "/", "dashboard" })
	public String dashboard(Model model) {

		User user = permissionHelper.getCurrentUser();
		if (user == null)
			return "redirect:login";
		user = permissionHelper.getCurrentUserWithRoles(user);

		model.addAttribute("username", user.getUsername());

		model.addAttribute("countQuiz", quizRepository.countByQuizTypeFromUser(QuizType.Quiz, user.getId()));
		model.addAttribute("countSurvey", quizRepository.countByQuizTypeFromUser(QuizType.Survey, user.getId()));
		model.addAttribute("countAnswer", answerRepository.countAnswersFromUser(user.getId()));
		model.addAttribute("totalParticipants", quizRepository.countTotalParticipants(user.getId()));

		List<Quiz> recQuizzes = quizRepository.findUserGroupQuizzes(user.getId());
		for (Quiz q : recQuizzes) {

			if (!answerRepository.findSelectedAnswers(q.getId(), user.getId()).isEmpty()) {
				q.setUserHasAnswered(true);
			}
		}
		model.addAttribute("recQuizzes", recQuizzes);

		List<Quiz> yourQuizzes = quizRepository.findUserQuizzes(user.getId());

		for (Quiz q : yourQuizzes) {

			q.setAttemptCount(quizRepository.countParticipants(q.getId()));

			q.setHasEditPerm(permissionHelper.checkPermission(user, q));
			q.setQuizIsOpen(permissionHelper.checkIfQuizIsOpen(q));
			q.setUserIsMember(permissionHelper.checkGroupMembershipAndOwnerShip(user, q));
		}

		model.addAttribute("yourQuizzes", yourQuizzes);

		return "dashboard";
	}

	/**
	 * Generate test data for the application, this should get removed IF the
	 * application is actually deployed
	 */
	@RequestMapping(value = { "test" })
	public String insertTest(Model model) {

		DataFactory dFactory = new DataFactory();

		try {
			UserRole adminRole = userRoleRepository.getRole("ROLE_ADMIN");
			if (adminRole == null) {
				adminRole = new UserRole("ROLE_ADMIN");
				userRoleRepository.persist(adminRole);
			}

			UserRole userRole = userRoleRepository.getRole("ROLE_USER");
			if (userRole == null) {
				userRole = new UserRole("ROLE_USER");
				userRoleRepository.persist(userRole);
			}

			User userAdmin = new User("admin", "ima17", true);
			userAdmin.encryptPassword();
			userAdmin.setEmail("admin@duckymail.at");
			userAdmin.setOccupation("IT Administrator");
			userRepository.persist(userAdmin);

			userAdmin.addUserRole(adminRole);
			userAdmin.addUserRole(userRole);
			userAdmin = userRepository.merge(userAdmin);

			User userUser = new User("user", "ima17", true);
			userUser.encryptPassword();
			userUser.setEmail("user@duckymail.at");
			userUser.setOccupation("Student");
			userUser.addUserTag("student");
			userUser.addUserTag("IT");
			userUser.addUserTag("computer engineering");
			userUser.addUserTag("SWENGA");
			userUser.addUserTag("FH Joanneum");
			userUser.addUserTag("food");
			userUser.addUserTag("party");
			userUser.addUserTag("drinking");
			userUser.addUserTag("animals");
			userRepository.persist(userUser);

			userUser.addUserRole(userRole);
			userUser = userRepository.merge(userUser);

			User user2 = new User(dFactory.getName(), "ima17", true);
			user2.encryptPassword();
			user2.setEmail(dFactory.getEmailAddress());
			user2.setOccupation("Doctor");
			user2.addUserTag("Security");
			user2.addUserTag("food");
			user2.addUserTag("zoo");
			user2.addUserTag("animals");
			user2.addUserTag("IT");
			userRepository.persist(user2);

			user2.addUserRole(userRole);
			user2 = userRepository.merge(user2);

			User user3 = new User(dFactory.getName(), "ima17", true);
			user3.encryptPassword();
			user3.setEmail(dFactory.getEmailAddress());
			user3.setOccupation("Infuencer");
			user3.addUserTag("animals");
			user3.addUserTag("food");
			user3.addUserTag("drinks");
			user3.addUserTag("travel");
			user3.addUserTag("party");
			user3.addUserTag("going out");
			user3.addUserTag("student");
			userRepository.persist(user3);

			user3.addUserRole(userRole);
			user3 = userRepository.merge(user3);

			User user4 = new User("BlauenKrausler", "ima17", true);
			user4.encryptPassword();
			user4.setEmail(dFactory.getEmailAddress());
			user4.setOccupation("Professor");
			user4.addUserTag("IMA");
			user4.addUserTag("swenga");
			user4.addUserTag("FH joanneum");
			user4.addUserTag("IT");
			user4.addUserTag("JAVA");
			user4.addUserTag("animals");
			userRepository.persist(user4);

			user4.addUserRole(userRole);
			user4 = userRepository.merge(user4);

			User user5 = new User(dFactory.getName(), "ima17", true);
			user5.encryptPassword();
			user5.setEmail(dFactory.getEmailAddress());
			user5.setOccupation("Sweet Taster");
			user5.addUserTag("food");
			user5.addUserTag("drinks");
			user5.addUserTag("IT");
			userRepository.persist(user5);

			user5.addUserRole(userRole);
			user5 = userRepository.merge(user5);

			User user6 = new User(dFactory.getName(), "ima17", true);
			user6.encryptPassword();
			user6.setEmail(dFactory.getEmailAddress());
			user6.setOccupation("Unemployed");
			user6.addUserTag("student");
			user6.addUserTag("JAVA");
			user6.addUserTag("IMA");
			user6.addUserTag("Fh joanneum");
			user6.addUserTag("IT");
			userRepository.persist(user6);

			user6.addUserRole(userRole);
			user6 = userRepository.merge(user6);

			User user7 = new User(dFactory.getName(), "ima17", true);
			user7.encryptPassword();
			user7.setEmail(dFactory.getEmailAddress());
			user7.setOccupation("Game Tester");
			userRepository.persist(user7);

			user7.addUserRole(userRole);
			user7 = userRepository.merge(user7);

			User user8 = new User(dFactory.getName(), "ima17", true);
			user8.encryptPassword();
			user8.setEmail(dFactory.getEmailAddress());
			user8.setOccupation("Speedrunner");
			user8.addUserTag("Student");
			user6.addUserTag("IT");
			userRepository.persist(user8);

			user8.addUserRole(userRole);
			user8 = userRepository.merge(user8);

			User user9 = new User(dFactory.getName(), "ima17", true);
			user9.encryptPassword();
			user9.setEmail(dFactory.getEmailAddress());
			user9.setOccupation("Zoo Manager");
			user9.addUserTag("zoo");
			userRepository.persist(user9);

			user9.addUserRole(userRole);
			user9 = userRepository.merge(user9);

			User user10 = new User(dFactory.getName(), "ima17", true);
			user10.encryptPassword();
			user10.setEmail(dFactory.getEmailAddress());
			user10.setOccupation("Barceeper");
			user10.addUserTag("student");
			userRepository.persist(user10);

			user10.addUserRole(userRole);
			user10 = userRepository.merge(user10);

			// userUser.addUserRole(userRole);
			// userRepository.merge(userUser);

			Group group1 = new Group(userUser, "Student", "Group for all students", false, true);
			group1.addMember(userUser);
			group1.addMember(user3);
			group1.addMember(user6);
			group1.addMember(user8);
			group1.addMember(user10);
			group1 = groupRepository.save(group1);

			Group group2 = new Group(user9, "Animal Lovers", "Group for people who love animals", false, false);
			group2.addMember(userUser);
			group2.addMember(user3);
			group2.addMember(user4);
			group2.addMember(user7);
			group2.addMember(user9);
			group2 = groupRepository.save(group2);

			Group group3 = new Group(userUser, "IMA Students", "Group for IMA Students", true, true);
			group3.addMember(userUser);
			group3.addMember(user6);
			group3.addMember(user8);
			group3 = groupRepository.save(group3);

			Group group4 = new Group(user4, "IMA17-SWENGA", "Group for IMA17 to thest their knoledge about SWENGA",
					true, false);
			group4.addMember(userUser);
			group4.addMember(user4);
			group4.addMember(user6);
			group4 = groupRepository.save(group4);

			Group group5 = new Group(user2, "IT-Interested People", "Group for people who are interested in IT", false,
					true);
			group5.addMember(userUser);
			group5.addMember(user4);
			group5.addMember(user6);
			group5.addMember(user8);
			group5.addMember(user2);
			group5.addMember(user5);
			group5.addMember(userAdmin);
			group5 = groupRepository.save(group5);

			createTestQuizzes(new User[] { userUser, user2, user3, user4, user5, user7, user8, user9, user10 },
					new Group[] { group1, group2, group3, group4, group5 });
			createTestSurv(new User[] { userUser, user2, user3, user4, user5, user7, user8, user9, user10 },
					new Group[] { group1, group2, group3, group4, group5 });

		} catch (Exception e) {
			model.addAttribute("errorMsg", "Error while creating test data: " + e.getMessage());
		}
		return "redirect:logout";
	}

	/**
	 * Helper function which adds a question with answers to a quiz
	 * 
	 * @param quiz         the quiz object to which the question should get added
	 * @param questionText the question text which will get displayed to the user as
	 *                     a String
	 * @param answers      answers for the question as a list
	 * @return the new question object
	 */
	private Question addTestQuestion(Quiz quiz, String questionText, ArrayList<Answer> answers) {
		Question q1 = new Question();
		q1.setQuestionText(questionText);
		q1.setQuiz(quiz);
		q1 = questionRepository.save(q1);

		for (Answer a : answers) {
			// answerRepository.save(a);
			a.setQuestion(q1);
			answerRepository.save(a);
		}

		return q1;
	}

	/**
	 * Helper function which creates a bunch of quizzes
	 * 
	 * @param owner  List of users which should be used as quiz owners
	 * @param groups List of groups which should be used as quiz group
	 */
	private void createTestQuizzes(User[] owner, Group[] groups) {
		// ------------------------QUIZ 1
		// ----------------------------------------------------------------------------
		Quiz quiz1 = new Quiz("Quiz to test the IT-Security knowledge", QuizType.Quiz, new Date());
		quiz1.setName("IT-Security Quiz");
		quiz1.setOwner(owner[0]);

		quiz1.addTagtoList("IT");
		quiz1.addTagtoList("Security");
		quiz1.addTagtoList("IT-Security");
		quiz1.addTagtoList("Computer");
		quiz1.addTagtoList("Computer Engineering");
		quiz1.setGroup(groups[4]);

		quiz1 = quizRepository.save(quiz1);

		ArrayList<Answer> answerQ1Q1 = new ArrayList<Answer>();
		answerQ1Q1.add(new Answer("A virus only for smartphones", false));
		answerQ1Q1.add(new Answer("A virus which mines bitcoins", false));
		answerQ1Q1.add(new Answer("Software which encrypts data and demands money", true));
		answerQ1Q1.add(new Answer("A dark web platform", false));
		addTestQuestion(quiz1, "What is encryption ransomware?", answerQ1Q1);

		ArrayList<Answer> answerQ1Q2 = new ArrayList<Answer>();
		answerQ1Q2.add(new Answer("Software which displays ads", true));
		answerQ1Q2.add(new Answer("Ads which are displayed in a computer game", false));
		answerQ1Q2.add(new Answer("A software tool to design advertisements", false));
		answerQ1Q2.add(new Answer("Ads which contain viruses", false));
		addTestQuestion(quiz1, "What is adware", answerQ1Q2);

		ArrayList<Answer> answerQ1Q3 = new ArrayList<Answer>();
		answerQ1Q3.add(new Answer("A backup tool for mac OS", false));
		answerQ1Q3.add(new Answer("A special screwdriver for desktop chassis", false));
		answerQ1Q3.add(new Answer("A scary horror game", false));
		answerQ1Q3.add(new Answer("A program which tries to manipulate the user", true));
		addTestQuestion(quiz1, "What is scareware?", answerQ1Q3);

		ArrayList<Answer> answerQ1Q4 = new ArrayList<Answer>();
		answerQ1Q4.add(new Answer("A device in the network which blocks unwanted traffic", true));
		answerQ1Q4.add(new Answer("Software which blocks unwanted traffic", true));
		answerQ1Q4.add(new Answer("A fireproof computer case", false));
		answerQ1Q4.add(new Answer("Software which keeps the CPU from overheating", false));
		addTestQuestion(quiz1, "What is a firewall?", answerQ1Q4);

		ArrayList<Answer> answerQ1Q5 = new ArrayList<Answer>();
		answerQ1Q5.add(new Answer("Exploits human behaviour", true));
		answerQ1Q5.add(new Answer("It only works on social media", false));
		answerQ1Q5.add(new Answer("It is a virus send over social media", false));
		answerQ1Q5.add(new Answer("It is about manipulating the user", true));
		addTestQuestion(quiz1, "What is true about social engineering?", answerQ1Q5);

//------------------------QUIZ 2 ----------------------------------------------------------------------------
		Quiz quiz2 = new Quiz("Easy quiz to test basic IT-Security knowledge", QuizType.Quiz, new Date());
		quiz2.setName("Easy IT-Security Quiz");
		quiz2.setOwner(owner[3]);

		quiz2.addTagtoList("IT");
		quiz2.addTagtoList("Security");
		quiz2.addTagtoList("IT-Security");
		quiz2.addTagtoList("Computer");
		quiz2.addTagtoList("Computer Engineering");
		quiz2.setGroup(null);

		quiz2 = quizRepository.save(quiz2);

		ArrayList<Answer> answerQ2Q1 = new ArrayList<Answer>();
		answerQ2Q1.add(new Answer("h1E2l3l4o5", false));
		answerQ2Q1.add(new Answer("hello1234", false));
		answerQ2Q1.add(new Answer("He!l5l$Oß!*", true));
		answerQ2Q1.add(new Answer("!qwerty!", false));
		addTestQuestion(quiz2, "Which of these passwords is the most secure one?", answerQ2Q1);

		ArrayList<Answer> answerQ2Q2 = new ArrayList<Answer>();
		answerQ2Q2.add(new Answer("Nothing", false));
		answerQ2Q2.add(new Answer("Send it to the manufacturer", false));
		answerQ2Q2.add(new Answer("Connect it to my pc and open the files", false));
		answerQ2Q2.add(new Answer("Give it to the shop owner", true));
		addTestQuestion(quiz2, "You found an usb drive in an coffee shop. What should you do?", answerQ2Q2);

		ArrayList<Answer> answerQ2Q3 = new ArrayList<Answer>();
		answerQ2Q3.add(new Answer("Pin", false));
		answerQ2Q3.add(new Answer("Password", true));
		answerQ2Q3.add(new Answer("Pattern", false));
		answerQ2Q3.add(new Answer("None", false));
		addTestQuestion(quiz2, "What is the most secure way for locking a smartphone?", answerQ2Q3);

		ArrayList<Answer> answerQ2Q4 = new ArrayList<Answer>();
		answerQ2Q4.add(new Answer("True", false));
		answerQ2Q4.add(new Answer("False", true));
		addTestQuestion(quiz2, "A computer can't be hacked if it is connected to a secured wifi.", answerQ2Q4);

		ArrayList<Answer> answerQ2Q5 = new ArrayList<Answer>();
		answerQ2Q5.add(new Answer("I tell him to keep my account secure", false));
		answerQ2Q5.add(new Answer("Before telling, I ask for his employee id", false));
		answerQ2Q5.add(new Answer("I don't tell him under any circumstances", true));
		answerQ2Q5.add(new Answer("I check the caller id before telling", false));
		addTestQuestion(quiz2, "If a bank employee calls me and asks for my banking password ...", answerQ2Q5);

//------------------------QUIZ 3 ----------------------------------------------------------------------------
		Quiz quiz3 = new Quiz("Testing knowedge of IMA students", QuizType.Quiz, new Date());
		quiz3.setName("IMA Quiz");
		quiz3.setOwner(owner[3]);

		quiz3.addTagtoList("IT");
		quiz3.addTagtoList("IMA");
		quiz3.addTagtoList("FH Joanneum");
		quiz3.addTagtoList("Computer");
		quiz3.addTagtoList("Computer Engineering");
		quiz3.setGroup(groups[2]);

		quiz3 = quizRepository.save(quiz3);

		ArrayList<Answer> answerQ3Q1 = new ArrayList<Answer>();
		answerQ3Q1.add(new Answer("A standard Let's Encrypt certificate", false));
		answerQ3Q1.add(new Answer("A wildcard certificate", false));
		answerQ3Q1.add(new Answer("An extended validation certificate", true));
		answerQ3Q1.add(new Answer("A domain validation certificate", false));
		addTestQuestion(quiz3, "Your company runs an e-banking service. What type of certificicate should you use?",
				answerQ3Q1);

		ArrayList<Answer> answerQ3Q2 = new ArrayList<Answer>();
		answerQ3Q2.add(new Answer("Yes it is always possible", false));
		answerQ3Q2.add(new Answer("Under normal circumstances not possible", true));
		answerQ3Q2.add(new Answer("There are ways to reach it", true));
		answerQ3Q2.add(new Answer("Yes, it is supported by most of the hypervisors", false));
		addTestQuestion(quiz3, "Is it possible to access other virtual machines data inside a virtual machine?",
				answerQ3Q2);

		ArrayList<Answer> answerQ3Q3 = new ArrayList<Answer>();
		answerQ3Q3.add(new Answer("A very secure server", false));
		answerQ3Q3.add(new Answer("Antivirus software", false));
		answerQ3Q3.add(new Answer("A tool for penetration testing", false));
		answerQ3Q3.add(new Answer("A server used to bait attackers", true));
		addTestQuestion(quiz3, "What is a Honeypot?", answerQ3Q3);

		ArrayList<Answer> answerQ3Q4 = new ArrayList<Answer>();
		answerQ3Q4.add(new Answer("It´s also called cookie hijacking", true));
		answerQ3Q4.add(new Answer("A hacker taking control of a server", false));
		answerQ3Q4.add(new Answer("Someone stealing credit card information", false));
		answerQ3Q4.add(new Answer("Stealing someone's authentication cookie", true));
		addTestQuestion(quiz3, "What is Session Hijacking?", answerQ3Q4);

		ArrayList<Answer> answerQ3Q5 = new ArrayList<Answer>();
		answerQ3Q5.add(new Answer("A list of decrypted password hashes", true));
		answerQ3Q5.add(new Answer("An important document in my little pony", false));
		answerQ3Q5.add(new Answer("A well formated XML file", false));
		answerQ3Q5.add(new Answer("Short name for a database table with zero entries", false));
		addTestQuestion(quiz3, "What is a rainbow table?", answerQ3Q5);

//------------------------QUIZ 4 ----------------------------------------------------------------------------
		Quiz quiz4 = new Quiz("Testing SWENGA knowedge of IMA17 students", QuizType.Quiz, new Date());
		quiz4.setName("SWENGA Quiz for IMA17");
		quiz4.setOwner(owner[3]);

		quiz4.addTagtoList("IT");
		quiz4.addTagtoList("IMA");
		quiz4.addTagtoList("FH Joanneum");
		quiz4.addTagtoList("SWENG");
		quiz4.addTagtoList("JAVA");
		quiz4.setGroup(groups[3]);

		quiz4 = quizRepository.save(quiz4);

		ArrayList<Answer> answerQ4Q1 = new ArrayList<Answer>();
		answerQ4Q1.add(new Answer("Get", false));
		answerQ4Q1.add(new Answer("Connect", false));
		answerQ4Q1.add(new Answer("Trace", false));
		answerQ4Q1.add(new Answer("Query", true));
		addTestQuestion(quiz4, "What request method does NOT exist?", answerQ4Q1);

		ArrayList<Answer> answerQ4Q2 = new ArrayList<Answer>();
		answerQ4Q2.add(new Answer("Cookies are programs", true));
		answerQ4Q2.add(new Answer("Cookies are sent from a server", false));
		answerQ4Q2.add(new Answer("Cookies are stored at the client", false));
		answerQ4Q2.add(new Answer("Coockies are sent back to the server after EVERY following request", false));
		addTestQuestion(quiz4, "What is NOT true about cookies?", answerQ4Q2);

		ArrayList<Answer> answerQ4Q3 = new ArrayList<Answer>();
		answerQ4Q3.add(new Answer("The controller notifies the model", false));
		answerQ4Q3.add(new Answer("The controller updates the view", true));
		answerQ4Q3.add(new Answer("The controller is updated by the model", false));
		answerQ4Q3.add(new Answer("The model reads the view", false));
		addTestQuestion(quiz4, "What is true about the model view controller?", answerQ4Q3);

		ArrayList<Answer> answerQ4Q4 = new ArrayList<Answer>();
		answerQ4Q4.add(new Answer("JSP is the acronym for Java Server Page", true));
		answerQ4Q4.add(new Answer("JSPs are text-based document", true));
		answerQ4Q4.add(
				new Answer("JSPs are documents which describes how to process a response to create a request", false));
		answerQ4Q4.add(new Answer("JSP is the aconym for Java Security Permissions", false));
		addTestQuestion(quiz4, "What is a JSP?", answerQ4Q4);

		ArrayList<Answer> answerQ4Q5 = new ArrayList<Answer>();
		answerQ4Q5.add(new Answer("ORM is the arconym for Optional Request Methods", false));
		answerQ4Q5.add(new Answer("ORM is used to design views", false));
		answerQ4Q5.add(new Answer("ORM is the acronym for Object-Relational Mapping", true));
		answerQ4Q5.add(new Answer("ORM is a technique for converting code from Haskell to Java", false));
		addTestQuestion(quiz4, "What is ORM?", answerQ4Q5);

//------------------------QUIZ 5 ----------------------------------------------------------------------------
		Quiz quiz5 = new Quiz("Testing knowedge of people about animals", QuizType.Quiz, new Date());
		quiz5.setName("Animal Quiz");
		quiz5.setOwner(owner[8]);

		quiz5.addTagtoList("animals");
		quiz5.addTagtoList("zoo");
		quiz5.setGroup(groups[1]);

		quiz5 = quizRepository.save(quiz5);

		ArrayList<Answer> answerQ5Q1 = new ArrayList<Answer>();
		answerQ5Q1.add(new Answer("True", false));
		answerQ5Q1.add(new Answer("False", true));
		addTestQuestion(quiz5, "Rabbits can life alone.", answerQ5Q1);

		ArrayList<Answer> answerQ5Q2 = new ArrayList<Answer>();
		answerQ5Q2.add(new Answer("When they want food", false));
		answerQ5Q2.add(new Answer("For greeting", true));
		answerQ5Q2.add(new Answer("When they are exited", true));
		answerQ5Q2.add(new Answer("When they have pain", false));
		addTestQuestion(quiz5, "When do horses usually neigh?", answerQ5Q2);

		ArrayList<Answer> answerQ5Q3 = new ArrayList<Answer>();
		answerQ5Q3.add(new Answer("By their teeth", true));
		answerQ5Q3.add(new Answer("By their hooves ", false));
		answerQ5Q3.add(new Answer("By their eyes", false));
		addTestQuestion(quiz5, "By what can doctors guess the age of horses?", answerQ5Q3);

		ArrayList<Answer> answerQ5Q4 = new ArrayList<Answer>();
		answerQ5Q4.add(new Answer("True", false));
		answerQ5Q4.add(new Answer("False", true));
		addTestQuestion(quiz5, "Dogs always dislike cats.", answerQ5Q4);

		ArrayList<Answer> answerQ5Q5 = new ArrayList<Answer>();
		answerQ5Q5.add(new Answer("Yes", true));
		answerQ5Q5.add(new Answer("No", false));
		addTestQuestion(quiz5, "Do cats usualy dislike to take a bath?", answerQ5Q5);

	}

	/**
	 * Helper function which creates a bunch of surveys
	 * 
	 * @param owner  List of users which should be used as survey owners
	 * @param groups List of groups which should be used as survey group
	 */
	private void createTestSurv(User[] owner, Group[] groups) {
//------------------------------Survey 1 -------------------------------------------------------------------------
		Quiz survey1 = new Quiz("Survey about food and drinks", QuizType.Survey, new Date());
		survey1.setName("Food Survey");
		survey1.setOwner(owner[4]);

		survey1.addTagtoList("Food");
		survey1.addTagtoList("Drinks");

		survey1 = quizRepository.save(survey1);

		ArrayList<Answer> answerS1Q1 = new ArrayList<Answer>();
		answerS1Q1.add(new Answer("Pancakes"));
		answerS1Q1.add(new Answer("Scrambled eggs"));
		answerS1Q1.add(new Answer("Bread/Toast"));
		answerS1Q1.add(new Answer("Muesli"));
		answerS1Q1.add(new Answer("Fruits"));
		answerS1Q1.add(new Answer("Nothing"));
		addTestQuestion(survey1, "What do you love to eat in the morning?", answerS1Q1);

		ArrayList<Answer> answerS1Q2 = new ArrayList<Answer>();
		answerS1Q2.add(new Answer("Water"));
		answerS1Q2.add(new Answer("Coffee"));
		answerS1Q2.add(new Answer("Tea"));
		answerS1Q2.add(new Answer("Juice"));
		answerS1Q2.add(new Answer("Soft drinks"));
		answerS1Q2.add(new Answer("Cocoa"));
		answerS1Q2.add(new Answer("Nothing"));
		addTestQuestion(survey1, "What do you love to drink in the morning", answerS1Q2);

		ArrayList<Answer> answerS1Q3 = new ArrayList<Answer>();
		answerS1Q3.add(new Answer("Salat"));
		answerS1Q3.add(new Answer("Sandwich"));
		answerS1Q3.add(new Answer("Pizza"));
		answerS1Q3.add(new Answer("Burger"));
		answerS1Q3.add(new Answer("Pasta"));
		answerS1Q3.add(new Answer("Rice"));
		answerS1Q3.add(new Answer("Something else"));
		answerS1Q3.add(new Answer("Nothing"));
		addTestQuestion(survey1, "What do you love to eat at lunch time?", answerS1Q3);

		ArrayList<Answer> answerS1Q4 = new ArrayList<Answer>();
		answerS1Q4.add(new Answer("Water"));
		answerS1Q4.add(new Answer("Coffee"));
		answerS1Q4.add(new Answer("Tea"));
		answerS1Q4.add(new Answer("Juice"));
		answerS1Q4.add(new Answer("Soft drinks"));
		answerS1Q4.add(new Answer("Cocoa"));
		answerS1Q4.add(new Answer("Nothing"));
		addTestQuestion(survey1, "What do you love to drink at lunch time?", answerS1Q4);

		ArrayList<Answer> answerS1Q5 = new ArrayList<Answer>();
		answerS1Q5.add(new Answer("Salat"));
		answerS1Q5.add(new Answer("Sandwich"));
		answerS1Q5.add(new Answer("Pizza"));
		answerS1Q5.add(new Answer("Burger"));
		answerS1Q5.add(new Answer("Pasta"));
		answerS1Q5.add(new Answer("Rice"));
		answerS1Q5.add(new Answer("Something else"));
		answerS1Q5.add(new Answer("Nothing"));
		addTestQuestion(survey1, "What do you love to eat in the evening?", answerS1Q5);

		ArrayList<Answer> answerS1Q6 = new ArrayList<Answer>();
		answerS1Q6.add(new Answer("Water"));
		answerS1Q6.add(new Answer("Coffee"));
		answerS1Q6.add(new Answer("Tea"));
		answerS1Q6.add(new Answer("Juice"));
		answerS1Q6.add(new Answer("Soft drinks"));
		answerS1Q6.add(new Answer("Cocoa"));
		answerS1Q6.add(new Answer("Nothing"));
		addTestQuestion(survey1, "What do you love to drink in the evening?", answerS1Q6);

//------------------------------Survey 2 -------------------------------------------------------------------------	
		Quiz survey2 = new Quiz("Survey about University for Students", QuizType.Survey, new Date());
		survey2.setName("University Survey");
		survey2.setOwner(owner[1]);

		survey2.addTagtoList("University");
		survey2.addTagtoList("Student");
		survey2.addTagtoList("Education");
		survey2.setGroup(groups[0]);

		survey2 = quizRepository.save(survey2);

		ArrayList<Answer> answerS2Q1 = new ArrayList<Answer>();
		answerS2Q1.add(new Answer("Yes"));
		answerS2Q1.add(new Answer("Medium"));
		answerS2Q1.add(new Answer("No"));
		addTestQuestion(survey2, "Do you like your university?", answerS2Q1);

		ArrayList<Answer> answerS2Q2 = new ArrayList<Answer>();
		answerS2Q2.add(new Answer("IT"));
		answerS2Q2.add(new Answer("Management"));
		answerS2Q2.add(new Answer("Law"));
		answerS2Q2.add(new Answer("Health"));
		answerS2Q2.add(new Answer("Art"));
		answerS2Q2.add(new Answer("Design"));
		answerS2Q2.add(new Answer("Sience"));
		answerS2Q2.add(new Answer("Automation"));
		answerS2Q2.add(new Answer("Something completely different"));
		addTestQuestion(survey2, "What do you study?", answerS2Q2);

		ArrayList<Answer> answerS2Q3 = new ArrayList<Answer>();
		answerS2Q3.add(new Answer("Yes"));
		answerS2Q3.add(new Answer("The most, yes"));
		answerS2Q3.add(new Answer("About half of them"));
		answerS2Q3.add(new Answer("Only some"));
		answerS2Q3.add(new Answer("No"));
		addTestQuestion(survey2, "Do you like your professors?", answerS2Q3);

		ArrayList<Answer> answerS2Q4 = new ArrayList<Answer>();
		answerS2Q4.add(new Answer("<1"));
		answerS2Q4.add(new Answer("1"));
		answerS2Q4.add(new Answer("2"));
		answerS2Q4.add(new Answer("3"));
		answerS2Q4.add(new Answer("4"));
		answerS2Q4.add(new Answer("5"));
		answerS2Q4.add(new Answer("6"));
		answerS2Q4.add(new Answer(">6"));
		addTestQuestion(survey2, "How many semesters are you already studying?", answerS2Q4);

		ArrayList<Answer> answerS2Q5 = new ArrayList<Answer>();
		answerS2Q5.add(new Answer("Yes"));
		answerS2Q5.add(new Answer("Only the seminars"));
		answerS2Q5.add(new Answer("Only the lectures"));
		answerS2Q5.add(new Answer("No"));
		addTestQuestion(survey2, "Do you visit the lectures and seminars regulary?", answerS2Q5);

		ArrayList<Answer> answerS2Q6 = new ArrayList<Answer>();
		answerS2Q6.add(new Answer("Yes"));
		answerS2Q6.add(new Answer("Mostly yes"));
		answerS2Q6.add(new Answer("Medium"));
		answerS2Q6.add(new Answer("Mostly no"));
		answerS2Q6.add(new Answer("No"));
		addTestQuestion(survey2, "Are you satisfied with your grades??", answerS2Q6);

//------------------------------Survey 3 -------------------------------------------------------------------------	
		Quiz survey3 = new Quiz("Survey about zoos and animal there", QuizType.Survey, new Date());
		survey3.setName("Animal/Zoo Survey");
		survey3.setOwner(owner[8]);
		survey3.addTagtoList("animals");
		survey3.addTagtoList("animal");
		survey3.addTagtoList("zoo");
		survey3.setGroup(groups[1]);
		survey3 = quizRepository.save(survey3);

		ArrayList<Answer> answerS3Q1 = new ArrayList<Answer>();
		answerS3Q1.add(new Answer("Once a week"));
		answerS3Q1.add(new Answer("Once a month"));
		answerS3Q1.add(new Answer("Once a year"));
		answerS3Q1.add(new Answer("Never"));
		addTestQuestion(survey3, "How often do you visit a zoo?", answerS3Q1);

		ArrayList<Answer> answerS3Q2 = new ArrayList<Answer>();
		answerS3Q2.add(new Answer("Yes"));
		answerS3Q2.add(new Answer("They are ok"));
		answerS3Q2.add(new Answer("No"));
		addTestQuestion(survey3, "Do you like lions?", answerS3Q2);

		ArrayList<Answer> answerS3Q3 = new ArrayList<Answer>();
		answerS3Q3.add(new Answer("Pandas"));
		answerS3Q3.add(new Answer("Monkeys"));
		answerS3Q3.add(new Answer("I like them the same"));
		answerS3Q3.add(new Answer("I do not like both"));
		addTestQuestion(survey3, "Do you prefer pandas or monkeys?", answerS3Q3);

		ArrayList<Answer> answerS3Q4 = new ArrayList<Answer>();
		answerS3Q4.add(new Answer("My family"));
		answerS3Q4.add(new Answer("My friends"));
		answerS3Q4.add(new Answer("My partner"));
		answerS3Q4.add(new Answer("No one"));
		addTestQuestion(survey3, "Who do you take with you when you visit a zoo?", answerS3Q4);

		ArrayList<Answer> answerS3Q5 = new ArrayList<Answer>();
		answerS3Q5.add(new Answer("Yes"));
		answerS3Q5.add(new Answer("Sometimes"));
		answerS3Q5.add(new Answer("No"));
		addTestQuestion(survey3, "Do you visit the restorants in the zoo?", answerS3Q5);

// ------------------------------Survey 4 -------------------------------------------------------------------------
		Quiz survey4 = new Quiz("Survey about your preferences", QuizType.Survey, new Date());
		survey4.setName("Preference Survey");
		survey4.setOwner(owner[2]);
		survey4 = quizRepository.save(survey4);

		ArrayList<Answer> answerS4Q1 = new ArrayList<Answer>();
		answerS4Q1.add(new Answer("Blue"));
		answerS4Q1.add(new Answer("Red"));
		answerS4Q1.add(new Answer("Yellow"));
		answerS4Q1.add(new Answer("Green"));
		answerS4Q1.add(new Answer("Pink"));
		answerS4Q1.add(new Answer("Black"));
		answerS4Q1.add(new Answer("White"));
		answerS4Q1.add(new Answer("None of the above"));
		addTestQuestion(survey4, "What is your favorite color?", answerS4Q1);

		ArrayList<Answer> answerS4Q2 = new ArrayList<Answer>();
		answerS4Q2.add(new Answer("Yes"));
		answerS4Q2.add(new Answer("No"));
		answerS4Q2.add(new Answer("No, but i want one"));
		addTestQuestion(survey4, "Do you have an iPhone?", answerS4Q2);

		ArrayList<Answer> answerS4Q3 = new ArrayList<Answer>();
		answerS4Q3.add(new Answer("Yes"));
		answerS4Q3.add(new Answer("Some times"));
		answerS4Q3.add(new Answer("No"));
		addTestQuestion(survey4, "Do you wear jewellery?", answerS4Q3);

		ArrayList<Answer> answerS4Q4 = new ArrayList<Answer>();
		answerS4Q4.add(new Answer("Yes"));
		answerS4Q4.add(new Answer("Some times"));
		answerS4Q4.add(new Answer("No"));
		addTestQuestion(survey4, "Do you use social media?", answerS4Q4);

		ArrayList<Answer> answerS4Q5 = new ArrayList<Answer>();
		answerS4Q5.add(new Answer("Facebook"));
		answerS4Q5.add(new Answer("Instagram"));
		answerS4Q5.add(new Answer("Twitter"));
		answerS4Q5.add(new Answer("Snapchat"));
		answerS4Q5.add(new Answer("Reddit"));
		answerS4Q5.add(new Answer("Jodel"));
		answerS4Q5.add(new Answer("I do not know"));
		answerS4Q5.add(new Answer("Another one"));
		addTestQuestion(survey4, "Which social meda plattform do you use the most?", answerS4Q5);

// ------------------------------Survey 5 -------------------------------------------------------------------------
		Quiz survey5 = new Quiz("Survey about going out", QuizType.Survey, new Date());
		survey5.setName("Survey about going out");
		survey5.setOwner(owner[9]);

		survey5.addTagtoList("going out");
		survey5.addTagtoList("party");
		survey5.addTagtoList("drinking");
		survey5 = quizRepository.save(survey5);

		ArrayList<Answer> answerS5Q1 = new ArrayList<Answer>();
		answerS5Q1.add(new Answer("Once a week"));
		answerS5Q1.add(new Answer("Once a month"));
		answerS5Q1.add(new Answer("Once a year"));
		addTestQuestion(survey5, "How often do you go out", answerS5Q1);

		ArrayList<Answer> answerS5Q2 = new ArrayList<Answer>();
		answerS5Q2.add(new Answer("My family"));
		answerS5Q2.add(new Answer("My friends"));
		answerS5Q2.add(new Answer("My partner"));
		answerS5Q2.add(new Answer("No one"));
		addTestQuestion(survey5, "Who do you take with you when you go out?", answerS5Q2);

		ArrayList<Answer> answerS5Q3 = new ArrayList<Answer>();
		answerS5Q3.add(new Answer("Yes"));
		answerS5Q3.add(new Answer("Some times"));
		answerS5Q3.add(new Answer("No"));
		addTestQuestion(survey5, "Do you drink alcohol?", answerS5Q3);

		ArrayList<Answer> answerS5Q4 = new ArrayList<Answer>();
		answerS5Q4.add(new Answer("I usually leave early"));
		answerS5Q4.add(new Answer("I usually stay all night"));
		answerS5Q4.add(new Answer("It is different every time"));
		answerS5Q4.add(new Answer("I usually do not remember"));
		addTestQuestion(survey5, "How long do you usually stay at a party?", answerS5Q4);

	}

	/**
	 * Handle all unknown requests and redirect them to the 404 page
	 */
	@RequestMapping("*")
	public String hello(HttpServletRequest request) {
		return "404";
	}

	/**
	 * Handle all exceptions display the error page
	 * 
	 * @param ex Thrown exception
	 */
	@ExceptionHandler(Exception.class)
	public String handleAllException(Exception ex) {
		return "errorpage";
	}

}
