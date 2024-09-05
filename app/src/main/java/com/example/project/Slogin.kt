package com.example.project

import LoginForm
import ProjectTheme
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.*
import java.lang.reflect.Modifier

@Composable
fun Slogin(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var stud_id by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    LoginForm(
        title = "Student Login",
        buttonText = "Student Login",
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
                validateLogin(email, password, navController) { error, studID ->
                    isLoading = false
                    if (error == null) {
                        // Successfully logged in
                        navController.navigate("StudentDashboard/$studID")

                    } else {
                        errorMessage = error
                    }
                }
            }
        },
        errorMessage = errorMessage,
        imageResId = R.drawable.im5,
        onForgotPasswordClick = { navController.navigate("ForgotPass") }
    )

}

fun validateLogin(
    email: String,
    password: String,
    navController: NavController,
    onValidationComplete: (String?, Int?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.getStudents().execute()
            if (response.isSuccessful) {
                val students = response.body() ?: emptyList()
                val student = students.find { it.stud_email == email && it.password == password }
                val studId = student?.id
                withContext(Dispatchers.Main) {
                    if (student != null) {
                        onValidationComplete(null, studId)
                        println(studId)
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
            println(e.message)
        }
    }
}


@Composable
fun ForgotPasswordCard() {
    Card(

        colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.1f))
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, color = Color.Blue)
            )
        }
    }
}

@Preview
@Composable
fun SloginPreview() {
    ProjectTheme {
        val navController = rememberNavController()
        Slogin(navController)
    }
}
