package ie.setu.domain


data class BodyMeasurement (
    var id: Int,
    var weight: Double,
    var height: Double,
    var waist: Double,
    var chest: Double,
    var userId: Int
)