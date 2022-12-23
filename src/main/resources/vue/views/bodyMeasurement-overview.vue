<template id="bodyMeasurement-overview">
  <div>
    <div>
      <ul class="bodyMeasurement-overview-list">
        <li v-for="bodyMeasurement in bodyMeasurements">
          <a :href="`/bodyMeasurements/${user.id}`">{{bodyMeasurement.weight}} ({{bodyMeasurement.height}})</a>
        </li>
      </ul>
    </div>
  </div>
</template>
<script>
Vue.component("bodyMeasurement-overview", {
  template: "#bodyMeasurement-overview",
  data: () => ({
    bodyMeasurements: [],
  }),
  created() {
    this.fetchUsers();
  },
  methods: {
    fetchUsers: function () {
      axios.get("/api/bodyMeasurements")
          .then(res => this.bodyMeasurements = res.data)
          .catch(() => alert("Error while fetching bodyMeasurements"));
    }
  }
});
</script>