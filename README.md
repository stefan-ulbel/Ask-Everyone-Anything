# SWENGA_Project_AEA
## AEA (Ask everyone everything)
* Day created: 16.05.2019
* subject: SWENGA
* IMA17

Team members:
* Andreas Raith
* Marcel Koschu
* Nadine Meister
* Stefan Ulbel

### Workload distribution

Stefan Ulbel:
* General concept and idea
* Group controller
* Group pages:
  * Create Group
  * Edit Group
  * Show public groups
  * Show my user groups
  * Admin: manage groups
* Sidebar/topbar/navbar templates

Andreas Raith:
* General concept
* Database model
* Quizzes/Surveys:
  * Create quiz/survey page
  * Edit quiz/survey page
  * View results page
* User profile page
* PDF download functionality   

Nadine Meister:
* General concept
* Bootstrap template
* Frontend design
* Testdata creation
* User profile page
* PDF download functionality   


Marcel Koschu:
* General concept
* Security concept and controller
* Testing
* Dashboard page
* Login and create account page

Note: Most big tasks were completed together in a group of two

### Setup instructions
#### Required:
 * Working Java Enviroment
 * Eclipse
 * Apache Tomcat v9.0
#### Setup
1. Clone or download the project source code from github
2. Create an new database configuration file named `db.properties` in the folder `/src`
3. Copy the following contend in the file and replace the <> with proper values
```
 db.url=jdbc:mysql://<ip>/<database-name>
 db.username=<username>
 db.password=<password>
```
4. Run the project on the apache tomcat server
5. Open a modern browser like Chrome and navigate to [http://localhost:8080/SWENGA_Project_AEA/](http://localhost:8080/SWENGA_Project_AEA/)
6. Call `/test` to create test data
7. Login with the information below

#### User information
Multiple users are available for login.
To enable admin functions login with `admin`.
All users including the admin have the password `ima17`.
Available username: `user`, `BlauenKrausler`
