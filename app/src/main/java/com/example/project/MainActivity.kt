package com.example.project

import AddAssignment
import AssignmentUploadScreen
import ProjectTheme
import StudentDetail
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project.ui.welcomepage.WelcomePage

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialUIApp()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MaterialUIApp() {
    val context = LocalContext.current
    ProjectTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "welcome") {
            composable("welcome") {
                WelcomePage(navController)
            }
            composable("Slogin") {
                Slogin(navController)
            }
            composable("Flogin") {
                Flogin(navController)
            }
            composable("ForgotPass") {
                ForgotPass(navController)
            }
            composable("ForgotFacultyPass") {
                ForgotFacultyPass(navController)
            }
            composable("StudentDashboard/{stud_id}") { backStackEntry ->
                val studId = backStackEntry.arguments?.getString("stud_id")?.toIntOrNull()
                if (studId != null) {
                    StudentDashboard(navController, studId)
                } else {
                    // Handle the invalid stud_id scenario
                    Toast.makeText(context, "Invalid Student ID", Toast.LENGTH_SHORT).show()

                }
            }
            composable("FacultyAttendanceScreen/{Fid}") { backStackEntry ->
                val Fid = backStackEntry.arguments?.getString("Fid")?.toIntOrNull()
                if (Fid != null) {
                    FacultyAttendanceScreen(navController, Fid)
                } else {
                    // Handle the invalid stud_id scenario
                    Toast.makeText(context, "Invalid Faculty ID", Toast.LENGTH_SHORT).show()

                }
            }
            composable("AddAssignment/{Fid}/{class_ID}"){ backStackEntry ->
                val Fid = backStackEntry.arguments?.getString("Fid")?.toIntOrNull()
                val class_ID = backStackEntry.arguments?.getString("class_ID")?.toIntOrNull()
                if (Fid != null) {
                    if (class_ID != null) {
                        AddAssignment(navController, Fid,class_ID)
                    }
                } else {
                    // Handle the invalid stud_id scenario
                    Toast.makeText(context, "Invalid Faculty ID", Toast.LENGTH_SHORT).show()

                }

            }
            composable("FacultyProfile/{Fid}"){ backStackEntry ->
                val Fid = backStackEntry.arguments?.getString("Fid")?.toIntOrNull()
                if (Fid != null) {
                    FacultyProfile(navController, Fid)
                } else {
                    // Handle the invalid stud_id scenario
                    Toast.makeText(context, "Invalid Faculty ID", Toast.LENGTH_SHORT).show()

                }

            }
            composable("StudentDetail/{Fid}") { backStackEntry ->
                val Fid = backStackEntry.arguments?.getString("Fid")?.toIntOrNull()
                if (Fid != null) {
                    StudentDetail(navController, Fid)
                } else {
                    // Handle the invalid stud_id scenario
                    Toast.makeText(context, "Invalid Faculty ID", Toast.LENGTH_SHORT).show()

                }
            }
            composable("AssignmentDetail/{Fid}") { backStackEntry ->
                val Fid = backStackEntry.arguments?.getString("Fid")?.toIntOrNull()
                if (Fid != null) {
                    AssignmentDetail(navController, Fid)
                } else {
                    // Handle the invalid stud_id scenario
                    Toast.makeText(context, "Invalid Faculty ID", Toast.LENGTH_SHORT).show()

                }
            }
            composable("Profile/{stud_id}") { backStackEntry ->
                val stud_id = backStackEntry.arguments?.getString("stud_id")?.toIntOrNull()
                if (stud_id != null) {
                    Profile(navController, stud_id)
                } else {
                    // Handle invalid stud_id scenario
                }
            }
            composable("Faculty/{stud_id}") { backStackEntry ->
                val stud_id = backStackEntry.arguments?.getString("stud_id")?.toIntOrNull()
                if (stud_id != null) {
                    FacultyDetail(navController, stud_id)
                } else {
                    // Handle invalid stud_id scenario
                }
            }
            composable("Assignment/{stud_id}") { backStackEntry ->
                val stud_id = backStackEntry.arguments?.getString("stud_id")?.toIntOrNull()
                if (stud_id != null) {
                    AssignmentUploadScreen(navController, stud_id)
                } else {
                    // Handle invalid stud_id scenario
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MaterialUIAppPreview() {
    MaterialUIApp()
}
