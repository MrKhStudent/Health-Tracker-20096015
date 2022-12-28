<template id=“bodyMeasurement-profile">
  <app-layout>
    <div v-if="noBodyMeasurementFound">
      <p> Sorry, we were not able to retrieve this bodyMeasurement.</p>
      <p> View <a :href="'/bodyMeasurements'">all bodyMeasurements</a>.</p>
    </div>
    <div class="card bg-light mb-3" v-if="!noBodyMeasurementFound">
      <div class="card-header">
        <div class="row">
          <div class="col-6"> BodyMeasurement Profile </div>
          <div class="col" align="right">
            <button rel="tooltip" title="Update"
                    class="btn btn-info btn-simple btn-link"
                    @click="updateBodyMeasurement()"> Update BodyMeasurement
              <!--          For using FontAwesome and Symbols instead of text-->
              <i class="far fa-save" aria-hidden="true"></i>
              <!----->
            </button>


            <!--            //Adding a delete button-->
            <button rel="tooltip" title="Delete"
                    class="btn btn-info btn-simple btn-link"
                    @click="deleteBodyMeasurement()"> Delete BodyMeasurement
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
            <input type="number" class="form-control" v-model="bodyMeasurement.id" name="id" readonly placeholder="Id"/>
          </div>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text" id="input-bodyMeasurement-weight">Weight</span>
            </div>
            <input type="number" class="form-control" v-model="bodyMeasurement.weight" name="weight" placeholder="Weight"/>
          </div>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text" id="input-bodyMeasurement-height">Height</span>
            </div>
            <input type="number" class="form-control" v-model="bodyMeasurement.height" name="height" placeholder="Height"/>
          </div>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text" id="input-bodyMeasurement-waist">Waist</span>
            </div>
            <input type="number" class="form-control" v-model="bodyMeasurement.waist" name="waist" placeholder="Waist"/>
          </div>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text" id="input-bodyMeasurement-chest">Chest</span>
            </div>
            <input type="number" class="form-control" v-model="bodyMeasurement.chest" name="chest" placeholder="Chest"/>
          </div>
        </form>
      </div>

      <!-------------------------------------- To add a new hyperlink for bodyMeasurement-->
      <div class="card-footer text-left">
        <p  v-if="bodyMeasurements.length == 0"> No bodyMeasurements yet...</p>
        <p  v-if="bodyMeasurements.length > 0"> BodyMeasurements so far...</p>
        <ul>
          <li v-for="bodyMeasurement in bodyMeasurements">
            {{ bodyMeasurement.weight }} (kg) with {{ bodyMeasurement.height }} (cm)
          </li>
        </ul>
      </div>
      <!---------------------------------------------------------------------->
    </div>
  </app-layout>
</template>



<script>
Vue.component("bodyMeasurement-profile", {
  template: "#bodyMeasurement-profile",
  data: () => ({
    bodyMeasurement: null,
    noBodyMeasurementFound: false,

//add the new bodyMeasurements array to the data area:
    bodyMeasurements: [],

  }),
  created: function () {
    const userId = this.$javalin.pathParams["bodyMeasurement-id"];
    const url = `/api/bodyMeasurements/${userId}`
    axios.get(url)
        .then(res => this.bodyMeasurement = res.data)
        .catch(error => {
          console.log("No bodyMeasurement found for id passed in the path parameter: " + error)
          this.noBodyMeasurementFound = true})


    // --------------------------------------------- fetch of the bodyMeasurements
    axios.get(url + `/bodyMeasurements`)
        .then(res => this.bodyMeasurements = res.data)
        .catch(error => {
          console.log("No bodyMeasurements added yet (this is ok): " + error)
        })
    //------------------------------------------------


  },
// ---------------------------------------- updateBodyMeasurement() method:
  methods: {
    updateBodyMeasurement: function () {
      const userId = this.$javalin.pathParams["bodyMeasurement-id"];
      const url = `/api/bodyMeasurements/${userId}`
      axios.patch(url,
          {
            weight: this.bodyMeasurement.weight,
            height: this.bodyMeasurement.height,
            waist: this.bodyMeasurement.waist,
            chest: this.bodyMeasurement.chest,

          })
          .then(response =>
              this.bodyMeasurement.push(response.data))
          .catch(error => {
            console.log(error)
          })
      alert("BodyMeasurement updated!")
    },
//--------------------------------------------------- deleteBodyMeasurement() method:
    deleteBodyMeasurement: function () {
      if (confirm("Do you really want to delete? :( ")) {
        const userId = this.$javalin.pathParams["bodyMeasurement-id"];
        const url = `/api/bodyMeasurements/${userId}`
        axios.delete(url)
            .then(response => {
              alert("BodyMeasurement deleted")
              //display the /bodyMeasurements endpoint
              window.location.href = '/bodyMeasurements';
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