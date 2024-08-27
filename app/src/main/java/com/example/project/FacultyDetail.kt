package com.example.project

import Model.Faculty
import Model.StudentLogin
import ProjectTheme
import RetrofitInstance
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyDetail(navController: NavController, studId: Int) {
    var facultyList by remember { mutableStateOf<List<Faculty>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var stud_id by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val context = LocalContext.current

    // Function to fetch and refresh faculty data
    suspend fun refreshData() {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getFaculty().execute()
            }


            if (response.isSuccessful) {
                facultyList = response.body() ?: emptyList()
            } else {
                errorMessage = "Failed to fetch data: ${response.code()}"
            }

            val Sresponse = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getStudents().execute()
            }
            if (Sresponse.isSuccessful) {
                stud_id=studId
                isLoading = false
            } else {
                errorMessage = "Error: ${response.message()}"
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(refreshTrigger) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Faculty Details")
                },
                actions = {
                    IconButton(onClick = {
                        refreshTrigger += 1
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
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
                    NavigationIcon(navController, "StudentDashboard/$studId", Icons.Filled.Home, "Home")
                    NavigationIcon(navController, "Assignment", Icons.Filled.Assignment, "Marks")
                    NavigationIcon(navController, "Faculty/$studId", Icons.Filled.School, "Faculty")
                    NavigationIcon(navController, "Profile/$studId", Icons.Filled.Person, "Profile")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Text(text = "Loading...", color = Color.Black, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(facultyList) { faculty ->
                        FacultyItem(
                            facultyName = faculty.name ?: "Unknown",
                            subject = faculty.department ?: "No Department"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FacultyItem(facultyName: String, subject: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = facultyName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = subject,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFacultyDetail() {
    ProjectTheme {
        FacultyDetail(navController = rememberNavController(), studId = 101)
    }
}
