<template id="user-bodyMeasurement-overview">
  <div>
    <h3>BodyMeasurements list </h3>
    <ul>
      <li v-for="bodyMeasurement in bodyMeasurements">
        {{bodyMeasurement.id}}: is {{bodyMeasurement.weight}} Kg with {{bodyMeasurement.height}} cm
      </li>
    </ul>
  </div>
</template>

<script>
Vue.component("user-bodyMeasurement-overview",{
  template: "#user-bodyMeasurement-overview",
  data: () => ({
    bodyMeasurements: [],
  }),
  created() {
    const userId = this.$javalin.pathParams["user-id"];
    axios.get(`/api/users/${userId}/bodyMeasurements`)
        .then(res => this.bodyMeasurements = res.data)
        .catch(() => alert("Error while fetching bodyMeasurements"));
  }
});
</script>