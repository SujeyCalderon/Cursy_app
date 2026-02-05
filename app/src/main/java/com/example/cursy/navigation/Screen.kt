package com.example.cursy.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Feed : Screen("feed")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
    
    object CreateCourse : Screen("create_course")
    object EditCourse : Screen("edit_course/{courseId}") {
        fun createRoute(courseId: String) = "edit_course/$courseId"
    }
    object CourseDetail : Screen("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
}