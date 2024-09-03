package com.example.project

import Model.AssignmentSubmit
import Model.PendingAssignment
import Model.StudentLogin
import ProjectTheme
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
    var pendingAssignments by remember { mutableStateOf<List<PendingAssignment>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var class_ID by remember { mutableStateOf<Int?>(null)}

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val assignments = fetchAssignmentDetails()
                val students = assignments.associate { assignment ->
                    assignment.studentId to getStudentsByStudId(assignment.studentId).firstOrNull()
                }
                val pendingAssignmentsData = getPendingAssignmentbyfaculty_id(Fid)

                assignmentDetails = assignments
                studentDetails = students as Map<Int, StudentLogin>
                pendingAssignments = pendingAssignmentsData
                class_ID = pendingAssignmentsData.firstOrNull()?.classId
                loading = false
            } catch (e: Exception) {
                errorMessage = e.message
                loading = false
            }
        }
    }

    fun handleDelete(assignmentId: Int) {
        coroutineScope.launch {
            try {
                deletePendingAssignment(assignmentId)
                // Refresh the pending assignments list after deletion
                val updatedPendingAssignments = getPendingAssignmentbyfaculty_id(Fid)
                pendingAssignments = updatedPendingAssignments
            } catch (e: Exception) {
                errorMessage = e.message
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
                },
                actions = {
                    IconButton(onClick = { navController.navigate("AddAssignment/${Fid}/${class_ID ?: 0}") }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Pending Assignment")
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
                .padding(innerPadding)
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
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        item {
                            Text(
                                text = "Submitted Assignments",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        assignmentDetails?.let { details ->
                            items(details) { assignment ->
                                AssignmentDetailsCard(assignment, studentDetails)
                            }
                        }
                        item {
                            Text(
                                text = "Pending Assignments",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        pendingAssignments?.let { assignments ->
                            items(assignments) { assignment ->
                                PendingAssignmentCard(
                                    pendingAssignment = assignment,
                                    onDelete = { assignmentId -> handleDelete(assignmentId) }
                                )
                            }
                        }
                        if (assignmentDetails.isNullOrEmpty() && pendingAssignments.isNullOrEmpty()) {
                            item {
                                Text(
                                    text = "No assignment details available",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AssignmentDetailsCard(
    assignment: AssignmentSubmit,
    studentDetails: Map<Int, StudentLogin>
) {
    val context = LocalContext.current
    val student = studentDetails[assignment.studentId]

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
                Text(
                    text = "Assignment Name: ${assignment.assignmentId}", // Update this if necessary
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Submission Time: ${assignment.submissionTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis
                )
            }

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

@Composable
fun PendingAssignmentCard(
    pendingAssignment: PendingAssignment,
    onDelete: (Int) -> Unit
) {
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
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = "Subject: ${pendingAssignment.subject}",
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Description: ${pendingAssignment.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Submission Deadline: ${pendingAssignment.submissionDeadline}",
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = { onDelete(pendingAssignment.id) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Assignment")
            }
        }
    }
}



private suspend fun getPendingAssignmentbyfaculty_id(facultyId: Int): List<PendingAssignment> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.getPendingAssignmentbyfaculty_id(facultyId).execute()
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


@Composable
fun PendingAssignmentsList(pendingAssignments: List<PendingAssignment>) {
    Column(modifier = Modifier.padding(16.dp)) {
        pendingAssignments.forEach { assignment ->
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "Subject: ${assignment.subject}",
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Description: ${assignment.description}",
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Submission Deadline: ${assignment.submissionDeadline}",
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
            NavigationIcon(navController, "FacultyProfile/$Fid", Icons.Filled.Person, "Profile")
        }
    }
}
@Composable
fun AssignmentDetailsList(
    assignmentDetails: List<AssignmentSubmit>,
    studentDetails: Map<Int, StudentLogin>
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        assignmentDetails.forEach { assignment ->
            val student = studentDetails[assignment.studentId]

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
                        Text(
                            text = "Assignment Name: ${assignment.assignmentId}", // This should be a description
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis
                        )
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

private suspend fun deletePendingAssignment(assignmentId: Int) {
    try {
        val response = RetrofitInstance.api.deletePendingAssignment(assignmentId)
        if (response.isSuccessful) {

        } else {
            throw Exception("Failed to delete pending assignment. Response code: ${response.code()}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        throw Exception("Error deleting pending assignment: ${e.message}")
    }
}

