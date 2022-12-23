<template id=“activity-profile">
  <app-layout>
    <div v-if="noActivityFound">
      <p> Sorry, we were not able to retrieve this activity.</p>
      <p> View <a :href="'/activities'">all activities</a>.</p>
    </div>
    <div class="card bg-light mb-3" v-if="!noActivityFound">
      <div class="card-header">
        <div class="row">
          <div class="col-6"> Activity Profile </div>
          <div class="col" align="right">
            <button rel="tooltip" title="Update"
                    class="btn btn-info btn-simple btn-link"
                    @click="updateActivity()"> Update Activity
              <!--          For using FontAwesome and Symbols instead of text-->
              <i class="far fa-save" aria-hidden="true"></i>
              <!----->
            </button>


            <!--            //Adding a delete button-->
            <button rel="tooltip" title="Delete"
                    class="btn btn-info btn-simple btn-link"
                    @click="deleteActivity()"> Delete Activity
              <!--          For using FontAwesome and Symbols instead of text-->
              <i class="fas fa-trash" aria-hidden="true"></i>
              <!----->
            </button>
            <!---------------------------->

          </div>
        </div>
      </div>
      <div class="card-body">
        <form>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text" id="input-user-id">User ID</span>
            </div>
            <input type="number" class="form-control" v-model="user.id" name="id" readonly placeholder="Id"/>
          </div>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text" id="input-activity-description">Description</span>
            </div>
            <input type="text" class="form-control" v-model="activity.description" name="description" placeholder="Description"/>
          </div>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text" id="input-activity-duration">Duration</span>
            </div>
            <input type="time" class="form-control" v-model="activity.duration" name="duration" placeholder="Duration"/>
          </div>
        </form>
      </div>

      <!-------------------------------------- To add a new hyperlink for activity-->
      <div class="card-footer text-left">
        <p  v-if="activities.length == 0"> No activities yet...</p>
        <p  v-if="activities.length > 0"> Activities so far...</p>
        <ul>
          <li v-for="activity in activities">
            {{ activity.description }} for {{ activity.duration }} minutes
          </li>
        </ul>
      </div>
      <!---------------------------------------------------------------------->
    </div>
  </app-layout>
</template>



<script>
Vue.component("activity-profile", {
template: "#activity-profile",
    data: () => ({
  activity: null,
  noActivityFound: false,

//add the new activities array to the data area:
  activities: [],

}),
    created: function () {
  const userId = this.$javalin.pathParams["user-id"];
  const url = `/api/activities/${userId}`
  axios.get(url)
      .then(res => this.activity = res.data)
      .catch(error => {
        console.log("No activity found for id passed in the path parameter: " + error)
        this.noActivityFound = true})


  // --------------------------------------------- fetch of the activities
  axios.get(url + `/activities`)
      .then(res => this.activities = res.data)
      .catch(error => {
        console.log("No activities added yet (this is ok): " + error)
      })
  //------------------------------------------------


},
// ---------------------------------------- updateActivity() method:
methods: {
  updateActivity: function () {
    const userId = this.$javalin.pathParams["user-id"];
    const url = `/api/activities/${userId}`
    axios.patch(url,
        {
          description: this.activity.description,
          duration: this.activity.duration,
          calories: this.activity.calories,
          started: this.activity.started,
        })
        .then(response =>
            this.activity.push(response.data))
        .catch(error => {
          console.log(error)
        })
    alert("Activity updated!")
  },
//--------------------------------------------------- deleteActivity() method:
  deleteActivity: function () {
    if (confirm("Do you really want to delete? :( ")) {
      const userId = this.$javalin.pathParams["user-id"];
      const url = `/api/activities/${userId}`
      axios.delete(url)
          .then(response => {
            alert("Activity deleted")
            //display the /activities endpoint
            window.location.href = '/activities';
          })
          .catch(function (error) {
            console.log(error)
          });
    }
  }
//---------------------------------------------------
}
});

</script>