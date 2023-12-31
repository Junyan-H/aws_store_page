// npm package that stores sensitive info in .env file
require('dotenv').config()


// require express package
const express= require('express')

// different routes for api endpoints
const workoutRoutes = require('./routes/movies')

//express app
const Fabflix_app = express()

//middleware to log requests
Fabflix_app.use((req,res,next) =>{
    console.log(req.path, req.method)
    next()
})

//routes
Fabflix_app.use('/api/workouts',workoutRoutes)

//listen for requests on port ...
Fabflix_app.listen(process.env.PORT, () =>{
    // console msg
    console.log('listening on port ',process.env.PORT)
})

