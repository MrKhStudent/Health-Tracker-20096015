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
  
  
............................................................ Front end
First, add a new endpoint in Javalin Config:
            get("/users/{user-id}/activities", VueComponent("<user-activity-overview></user-activity-overview>"))

Then Create a 'user-activity-overview.vue' file in resources/vue/views directory:
             ...
             </template>
             ...
             </script>
And make the change in user-profile.vue to add a new hyperlink.

Then, put a new div in the home-page.vue





