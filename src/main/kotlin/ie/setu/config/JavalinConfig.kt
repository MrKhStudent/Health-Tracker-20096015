package ie.setu.config
import ie.setu.controllers.HealthTrackerController
import  io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*

class JavalinConfig {
    fun startJavalinService(): Javalin {
        val app = Javalin.create().apply {
            exception(Exception::class.java) { e, ctx -> e.printStackTrace() }
            error(404) { ctx -> ctx.json("404 - Not Found") }
        }.start(getHerokuAssignedPort())

        registerRoutes(app)
        return app
    }
    // update the code so that Javalin can run on a remote Heroku host
    private fun getHerokuAssignedPort(): Int {
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
                }
                path("/email/{email}"){
                    get(HealthTrackerController::getUserByEmail)
                }

            }
        }
    }

}
