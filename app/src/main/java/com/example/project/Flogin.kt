package com.example.project

import LoginForm
import ProjectTheme
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.*

@Composable
fun Flogin(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

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
                errorMessage = ""
                validateFacultyLogin(email, password, navController) { error, facultyId ->
                    isLoading = false
                    if (error == null) {
                        facultyId?.let {
                            navController.navigate("FacultyAttendanceScreen/$it")
                        }
                    } else {
                        errorMessage = error
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        },
        errorMessage = errorMessage,
        imageResId = R.drawable.im4
    )
}

fun validateFacultyLogin(
    email: String,
    password: String,
    navController: NavController,
    onValidationComplete: (String?, String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.getFaculty().execute()
            if (response.isSuccessful) {
                val facultyList = response.body() ?: emptyList()
                val faculty = facultyList.find { it.email == email && it.password == password }

                withContext(Dispatchers.Main) {
                    if (faculty != null) {
                        onValidationComplete(null, faculty.id.toString()) // Pass faculty ID
                    } else {
                        onValidationComplete("Invalid email or password.", null)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onValidationComplete("Error fetching data: ${response.code()}", null)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onValidationComplete("Error: ${e.message}", null)
            }
        }
    }
}


@Preview
@Composable
fun FloginPreview() {
    ProjectTheme {
        val navController = rememberNavController()
        Flogin(navController)
    }
}
