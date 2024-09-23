
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import DonateDonateManager from "./components/listers/DonateDonateCards"
import DonateDonateDetail from "./components/listers/DonateDonateDetail"

import RequestRequestManager from "./components/listers/RequestRequestCards"
import RequestRequestDetail from "./components/listers/RequestRequestDetail"


import BooksBooksManager from "./components/listers/BooksBooksCards"
import BooksBooksDetail from "./components/listers/BooksBooksDetail"


export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/donates/donates',
                name: 'DonateDonateManager',
                component: DonateDonateManager
            },
            {
                path: '/donates/donates/:id',
                name: 'DonateDonateDetail',
                component: DonateDonateDetail
            },

            {
                path: '/requests/requests',
                name: 'RequestRequestManager',
                component: RequestRequestManager
            },
            {
                path: '/requests/requests/:id',
                name: 'RequestRequestDetail',
                component: RequestRequestDetail
            },


            {
                path: '/books/books',
                name: 'BooksBooksManager',
                component: BooksBooksManager
            },
            {
                path: '/books/books/:id',
                name: 'BooksBooksDetail',
                component: BooksBooksDetail
            },



    ]
})
