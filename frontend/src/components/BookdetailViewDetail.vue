<template>

    <v-card outlined>
        <v-card-title>
            Bookdetail # {{$route.params.id }}
        </v-card-title>

        <v-card-text>
            <div>
                <Number label="BookId" v-model="item.bookId" :editMode="editMode" @change="change" />
            </div>
            <div>
                <String label="Status" v-model="item.status" :editMode="editMode" @change="change" />
            </div>
        </v-card-text>
    </v-card>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'BookdetailViewDetail',
    props: {
      value: Object,
    },
    data: () => ({
        item : [],
    }),
    async created() {
      var params = this.$route.params;
      var temp = await axios.get(axios.fixUrl('/bookdetails/' + params.id))

      this.item = temp.data;

    },
    methods: {
    }
  }
</script>

