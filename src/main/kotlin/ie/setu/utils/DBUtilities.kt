package ie.setu.utils

import com.fasterxml.jackson.databind.ObjectMapper

import ie.setu.domain.User
import ie.setu.domain.db.Users
import ie.setu.domain.Activity
import ie.setu.domain.db.Activities
import ie.setu.domain.BodyMeasurement
import ie.setu.domain.db.BodyMeasurements
import ie.setu.domain.Calorie
import ie.setu.domain.db.Calories
import ie.setu.domain.Workout
import ie.setu.domain.db.Workouts
import org.jetbrains.exposed.sql.ResultRow
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kong.unirest.JsonNode
import java.net.http.HttpResponse

fun mapToUser(it: ResultRow) = User(
    id = it[Users.id],
    name = it[Users.name],
    email = it[Users.email]
)

fun mapToActivity(it: ResultRow) = Activity(
    id = it[Activities.id],
    description = it[Activities.description],
    duration = it[Activities.duration],
    started = it[Activities.started],
    calories = it[Activities.calories],
    userId = it[Activities.userId]
)

fun mapToBodyMeasurement(it: ResultRow) = BodyMeasurement(
    id = it[BodyMeasurements.id],
    weight = it[BodyMeasurements.weight],
    height = it[BodyMeasurements.height],
    waist = it[BodyMeasurements.waist],
    chest = it[BodyMeasurements.chest],
    userId = it[BodyMeasurements.userId]
)

fun mapToCalorie(it: ResultRow) = Calorie(
    id = it[Calories.id],
    breakfast = it[Calories.breakfast],
    lunch = it[Calories.lunch],
    dinner = it[Calories.dinner],
    snack = it[Calories.snack],
    userId = it[Calories.userId]
)


fun mapToWorkout(it: ResultRow) = Workout(
    id = it[Workouts.id],
    description = it[Workouts.description],
    duration = it[Workouts.duration],
    numbers = it[Workouts.numbers],
    userId = it[Workouts.userId]
)









// These parts was mentioned in JSONUtilities file in lecture

/*
inline fun <reified T: Any> jsonNodeToObject(jsonNode: HttpResponse<JsonNode>) : T {
    return jsonToObject<T>(jsonNode.body.toString())
}*/


//--------------- to map all the JSONs to Objects in our system
/*

fun jsonObjectMapper(): ObjectMapper
        = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(JodaModule())
        .registerModule(KotlinModule().Builder().build)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
*/
