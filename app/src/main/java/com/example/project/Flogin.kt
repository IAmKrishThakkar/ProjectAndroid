package com.example.project

import LoginForm
import ProjectTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun Flogin(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LoginForm(
        title = "Faculty Login",
        buttonText = "Faculty Login",
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        passwordVisibility = passwordVisibility,
        onPasswordVisibilityChange = { passwordVisibility = !passwordVisibility },
        isLoading = isLoading,
        onSubmit = {
            if (email.isEmpty() || password.isEmpty()) {
                errorMessage = "Please fill in both fields."
            } else {
                isLoading = true
                // Handle login logic here
                // Simulate login success for now
                navController.navigate("StudentDashboard")
                isLoading = false
            }
        },
        errorMessage = errorMessage,
        imageResId = R.drawable.im4
    )
}

@Preview
@Composable
fun FloginPreview() {
    ProjectTheme {
        val navController = rememberNavController()
        Flogin(navController)
    }
}
