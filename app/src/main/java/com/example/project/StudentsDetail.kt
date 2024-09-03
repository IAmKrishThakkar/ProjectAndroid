import Model.StudentLogin
import Model.classes
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.project.NavigationIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetail(navController: NavController, Fid: Int) {
    var selectedClass by remember { mutableStateOf<classes?>(null) }
    var students by remember { mutableStateOf<List<StudentLogin>>(emptyList()) }
    var classesList by remember { mutableStateOf<List<classes>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var classID by remember { mutableStateOf<Int?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Fid) {
        coroutineScope.launch {
            isLoading = true
            try {
                classesList = getClassesByFacultyId(Fid)
            } catch (e: Exception) {
                errorMessage = "Error fetching classes: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(classesList) {
        coroutineScope.launch {
            if (classesList.isNotEmpty()) {
                val studentsList = mutableListOf<StudentLogin>()
                classesList.forEach { classItem ->
                    try {
                        val studentsForClass = fetchStudentsDetail(classItem.id)
                        studentsList.addAll(studentsForClass)
                    } catch (e: Exception) {
                        errorMessage = "Error fetching students for class ${classItem.className}: ${e.message}"
                    }
                }
                students = studentsList
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Students Detail", style = MaterialTheme.typography.titleLarge) },
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
    ) { paddingValues ->
        if (isLoading) {
            LoadingScreen(paddingValues)
        } else if (errorMessage != null) {
            ErrorScreen(errorMessage = errorMessage!!) {
                // Retry logic here
                coroutineScope.launch {
                    isLoading = true
                    try {
                        classesList = getClassesByFacultyId(Fid)
                    } catch (e: Exception) {
                        errorMessage = "Error fetching classes: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            }
        } else {
            StudentDetailContent(classesList, students, paddingValues)
        }
    }
}

@Composable
fun LoadingScreen(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Error: $errorMessage", color = Color.Red, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)) {
                Text("Retry")
            }
        }
    }
}


@Composable
fun StudentDetailContent(classesList: List<classes>, students: List<StudentLogin>, paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        classesList.forEach { classItem ->
            item {
                ClassDetail(classItem = classItem) // Display class details
                Spacer(modifier = Modifier.height(16.dp))

                val studentsForClass = students.filter { it.class_id == classItem.id } // Filter students by classId
                studentsForClass.forEach { student ->
                    StudentCard(studentLogin = student) // Display each student
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}



@Composable
fun StudentCard(studentLogin: StudentLogin) {
    val photourl = "https://netxgroup.in/students/"
    val completeUrl = "$photourl${studentLogin.profilePhoto}"
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* Handle click */ },
        shape = RoundedCornerShape(12.dp) // Rounded corners for modern look
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = completeUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)) // Better background effect
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(studentLogin.stud_name, style = MaterialTheme.typography.titleLarge)
                Text(studentLogin.roll_no, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Fees: ${studentLogin.feeStatus}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (studentLogin.feeStatus == "Paid") Color.Green else Color.Red
                )
            }
        }
    }
}



@Composable
fun ClassDetail(classItem: classes) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background),

        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Class Name: ${classItem.className}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Students", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private suspend fun fetchStudentsDetail(classId: Int): List<StudentLogin> {
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

private suspend fun getClassesByFacultyId(facultyId: Int): List<classes> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.getClassesByFacultyId(facultyId).execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("Failed to load classes. Response code: ${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error fetching classes: ${e.message}")
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun FacultyAttendanceScreenPreview() {
    ProjectTheme {
        val navController = rememberNavController()
        StudentDetail(navController = navController, Fid = 101)
    }
}
