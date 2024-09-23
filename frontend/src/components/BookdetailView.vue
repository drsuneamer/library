<template>

    <v-data-table
        :headers="headers"
        :items="bookdetail"
        :items-per-page="5"
        class="elevation-1"
    ></v-data-table>

</template>

<script>
    const axios = require('axios').default;

    export default {
        name: 'BookdetailView',
        props: {
            value: Object,
            editMode: Boolean,
            isNew: Boolean
        },
        data: () => ({
            headers: [
                { text: "id", value: "id" },
                { text: "bookId", value: "bookId" },
                { text: "status", value: "status" },
            ],
            bookdetail : [],
        }),
          async created() {
            var temp = await axios.get(axios.fixUrl('/bookdetails'))

            temp.data._embedded.bookdetails.map(obj => obj.id=obj._links.self.href.split("/")[obj._links.self.href.split("/").length - 1])

            this.bookdetail = temp.data._embedded.bookdetails;
        },
        methods: {
        }
    }
</script>

