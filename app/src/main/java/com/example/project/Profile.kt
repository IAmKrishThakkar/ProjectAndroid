package com.example.project

import Model.StudentLogin
import ProjectTheme
import RetrofitInstance
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.project.ui.theme.CustomBlue
import com.example.project.ui.theme.CustomGray
import com.example.project.ui.theme.CustomWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Profile(navController: NavHostController, studId: Int) {
    var studentProfile by remember { mutableStateOf<StudentLogin?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var rollno by remember { mutableStateOf<String?>(null)}
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load student profile when the composable is first displayed
    LaunchedEffect(studId) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getStudents().execute()
            }
            if (response.isSuccessful) {
                studentProfile = response.body()?.find { it.id == studId }
                rollno= studentProfile?.roll_no
                isLoading = false
            } else {
                errorMessage = "Error: ${response.message()}"
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Exception: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = CustomBlue,
                contentColor = CustomWhite,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NavigationIcon(navController, "StudentDashboard/$studId", Icons.Filled.Home, "Home")
                    NavigationIcon(navController, "Assignment/$studId", Icons.Filled.Assignment, "Courses")
                    NavigationIcon(navController, "Faculty/$studId", Icons.Filled.School, "Assignments")
                    NavigationIcon(navController, "Profile/$studId", Icons.Filled.Person, "Profile")
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            // Show a loading indicator while fetching the data
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CustomBlue)
            }
        } else if (errorMessage != null) {
            // Show error message if any
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
        } else {
            // Show the profile details
            studentProfile?.let { profile ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Student Details
                    item {
                        ProfilePicture(
                            profilepic = "https://netxgroup.in/students/${profile.profilePhoto}",
                            title = profile.stud_name,
                            details = listOf(
                                "${profile.department} ${profile.semester}",
                                "Roll No- ${profile.roll_no}"
                            )
                        )
                        println("Profilepc:${profile.profilePhoto}")
                    }

                    // Academic Details
                    item {
                        ProfileSection(
                            title = "ACADEMIC DETAILS",
                            details = listOf(
                                "DEPARTMENT: ${profile.department}",
                                "SEMESTER: ${profile.semester}",
                                "ACADEMIC YEAR: ${profile.academicYear}"
                            )
                        )
                    }

                    // About Section
                    item {
                        ProfileSection(
                            title = "About",
                            details = listOf(
                                "Date of Birth: ${profile.dateOfBirth}",
                                "Gender: ${profile.gender}"
                            )
                        )
                    }

                    // Contact Details
                    item {
                        ProfileSection(
                            title = "Contact Details",
                            details = listOf(
                                "Email: ${profile.stud_email}",
                                "Address: ${profile.address}"
                            )
                        )
                    }

                    // Current / Ongoing Courses
                    item {
                        Text(
                            text = "Current / Ongoing Courses",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CustomBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OnGoingCourse(text = "${profile.currentCourses}")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfilePicture(profilepic: String, title: String, details: List<String>) {
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
fun ProfileSection(title: String, details: List<String>) {
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

@Composable
fun OnGoingCourse(text: String) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .width(150.dp)
            .clip(CircleShape)
            .background(CustomBlue),
        contentAlignment = Alignment.Center // Center the content inside the box
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.School, // You can change the icon as needed
                contentDescription = null,
                tint = CustomWhite,
                modifier = Modifier.size(24.dp) // Adjust icon size if needed
            )
            Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
            Text(
                text = text,
                color = CustomWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfile() {
    ProjectTheme {
        Profile(
            navController = rememberNavController(),
            studId = 101
        )
    }
}
