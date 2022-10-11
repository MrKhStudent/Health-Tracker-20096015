// This is the User Data Access Object (DAO).
// It's single responsibility is to manage a collection of User objects in the app.

package ie.setu.domain.repository
import ie.setu.domain.User
import ie.setu.domain.db.Users
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import ie.setu.utils.mapToUser

class UserDAO {

    fun getAll() : ArrayList<User>{
        val userList: ArrayList<User> = arrayListOf()
        transaction {
            Users.selectAll().map {
                userList.add(mapToUser(it)) } //This is a bespoke utility method for
        // taking the database table information and placing it, field by field,
        // into a User object.
        }
        return userList
    }

    fun findById(id: Int): User?{
        return null
    }

    fun save(user: User){
    }

    fun findByEmail(email: String) :User?{
        return null
    }

    fun delete(id: Int) {
    }

    fun update(id: Int, user: User){
    }
}
