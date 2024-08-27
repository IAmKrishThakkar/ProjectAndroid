package com.example.project

import Model.Attendance
import Model.PendingAssignment
import Model.StudentTimetable
import ProjectTheme
import RetrofitInstance
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(navController: NavController, studID: Int) {
    var studentName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var rollno by remember { mutableStateOf<String?>(null) }
    var classId by remember { mutableStateOf<Int?>(null) }
    var timetable by remember { mutableStateOf<List<StudentTimetable>>(emptyList()) }
    var assignment by remember { mutableStateOf<List<PendingAssignment>>(emptyList()) }
    var attendance by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currentDay = LocalDate.now().dayOfWeek.name
    var refreshTrigger by remember { mutableStateOf(0) } // Trigger state for refreshing

    val context = LocalContext.current

    // Function to fetch and refresh data
    suspend fun refreshData() {
        isLoading = true
        try {
            // Fetch student data
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getStudents().execute()
            }

            if (response.isSuccessful) {
                val students = response.body() ?: emptyList()
                val student = students.find { it.id == studID }
                if (student != null) {
                    studentName = student.stud_name
                    classId = student.class_id
                    rollno = student.roll_no

                    // Fetch timetable
                    classId?.let {
                        val timetableResponse = withContext(Dispatchers.IO) {
                            RetrofitInstance.api.getTimetable(it).execute()
                        }
                        if (timetableResponse.isSuccessful) {
                            timetable = timetableResponse.body() ?: emptyList()
                        } else {
                            errorMessage = "Failed to fetch timetable: ${timetableResponse.code()}"
                        }

                        // Fetch pending assignments
                        val pendingAssignmentResponse = withContext(Dispatchers.IO) {
                            RetrofitInstance.api.getPendingAssignment(it).execute()
                        }
                        if (pendingAssignmentResponse.isSuccessful) {
                            assignment = pendingAssignmentResponse.body() ?: emptyList()
                        } else {
                            errorMessage = "Failed to fetch pending assignments: ${pendingAssignmentResponse.code()}"
                        }

                        // Fetch attendance
                        val attendanceResponse = withContext(Dispatchers.IO) {
                            RetrofitInstance.api.getAttendance(it).execute()
                        }
                        if (attendanceResponse.isSuccessful) {
                            attendance = (attendanceResponse.body() ?: emptyList()) as List<Attendance>
                        } else {
                            errorMessage = "Failed to fetch attendance: ${attendanceResponse.code()}"
                        }
                    }
                    Toast.makeText(context, "Welcome, ${student.stud_name} Roll No: $rollno $currentDay", Toast.LENGTH_SHORT).show()
                } else {
                    errorMessage = "Student not found."
                }
            } else {
                errorMessage = "Failed to fetch data: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Trigger the data fetch when the composable is first launched or when refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = when {
                        studentName != null -> "$studentName : $rollno"
                        isLoading -> "Loading..."
                        errorMessage != null -> errorMessage ?: "Error"
                        else -> "Student Dashboard"
                    })
                },
                actions = {
                    IconButton(onClick = {
                        refreshTrigger += 1 // Increment trigger to refresh data
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }

                    IconButton(onClick = { /* Handle menu click */ }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
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
                    NavigationIcon(navController, "StudentDashboard/$studID", Icons.Filled.Home, "Home")
                    NavigationIcon(navController, "Assignment", Icons.Filled.Assignment, "Marks")
                    NavigationIcon(navController, "Faculty/$studID", Icons.Filled.School, "Faculty")
                    NavigationIcon(navController, "Profile/$studID", Icons.Filled.Person, "Profile")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        item {
                            SectionTitle(title = "Today's Classes")
                        }

                        item {
                            val classesToday = timetable.filter {
                                it.day.equals(currentDay, ignoreCase = true) && it.classId == classId
                            }

                            if (classesToday.isNotEmpty()) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    items(classesToday) { classInfo ->
                                        TodayClassCard(
                                            subject = classInfo.subject,
                                            stime = classInfo.startTime,
                                            etime = classInfo.endTime
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "No classes today",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        item {
                            SectionTitle(title = "Attendance")
                        }

                        item {
                            AttendanceArea()
                        }

                        item {
                            SectionTitle(title = "Pending Assignments")
                        }

                        items(assignment) { assign ->
                            PendingAssignmentCard(
                                subject = assign.subject,
                                description = assign.description,
                                dueDate = assign.submissionDeadline
                            )
                        }
                    }
                }
            }
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CurrentMonthDisplay() {
    val currentDate = LocalDate.now()
    val currentMonth = currentDate.month
    val currentYear = currentDate.year
    val daysInMonth = currentDate.lengthOfMonth()
    val firstDayOfMonth = currentDate.withDayOfMonth(1).dayOfWeek.value
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formattedDate = currentDate.format(formatter)

    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        Text(
            text = "$currentMonth : $currentYear",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )

        Spacer(modifier = Modifier.height(8.dp))

        DayNamesRow()

        Spacer(modifier = Modifier.height(8.dp))

        CalendarGrid(daysInMonth, firstDayOfMonth)
    }
}

@Composable
fun DayNamesRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun CalendarGrid(daysInMonth: Int, firstDayOfMonth: Int) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .height(260.dp)

    ) {
        // Add blank spaces for the days before the first of the month
        items(firstDayOfMonth - 1) {
            Box(modifier = Modifier
                .size(20.dp)
            ) // Empty box for padding
        }

        // Add day numbers
        items(daysInMonth) { index ->
            val day = index + 1
            DayCard(day)
        }
    }
}

@Composable
fun DayCard(day: Int) {
    Card(
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.small),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceArea() {

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(7.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            CurrentMonthDisplay()
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun TodayClassCard(subject: String, stime: String, etime: String) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .height(110.dp)
            .width(185.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 9.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = subject, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Start: $stime", style = MaterialTheme.typography.titleSmall)
            Text(text = "End: $etime", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun PendingAssignmentCard(subject: String, description: String, dueDate: String) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 9.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subject,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSecondary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Due: $dueDate",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewStudentDashboard() {
    ProjectTheme {
        StudentDashboard(
            navController = rememberNavController(),
            studID = 1
        )
    }
}
