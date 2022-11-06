//This class will contain a series of Integration Tests to make sure that
//  the endpoint inputs to the System Under Test (SUT) calls will result
//  in the desired output from the actual remote database in Heroku, via a localhost.


package ie.setu.controllers

import ie.setu.config.DbConfig
import ie.setu.domain.User
import ie.setu.domain.Activity
import ie.setu.domain.BodyMeasurement
import ie.setu.domain.Calorie
import ie.setu.domain.Workout
import ie.setu.helpers.ServerContainer
import ie.setu.helpers.*
import ie.setu.utils.jsonNodeToObject
import ie.setu.utils.jsonToObject
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.Unirest
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthTrackerControllerTest {

    private val db = DbConfig().getDbConnection()
    private val app = ServerContainer.instance
    private val origin = "http://localhost:" + app.port()

    // //////////////////////////// ReadUsers//////////////////////
    @Nested
    inner class ReadUsers {
        //First test - get all users
        @Test
        fun `get all users from the database returns 200 or 404 response`() {
            val response = Unirest.get(origin + "/api/users/").asString()
            if (response.status == 200) {
                val retrievedUsers: ArrayList<User> = jsonToObject(response.body.toString())
                assertNotEquals(0, retrievedUsers.size)
            } else {
                assertEquals(404, response.status)
            }
        }

        //     get by user id when user doesn't exist
        @Test
        fun `get user by id when user does not exist returns 404 response`() {
            //Arrange - test data for user id
            val id = Integer.MIN_VALUE
            // Act - attempt to retrieve the non-existent user from the database
            val retrieveResponse = Unirest.get(origin + "/api/users/${id}").asString()
            // Assert -  verify return code
            //We have set no HTTP response codes based on our interrogation with the database.
            assertEquals(404, retrieveResponse.status)
        }

        //    get by user email when user doesn't exist
        @Test
        fun `get user by email when user does not exist returns 404 response`() {
            // Arrange & Act - attempt to retrieve the non-existent user from the database
            val retrieveResponse = Unirest.get(origin + "/api/users/email/${nonExistingEmail}").asString()
            // Assert -  verify return code
            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `getting a user by id when id exists, returns a 200 response`() {

            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser: User = jsonToObject(addResponse.body.toString())

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = retrieveUserById(addedUser.id)
            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        @Test
        fun `getting a user by email when email exists, returns a 200 response`() {

            //Arrange - add the user
            addUser(validName, validEmail)

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = retrieveUserByEmail(validEmail)
            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            val retrievedUser: User = jsonToObject(retrieveResponse.body.toString())
            deleteUser(retrievedUser.id)
        }
    }

    // ///////////////////// CreateUsers (add a User) ////////////////////////////////
    @Nested
    inner class CreateUsers {
        @Test
        fun `add a user with correct details returns a 201 response`() {

            //First we are adding the user and checking the response.
            // Secondly, we are retrieving the added user to verify that the name and email were added correctly.
            // Finally, we delete the record we just added,
            // so that we are restoring the database to the state before the test started.

            //Arrange & Act & Assert
            //    add the user and verify return code (using fixture data)
            val addResponse = addUser(validName, validEmail)
            assertEquals(201, addResponse.status)

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = retrieveUserByEmail(validEmail)
            assertEquals(200, retrieveResponse.status)

            //Assert - verify the contents of the retrieved user
            val retrievedUser: User = jsonToObject(addResponse.body.toString())
            assertEquals(validEmail, retrievedUser.email)
            assertEquals(validName, retrievedUser.name)

            //After - restore the db to previous state by deleting the added user
            val deleteResponse = deleteUser(retrievedUser.id)
            assertEquals(204, deleteResponse.status)
        }
    }


    // //////////////////////////// UpdateUsers //////////////////////
    @Nested
    inner class UpdateUsers {
        @Test
        fun `updating a user when it exists, returns a 204 response`() {

            //Arrange - add the user that we plan to do an update on
            val addedResponse = addUser(validName, validEmail)
            val addedUser: User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - update the email and name of the retrieved user and assert 204 is returned
            assertEquals(204, updateUser(addedUser.id, updatedName, updatedEmail).status)

            //Act & Assert - retrieve updated user and assert details are correct
            val updatedUserResponse = retrieveUserById(addedUser.id)
            val updatedUser: User = jsonToObject(updatedUserResponse.body.toString())
            assertEquals(updatedName, updatedUser.name)
            assertEquals(updatedEmail, updatedUser.email)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        @Test
        fun `updating a user when it doesn't exist, returns a 404 response`() {

            //Arrange - creating some text fixture data
            val updatedName = "Updated Name"
            val updatedEmail = "Updated Email"

            //Act & Assert - attempt to update the email and name of user that doesn't exist
            assertEquals(404, updateUser(-1, updatedName, updatedEmail).status)
        }
    }


    // //////////////////////////// DeleteUsers //////////////////////
    @Nested
    inner class DeleteUsers {
        @Test
        fun `deleting a user when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteUser(-1).status)
        }

        @Test
        fun `deleting a user when it exists, returns a 204 response`() {

            //Arrange - add the user that we plan to do a delete on
            val addedResponse = addUser(validName, validEmail)
            val addedUser: User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)

            //Act & Assert - attempt to retrieve the deleted user --> 404 response
            assertEquals(404, retrieveUserById(addedUser.id).status)
        }
    }


    ////////////////////////////////////////////////// Activity Tests /////////////////////////////////
    @Nested
    inner class CreateActivities {

        @Test
        fun `add an activity when a user exists for it, returns a 201 response`() {

            //Arrange - add a user and an associated activity that we plan to do a delete on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addActivityResponse = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse.status)

            //After - delete the user (Activity will cascade delete in the database)
            deleteUser(addedUser.id)
        }

        @Test
        fun `add an activity when no user exists for it, returns a 404 response`() {

            //Arrange - check there is no user for -1 id
            val userId = -1
            assertEquals(404, retrieveUserById(userId).status)

            val addActivityResponse = addActivity(
                activities.get(0).description, activities.get(0).duration,
                activities.get(0).calories, activities.get(0).started, userId
            )
            assertEquals(404, addActivityResponse.status)
        }
    }

    @Nested
    inner class ReadActivities {

        @Test
        fun `get all activities from the database returns 200 or 404 response`() {
            val response = retrieveAllActivities()
            if (response.status == 200) {
                val retrievedActivities = jsonNodeToObject<Array<Activity>>(response)
                assertNotEquals(0, retrievedActivities.size)
            } else {
                assertEquals(404, response.status)
            }
        }

        @Test
        fun `get all activities by user id when user and activities exists returns 200 response`() {
            //Arrange - add a user and 3 associated activities that we plan to retrieve
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id
            )
            addActivity(
                activities[1].description, activities[1].duration,
                activities[1].calories, activities[1].started, addedUser.id
            )
            addActivity(
                activities[2].description, activities[2].duration,
                activities[2].calories, activities[2].started, addedUser.id
            )

            //Assert and Act - retrieve the three added activities by user id
            val response = retrieveActivitiesByUserId(addedUser.id)
            assertEquals(200, response.status)
            val retrievedActivities = jsonNodeToObject<Array<Activity>>(response)
            assertEquals(3, retrievedActivities.size)

            //After - delete the added user and assert a 204 is returned (activities are cascade deleted)
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all activities by user id when no activities exist returns 404 response`() {
            //Arrange - add a user
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            //Assert and Act - retrieve the activities by user id
            val response = retrieveActivitiesByUserId(addedUser.id)
            assertEquals(404, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all activities by user id when no user exists returns 404 response`() {
            //Arrange
            val userId = -1

            //Assert and Act - retrieve activities by user id
            val response = retrieveActivitiesByUserId(userId)
            assertEquals(404, response.status)
        }

        @Test
        fun `get activity by activity id when no activity exists returns 404 response`() {
            //Arrange
            val activityId = -1
            //Assert and Act - attempt to retrieve the activity by activity id
            val response = retrieveActivityByActivityId(activityId)
            assertEquals(404, response.status)
        }


        @Test
        fun `get activity by activity id when activity exists returns 200 response`() {
            //Arrange - add a user and associated activity
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse = addActivity(
                activities[0].description,
                activities[0].duration, activities[0].calories,
                activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse.status)
            val addedActivity = jsonNodeToObject<Activity>(addActivityResponse)

            //Act & Assert - retrieve the activity by activity id
            val response = retrieveActivityByActivityId(addedActivity.id)
            assertEquals(200, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

    }

    @Nested
    inner class UpdateActivities {

        @Test
        fun `updating an activity by activity id when it doesn't exist, returns a 404 response`() {
            val userId = -1
            val activityID = -1

            //Arrange - check there is no user for -1 id
            assertEquals(404, retrieveUserById(userId).status)

            //Act & Assert - attempt to update the details of an activity/user that doesn't exist
            assertEquals(
                404, updateActivity(
                    activityID, updatedDescription, updatedDuration,
                    updatedCalories, updatedStarted, userId
                ).status
            )
        }

        @Test
        fun `updating an activity by activity id when it exists, returns 204 response`() {

            //Arrange - add a user and an associated activity that we plan to do an update on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse = addActivity(
                activities[0].description,
                activities[0].duration, activities[0].calories,
                activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse.status)
            val addedActivity = jsonNodeToObject<Activity>(addActivityResponse)

            //Act & Assert - update the added activity and assert a 204 is returned
            val updatedActivityResponse = updateActivity(
                addedActivity.id, updatedDescription,
                updatedDuration, updatedCalories, updatedStarted, addedUser.id
            )
            assertEquals(204, updatedActivityResponse.status)

            //Assert that the individual fields were all updated as expected
            val retrievedActivityResponse = retrieveActivityByActivityId(addedActivity.id)
            val updatedActivity = jsonNodeToObject<Activity>(retrievedActivityResponse)
            assertEquals(updatedDescription, updatedActivity.description)
            assertEquals(updatedDuration, updatedActivity.duration, 0.1)
            assertEquals(updatedCalories, updatedActivity.calories)
            assertEquals(updatedStarted, updatedActivity.started)

            //After - delete the user
            deleteUser(addedUser.id)
        }
    }

    @Nested
    inner class DeleteActivities {

        @Test
        fun `deleting an activity by activity id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteActivityByActivityId(-1).status)
        }

        @Test
        fun `deleting activities by user id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteActivitiesByUserId(-1).status)
        }

        @Test
        fun `deleting an activity by id when it exists, returns a 204 response`() {

            //Arrange - add a user and an associated activity that we plan to do a delete on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse.status)

            //Act & Assert - delete the added activity and assert a 204 is returned
            val addedActivity = jsonNodeToObject<Activity>(addActivityResponse)
            assertEquals(204, deleteActivityByActivityId(addedActivity.id).status)

            //After - delete the user
            deleteUser(addedUser.id)
        }

        @Test
        fun `deleting all activities by userid when it exists, returns a 204 response`() {

            //Arrange - add a user and 3 associated activities that we plan to do a cascade delete
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse1 = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse1.status)
            val addActivityResponse2 = addActivity(
                activities[1].description, activities[1].duration,
                activities[1].calories, activities[1].started, addedUser.id
            )
            assertEquals(201, addActivityResponse2.status)
            val addActivityResponse3 = addActivity(
                activities[2].description, activities[2].duration,
                activities[2].calories, activities[2].started, addedUser.id
            )
            assertEquals(201, addActivityResponse3.status)

            //Act & Assert - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)

            //Act & Assert - attempt to retrieve the deleted activities
            val addedActivity1 = jsonNodeToObject<Activity>(addActivityResponse1)
            val addedActivity2 = jsonNodeToObject<Activity>(addActivityResponse2)
            val addedActivity3 = jsonNodeToObject<Activity>(addActivityResponse3)
            assertEquals(404, retrieveActivityByActivityId(addedActivity1.id).status)
            assertEquals(404, retrieveActivityByActivityId(addedActivity2.id).status)
            assertEquals(404, retrieveActivityByActivityId(addedActivity3.id).status)
        }
    }


    ////////////////////////////////////////////////// BodyMeasurement Tests /////////////////////////////////
    @Nested
    inner class CreateBodyMeasurements {

        @Test
        fun `add an bodyMeasurement when a user exists for it, returns a 201 response`() {

            //Arrange - add a user and an associated bodyMeasurement that we plan to do a delete on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addBodyMeasurementResponse = addBodyMeasurement(
                bodyMeasurements[0].weight, bodyMeasurements[0].height,
                bodyMeasurements[0].waist, bodyMeasurements[0].chest, addedUser.id
            )
            assertEquals(201, addBodyMeasurementResponse.status)

            //After - delete the user (BodyMeasurement will cascade delete in the database)
            deleteUser(addedUser.id)
        }

        @Test
        fun `add an bodyMeasurement when no user exists for it, returns a 404 response`() {

            //Arrange - check there is no user for -1 id
            val userId = -1
            assertEquals(404, retrieveUserById(userId).status)

            val addBodyMeasurementResponse = addBodyMeasurement(
                bodyMeasurements.get(0).weight, bodyMeasurements.get(0).height,
                bodyMeasurements.get(0).waist, bodyMeasurements.get(0).chest, userId
            )
            assertEquals(404, addBodyMeasurementResponse.status)
        }
    }

    @Nested
    inner class ReadBodyMeasurements {

        @Test
        fun `get all bodyMeasurements from the database returns 200 or 404 response`() {
            val response = retrieveAllBodyMeasurements()
            if (response.status == 200) {
                val retrievedBodyMeasurements = jsonNodeToObject<Array<BodyMeasurement>>(response)
                assertNotEquals(0, retrievedBodyMeasurements.size)
            } else {
                assertEquals(404, response.status)
            }
        }

        @Test
        fun `get all bodyMeasurements by user id when user and bodyMeasurements exists returns 200 response`() {
            //Arrange - add a user and 3 associated bodyMeasurements that we plan to retrieve
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            addBodyMeasurement(
                bodyMeasurements[0].weight, bodyMeasurements[0].height,
                bodyMeasurements[0].waist, bodyMeasurements[0].chest, addedUser.id
            )
            addBodyMeasurement(
                bodyMeasurements[1].weight, bodyMeasurements[1].height,
                bodyMeasurements[1].waist, bodyMeasurements[1].chest, addedUser.id
            )
            addBodyMeasurement(
                bodyMeasurements[2].weight, bodyMeasurements[2].height,
                bodyMeasurements[2].waist, bodyMeasurements[2].chest, addedUser.id
            )

            //Assert and Act - retrieve the three added bodyMeasurements by user id
            val response = retrieveBodyMeasurementsByUserId(addedUser.id)
            assertEquals(200, response.status)
            val retrievedBodyMeasurements = jsonNodeToObject<Array<BodyMeasurement>>(response)
            assertEquals(3, retrievedBodyMeasurements.size)

            //After - delete the added user and assert a 204 is returned (bodyMeasurements are cascade deleted)
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all bodyMeasurements by user id when no bodyMeasurements exist returns 404 response`() {
            //Arrange - add a user
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            //Assert and Act - retrieve the bodyMeasurements by user id
            val response = retrieveBodyMeasurementsByUserId(addedUser.id)
            assertEquals(404, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all bodyMeasurements by user id when no user exists returns 404 response`() {
            //Arrange
            val userId = -1

            //Assert and Act - retrieve bodyMeasurements by user id
            val response = retrieveBodyMeasurementsByUserId(userId)
            assertEquals(404, response.status)
        }

        @Test
        fun `get bodyMeasurement by bodyMeasurement id when no bodyMeasurement exists returns 404 response`() {
            //Arrange
            val bodyMeasurementId = -1
            //Assert and Act - attempt to retrieve the bodyMeasurement by bodyMeasurement id
            val response = retrieveBodyMeasurementByBodyMeasurementId(bodyMeasurementId)
            assertEquals(404, response.status)
        }


        @Test
        fun `get bodyMeasurement by bodyMeasurement id when bodyMeasurement exists returns 200 response`() {
            //Arrange - add a user and associated bodyMeasurement
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addBodyMeasurementResponse = addBodyMeasurement(
                bodyMeasurements[0].weight,
                bodyMeasurements[0].height, bodyMeasurements[0].waist,
                bodyMeasurements[0].chest, addedUser.id
            )
            assertEquals(201, addBodyMeasurementResponse.status)
            val addedBodyMeasurement = jsonNodeToObject<BodyMeasurement>(addBodyMeasurementResponse)

            //Act & Assert - retrieve the bodyMeasurement by bodyMeasurement id
            val response = retrieveBodyMeasurementByBodyMeasurementId(addedBodyMeasurement.id)
            assertEquals(200, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

    }

    @Nested
    inner class UpdateBodyMeasurements {

        @Test
        fun `updating an bodyMeasurement by bodyMeasurement id when it doesn't exist, returns a 404 response`() {
            val userId = -1
            val bodyMeasurementID = -1

            //Arrange - check there is no user for -1 id
            assertEquals(404, retrieveUserById(userId).status)

            //Act & Assert - attempt to update the details of an bodyMeasurement/user that doesn't exist
            assertEquals(
                404, updateBodyMeasurement(
                    bodyMeasurementID, updatedWeight, updatedHeight,
                    updatedWaist, updatedChest, userId
                ).status
            )
        }

        @Test
        fun `updating an bodyMeasurement by bodyMeasurement id when it exists, returns 204 response`() {

            //Arrange - add a user and an associated bodyMeasurement that we plan to do an update on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addBodyMeasurementResponse = addBodyMeasurement(
                bodyMeasurements[0].weight,
                bodyMeasurements[0].height, bodyMeasurements[0].waist,
                bodyMeasurements[0].chest, addedUser.id
            )
            assertEquals(201, addBodyMeasurementResponse.status)
            val addedBodyMeasurement = jsonNodeToObject<BodyMeasurement>(addBodyMeasurementResponse)

            //Act & Assert - update the added bodyMeasurement and assert a 204 is returned
            val updatedBodyMeasurementResponse = updateBodyMeasurement(
                addedBodyMeasurement.id, updatedWeight,
                updatedHeight, updatedWaist, updatedChest, addedUser.id
            )
            assertEquals(204, updatedBodyMeasurementResponse.status)

            //Assert that the individual fields were all updated as expected
            val retrievedBodyMeasurementResponse = retrieveBodyMeasurementByBodyMeasurementId(addedBodyMeasurement.id)
            val updatedBodyMeasurement = jsonNodeToObject<BodyMeasurement>(retrievedBodyMeasurementResponse)
            assertEquals(updatedWeight, updatedBodyMeasurement.weight)
            assertEquals(updatedHeight, updatedBodyMeasurement.height, 0.1)
            assertEquals(updatedWaist, updatedBodyMeasurement.waist)
            assertEquals(updatedChest, updatedBodyMeasurement.chest)

            //After - delete the user
            deleteUser(addedUser.id)
        }
    }

    @Nested
    inner class DeleteBodyMeasurements {

        @Test
        fun `deleting an bodyMeasurement by bodyMeasurement id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteBodyMeasurementByBodyMeasurementId(-1).status)
        }

        @Test
        fun `deleting bodyMeasurements by user id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteBodyMeasurementsByUserId(-1).status)
        }

        @Test
        fun `deleting an bodyMeasurement by id when it exists, returns a 204 response`() {

            //Arrange - add a user and an associated bodyMeasurement that we plan to do a delete on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addBodyMeasurementResponse = addBodyMeasurement(
                bodyMeasurements[0].weight, bodyMeasurements[0].height,
                bodyMeasurements[0].waist, bodyMeasurements[0].chest, addedUser.id
            )
            assertEquals(201, addBodyMeasurementResponse.status)

            //Act & Assert - delete the added bodyMeasurement and assert a 204 is returned
            val addedBodyMeasurement = jsonNodeToObject<BodyMeasurement>(addBodyMeasurementResponse)
            assertEquals(204, deleteBodyMeasurementByBodyMeasurementId(addedBodyMeasurement.id).status)

            //After - delete the user
            deleteUser(addedUser.id)
        }

        @Test
        fun `deleting all bodyMeasurements by userid when it exists, returns a 204 response`() {

            //Arrange - add a user and 3 associated bodyMeasurements that we plan to do a cascade delete
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addBodyMeasurementResponse1 = addBodyMeasurement(
                bodyMeasurements[0].weight, bodyMeasurements[0].height,
                bodyMeasurements[0].waist, bodyMeasurements[0].chest, addedUser.id
            )
            assertEquals(201, addBodyMeasurementResponse1.status)
            val addBodyMeasurementResponse2 = addBodyMeasurement(
                bodyMeasurements[1].weight, bodyMeasurements[1].height,
                bodyMeasurements[1].waist, bodyMeasurements[1].chest, addedUser.id
            )
            assertEquals(201, addBodyMeasurementResponse2.status)
            val addBodyMeasurementResponse3 = addBodyMeasurement(
                bodyMeasurements[2].weight, bodyMeasurements[2].height,
                bodyMeasurements[2].waist, bodyMeasurements[2].chest, addedUser.id
            )
            assertEquals(201, addBodyMeasurementResponse3.status)

            //Act & Assert - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)

            //Act & Assert - attempt to retrieve the deleted bodyMeasurements
            val addedBodyMeasurement1 = jsonNodeToObject<BodyMeasurement>(addBodyMeasurementResponse1)
            val addedBodyMeasurement2 = jsonNodeToObject<BodyMeasurement>(addBodyMeasurementResponse2)
            val addedBodyMeasurement3 = jsonNodeToObject<BodyMeasurement>(addBodyMeasurementResponse3)
            assertEquals(404, retrieveBodyMeasurementByBodyMeasurementId(addedBodyMeasurement1.id).status)
            assertEquals(404, retrieveBodyMeasurementByBodyMeasurementId(addedBodyMeasurement2.id).status)
            assertEquals(404, retrieveBodyMeasurementByBodyMeasurementId(addedBodyMeasurement3.id).status)
        }
    }

////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////// Calorie Tests /////////////////////////////////
@Nested
inner class CreateCalories {

    @Test
    fun `add a calorie when a user exists for it, returns a 201 response`() {

        //Arrange - add a user and an associated calorie that we plan to do a delete on
        val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

        val addCalorieResponse = addCalorie(
            calories[0].breakfast, calories[0].lunch,
            calories[0].dinner, calories[0].snack, addedUser.id
        )
        assertEquals(201, addCalorieResponse.status)

        //After - delete the user (Calorie will cascade delete in the database)
        deleteUser(addedUser.id)
    }

    @Test
    fun `add a calorie when no user exists for it, returns a 404 response`() {

        //Arrange - check there is no user for -1 id
        val userId = -1
        assertEquals(404, retrieveUserById(userId).status)

        val addCalorieResponse = addCalorie(
            calories.get(0).breakfast, calories.get(0).lunch,
            calories.get(0).dinner, calories.get(0).snack, userId
        )
        assertEquals(404, addCalorieResponse.status)
    }
}

    @Nested
    inner class ReadCalories {

        @Test
        fun `get all calories from the database returns 200 or 404 response`() {
            val response = retrieveAllCalories()
            if (response.status == 200) {
                val retrievedCalories = jsonNodeToObject<Array<Calorie>>(response)
                assertNotEquals(0, retrievedCalories.size)
            } else {
                assertEquals(404, response.status)
            }
        }

        @Test
        fun `get all calories by user id when user and calories exists returns 200 response`() {
            //Arrange - add a user and 3 associated calories that we plan to retrieve
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            addCalorie(
                calories[0].breakfast, calories[0].lunch,
                calories[0].dinner, calories[0].snack, addedUser.id
            )
            addCalorie(
                calories[1].breakfast, calories[1].lunch,
                calories[1].dinner, calories[1].snack, addedUser.id
            )
            addCalorie(
                calories[2].breakfast, calories[2].lunch,
                calories[2].dinner, calories[2].snack, addedUser.id
            )

            //Assert and Act - retrieve the three added calories by user id
            val response = retrieveCaloriesByUserId(addedUser.id)
            assertEquals(200, response.status)
            val retrievedCalories = jsonNodeToObject<Array<Calorie>>(response)
            assertEquals(3, retrievedCalories.size)

            //After - delete the added user and assert a 204 is returned (calories are cascade deleted)
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all calories by user id when no calories exist returns 404 response`() {
            //Arrange - add a user
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            //Assert and Act - retrieve the calories by user id
            val response = retrieveCaloriesByUserId(addedUser.id)
            assertEquals(404, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all calories by user id when no user exists returns 404 response`() {
            //Arrange
            val userId = -1

            //Assert and Act - retrieve calories by user id
            val response = retrieveCaloriesByUserId(userId)
            assertEquals(404, response.status)
        }

        @Test
        fun `get calorie by calorie id when no calorie exists returns 404 response`() {
            //Arrange
            val calorieId = -1
            //Assert and Act - attempt to retrieve the calorie by calorie id
            val response = retrieveCalorieByCalorieId(calorieId)
            assertEquals(404, response.status)
        }


        @Test
        fun `get calorie by calorie id when calorie exists returns 200 response`() {
            //Arrange - add a user and associated calorie
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addCalorieResponse = addCalorie(
                calories[0].breakfast,
                calories[0].lunch, calories[0].dinner,
                calories[0].snack, addedUser.id
            )
            assertEquals(201, addCalorieResponse.status)
            val addedCalorie = jsonNodeToObject<Calorie>(addCalorieResponse)

            //Act & Assert - retrieve the calorie by calorie id
            val response = retrieveCalorieByCalorieId(addedCalorie.id)
            assertEquals(200, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

    }

    @Nested
    inner class UpdateCalories {

        @Test
        fun `updating a calorie by calorie id when it doesn't exist, returns a 404 response`() {
            val userId = -1
            val calorieID = -1

            //Arrange - check there is no user for -1 id
            assertEquals(404, retrieveUserById(userId).status)

            //Act & Assert - attempt to update the details of a calorie/user that doesn't exist
            assertEquals(
                404, updateCalorie(
                    calorieID, updatedBreakfast, updatedLunch,
                    updatedDinner, updatedSnack, userId
                ).status
            )
        }

        @Test
        fun `updating a calorie by calorie id when it exists, returns 204 response`() {

            //Arrange - add a user and an associated calorie that we plan to do an update on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addCalorieResponse = addCalorie(
                calories[0].breakfast,
                calories[0].lunch, calories[0].dinner,
                calories[0].snack, addedUser.id
            )
            assertEquals(201, addCalorieResponse.status)
            val addedCalorie = jsonNodeToObject<Calorie>(addCalorieResponse)

            //Act & Assert - update the added calorie and assert a 204 is returned
            val updatedCalorieResponse = updateCalorie(
                addedCalorie.id, updatedBreakfast,
                updatedLunch, updatedDinner, updatedSnack, addedUser.id
            )
            assertEquals(204, updatedCalorieResponse.status)

            //Assert that the individual fields were all updated as expected
            val retrievedCalorieResponse = retrieveCalorieByCalorieId(addedCalorie.id)
            val updatedCalorie = jsonNodeToObject<Calorie>(retrievedCalorieResponse)
            assertEquals(updatedBreakfast, updatedCalorie.breakfast)
            assertEquals(updatedLunch, updatedCalorie.lunch, 0.1)
            assertEquals(updatedDinner, updatedCalorie.dinner)
            assertEquals(updatedSnack, updatedCalorie.snack)

            //After - delete the user
            deleteUser(addedUser.id)
        }
    }

    @Nested
    inner class DeleteCalories {

        @Test
        fun `deleting a calorie by calorie id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteCalorieByCalorieId(-1).status)
        }

        @Test
        fun `deleting calories by user id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteCaloriesByUserId(-1).status)
        }

        @Test
        fun `deleting a calorie by id when it exists, returns a 204 response`() {

            //Arrange - add a user and an associated calorie that we plan to do a delete on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addCalorieResponse = addCalorie(
                calories[0].breakfast, calories[0].lunch,
                calories[0].dinner, calories[0].snack, addedUser.id
            )
            assertEquals(201, addCalorieResponse.status)

            //Act & Assert - delete the added calorie and assert a 204 is returned
            val addedCalorie = jsonNodeToObject<Calorie>(addCalorieResponse)
            assertEquals(204, deleteCalorieByCalorieId(addedCalorie.id).status)

            //After - delete the user
            deleteUser(addedUser.id)
        }

        @Test
        fun `deleting all calories by userid when it exists, returns a 204 response`() {

            //Arrange - add a user and 3 associated calories that we plan to do a cascade delete
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addCalorieResponse1 = addCalorie(
                calories[0].breakfast, calories[0].lunch,
                calories[0].dinner, calories[0].snack, addedUser.id
            )
            assertEquals(201, addCalorieResponse1.status)
            val addCalorieResponse2 = addCalorie(
                calories[1].breakfast, calories[1].lunch,
                calories[1].dinner, calories[1].snack, addedUser.id
            )
            assertEquals(201, addCalorieResponse2.status)
            val addCalorieResponse3 = addCalorie(
                calories[2].breakfast, calories[2].lunch,
                calories[2].dinner, calories[2].snack, addedUser.id
            )
            assertEquals(201, addCalorieResponse3.status)

            //Act & Assert - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)

            //Act & Assert - attempt to retrieve the deleted calories
            val addedCalorie1 = jsonNodeToObject<Calorie>(addCalorieResponse1)
            val addedCalorie2 = jsonNodeToObject<Calorie>(addCalorieResponse2)
            val addedCalorie3 = jsonNodeToObject<Calorie>(addCalorieResponse3)
            assertEquals(404, retrieveCalorieByCalorieId(addedCalorie1.id).status)
            assertEquals(404, retrieveCalorieByCalorieId(addedCalorie2.id).status)
            assertEquals(404, retrieveCalorieByCalorieId(addedCalorie3.id).status)
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////// Workout Tests /////////////////////////////////
@Nested
inner class CreateWorkouts {

    @Test
    fun `add a workout when a user exists for it, returns a 201 response`() {

        //Arrange - add a user and an associated workout that we plan to do a delete on
        val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

        val addWorkoutResponse = addWorkout(
            workouts[0].description, workouts[0].duration,
            workouts[0].numbers, addedUser.id
        )
        assertEquals(201, addWorkoutResponse.status)

        //After - delete the user (Workout will cascade delete in the database)
        deleteUser(addedUser.id)
    }

    @Test
    fun `add a workout when no user exists for it, returns a 404 response`() {

        //Arrange - check there is no user for -1 id
        val userId = -1
        assertEquals(404, retrieveUserById(userId).status)

        val addWorkoutResponse = addWorkout(
            workouts.get(0).description, workouts.get(0).duration,
            workouts.get(0).numbers, userId
        )
        assertEquals(404, addWorkoutResponse.status)
    }
}

    @Nested
    inner class ReadWorkouts {

        @Test
        fun `get all workouts from the database returns 200 or 404 response`() {
            val response = retrieveAllWorkouts()
            if (response.status == 200) {
                val retrievedWorkouts = jsonNodeToObject<Array<Workout>>(response)
                assertNotEquals(0, retrievedWorkouts.size)
            } else {
                assertEquals(404, response.status)
            }
        }

        @Test
        fun `get all workouts by user id when user and workouts exists returns 200 response`() {
            //Arrange - add a user and 3 associated workouts that we plan to retrieve
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            addWorkout(
                workouts[0].description, workouts[0].duration,
                workouts[0].numbers, addedUser.id
            )
            addWorkout(
                workouts[1].description, workouts[1].duration,
                workouts[1].numbers, addedUser.id
            )
            addWorkout(
                workouts[2].description, workouts[2].duration,
                workouts[2].numbers, addedUser.id
            )

            //Assert and Act - retrieve the three added workouts by user id
            val response = retrieveWorkoutsByUserId(addedUser.id)
            assertEquals(200, response.status)
            val retrievedWorkouts = jsonNodeToObject<Array<Workout>>(response)
            assertEquals(3, retrievedWorkouts.size)

            //After - delete the added user and assert a 204 is returned (workouts are cascade deleted)
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all workouts by user id when no workouts exist returns 404 response`() {
            //Arrange - add a user
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            //Assert and Act - retrieve the workouts by user id
            val response = retrieveWorkoutsByUserId(addedUser.id)
            assertEquals(404, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all workouts by user id when no user exists returns 404 response`() {
            //Arrange
            val userId = -1

            //Assert and Act - retrieve workouts by user id
            val response = retrieveWorkoutsByUserId(userId)
            assertEquals(404, response.status)
        }

        @Test
        fun `get workout by workout id when no workout exists returns 404 response`() {
            //Arrange
            val workoutId = -1
            //Assert and Act - attempt to retrieve the workout by workout id
            val response = retrieveWorkoutByWorkoutId(workoutId)
            assertEquals(404, response.status)
        }


        @Test
        fun `get workout by workout id when workout exists returns 200 response`() {
            //Arrange - add a user and associated workout
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addWorkoutResponse = addWorkout(
                workouts[0].description,
                workouts[0].duration, workouts[0].numbers,
                addedUser.id
            )
            assertEquals(201, addWorkoutResponse.status)
            val addedWorkout = jsonNodeToObject<Workout>(addWorkoutResponse)

            //Act & Assert - retrieve the workout by workout id
            val response = retrieveWorkoutByWorkoutId(addedWorkout.id)
            assertEquals(200, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

    }

    @Nested
    inner class UpdateWorkouts {

        @Test
        fun `updating a workout by workout id when it doesn't exist, returns a 404 response`() {
            val userId = -1
            val workoutID = -1

            //Arrange - check there is no user for -1 id
            assertEquals(404, retrieveUserById(userId).status)

            //Act & Assert - attempt to update the details of a workout/user that doesn't exist
            assertEquals(
                404, updateWorkout(
                    workoutID, updatedDescriptionWorkout, updatedDurationWorkout,
                    updatedNumbers,   userId
                ).status
            )
        }

        @Test
        fun `updating a workout by workout id when it exists, returns 204 response`() {

            //Arrange - add a user and an associated workout that we plan to do an update on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addWorkoutResponse = addWorkout(
                workouts[0].description,
                workouts[0].duration, workouts[0].numbers,
                addedUser.id
            )
            assertEquals(201, addWorkoutResponse.status)
            val addedWorkout = jsonNodeToObject<Workout>(addWorkoutResponse)

            //Act & Assert - update the added workout and assert a 204 is returned
            val updatedWorkoutResponse = updateWorkout(
                addedWorkout.id, updatedDescriptionWorkout,
                updatedDurationWorkout, updatedNumbers,   addedUser.id
            )
            assertEquals(204, updatedWorkoutResponse.status)

            //Assert that the individual fields were all updated as expected
            val retrievedWorkoutResponse = retrieveWorkoutByWorkoutId(addedWorkout.id)
            val updatedWorkout = jsonNodeToObject<Workout>(retrievedWorkoutResponse)
            assertEquals(updatedDescriptionWorkout, updatedWorkout.description)
            assertEquals(updatedDurationWorkout, updatedWorkout.duration, 0.1)
            assertEquals(updatedNumbers, updatedWorkout.numbers)


            //After - delete the user
            deleteUser(addedUser.id)
        }
    }

    @Nested
    inner class DeleteWorkouts {

        @Test
        fun `deleting a workout by workout id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteWorkoutByWorkoutId(-1).status)
        }

        @Test
        fun `deleting workouts by user id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteWorkoutsByUserId(-1).status)
        }

        @Test
        fun `deleting a workout by id when it exists, returns a 204 response`() {

            //Arrange - add a user and an associated workout that we plan to do a delete on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addWorkoutResponse = addWorkout(
                workouts[0].description, workouts[0].duration,
                workouts[0].numbers, addedUser.id
            )
            assertEquals(201, addWorkoutResponse.status)

            //Act & Assert - delete the added workout and assert a 204 is returned
            val addedWorkout = jsonNodeToObject<Workout>(addWorkoutResponse)
            assertEquals(204, deleteWorkoutByWorkoutId(addedWorkout.id).status)

            //After - delete the user
            deleteUser(addedUser.id)
        }

        @Test
        fun `deleting all workouts by userid when it exists, returns a 204 response`() {

            //Arrange - add a user and 3 associated workouts that we plan to do a cascade delete
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addWorkoutResponse1 = addWorkout(
                workouts[0].description, workouts[0].duration,
                workouts[0].numbers, addedUser.id
            )
            assertEquals(201, addWorkoutResponse1.status)
            val addWorkoutResponse2 = addWorkout(
                workouts[1].description, workouts[1].duration,
                workouts[1].numbers, addedUser.id
            )
            assertEquals(201, addWorkoutResponse2.status)
            val addWorkoutResponse3 = addWorkout(
                workouts[2].description, workouts[2].duration,
                workouts[2].numbers, addedUser.id
            )
            assertEquals(201, addWorkoutResponse3.status)

            //Act & Assert - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)

            //Act & Assert - attempt to retrieve the deleted workouts
            val addedWorkout1 = jsonNodeToObject<Workout>(addWorkoutResponse1)
            val addedWorkout2 = jsonNodeToObject<Workout>(addWorkoutResponse2)
            val addedWorkout3 = jsonNodeToObject<Workout>(addWorkoutResponse3)
            assertEquals(404, retrieveWorkoutByWorkoutId(addedWorkout1.id).status)
            assertEquals(404, retrieveWorkoutByWorkoutId(addedWorkout2.id).status)
            assertEquals(404, retrieveWorkoutByWorkoutId(addedWorkout3.id).status)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////// Helper Functions//////////////////////////

    //As we will be adding a user in future tests too, it makes sense to create a helper function.
//helper function to add a test user to the database
    private fun addUser(name: String, email: String): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/users")
            .body("{\"name\":\"$name\", \"email\":\"$email\"}")
            .asJson()
    }

    //helper function to delete a test user from the database
    private fun deleteUser(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id").asString()
    }

    //helper function to retrieve a test user from the database by email
    private fun retrieveUserByEmail(email: String): HttpResponse<String> {
        return Unirest.get(origin + "/api/users/email/${email}").asString()
    }

    //helper function to retrieve a test user from the database by id
    private fun retrieveUserById(id: Int): HttpResponse<String> {
        return Unirest.get(origin + "/api/users/${id}").asString()
    }

    //helper function to add a test user to the database
    private fun updateUser(id: Int, name: String, email: String): HttpResponse<JsonNode> {
        return Unirest.patch(origin + "/api/users/$id")
            .body("{\"name\":\"$name\", \"email\":\"$email\"}")
            .asJson()
    }

    //helper function to retrieve all activities
    private fun retrieveAllActivities(): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/activities").asJson()
    }

    //helper function to retrieve activities by user id
    private fun retrieveActivitiesByUserId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/users/${id}/activities").asJson()
    }

    //helper function to retrieve activity by activity id
    private fun retrieveActivityByActivityId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/activities/${id}").asJson()
    }

    //helper function to delete an activity by activity id
    private fun deleteActivityByActivityId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/activities/$id").asString()
    }

    //helper function to delete an activity by activity id
    private fun deleteActivitiesByUserId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id/activities").asString()
    }

    //helper function to add a test user to the database
    private fun updateActivity(
        id: Int, description: String, duration: Double, calories: Int,
        started: DateTime, userId: Int
    ): HttpResponse<JsonNode> {
        return Unirest.patch(origin + "/api/activities/$id")
            .body(
                """
                {
                  "description":"$description",
                  "duration":$duration,
                  "calories":$calories,
                  "started":"$started",
                  "userId":$userId
                }
            """.trimIndent()
            ).asJson()
    }

    //helper function to add an activity
    private fun addActivity(
        description: String, duration: Double, calories: Int,
        started: DateTime, userId: Int
    ): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/activities")
            .body(
                """
                {
                   "description":"$description",
                   "duration":$duration,
                   "calories":$calories,
                   "started":"$started",
                   "userId":$userId
                }
            """.trimIndent()
            )
            .asJson()
    }


    ///////////////////////////////////////////////////// helper bodyMeasurements///////////////////
//helper function to retrieve all bodyMeasurements
    private fun retrieveAllBodyMeasurements(): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/bodyMeasurements").asJson()
    }

    //helper function to retrieve bodyMeasurements by user id
    private fun retrieveBodyMeasurementsByUserId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/users/${id}/bodyMeasurements").asJson()
    }

    //helper function to retrieve bodyMeasurement by bodyMeasurement id
    private fun retrieveBodyMeasurementByBodyMeasurementId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/bodyMeasurements/${id}").asJson()
    }

    //helper function to delete an bodyMeasurement by bodyMeasurement id
    private fun deleteBodyMeasurementByBodyMeasurementId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/bodyMeasurements/$id").asString()
    }

    //helper function to delete an bodyMeasurement by bodyMeasurement id
    private fun deleteBodyMeasurementsByUserId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id/bodyMeasurements").asString()
    }

    //helper function to add a test user to the database
    private fun updateBodyMeasurement(
        id: Int, weight: Double, height: Double, waist: Double,
        chest: Double, userId: Int
    ): HttpResponse<JsonNode> {
        return Unirest.patch(origin + "/api/bodyMeasurements/$id")
            .body(
                """
                {
                  "weight":$weight,
                  "height":$height,
                  "waist":$waist,
                  "chest":$chest,
                  "userId":$userId
                }
            """.trimIndent()
            ).asJson()
    }

    //helper function to add an bodyMeasurement
    private fun addBodyMeasurement(
        weight: Double,
        height: Double,
        waist: Double,
        chest: Double,
        userId: Int
    ): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/bodyMeasurements")
            .body(
                """
                {
                  "weight":$weight,
                  "height":$height,
                  "waist":$waist,
                  "chest":$chest,
                  "userId":$userId
                }
            """.trimIndent()
            )
            .asJson()
    }




    ///////////////////////////////////////////////////// helper calories///////////////////
//helper function to retrieve all calories
    private fun retrieveAllCalories(): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/calories").asJson()
    }

    //helper function to retrieve calories by user id
    private fun retrieveCaloriesByUserId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/users/${id}/calories").asJson()
    }

    //helper function to retrieve calorie by calorie id
    private fun retrieveCalorieByCalorieId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/calories/${id}").asJson()
    }

    //helper function to delete an calorie by calorie id
    private fun deleteCalorieByCalorieId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/calories/$id").asString()
    }

    //helper function to delete an calorie by calorie id
    private fun deleteCaloriesByUserId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id/calories").asString()
    }

    //helper function to add a test user to the database
    private fun updateCalorie(
        id: Int, breakfast: Double, lunch: Double, dinner: Double,
        snack: Double, userId: Int
    ): HttpResponse<JsonNode> {
        return Unirest.patch(origin + "/api/calories/$id")
            .body(
                """
                {
                  "breakfast":$breakfast,
                  "lunch":$lunch,
                  "dinner":$dinner,
                  "snack":$snack,
                  "userId":$userId
                }
            """.trimIndent()
            ).asJson()
    }

    //helper function to add an calorie
    private fun addCalorie(
        breakfast: Double,
        lunch: Double,
        dinner: Double,
        snack: Double,
        userId: Int
    ): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/calories")
            .body(
                """
                {
                  "breakfast":$breakfast,
                  "lunch":$lunch,
                  "dinner":$dinner,
                  "snack":$snack,
                  "userId":$userId
                }
            """.trimIndent()
            )
            .asJson()
    }

    ///////////////////////////////////////////////////// helper workouts///////////////////
//helper function to retrieve all workouts
    private fun retrieveAllWorkouts(): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/workouts").asJson()
    }

    //helper function to retrieve workouts by user id
    private fun retrieveWorkoutsByUserId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/users/${id}/workouts").asJson()
    }

    //helper function to retrieve workout by workout id
    private fun retrieveWorkoutByWorkoutId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/workouts/${id}").asJson()
    }

    //helper function to delete a workout by workout id
    private fun deleteWorkoutByWorkoutId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/workouts/$id").asString()
    }

    //helper function to delete a workout by workout id
    private fun deleteWorkoutsByUserId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id/workouts").asString()
    }

    //helper function to add a test user to the database
    private fun updateWorkout(
        id: Int, description: String, duration: Double, numbers: Int,
        userId: Int
    ): HttpResponse<JsonNode> {
        return Unirest.patch(origin + "/api/workouts/$id")
            .body(
                """
            {
              "description":"$description",
              "duration":$duration,
              "numbers":$numbers,
              "userId":$userId
            }
        """.trimIndent()
            ).asJson()
    }

    //helper function to add a workout
    private fun addWorkout(
        description: String, duration: Double, numbers: Int,
        userId: Int
    ): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/workouts")
            .body(
                """
            {
               "description":"$description",
               "duration":$duration,
               "numbers":$numbers,
               "userId":$userId
            }
        """.trimIndent()
            )
            .asJson()
    }






}




