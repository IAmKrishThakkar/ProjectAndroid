package com.example.project

import ErrorScreen
import LoadingScreen
import Model.Faculty
import Model.StudentLogin
import ProjectTheme
import RetrofitInstance
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyDetail(navController: NavController, studId: Int) {
    var facultyList by remember { mutableStateOf<List<Faculty>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(refreshTrigger) {
        coroutineScope.launch { refreshData() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Faculty Details", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshTrigger += 1 }) {
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
                    NavigationIcon(navController, "Assignment/$studId", Icons.Filled.Assignment, "Marks")
                    NavigationIcon(navController, "Faculty/$studId", Icons.Filled.School, "Faculty")
                    NavigationIcon(navController, "Profile/$studId", Icons.Filled.Person, "Profile")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LoadingScreen(paddingValues)
            } else if (errorMessage != null) {
                ErrorScreen(errorMessage = errorMessage!!) {
                    coroutineScope.launch { refreshData() }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(facultyList) { faculty ->
                        FacultyCard(faculty)
                    }
                }
            }
        }
    }
}

@Composable
fun FacultyCard(faculty: Faculty) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* Handle click */ },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Replace with faculty profile image if available
            Image(
                painter = rememberAsyncImagePainter(model = "https://netxgroup.in/students/${faculty.profilePhoto}"),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(faculty.name ?: "Unknown", style = MaterialTheme.typography.titleLarge)
                Text(faculty.department ?: "No Department", style = MaterialTheme.typography.bodyMedium)
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
