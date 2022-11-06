package ie.setu.domain

import org.joda.time.DateTime

data class Workout (
    var id: Int,
    var description: String,
    var duration: Double,
    var numbers: Int,
    var userId: Int
)
