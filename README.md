# healthtrackercontroller-20096015

Some points that can help in the future:


For adding a feature, there are three general steps, regardless of testing:
  1. updating "DAO"
  2. updating "HealthTrackerController"
  3. updating "Javalin"
  
  
Be sure about the Credentials that you insereted. Some test fails might be related to your database. 
  So make sure that you have inserted the "host", "password",... exactly correct in Kotlin db file.
  


PgAdmin >> Query >> delete from users; 
  Try to keep it clean for testing. 
  
  
