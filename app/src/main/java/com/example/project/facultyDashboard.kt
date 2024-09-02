package com.example.project

import Model.StudentLogin
import Model.classes
import ProjectTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.annotation.RequiresApi
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.project.ui.theme.CustomWhite

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FacultyAttendanceScreen(navController: NavController, Fid: Int) {
    var selectedClass by remember { mutableStateOf<classes?>(null) }
    var students by remember { mutableStateOf<List<StudentLogin>>(emptyList()) }
    var attendanceStatus by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var classesList by remember { mutableStateOf<List<classes>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Fid) {
        isLoading = true
        try {
            classesList = getClassesByFacultyId(Fid)
        } catch (e: Exception) {
            errorMessage = "Error fetching classes: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Take Attendance", style = MaterialTheme.typography.titleLarge) },
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
                    NavigationIcon(navController, "Faculty/$Fid", Icons.Filled.Face, "Students")
                    NavigationIcon(navController, "Profile/$Fid", Icons.Filled.Person, "Profile")
                }
            }
        },
        content = { paddingValues ->
            Column(modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    errorMessage?.let {
                        Text("Error: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    // Display classes as cards
                    if (classesList.isNotEmpty()) {
                        Text("Select a Class:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn {
                            items(classesList) { classItem ->
                                ClassCard(
                                    classItem = classItem,
                                    isSelected = selectedClass == classItem,
                                    onClick = {
                                        selectedClass = classItem
                                        coroutineScope.launch {
                                            students = fetchStudents(classItem.id)
                                            attendanceStatus = students.associate { it.id to 0 }
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    } else {
                        Text("No classes available")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display list of students with enhanced card styling
                    students.forEach { student ->
                        Card(
                            elevation = CardDefaults.cardElevation(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(student.stud_name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Class: ${student.roll_no}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                                val status = attendanceStatus[student.id] ?: 0
                                val buttonColor = when (status) {
                                    0 -> Color.Red
                                    1 -> Color.Green
                                    2 -> Color.Blue
                                    else -> Color.Gray
                                }
                                Button(
                                    onClick = {
                                        attendanceStatus = attendanceStatus.toMutableMap().also {
                                            it[student.id] = when (status) {
                                                0 -> 1
                                                1 -> 2
                                                2 -> 0
                                                else -> 0
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = Color.White),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        when (status) {
                                            0 -> "Absent"
                                            1 -> "Present"
                                            2 -> "Granted"
                                            else -> "Unknown"
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit attendance button with enhanced styling
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                students.forEach { student ->
                                    val status = attendanceStatus[student.id] ?: 0
                                    insertAttendance(student.id, status)
                                }
                                Toast.makeText(context, "Attendance Submitted", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Submit Attendance")
                    }
                }
            }
        }
    )
}

@Composable
fun ClassCard(
    classItem: classes,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(if (isSelected) Color.White else Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(classItem.className, style = MaterialTheme.typography.bodyLarge)
                Text(classItem.department, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


private suspend fun getClassesByFacultyId(facultyId: Int): List<classes> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.getClassesByFacultyId(facultyId).execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

private suspend fun fetchStudents(classId: Int): List<StudentLogin> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.getStudentsByClassId(classId).execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private suspend fun insertAttendance(studentId: Int, status: Int) {
    withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf(
                "student_id" to studentId.toString(),
                "status" to status.toString(),
                "date" to getCurrentDate()
            )

            val response = RetrofitInstance.api.insertAttendance(
                studentId = requestBody["student_id"]!!.toRequestBody(),
                status = requestBody["status"]!!.toRequestBody(),
                date = requestBody["date"]!!.toRequestBody()
            )

            if (!response.isSuccessful) {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentDate(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return current.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun FacultyAttendanceScreenPreview() {
    ProjectTheme {
        val navController = rememberNavController()
        FacultyAttendanceScreen(navController = navController, Fid = 101)
    }
}
