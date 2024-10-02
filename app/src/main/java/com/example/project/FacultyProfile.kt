package com.example.project

import Model.Faculty
import ProjectTheme
import RetrofitInstance
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.project.ui.theme.CustomBlue
import com.example.project.ui.theme.CustomGray
import com.example.project.ui.theme.CustomWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyProfile(navController: NavController, Fid: Int) {
    var facultyProfile by remember { mutableStateOf<Faculty?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Fid) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getFaculty().execute()
            }
            if (response.isSuccessful) {
                facultyProfile = response.body()?.find { it.id == Fid }
            } else {
                errorMessage = "Error: ${response.message()}"
            }
        } catch (e: Exception) {
            errorMessage = "Exception: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile", color = Color.Black) },
                actions = {
                    IconButton(onClick = { logout1(navController) }) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color.Black
                        )
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
                    NavigationIcon(navController, "FacultyAttendanceScreen/$Fid", Icons.Filled.Home, "Home")
                    NavigationIcon(navController, "AssignmentDetail/$Fid", Icons.Filled.Assignment, "AssignmentManage")
                    NavigationIcon(navController, "StudentDetail/$Fid", Icons.Filled.Face, "Students")
                    NavigationIcon(navController, "FacultyProfile/$Fid", Icons.Filled.Person, "Profile")
                }
            }
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CustomBlue)
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                facultyProfile?.let { profile ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            FacultyProfile(
                                profilepic = "https://netxgroup.in/students/${profile.profilePhoto}",
                                title = profile.name ?: "N/A",
                                details = listOf(
                                )
                            )
                        }
                        item {
                            FacultyProfileSection(
                                title = "About",
                                details = listOf(
                                    "Department: ${profile.department}",
                                    "Qualification: ${profile.qualification}",
                                    "Passing Year: ${profile.passingYear}",
                                    "Date of Birth: ${profile.dateOfBirth}"
                                )
                            )
                        }
                        item {
                            FacultyProfileSection(
                                title = "Contact Details",
                                details = listOf(
                                    "Email: ${profile.email}"
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun logout1(navController: NavController) {

    navController.navigate("welcome") {
        popUpTo("welcome") { inclusive = true }
    }
}
@Composable
fun FacultyProfile(profilepic: String, title: String, details: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(CircleShape)
            .background(CustomBlue),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture
        Image(
            painter = rememberAsyncImagePainter(model = profilepic),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CustomGray)
                .border(4.dp, color = CustomBlue, shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp)) // Space between image and details

        // Details
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = CustomWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            details.forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = CustomWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun FacultyProfileSection(title: String, details: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CustomBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            details.forEach {
                Text(
                    text = it,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFacultyProfile() {
    ProjectTheme {
        FacultyProfile(
            navController = rememberNavController(),
            Fid = 1
        )
    }
}
