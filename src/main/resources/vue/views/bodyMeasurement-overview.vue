<template id="bodyMeasurement-overview">
  <app-layout>
    <!-- --------------------------------------------------- Add button a new bodyMeasurement-->
    <div class="card bg-light mb-3">
      <div class="card-header">
        <div class="row">
          <div class="col-6">
            BodyMeasurements
          </div>
          <div class="col" align="right">
            <button rel="tooltip" title="Add"
                    class="btn btn-info btn-simple btn-link"
                    @click="hideForm =!hideForm">
              <i class="fa fa-plus" aria-hidden="true"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card-body" :class="{ 'd-none': hideForm}">
      <form id="addBodyMeasurement">
        <div class="input-group mb-3">
          <div class="input-group-prepend">
            <span class="input-group-text" id="input-bodyMeasurement-weight">Weight</span>
          </div>
          <input type="duration" class="form-control" v-model="formData.weight" name="weight" placeholder="Weight"/>
        </div>
        <div class="input-group mb-3">
          <div class="input-group-prepend">
            <span class="input-group-text" id="input-bodyMeasurement-height">Height</span>
          </div>
          <input type="duration" class="form-control" v-model="formData.height" name="height" placeholder="Height"/>
        </div>
        <div class="input-group mb-3">
          <div class="input-group-prepend">
            <span class="input-group-text" id="input-bodyMeasurement-waist">Waist</span>
          </div>
          <input type="duration" class="form-control" v-model="formData.waist" name="waist" placeholder="Waist"/>
        </div>
        <div class="input-group mb-3">
          <div class="input-group-prepend">
            <span class="input-group-text" id="input-bodyMeasurement-chest">Chest</span>
          </div>
          <input type="duration" class="form-control" v-model="formData.chest" name="chest" placeholder="Chest"/>
        </div>
      </form>
      <button rel="tooltip" title="Update" class="btn btn-info btn-simple btn-link" @click="addBodyMeasurement()">Add BodyMeasurement</button>
    </div>
    <!-- ---------------------------------------------------->

    <div class="list-group list-group-flush">
      <div class="list-group-item d-flex align-items-start"
           v-for="(bodyMeasurement,index) in bodyMeasurements" v-bind:key="index">
        <div class="mr-auto p-2">
          <span><a :href="`/activities/${activity.id}`"> {{ activity.description }} (Duration: {{ activity.duration }} mins, Calories: {{ activity.calories }} cal, Started at: {{ activity.started }})</a></span>
          <span><a :href="`/bodyMeasurements/${bodyMeasurement.id}`"> {{ bodyMeasurement.weight}}  (Height: {{ bodyMeasurement.height }} cm, Waist: {{ bodyMeasurement.waist }} cm, Chest:{{ bodyMeasurement.chest }})</a></span>
<!--          <span><a :href="`/bodyMeasurements/${bodyMeasurement.id}`"> weight:{{bodyMeasurement.weight}}kg  (height:{{ bodyMeasurement.height}}cm, waist:{{ bodyMeasurement.waist}} cm, chest:{{ bodyMeasurement.chest}} cm)</a></span>-->
        </div>

        <!--------------add an "update" and "delete" button for each bodyMeasurement-->
        <div class="p2">
          <a :href="`/bodyMeasurements/${bodyMeasurement.id}`">
            <button rel="tooltip" title="Update" class="btn btn-info btn-simple btn-link">
              <i class="fa fa-pencil" aria-hidden="true"></i>
            </button>

            <!--------------"delete" button for each bodyMeasurement-->
            <button rel="tooltip" title="Delete" class="btn btn-info btn-simple btn-link"
                    @click="deleteBodyMeasurement(bodyMeasurement, index)">
              <i class="fas fa-trash" aria-hidden="true"></i>
            </button>

          </a>
        </div>
        <!-------------------------------------->


      </div>
    </div>
  </app-layout>
</template>


<script>
Vue.component("bodyMeasurement-overview", {
  template: "#bodyMeasurement-overview",
  data: () => ({
    bodyMeasurements: [],

    /*For adding a new bodyMeasurement:*/
    formData: [],
    hideForm :true,

  }),
  created() {
    this.fetchBodyMeasurements();
  },
  methods: {
    fetchBodyMeasurements: function () {
      axios.get("/api/bodyMeasurements")
          .then(res => this.bodyMeasurements = res.data)
          .catch(() => alert("Error while fetching bodyMeasurements"));
    },

    <!----------------------------------------------"delete" button for each bodyMeasurement-->
    deleteBodyMeasurement: function (bodyMeasurement, index) {
      if (confirm('Are you sure you want to delete this bodyMeasurement? This action cannot be undone.', 'Warning')) {
        //bodyMeasurement confirmed delete
        const userId = bodyMeasurement.id;
        const url = `/api/bodyMeasurements/${userId}`;
        axios.delete(url)
            .then(response =>
                //delete from the local state so Vue will reload list automatically
                this.bodyMeasurements.splice(index, 1).push(response.data))
            .catch(function (error) {
              console.log(error)
            });
      }
    },
    <!---------------------------------------------------------------------------- -->

    <!----------------------------------------------"add" button for a new bodyMeasurement-->
    addBodyMeasurement: function (){
      const url = `/api/bodyMeasurements`;
      axios.post(url,
          {
            weight: this.formData.weight,
            height: this.formData.height,
            waist: this.formData.waist,
            chest: this.formData.chest,
          })
          .then(response => {
            this.bodyMeasurements.push(response.data)
            this.hideForm= true;
          })
          .catch(error => {
            console.log(error)
          })
    }
    <!---------------------------------------------------------------------------- -->

  }
});
</script>

