<template id="bodyMeasurement-profile">
  <div>
    <form v-if="bodyMeasurement">
      <label class="col-form-label">User ID: </label>
      <input class="form-control" v-model="user.id" name="id" type="number" readonly/><br>
      <label class="col-form-label">Weight: </label>
      <input class="form-control" v-model="bodyMeasurement.weight" name="weight" type="number"/><br>
      <label class="col-form-label">Height: </label>
      <input class="form-control" v-model="bodyMeasurement.height" name="height" type="number"/><br>
    </form>
  </div>
</template>

<script>
Vue.component("bodyMeasurement-profile", {
  template: "#bodyMeasurement-profile",
  data: () => ({
    bodyMeasurement: null
  }),
  created: function () {
    const userId = this.$javalin.pathParams["user-id"];
    const url = `/api/bodyMeasurements/${userId}`
    axios.get(url)
        .then(res => this.bodyMeasurement = res.data)
        .catch(() => alert("Error while fetching bodyMeasurement" + userId));
  }
});
</script>