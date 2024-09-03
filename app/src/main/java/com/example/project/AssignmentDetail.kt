package com.example.project

import Model.AssignmentSubmit
import Model.PendingAssignment
import Model.StudentLogin
import ProjectTheme
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
fun AssignmentDetail(navController: NavController, Fid: Int) {
    var assignmentDetails by remember { mutableStateOf<List<AssignmentSubmit>?>(null) }
    var studentDetails by remember { mutableStateOf<Map<Int, StudentLogin>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var assignmentsDetail by remember { mutableStateOf<Map<Int, PendingAssignment>>(emptyMap()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val assignments = fetchAssignmentDetails()
                val students = assignments.associate { assignment ->
                    assignment.studentId to getStudentsByStudId(assignment.studentId).firstOrNull()
                }
                val pendingAssignments = assignments.associate { assignment ->
                    assignment.assignmentId to getPendingAssignmentByAssId(assignment.assignmentId).firstOrNull()
                }
                assignmentDetails = assignments
                studentDetails = students as Map<Int, StudentLogin>
                assignmentsDetail = pendingAssignments as Map<Int, PendingAssignment>
                loading = false
            } catch (e: Exception) {
                errorMessage = e.message
                loading = false
            }
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
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, Fid)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding() + 56.dp,
                    bottom = innerPadding.calculateBottomPadding() + 56.dp // BottomAppBar height
                )
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    Snackbar(modifier = Modifier.align(Alignment.Center)) {
                        Text(text = "Error: $errorMessage")
                    }
                }
                assignmentDetails != null && assignmentDetails!!.isNotEmpty() -> {
                    Spacer(modifier = Modifier.height(15.dp))
                    AssignmentDetailsList(assignmentDetails!!, studentDetails, assignmentsDetail)
                }
                else -> {
                    Text("No assignment details available", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, Fid: Int) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
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

@Composable
fun AssignmentDetailsList(
    assignmentDetails: List<AssignmentSubmit>,
    studentDetails: Map<Int, StudentLogin>,
    assignmentsDetail: Map<Int, PendingAssignment>
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        assignmentDetails.forEach { assignment ->
            val student = studentDetails[assignment.studentId]
            val ass = assignmentsDetail[assignment.assignmentId]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column for content
                    Column(
                        modifier = Modifier
                            .weight(1f) // Takes up remaining space
                    ) {
                        student?.let {
                            Text(
                                text = "Student Name: ${it.stud_name}, Roll No: ${it.roll_no}",
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ass?.let {
                            Text(
                                text = "Assignment Name: ${it.subject}",
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Submission Time: ${assignment.submissionTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // PDF Icon button
                    IconButton(
                        onClick = {
                            val url = "https://netxgroup.in/students/${assignment.fileLocation}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(start = 8.dp) // Padding to separate from content
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "Open PDF")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AssignmentDetailPreview() {
    ProjectTheme {
        val navController = rememberNavController()
        AssignmentDetail(navController = navController, Fid = 101)
    }
}

suspend fun fetchAssignmentDetails(): List<AssignmentSubmit> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.getAssignment().execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch assignment details: ${response.message()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error fetching assignment details: ${e.message}")
        }
    }
}

private suspend fun getStudentsByStudId(studId: Int): List<StudentLogin> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.getStudentsByStudId(studId).execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("Failed to load students. Response code: ${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error fetching students: ${e.message}")
        }
    }
}

private suspend fun getPendingAssignmentByAssId(assId: Int): List<PendingAssignment> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.getPendingAssignmentByAss_id(assId).execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("Failed to load pending assignments. Response code: ${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error fetching pending assignments: ${e.message}")
        }
    }
}
