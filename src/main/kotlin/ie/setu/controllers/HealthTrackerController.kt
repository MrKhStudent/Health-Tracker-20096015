// The responsibility of this class is to manage the IO between the DAOs and the JSON
// context. Remember, the DAOs handle the collection of users, activities,...

package ie.setu.controllers

import ie.setu.domain.User
import ie.setu.domain.repository.UserDAO
import ie.setu.domain.Activity
import ie.setu.domain.repository.ActivityDAO
import ie.setu.domain.BodyMeasurement
import ie.setu.domain.repository.BodyMeasurementDAO
import ie.setu.domain.Calorie
import ie.setu.domain.repository.CalorieDAO
import ie.setu.domain.Workout
import ie.setu.domain.repository.WorkoutDAO
import io.javalin.http.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.javalin.plugin.openapi.annotations.*
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import ie.setu.utils.jsonToObject


object HealthTrackerController {
    private val userDao = UserDAO()
    private val activityDAO = ActivityDAO()
    private val bodyMeasurementDAO = BodyMeasurementDAO()
    private val calorieDAO = CalorieDAO()
    private val workoutDAO = WorkoutDAO()

    ///////////////////////////////////////////////////////////// getAllUsers
    // OpenApi describes your API
    @OpenApi(
        summary = "Get all users",
        operationId = "getAllUsers",
        tags = ["User"],
        path = "/api/users",
        method = HttpMethod.GET,
        responses = [OpenApiResponse("200", [OpenApiContent(Array<User>::class)])]
    )
    fun getAllUsers(ctx: Context){
        // Tests - Refactoring
        // Adding HTTP response codes to getAllUsers
        val users = userDao.getAll()
        if (users.size != 0){
            ctx.status(200)
        }
        else{
            ctx.status(404)
        }
        ctx.json(users)
    }

    ///////////////////////////////////////////////////////// getUserByUserId
    @OpenApi(
        summary = "Get user by ID",
        operationId = "getUserById",
        tags = ["User"],
        path = "/api/users/{user-id}",
        method = HttpMethod.GET,
        pathParams = [OpenApiParam("user-id", Int::class, "The user ID")],
        responses  = [OpenApiResponse("200", [OpenApiContent(User::class)])]
    )
    fun getUserByUserId(ctx: Context) {
        val user = userDao.findById(ctx.pathParam("user-id").toInt())
        if (user != null) {
            ctx.json(user)

            //We have set no HTTP response codes based on our interrogation with the database.
            ctx.status(200)
        }
        //We have set no HTTP response codes based on our interrogation with the database.
            else{
            ctx.status(404)
        }
    }

    /////////////////////////////////////////////////////////// getUserByEmail
    @OpenApi(
        summary = "Get user by Email",
        operationId = "getUserByEmail",
        tags = ["User"],
        path = "/api/users/email/{email}",
        method = HttpMethod.GET,
        pathParams = [OpenApiParam("email", Int::class, "The user email")],
        responses  = [OpenApiResponse("200", [OpenApiContent(User::class)])]
    )
    fun getUserByEmail(ctx: Context){
        val user = userDao.findByEmail(ctx.pathParam("email"))
        if (user != null){
            ctx.json(user)
            ctx.status(200)
        }
        else {
            ctx.status(404)
        }
    }

    /////////////////////////////////////////////////////////// addUser
    @OpenApi(
        summary = "Add User",
        operationId = "addUser",
        tags = ["User"],
        path = "/api/users",
        method = HttpMethod.POST,
        pathParams = [OpenApiParam("user-id", Int::class, "The user ID")],
        responses  = [OpenApiResponse("200")]
    )
    fun addUser(ctx: Context) {
        val user : User = jsonToObject(ctx.body())
        val userId = userDao.save(user)
        if (userId != null) {
            user.id = userId
            ctx.json(user)
            ctx.status(201)
        }
    }

    /////////////////////////////////////////////////////////// deleteUser
    @OpenApi(
        summary = "Delete user by ID",
        operationId = "deleteUserById",
        tags = ["User"],
        path = "/api/users/{user-id}",
        method = HttpMethod.DELETE,
        pathParams = [OpenApiParam("user-id", Int::class, "The user ID")],
        responses  = [OpenApiResponse("204")]
    )
    fun deleteUser(ctx: Context){
        if (userDao.delete(ctx.pathParam("user-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    /////////////////////////////////////////////////////////// updateUser
    @OpenApi(
        summary = "Update user by ID",
        operationId = "updateUserById",
        tags = ["User"],
        path = "/api/users/{user-id}",
        method = HttpMethod.PATCH,
        pathParams = [OpenApiParam("user-id", Int::class, "The user ID")],
        responses  = [OpenApiResponse("204")]
    )
    fun updateUser(ctx: Context){
        val foundUser : User = jsonToObject(ctx.body())
        if ((userDao.update(id = ctx.pathParam("user-id").toInt(), user=foundUser)) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

////////////////////////////////////////////// Activity ////////////////////////////////////////

    ////////////////////////////////////////////////////// getAllActivities
    /*fun getAllActivities(ctx: Context) {
        //mapper handles the deserialization of Joda date into a String.
        val mapper = jacksonObjectMapper()
            .registerModule(JodaModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        ctx.json(mapper.writeValueAsString( activityDAO.getAll() ))
    }*/

    /*@OpenApi(
        summary = "Get all activities",
        operationId = "getAllActivities",
        tags = ["Activity"],
        path = "/api/activities",
        method = HttpMethod.GET,
        responses = [OpenApiResponse("200", [OpenApiContent(Array<User>::class)])]
    )*/
    fun getAllActivities(ctx: Context) {
        val activities = activityDAO.getAll()
        if (activities.size != 0) {
            ctx.status(200)
        }
        else{
            ctx.status(404)
        }
        ctx.json(activities)
    }
    ////////////////////////////////////////////////////// getActivitiesByUserId

    /*@OpenApi(
        summary = "Get activity by ID",
        operationId = "getActivitiesById",
        tags = ["Activity"],
        path = "/api/activities/{user-id}",
        method = HttpMethod.GET,
        pathParams = [OpenApiParam("user-id", Int::class, "The activity ID")],
        responses  = [OpenApiResponse("200", [OpenApiContent(User::class)])]
    )*/

    fun getActivitiesByUserId(ctx: Context) {
        if (userDao.findById(ctx.pathParam("user-id").toInt()) != null) {
            val activities = activityDAO.findByUserId(ctx.pathParam("user-id").toInt())
            if (activities.isNotEmpty()) {
                ctx.json(activities)
                ctx.status(200)
            }
            else{
                ctx.status(404)
            }
        }
        else{
            ctx.status(404)
        }
    }
    ////////////////////////////////////////////////////// addActivity

    /*@OpenApi(
        summary = "Add Activity",
        operationId = "addActivity",
        tags = ["Activity"],
        path = "/api/activities",
        method = HttpMethod.POST,
        pathParams = [OpenApiParam("user-id", Int::class, "The activity ID")],
        responses  = [OpenApiResponse("200")]
    )*/

    fun addActivity(ctx: Context) {
        val activity : Activity = jsonToObject(ctx.body())
        val userId = userDao.findById(activity.userId)
        if (userId != null) {
            val activityId = activityDAO.save(activity)
            activity.id = activityId
            ctx.json(activity)
            ctx.status(201)
        }
        else{
            ctx.status(404)
        }
    }

    /////////////////////////////////////////// getActivitiesByActivityId


    fun getActivitiesByActivityId(ctx: Context) {
        val activity = activityDAO.findByActivityId((ctx.pathParam("activity-id").toInt()))
        if (activity != null){
            ctx.json(activity)
            ctx.status(200)
        }
        else{
            ctx.status(404)
        }
    }
    /////////////////////////////////////////// deleteActivityByActivityId
    fun deleteActivityByActivityId(ctx: Context){
        if (activityDAO.deleteByActivityId(ctx.pathParam("activity-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    /////////////////////////////////////////// deleteActivityByUserId
    fun deleteActivityByUserId(ctx: Context){
        if (activityDAO.deleteByUserId(ctx.pathParam("user-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    /////////////////////////////////////////// updateActivity
    fun updateActivity(ctx: Context){
        val activity : Activity = jsonToObject(ctx.body())
        if (activityDAO.updateByActivityId(
                activityId = ctx.pathParam("activity-id").toInt(),
                activityToUpdate =activity) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

////////////////////////////////////////////// BodyMeasurement ///////////////////////////////
////////////////////////////////////////////////////// getAllBodyMeasurements
    fun getAllBodyMeasurements(ctx: Context) {
        val bodyMeasurements = bodyMeasurementDAO.getAll()
        if (bodyMeasurements.size != 0) {
            ctx.status(200)
        }
        else{
            ctx.status(404)
        }
        ctx.json(bodyMeasurements)
    }

    ////////////////////////////////////////////////////// getBodyMeasurementsByUserId
    fun getBodyMeasurementsByUserId(ctx: Context) {
        if (userDao.findById(ctx.pathParam("user-id").toInt()) != null) {
            val bodyMeasurements = bodyMeasurementDAO.findByUserId(ctx.pathParam("user-id").toInt())
            if (bodyMeasurements.isNotEmpty()) {
                ctx.json(bodyMeasurements)
                ctx.status(200)
            }
            else{
                ctx.status(404)
            }
        }
        else{
            ctx.status(404)
        }
    }

    ////////////////////////////////////////////////////// addBodyMeasurement
    fun addBodyMeasurement(ctx: Context) {
        val bodyMeasurement : BodyMeasurement = jsonToObject(ctx.body())
        val userId = userDao.findById(bodyMeasurement.userId)
        if (userId != null) {
            val bodyMeasurementId = bodyMeasurementDAO.save(bodyMeasurement)
            bodyMeasurement.id = bodyMeasurementId
            ctx.json(bodyMeasurement)
            ctx.status(201)
        }
        else{
            ctx.status(404)
        }
    }


    /////////////////////////////////////////// getBodyMeasurementsByBodyMeasurementId
    fun getBodyMeasurementsByBodyMeasurementId(ctx: Context) {
        val bodyMeasurement = bodyMeasurementDAO.findByBodyMeasurementId((ctx.pathParam("bodyMeasurement-id").toInt()))
        if (bodyMeasurement != null){
            ctx.json(bodyMeasurement)
            ctx.status(200)
        }
        else{
            ctx.status(404)
        }
    }

    /////////////////////////////////////////// deleteBodyMeasurementByBodyMeasurementId
    fun deleteBodyMeasurementByBodyMeasurementId(ctx: Context){
        if (bodyMeasurementDAO.deleteByBodyMeasurementId(ctx.pathParam("bodyMeasurement-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    /////////////////////////////////////////// deleteBodyMeasurementByUserId
    fun deleteBodyMeasurementByUserId(ctx: Context){
        if (bodyMeasurementDAO.deleteByUserId(ctx.pathParam("user-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    /////////////////////////////////////////// updateBodyMeasurement
    fun updateBodyMeasurement(ctx: Context){
        val bodyMeasurement : BodyMeasurement = jsonToObject(ctx.body())
        if (bodyMeasurementDAO.updateByBodyMeasurementId(
                bodyMeasurementId = ctx.pathParam("bodyMeasurement-id").toInt(),
                bodyMeasurementToUpdate =bodyMeasurement) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }


//////////////////////////////////////////// Calorie ///////////////////////////////////////////
////////////////////////////////////////////////////// getAllCalories
    fun getAllCalories(ctx: Context) {
        val calories = calorieDAO.getAll()
        if (calories.size != 0) {
            ctx.status(200)
        }
        else{
            ctx.status(404)
        }
        ctx.json(calories)
    }
    ////////////////////////////////////////////////////// getCaloriesByUserId
    fun getCaloriesByUserId(ctx: Context) {
        if (userDao.findById(ctx.pathParam("user-id").toInt()) != null) {
            val calories = calorieDAO.findByUserId(ctx.pathParam("user-id").toInt())
            if (calories.isNotEmpty()) {
                ctx.json(calories)
                ctx.status(200)
            }
            else{
                ctx.status(404)
            }
        }
        else{
            ctx.status(404)
        }
    }
    ////////////////////////////////////////////////////// addCalorie
    fun addCalorie(ctx: Context) {
        val calorie : Calorie = jsonToObject(ctx.body())
        val userId = userDao.findById(calorie.userId)
        if (userId != null) {
            val calorieId = calorieDAO.save(calorie)
            calorie.id = calorieId
            ctx.json(calorie)
            ctx.status(201)
        }
        else{
            ctx.status(404)
        }
    }
    /////////////////////////////////////////// getCaloriesByCalorieId
    fun getCaloriesByCalorieId(ctx: Context) {
        val calorie = calorieDAO.findByCalorieId((ctx.pathParam("calorie-id").toInt()))
        if (calorie != null){
            ctx.json(calorie)
            ctx.status(200)
        }
        else{
            ctx.status(404)
        }
    }

    /////////////////////////////////////////// deleteCalorieByCalorieId
    fun deleteCalorieByCalorieId(ctx: Context){
        if (calorieDAO.deleteByCalorieId(ctx.pathParam("calorie-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    /////////////////////////////////////////// deleteCalorieByUserId
    fun deleteCalorieByUserId(ctx: Context){
        if (calorieDAO.deleteByUserId(ctx.pathParam("user-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    /////////////////////////////////////////// updateCalorie
    fun updateCalorie(ctx: Context){
        val calorie : Calorie = jsonToObject(ctx.body())
        if (calorieDAO.updateByCalorieId(
                calorieId = ctx.pathParam("calorie-id").toInt(),
                calorieToUpdate =calorie) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

//////////////////////////////////////////// Workout ///////////////////////////////////////
////////////////////////////////////////////////////// getAllWorkouts
    fun getAllWorkouts(ctx: Context) {
        val workouts = workoutDAO.getAll()
        if (workouts.size != 0) {
            ctx.status(200)
        }
        else{
            ctx.status(404)
        }
        ctx.json(workouts)
    }

    ////////////////////////////////////////////////////// getWorkoutsByUserId
    fun getWorkoutsByUserId(ctx: Context) {
        if (userDao.findById(ctx.pathParam("user-id").toInt()) != null) {
            val workouts = workoutDAO.findByUserId(ctx.pathParam("user-id").toInt())
            if (workouts.isNotEmpty()) {
                ctx.json(workouts)
                ctx.status(200)
            }
            else{
                ctx.status(404)
            }
        }
        else{
            ctx.status(404)
        }
    }

    ////////////////////////////////////////////////////// addWorkout
    fun addWorkout(ctx: Context) {
        val workout : Workout = jsonToObject(ctx.body())
        val userId = userDao.findById(workout.userId)
        if (userId != null) {
            val workoutId = workoutDAO.save(workout)
            workout.id = workoutId
            ctx.json(workout)
            ctx.status(201)
        }
        else{
            ctx.status(404)
        }
    }
    /////////////////////////////////////////// getWorkoutsByWorkoutId
    fun getWorkoutsByWorkoutId(ctx: Context) {
        val workout = workoutDAO.findByWorkoutId((ctx.pathParam("workout-id").toInt()))
        if (workout != null){
            ctx.json(workout)
            ctx.status(200)
        }
        else{
            ctx.status(404)
        }
    }

    /////////////////////////////////////////// deleteWorkoutByWorkoutId
    fun deleteWorkoutByWorkoutId(ctx: Context){
        if (workoutDAO.deleteByWorkoutId(ctx.pathParam("workout-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    /////////////////////////////////////////// deleteWorkoutByUserId
    fun deleteWorkoutByUserId(ctx: Context){
        if (workoutDAO.deleteByUserId(ctx.pathParam("user-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }
    /////////////////////////////////////////// updateWorkout
    fun updateWorkout(ctx: Context){
        val workout : Workout = jsonToObject(ctx.body())
        if (workoutDAO.updateByWorkoutId(
                workoutId = ctx.pathParam("workout-id").toInt(),
                workoutToUpdate =workout) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }


}