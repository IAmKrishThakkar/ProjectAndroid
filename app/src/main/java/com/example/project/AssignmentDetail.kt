package com.example.project

import Model.AssignmentSubmit
import ProjectTheme
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
fun AssignmentDetail(navController: NavController, Fid: Int) {
    val (assignmentDetails, setAssignmentDetails) = remember { mutableStateOf<List<AssignmentSubmit>?>(null) }
    val (loading, setLoading) = remember { mutableStateOf(true) }
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        setLoading(true)
        try {
            val response = RetrofitInstance.api.getAssignment().execute()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    setAssignmentDetails(body)
                } else {
                    setErrorMessage("No data received")
                }
            } else {
                setErrorMessage("Failed to fetch assignment details: ${response.message()}")
            }
        } catch (e: Exception) {
            setErrorMessage("Error: ${e.message}")
        } finally {
            setLoading(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignment Details", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NavigationIcon(navController, "FacultyAttendanceScreen/$Fid", Icons.Filled.Home, "Home")
                    NavigationIcon(navController, "AssignmentDetail/$Fid", Icons.Filled.Assignment, "AssignmentManage")
                    NavigationIcon(navController, "StudentDetail/$Fid", Icons.Filled.Face, "Students")
                    NavigationIcon(navController, "Profile/$Fid", Icons.Filled.Person, "Profile")
                }
            }
        }
    ) {
        if (loading) {
            // Show a progress indicator
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else if (errorMessage != null) {
            // Show an error message
            Text("Error: $errorMessage", color = Color.Red, modifier = Modifier.padding(16.dp))
        } else if (assignmentDetails != null && assignmentDetails.isNotEmpty()) {
            AssignmentDetailsList(assignmentDetails)
        } else {
            Text("No assignment details available", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun AssignmentDetailsList(assignmentDetails: List<AssignmentSubmit>) {
    Column(modifier = Modifier.padding(16.dp)) {
        assignmentDetails.forEach { assignment ->
            Text(text = "Assignment ID: ${assignment.assignmentId}")
            println(assignment.assignmentId)
            Text(text = "Student ID: ${assignment.studentId}")
            Text(text = "Submission Time: ${assignment.submissionTime}")
            Text(text = "File Location: ${assignment.fileLocation}")
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AssD() {
    ProjectTheme {
        val navController = rememberNavController()
        AssignmentDetail(navController = navController, Fid = 101)
    }
}
