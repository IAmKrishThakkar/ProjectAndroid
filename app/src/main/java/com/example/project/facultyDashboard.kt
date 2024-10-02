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

import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyAttendanceScreen(navController: NavController, Fid: Int) {
    var selectedClass by remember { mutableStateOf<classes?>(null) }
    var students by remember { mutableStateOf<List<StudentLogin>>(emptyList()) }
    var attendanceStatus by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var selectedDate by remember { mutableStateOf(LocalDateTime.now()) }
    var classesList by remember { mutableStateOf<List<classes>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Date Picker Dialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDay, 0, 0)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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

    Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Color(0xFFe0f7fa), Color(0xFFb2ebf2))))){
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Take Attendance", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                BottomNavigationBar1(navController, Fid)
            },
            content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        errorMessage?.let {
                            Text("Error: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        // Date Picker Button
                        Text("Selected Date: ${selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}", style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { datePickerDialog.show() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).shadow(8.dp, shape = MaterialTheme.shapes.medium)
                        ) {
                            Text("Select Date")
                        }

                        // Class selection
                        if (classesList.isNotEmpty()) {
                            Text("Select a Class:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))

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
                            Text("No classes available", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Student attendance cards
                        students.forEach { student ->
                            AttendanceCard(student, attendanceStatus, { status ->
                                attendanceStatus = attendanceStatus.toMutableMap().also { it[student.id] = status }
                            })
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit attendance button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    students.forEach { student ->
                                        val status = attendanceStatus[student.id] ?: 0
                                        insertOrUpdateAttendance(student.id, status, selectedDate)
                                    }
                                    Toast.makeText(context, "Attendance Submitted", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth().shadow(8.dp, shape = MaterialTheme.shapes.large)
                        ) {
                            Text("Submit Attendance")
                        }
                    }
                }
            }
        )
    }
}

// Attendance Card with Creative Design
@Composable
fun AttendanceCard(student: StudentLogin, attendanceStatus: Map<Int, Int>, onStatusChange: (Int) -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clip(MaterialTheme.shapes.large) // Rounded corners
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(student.stud_name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Class: ${student.roll_no}", style = MaterialTheme.typography.bodySmall)
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
                    onStatusChange(when (status) {
                        0 -> 1
                        1 -> 2
                        2 -> 0
                        else -> 0
                    })
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = Color.White),
                shape = MaterialTheme.shapes.medium // Rounded button
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

// Class Card with Creative Design
@Composable
fun ClassCard(
    classItem: classes,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp), // Increased shadow
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clip(MaterialTheme.shapes.large) // Rounded corners
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(classItem.className, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(classItem.department, style = MaterialTheme.typography.bodySmall)
            }
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}

// Bottom Navigation Bar with Consistent Styling
@Composable
fun BottomNavigationBar1(navController: NavController, Fid: Int) {
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

// Other functions remain unchanged...

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun FacultyAttendanceScreenPreview() {
    ProjectTheme {
        val navController = rememberNavController()
        FacultyAttendanceScreen(navController = navController, Fid = 101)
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
private suspend fun insertOrUpdateAttendance(studentId: Int, status: Int, selectedDate: LocalDateTime) {
    withContext(Dispatchers.IO) {
        try {
            val date = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // Check if attendance exists for this student and date
            val existingAttendance = RetrofitInstance.api.getAttendanceForDate(studentId, date).execute()

            if (existingAttendance.isSuccessful && existingAttendance.body()?.isNotEmpty() == true) {
                // Attendance exists, update it
                val requestBody = mapOf(
                    "id" to existingAttendance.body()?.get(0)?.id.toString(),  // Use existing attendance ID
                    "student_id" to studentId.toString(),
                    "status" to status.toString(),
                    "date" to date
                )

                val response = RetrofitInstance.api.updateAttendance(
                    id = requestBody["id"]!!.toRequestBody(),
                    studentId = requestBody["student_id"]!!.toRequestBody(),
                    status = requestBody["status"]!!.toRequestBody(),
                    date = requestBody["date"]!!.toRequestBody()
                )

                if (response.isSuccessful) {
                    println("Attendance updated successfully for student $studentId")
                } else {
                    println("Failed to update attendance: ${response.errorBody()?.string()}")
                }
            } else {
                // No attendance exists, insert new record
                insertAttendance(studentId, status, date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private suspend fun insertAttendance(studentId: Int, status: Int, date: String) {
    withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf(
                "student_id" to studentId.toString(),
                "status" to status.toString(),
                "date" to date
            )

            val response = RetrofitInstance.api.insertAttendance(
                studentId = requestBody["student_id"]!!.toRequestBody(),
                status = requestBody["status"]!!.toRequestBody(),
                date = requestBody["date"]!!.toRequestBody()
            )

            if (response.isSuccessful) {
                println("Attendance inserted successfully for student $studentId")
            } else {
                println("Failed to insert attendance: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}




