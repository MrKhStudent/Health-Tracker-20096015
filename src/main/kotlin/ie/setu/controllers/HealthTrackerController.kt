// The responsibility of this class is to manage the IO between the UserDAO and the JSON context.
// Remember, the UserDAO handles the collection of users.
package ie.setu.controllers

import ie.setu.domain.repository.UserDAO
import io.javalin.http.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ie.setu.domain.User

object HealthTrackerController {
    private val userDao = UserDAO()

    fun getAllUsers(ctx: Context){
        ctx.json(userDao.getAll())
    }
    fun getUserByUserId(ctx: Context){
        val user = userDao.findById(ctx.pathParam("user-id").toInt())
        if (user != null){
            ctx.json(user)
        }
    }
    ////////////////////////// Exercise 4 (myself)
    fun getUserByEmail(ctx: Context){
        val user = userDao.findByEmail(ctx.pathParam("email"))
        if (user != null){
            ctx.json(user)
        }
    }
    /////////////////////////////////////
    fun addUser(ctx: Context){
        val mapper = jacksonObjectMapper()
        val user = mapper.readValue<User>(ctx.body())
        userDao.save(user)
    }
    ////////////////////////// Exercise 4

    /*fun findByEmail(email: String) :User?{
        return null
    }
    fun delete(id: Int) {
    }
    fun update(id: Int, user: User){
    }*/
    ////////////////////////////////////
}
