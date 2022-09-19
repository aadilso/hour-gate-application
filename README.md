# HourGate - Android based workforce management application

Application developed for a small security services business in order to reduce several inefficiencies of day to day operations <br />

Some features include: <br />
* User authentication for administrators and employees <br />
* Saving user states (to avoid having to log in again once application is closed) <br />
* Sites page for administrators to search current contracted sites and add/remove any new/expiring sites  <br /> 
* Clock in/out page for employees to select sites to sign into/off, (sign on/off is only allowed if user is within a specified distance of the site) <br/> 
* Forgot password feature for employees only (by design) <br />
* My profile page with account information of the logged in administrator or employee <br />
* Employees page for administrators to search current employees and add/remove any new/old employees <br />
* Reports page for administrators to generate reports on employee work hours and wages due (if required can be generated for specific months and/or specific employees instead of for the whole workforce/year) <br />
* Ablity for administrators to print these generated reports or save them externally outside the application <br />







# Using the project
* For a quick video demo of the project: https://hourgate-app-demo.s3.eu-west-2.amazonaws.com/index.html <br />
* To download on Google Play Store: https://play.google.com/store/apps/details?id=com.employeemanagement.hourgate <br />

# Running the project 
To run the project locally:  <br />
* Clone the repository  <br />
* Download Android Studio: https://developer.android.com/studio <br />
* Run a emulator on Andriod Studio: https://developer.android.com/studio/run/emulator  <br />

 * Please note that the google-services.json file is not included in this repo hence you will need to create your own Cloud Firestore Database and connect it to your own Android Studio project to use any application features which require the database (which is essentially most of the features): https://firebase.google.com/docs/firestore/quickstart#java <br />
 
 * Also note that to use any features that require location you will need to create your own Google Maps API keys and add them to your pwn Android Studio project: https://developers.google.com/maps/documentation/android-sdk/get-api-key
 
 



