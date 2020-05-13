# SocioRecipe-App

Introduction:
 Android recipes sharing app will provide the user with a facility to upload recipes like a social platform, other users will be able to see the uploaded recipes on their home page, this app will work with GPS, detect user location and show only those recipes to a user that are uploaded from his/her country. The user will be allowed to save recipes for offline mode.

Google photos link to demo presentation:
https://photos.app.goo.gl/TGZQnXqPgWuw7kM66

Functionalities:
1.	Signup
2.	log in
3.	recover the password.
4.	See recipes / Open recipes. 
5.	Search for recipes.
6.	Upload recipes.
7.	Save recipes for offline.
8.	Logout.


What the program does:
  The program starts off with a splash page that contains the name and short description of the app. It also has a ‘Start’ button that allows user to enter the application. The button redirects the user to the login interface. For the first time users, there is an option to access the sign up interface at the bottom of the login page. 
  After a successful login, the app redirects to the main screen where it displays the list of recipes. There is a bottom navigation menu with four buttons representing the four pages of the application. The four pages of the application and what they do are explained as follows:

1.	Home page is the default page. On this page, the user is displayed all the recipes posted by the people of his/her own country. The application detects the country of the user by a using android’s location manager service and the user is able to see only those recipes which are posted by the other users of the same country that the current user is logged in from. When you open any recipe, there is a save button at the very bottom of the recipe details that allow user to save that recipe for offline use.
2.	Search page has a search input field which allows user to find all the recipes posted through the application, not just the recipes posted from the same country. When user enters a keyword in search filed, the field suggests recipes staring from that keyword. These search suggestions are case sensitive as the query searches the firebase’s database which does not support case insensitive query yet.
3.	Add page allows user to add and publish a recipe for the public. This page has a simple interface with few options and fields. On the top, there is an option of selecting an image for the recipe, then there is a form for the Title and Description of the recipe. The title must be of at least 20 characters and description to be of 50 characters and the recipe image is mandatory. User cannot post any recipe without an image. Finally, there is a save button that saves the recipe details to the Firebase database along with the country name of the recipe author.
4.	Profile page has some information about the user like Profile Photo and Username. Additionally, there are some options including Change Password, Saved Recipes and Sign Out. The Change Password option allows user to change his/her password by filling a form. This form requires old password and new password. On entering both the fields, the password gets changed. Saved Recipe option takes user to another page which displays all the recipes he/she have saved for offline use.


How the backend of the app works (Firebase/Database):

Establishing connection to Firebase

  Initially, we need to add some libraries required to work with the firebase. In my project, I have added four important libraries (required for our application) into the build.gradle. These are for Connectivity, Authentication, Storage and Database management. A json file is also required which help syncing the libraries. 
After successfully syncing all the libraries, we can use the objects and operations defined in those libraries. For this, we need to create instances of the required classes into our activities where firebase operations are involved.

Initially, when a user wants to register and accesses the Signup activity, it uses firebase authentication object (for signing up the user) and firebase storage and database objects (for storing user’s image and other details)

This activity involves functions:

1.	uploadImage()
This function is responsible for saving profile image into the firebase database. Firebase Storage Reference is used to establish a link towards the target.
2.	saverUserDetails()
This function is used to save the information of user into the firebase database. We pass relevant parameters and all the details are automatically stored on firebase.
3.	sendVerificationEmail()
this function of firebase is responsible to send a verification email to the userer’s email address.

Similarly, the sign in activity uses firebase authentication object for signing in the user. It performs two major tasks. The first is that if the user has signed up but has not yet verified his/her email, it would display an option to “resend verification email” and the activity will call the function sendVerificationEmail(). On the other hand, if the user has successfully verified the email, then the activity calls another function signInWithEmailAndPassword(). This function requires necessary login credentials as parameters and finally it allows user to login.
In the Home Fragment, again firebase objects are needed to load the recipes which are stored on the server. The main function in this fragment is getDataFromFirebase(). This function is responsible for loading recipes posted in the same country and providing to the application. firebaseDatabaseReference object is used to get the recipe details. On retrieving the recipes, these are stored in a list and shown to the user by the help of a makeview() function.
Location is another very important operation is performed in this activity. That is for the tracing of location of the user. The application asks user to turn on GPS in case if device’s GPS is turned OFF. In this regard, a function turnOnGps() is created which takes the user to the GPS setting of the device through intent.
getLocation() function is used to obtain the location of the user in terms of latitude and longitude. The latitude and longitude values is then passed onto another function getCountryName(). In this function Geocoder object is used to get the country with that latitude and longitude values. 
This value is used in all the activities where user either finds or scrolls the recipes. The Process of getting location is similar in the rest of the activities.
Another important fragment is Post Fragment. This fragment stores newly added recipes. It uses firebaseDatabaseReference object which stores all the information of new recipe to the firebase. The main function which is used in this fragment is saveData() which utilizes and saves the firebaseDatabaseReference object.
Firebase objects are also involved in another activity ChangePasswordActivity. It requires authCredentials object and a function reauthecticate() which reauthenticates user whenever the password is changed. Within the reauthenticate function, the updatePassword() function is also used.

SQLite Database:
To save recipes locally, I have used SQLite. There is a SQLite manager file named “SqliteDatabase” which controls all the operations needed from connecting to storing the data in the database. The activity which uses sqlite is “SaveOffline” activity. This activity uses the operations defined in the SQLite file and stores the data locally into the database which is available for the user even if the user is offline.

Instructions/Directions to use the app:

o	Creating an Account
This first step for every user is to get registered with the application. The user needs to fill a registration form along with a valid email address. This email address will be sent a confirmation link which would verify the user and hence the user will get registered. Once the user is registered, he/she needs to login to the application by using email and password. 

o	Scrolling Recipes
Once the user is logged in, he/she can scroll the list of recipes posted by the people on the main screen. The user can click on any recipe and see the details of that recipe. If user wants to save the recipe for future use, then he/she can save it. All the saved recipes can be accessed from the profile page.

o	Finding Recipes
If the user wants to find a specific recipe then there is a search button on the bottom of main screen which opens a search page with an input field. The user can enter the keywords and the field will suggest the relevant recipes. The user can select his/her desired recipe and again, if he/she wants to save it, he/she can save it by clicking the save button.

o	Posting Recipes
If the user wants to publish his/her own recipes for the public then he/she can click on the Add button at the bottom navigation menu. It opens a page where user can enter all the details of the recipe (image, title and description). On clicking on Post button, the recipe is published and made available for the public.

o	Changing Password
To change the password, the user can click on Profile button on the bottom navigation menu. It opens the profile page with profile information of the user. It also contains a change password option which can be used to change the password. The user just needs to click on Change Password and application will take user to another screen with a form of two fields, Old Password and New Password. The user needs to enter the old password and the new. Then, the passwords will get updated.

o	Saved Recipes
There is another option in the profile page for the users. The ‘Saved Recipes’ option allows the user to access previously Saved Recipes. By clicking on it the user would be able to see all the saved recipes even when he/she is offline.

o	Signing Out
A user can sign out by clicking on sign out button available on profile page. The user will not be signed out automatically by closing the application.

