<template id="user-profile">
  <app-layout>
    <div v-if="noUserFound">
      <p> Sorry, we were not able to retrieve this user.</p>
      <p> View <a :href="'/users'">all users</a>.</p>
    </div>
    <div class="card bg-light mb-3" v-if="!noUserFound">
      <div class="card-header">
        <div class="row">
          <div class="col-6"> User Profile </div>
          <div class="col" align="right">
            <button rel="tooltip" title="Update"
                    class="btn btn-info btn-simple btn-link"
                    @click="updateUser()"> Update User
<!--          For using FontAwesome and Symbols instead of text-->
              <i class="far fa-save" aria-hidden="true"></i>
              <!----->
            </button>


<!--            //Adding a delete button-->
            <button rel="tooltip" title="Delete"
                    class="btn btn-info btn-simple btn-link"
                    @click="deleteUser()"> Delete User
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
              <span class="input-group-text" id="input-user-name">Name</span>
            </div>
            <input type="text" class="form-control" v-model="user.name" name="name" placeholder="Name"/>
          </div>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text" id="input-user-email">Email</span>
            </div>
            <input type="email" class="form-control" v-model="user.email" name="email" placeholder="Email"/>
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
      <!-------------------------------------- To add a new hyperlink for bodyMeasurement-->
      <div class="card-footer text-left">
        <p  v-if="bodyMeasurements.length == 0"> No bodyMeasurements yet...</p>
        <p  v-if="bodyMeasurements.length > 0"> BodyMeasurements so far...</p>
        <ul>
          <li v-for="bodyMeasurement in bodyMeasurements">
            {{ bodyMeasurement.weight }} kg with {{ bodyMeasurement.height }} cm
          </li>
        </ul>
      </div>
      <!---------------------------------------------------------------------->
    </div>
  </app-layout>
</template>



<script>
  Vue.component("user-profile", {
  template: "#user-profile",
  data: () => ({
    user: null,
    noUserFound: false,

//add the new activities array to the data area:
    activities: [],
    bodyMeasurements: [],

  }),
  created: function () {
    const userId = this.$javalin.pathParams["user-id"];
    const url = `/api/users/${userId}`
    axios.get(url)
        .then(res => this.user = res.data)
        .catch(error => {
          console.log("No user found for id passed in the path parameter: " + error)
          this.noUserFound = true})


    // --------------------------------------------- fetch of the activities
    axios.get(url + `/activities`)
        .then(res => this.activities = res.data)
        .catch(error => {
          console.log("No activities added yet (this is ok): " + error)
        })
    //------------------------------------------------
    // --------------------------------------------- fetch of the bodyMeasurements
    axios.get(url + `/bodyMeasurements`)
        .then(res => this.bodyMeasurements = res.data)
        .catch(error => {
          console.log("No bodyMeasurements added yet (this is ok): " + error)
        })

  },
  // ---------------------------------------- updateUser() method:
  methods: {
    updateUser: function () {
      const userId = this.$javalin.pathParams["user-id"];
      const url = `/api/users/${userId}`
      axios.patch(url,
          {
            name: this.user.name,
            email: this.user.email
          })
          .then(response =>
              this.user.push(response.data))
          .catch(error => {
            console.log(error)
          })
      alert("User updated!")
    },
//--------------------------------------------------- deleteUser() method:
    deleteUser: function () {
      if (confirm("Do you really want to delete? :( ")) {
        const userId = this.$javalin.pathParams["user-id"];
        const url = `/api/users/${userId}`
        axios.delete(url)
            .then(response => {
              alert("User deleted")
              //display the /users endpoint
              window.location.href = '/users';
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














