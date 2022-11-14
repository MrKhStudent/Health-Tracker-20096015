package ie.setu.config

import ie.setu.controllers.HealthTrackerController
import  io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.swagger.v3.oas.models.info.Info
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import ie.setu.utils.jsonObjectMapper
import io.javalin.plugin.json.JavalinJackson
import io.javalin.plugin.rendering.vue.VueComponent

class JavalinConfig {

    fun startJavalinService(): Javalin {
        val app = Javalin.create {
            it.registerPlugin(getConfiguredOpenApiPlugin())
            it.defaultContentType = "application/json"
            //added this jsonMapper for our integration tests - serialise objects to json
            it.jsonMapper(JavalinJackson(jsonObjectMapper()))
            it.enableWebjars()
        }.apply {
            exception(Exception::class.java) { e, _ -> e.printStackTrace() }
            error(404) { ctx -> ctx.json("404 - Not Found") }
        }.start(getRemoteAssignedPort())

        registerRoutes(app)
        return app
    }
    // update the code so that Javalin can run on a remote Heroku host
    private fun getRemoteAssignedPort(): Int {
        val herokuPort = System.getenv("PORT")
        return if (herokuPort != null) {
            Integer.parseInt(herokuPort)
        } else 7002
    }

    private fun registerRoutes(app: Javalin){
        app.routes{
            path("/api/users"){
                get(HealthTrackerController::getAllUsers)
                post(HealthTrackerController::addUser)   // Register a new endpoint to retrieve a specific user
                path("{user-id}"){
                    get(HealthTrackerController::getUserByUserId)
                    delete(HealthTrackerController::deleteUser)
                    patch(HealthTrackerController::updateUser)
                    //The overall path is: "/api/users/:user-id/activities"
                    path("activities"){
                        get(HealthTrackerController::getActivitiesByUserId)
                        delete(HealthTrackerController::deleteActivityByUserId)
                    }
                    path("bodyMeasurements"){
                        get(HealthTrackerController::getBodyMeasurementsByUserId)
                        delete(HealthTrackerController::deleteBodyMeasurementByUserId)
                    }
                    path("calories"){
                        get(HealthTrackerController::getCaloriesByUserId)
                        delete(HealthTrackerController::deleteCalorieByUserId)
                    }
                    path("workouts"){
                        get(HealthTrackerController::getWorkoutsByUserId)
                        delete(HealthTrackerController::deleteWorkoutByUserId)
                    }
                }
                path("/email/{email}"){
                    get(HealthTrackerController::getUserByEmail)
                }
            }
            path("/api/activities") {
                get(HealthTrackerController::getAllActivities)
                post(HealthTrackerController::addActivity)
                path("{activity-id}") {
                    get(HealthTrackerController::getActivitiesByActivityId)
                    delete(HealthTrackerController::deleteActivityByActivityId)
                    patch(HealthTrackerController::updateActivity)
                }
            }
            path("/api/bodyMeasurements") {
                get(HealthTrackerController::getAllBodyMeasurements)
                post(HealthTrackerController::addBodyMeasurement)
                path("{bodyMeasurement-id}") {
                    get(HealthTrackerController::getBodyMeasurementsByBodyMeasurementId)
                    delete(HealthTrackerController::deleteBodyMeasurementByBodyMeasurementId)
                    patch(HealthTrackerController::updateBodyMeasurement)
                }
            }
            path("/api/calories") {
                get(HealthTrackerController::getAllCalories)
                post(HealthTrackerController::addCalorie)
                path("{calorie-id}") {
                    get(HealthTrackerController::getCaloriesByCalorieId)
                    delete(HealthTrackerController::deleteCalorieByCalorieId)
                    patch(HealthTrackerController::updateCalorie)
                }
            }
            path("/api/workouts") {
                get(HealthTrackerController::getAllWorkouts)
                post(HealthTrackerController::addWorkout)
                path("{workout-id}") {
                    get(HealthTrackerController::getWorkoutsByWorkoutId)
                    delete(HealthTrackerController::deleteWorkoutByWorkoutId)
                    patch(HealthTrackerController::updateWorkout)
                }
            }

            //---------------------------------------------- Registering our Vue routes

            // The @routeComponent that we added in layout.html earlier will be replaced
            // by the String inside of VueComponent. This means a call to / will load
            // the layout and display our <home-page> component.
            get("/", VueComponent("<home-page></home-page>"))
            get("/users", VueComponent("<user-overview></user-overview>"))
            get("/users/{user-id}", VueComponent("<user-profile></user-profile>"))
            get("/users/{user-id}/activities", VueComponent("<user-activity-overview></user-activity-overview>"))
            get("/users/{user-id}/bodyMeasurements", VueComponent("<user-bodyMeasurement-overview></user-bodyMeasurement-overview>"))
            get("/users/{user-id}/calories", VueComponent("<user-calorie-overview></user-calorie-overview>"))
            get("/users/{user-id}/workouts", VueComponent("<user-workout-overview></user-workout-overview>"))

        }
    }
    fun getConfiguredOpenApiPlugin() = OpenApiPlugin(
        OpenApiOptions(
            Info().apply {
                title("Health Tracker App")
                version("1.0")
                description("Health Tracker API")
            }
        ).apply {
            path("/swagger-docs") // endpoint for OpenAPI json
            swagger(SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
            reDoc(ReDocOptions("/redoc")) // endpoint for redoc
        }
    )

}