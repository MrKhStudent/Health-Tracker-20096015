package ie.setu.helpers

import ie.setu.domain.User
import ie.setu.domain.Activity
import ie.setu.domain.BodyMeasurement
import ie.setu.domain.Calorie
import ie.setu.domain.Workout
import ie.setu.domain.db.Activities
import ie.setu.domain.db.BodyMeasurements
import ie.setu.domain.db.Calories
import ie.setu.domain.db.Workouts
import ie.setu.domain.db.Users
import ie.setu.domain.repository.ActivityDAO
import ie.setu.domain.repository.BodyMeasurementDAO
import ie.setu.domain.repository.CalorieDAO
import ie.setu.domain.repository.WorkoutDAO
import ie.setu.domain.repository.UserDAO
import org.jetbrains.exposed.sql.SchemaUtils
import org.joda.time.DateTime

val nonExistingEmail = "112233445566778testUser@xxxxx.xx"
val validName = "Test User 1"
val validEmail = "testuser1@test.com"
val updatedName = "Updated Name"
val updatedEmail = "Updated Email"

val updatedDescription = "Updated Description"
val updatedDuration = 30.0
val updatedCalories = 945
val updatedStarted = DateTime.parse("2020-06-11T05:59:27.258Z")

val updatedWeight = 65.4
val updatedHeight = 174.5
val updatedWaist = 65.3
val updatedChest = 102.2

val updatedBreakfast = 399.1
val updatedLunch = 1030.0
val updatedDinner = 632.4
val updatedSnack = 210.5

val updatedDescriptionWorkout = "Updated Description Workout"
val updatedDurationWorkout = 60.0
val updatedNumbers = 65


val users = arrayListOf<User>(
    User(name = "Alice Wonderland", email = "alice@wonderland.com", id = 1),
    User(name = "Bob Cat", email = "bob@cat.ie", id = 2),
    User(name = "Mary Contrary", email = "mary@contrary.com", id = 3),
    User(name = "Carol Singer", email = "carol@singer.com", id = 4)
)
//----------------------------------------- activities -----------------
val activities = arrayListOf<Activity>(
    Activity(id = 1, description = "Running", duration = 22.0, calories = 230, started = DateTime.now(), userId = 1),
    Activity(id = 2, description = "Hopping", duration = 10.5, calories = 80, started = DateTime.now(), userId = 1),
    Activity(id = 3, description = "Walking", duration = 12.0, calories = 120, started = DateTime.now(), userId = 2)
)

//----------------------------------------- bodyMeasurements -----------------
val bodyMeasurements = arrayListOf<BodyMeasurement>(
    BodyMeasurement(id = 1, weight = 45.2, height = 165.5, waist = 48.2, chest = 60.5, userId = 1),
    BodyMeasurement(id = 2, weight = 87.3, height = 171.2, waist = 92.5, chest = 110.7, userId = 2),
    BodyMeasurement(id = 3, weight = 72.6, height = 177.8, waist = 80.4, chest = 90.3, userId = 3)
)


//----------------------------------------- calories -----------------
val calories = arrayListOf<Calorie>(
    Calorie(id = 1, breakfast = 500.1, lunch = 1010.5, dinner = 669.2, snack = 230.5, userId = 1),
    Calorie(id = 2, breakfast = 650.3, lunch = 1000.2, dinner = 549.5, snack = 201.7, userId = 2),
    Calorie(id = 3, breakfast = 521.6, lunch = 1221.8, dinner = 490.4, snack = 121.3, userId = 3)
)

//----------------------------------------- workouts -----------------
val workouts = arrayListOf<Workout>(
    Workout(id = 1, description = "push up", duration = 60.5, numbers = 12, userId = 1),
    Workout(id = 2, description = "squat", duration = 30.1, numbers = 9, userId = 2),
    Workout(id = 3, description = "mountain climber", duration = 60.2, numbers = 34, userId = 3)
)


//----------------------------------------- Users -----------------
fun populateUserTable(): UserDAO {
    SchemaUtils.create(Users)
    val userDAO = UserDAO()
    userDAO.save(users[0])
    userDAO.save(users[1])
    userDAO.save(users[2])
    return userDAO
}

//----------------------------------------- Activities -----------------
fun populateActivityTable(): ActivityDAO {
    SchemaUtils.create(Activities)
    val activityDAO = ActivityDAO()
    activityDAO.save(activities[0])
    activityDAO.save(activities[1])
    activityDAO.save(activities[2])
    return activityDAO
}

//----------------------------------------- BodyMeasurements -----------------
fun populateBodyMeasurementTable(): BodyMeasurementDAO {
    SchemaUtils.create(BodyMeasurements)
    val bodyMeasurementDAO = BodyMeasurementDAO()
    bodyMeasurementDAO.save(bodyMeasurements[0])
    bodyMeasurementDAO.save(bodyMeasurements[1])
    bodyMeasurementDAO.save(bodyMeasurements[2])
    return bodyMeasurementDAO
}

//----------------------------------------- Calories -----------------
fun populateCalorieTable(): CalorieDAO {
    SchemaUtils.create(Calories)
    val calorieDAO = CalorieDAO()
    calorieDAO.save(calories[0])
    calorieDAO.save(calories[1])
    calorieDAO.save(calories[2])
    return calorieDAO
}

//----------------------------------------- workouts -----------------
fun populateWorkoutTable(): WorkoutDAO {
    SchemaUtils.create(Workouts)
    val workoutDAO = WorkoutDAO()
    workoutDAO.save(workouts[0])
    workoutDAO.save(workouts[1])
    workoutDAO.save(workouts[2])
    return workoutDAO
}