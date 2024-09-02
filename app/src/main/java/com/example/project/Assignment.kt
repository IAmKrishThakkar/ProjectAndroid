import Model.PendingAssignment
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.project.NavigationIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentUploadScreen(navController: NavController, studID: Int) {
    val context = LocalContext.current
    var assignments by remember { mutableStateOf<List<PendingAssignment>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var uploadSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) } // Progress as a percentage
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Fetch data when screen is first displayed
    LaunchedEffect(studID) {
        coroutineScope.launch {
            val classId = getStudentClassId(studID)
            if (classId != null) {
                assignments = getPendingAssignments(classId) ?: emptyList()
            } else {
                errorMessage = "Failed to get student class ID"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Assignment") }
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
                    NavigationIcon(navController, "Assignment/$studID", Icons.Filled.Assignment, "Marks")
                    NavigationIcon(navController, "Faculty/$studID", Icons.Filled.School, "Faculty")
                    NavigationIcon(navController, "Profile/$studID", Icons.Filled.Person, "Profile")
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {}
    ) { innerPadding ->
        AssignmentUploadContent(
            modifier = Modifier.padding(innerPadding),
            assignments = assignments,
            errorMessage = errorMessage,
            fileUri = fileUri,
            onFileUriChange = { uri -> fileUri = uri },
            onSubmit = { assignmentId ->
                if (fileUri != null) {
                    coroutineScope.launch {
                        isLoading = true
                        uploadProgress = 0f
                        try {
                            submitAssignment(
                                studentId = studID,
                                assignmentId = assignmentId,
                                fileUri = fileUri!!,
                                context = context,
                                onProgressUpdate = { progress ->
                                    uploadProgress = progress // Update progress
                                }
                            )
                            snackbarHostState.showSnackbar("Upload successful!")
                            uploadSuccess = true
                        } catch (e: Exception) {
                            errorMessage = "Submission failed: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    errorMessage = "Please select a file to upload"
                }
            },
            uploadSuccess = uploadSuccess,
            isLoading = isLoading,
            uploadProgress = uploadProgress
        )
    }
}

@Composable
fun AssignmentUploadContent(
    modifier: Modifier,
    assignments: List<PendingAssignment>,
    errorMessage: String?,
    fileUri: Uri?,
    onFileUriChange: (Uri?) -> Unit,
    onSubmit: (Int) -> Unit,
    uploadSuccess: Boolean,
    isLoading: Boolean,
    uploadProgress: Float
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onFileUriChange(uri) // Update the selected file URI
    }

    Column(modifier = modifier.padding(16.dp)) {
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            // Display loading progress
            CircularProgressIndicator(progress = uploadProgress, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Uploading... ${(uploadProgress * 100).toInt()}%", fontSize = 18.sp)
            LinearProgressIndicator(progress = uploadProgress, modifier = Modifier.fillMaxWidth())
        } else {
            if (assignments.isNotEmpty()) {
                var selectedAssignmentId by remember { mutableStateOf(assignments.first().id) }

                // Display assignment selection
                Text("Select Assignment", fontSize = 20.sp)
                assignments.forEach { assignment ->
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        RadioButton(
                            selected = (assignment.id == selectedAssignmentId),
                            onClick = { selectedAssignmentId = assignment.id }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(assignment.subject)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // File picker button
                Button(onClick = {
                    filePickerLauncher.launch("application/pdf") // Open file picker
                }) {
                    Text("Pick Assignment File")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit button
                Button(onClick = { onSubmit(selectedAssignmentId) }) {
                    Text("Submit Assignment")
                }
            } else {
                Text("No pending assignments available")
            }
        }
    }
}

suspend fun submitAssignment(
    studentId: Int,
    assignmentId: Int,
    fileUri: Uri,
    context: Context,
    onProgressUpdate: (Float) -> Unit // New parameter for progress updates
) {
    var retryCount = 0
    val maxRetries = 3
    var successful = false

    while (retryCount < maxRetries && !successful) {
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: throw IOException("Failed to open input stream for URI: $fileUri")

            val file = File(context.cacheDir, "assignment_${System.currentTimeMillis()}.pdf")
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            if (!file.exists() || file.length() == 0L) {
                throw IOException("Failed to create a valid file for upload")
            }

            val requestFile = RequestBody.create("application/pdf".toMediaTypeOrNull(), file)
            val filePart = MultipartBody.Part.createFormData("pdf", file.name, requestFile)

            val studentIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), studentId.toString())
            val assignmentIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), assignmentId.toString())

            val response = RetrofitInstance.api.postAssignment(studentIdPart, assignmentIdPart, filePart)

            if (response.isSuccessful) {
                successful = true
                println("Upload successful!")
            } else {
                throw IOException("Upload failed with response code ${response.code()} and message: ${response.message()}")
            }
        } catch (e: Exception) {
            println("Retry ${retryCount + 1}: ${e.message}")
            retryCount++
            if (retryCount >= maxRetries) {
                throw IOException("Submission failed after $maxRetries retries: ${e.message}")
            }
        }
    }
}


suspend fun getStudentClassId(studentId: Int): Int? {
    return withContext(Dispatchers.IO) {  // Ensures network request runs on background thread
        try {
            val response = RetrofitInstance.api.getStudents().execute()
            val student = response.body()?.find { it.id == studentId }
            student?.class_id
        } catch (e: HttpException) {
            println("HTTP Exception: ${e.message}")
            null
        } catch (e: IOException) {
            println("Network Exception: ${e.message}")
            null
        }
    }
}

suspend fun getPendingAssignments(classId: Int): List<PendingAssignment>? {
    return withContext(Dispatchers.IO) {  // Ensures network request runs on background thread
        try {
            val response = RetrofitInstance.api.getPendingAssignment(classId).execute()
            response.body()
        } catch (e: HttpException) {
            println("HTTP Exception: ${e.message}")
            null
        } catch (e: IOException) {
            println("Network Exception: ${e.message}")
            null
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun Preview() {
    ProjectTheme {
        AssignmentUploadScreen(
            navController = rememberNavController(),
            studID = 1
        )
    }
}