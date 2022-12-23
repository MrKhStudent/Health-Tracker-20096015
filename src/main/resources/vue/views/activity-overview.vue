<template id="activity-overview">
  <app-layout>
    <!-- --------------------------------------------------- Add button a new activity-->
    <div class="card bg-light mb-3">
      <div class="card-header">
        <div class="row">
          <div class="col-6">
            Activities
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
      <form id="addActivity">
        <div class="input-group mb-3">
          <div class="input-group-prepend">
            <span class="input-group-text" id="input-activity-description">Description</span>
          </div>
          <input type="text" class="form-control" v-model="formData.description" description="description" placeholder="Description"/>
        </div>
        <div class="input-group mb-3">
          <div class="input-group-prepend">
            <span class="input-group-text" id="input-activity-duration">Duration</span>
          </div>
          <input type="time" class="form-control" v-model="formData.duration" description="duration" placeholder="Duration"/>
        </div>
        <div class="input-group mb-3">
          <div class="input-group-prepend">
            <span class="input-group-text" id="input-activity-calories">Calories</span>
          </div>
          <input type="number" class="form-control" v-model="formData.calories" description="calories" placeholder="Calories"/>
        </div>
        <div class="input-group mb-3">
          <div class="input-group-prepend">
            <span class="input-group-text" id="input-activity-started">Started</span>
          </div>
          <input type="datetime-local" class="form-control" v-model="formData.started" description="started" placeholder="Started"/>
        </div>
      </form>
      <button rel="tooltip" title="Update" class="btn btn-info btn-simple btn-link" @click="addActivity()">Add Activity</button>
    </div>
    <!-- ---------------------------------------------------->

    <div class="list-group list-group-flush">
      <div class="list-group-item d-flex align-items-start"
           v-for="(activity,index) in activities" v-bind:key="index">
        <div class="mr-auto p-2">
          <span><a :href="`/activities/${user.id}`"> {{ activity.description }} ({{ activity.duration }}) ({{ activity.calories }}) ({{ activity.started }})</a></span>
        </div>

        <!--------------add an "update" and "delete" button for each activity-->
        <div class="p2">
          <a :href="`/activities/${user.id}`">
            <button rel="tooltip" title="Update" class="btn btn-info btn-simple btn-link">
              <i class="fa fa-pencil" aria-hidden="true"></i>
            </button>

            <!--------------"delete" button for each activity-->
            <button rel="tooltip" title="Delete" class="btn btn-info btn-simple btn-link"
                    @click="deleteActivity(activity, index)">
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
Vue.component("activity-overview", {
  template: "#activity-overview",
  data: () => ({
    activities: [],

    /*For adding a new activity:*/
    formData: [],
    hideForm :true,

  }),
  created() {
    this.fetchActivities();
  },
  methods: {
    fetchActivities: function () {
      axios.get("/api/activities")
          .then(res => this.activities = res.data)
          .catch(() => alert("Error while fetching activities"));
    },

    <!----------------------------------------------"delete" button for each activity-->
    deleteActivity: function (activity, index) {
      if (confirm('Are you sure you want to delete this activity? This action cannot be undone.', 'Warning')) {
        //activity confirmed delete
        const userId = user.id;
        const url = `/api/activities/${userId}`;
        axios.delete(url)
            .then(response =>
                //delete from the local state so Vue will reload list automatically
                this.activities.splice(index, 1).push(response.data))
            .catch(function (error) {
              console.log(error)
            });
      }
    },
    <!---------------------------------------------------------------------------- -->

    <!----------------------------------------------"add" button for a new activity-->
    addActivity: function (){
      const url = `/api/activities`;
      axios.post(url,
          {
            description: this.formData.description,
            duration: this.formData.duration,
            calories: this.formData.calories,
            started: this.formData.started,
          })
          .then(response => {
            this.activities.push(response.data)
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