import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignment(navController: NavController, Fid: Int, class_ID: Int) {
    // State for user inputs
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var submissionDeadline by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Date picker state
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var showDatePicker by remember { mutableStateOf(false) }

    // Coroutine scope for launching network requests
    val coroutineScope = rememberCoroutineScope()

    // UI Elements
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Assignment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(text = "Add New Assignment", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Subject Input
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Submission Deadline Input
            OutlinedTextField(
                value = submissionDeadline,
                onValueChange = { submissionDeadline = it },
                label = { Text("Submission Deadline") },
                readOnly = true, // Make this field read-only to ensure the user selects the date using the DatePicker
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = "Select Date")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        // Perform API call to post the assignment
                        try {
                            val response = RetrofitInstance.api.postPendingAssignment(
                                id = null,
                                classId = class_ID.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                                facultyId = Fid.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                                subject = subject.toRequestBody("text/plain".toMediaTypeOrNull()),
                                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                                submissionDeadline = submissionDeadline.toRequestBody("text/plain".toMediaTypeOrNull())
                            )

                            if (response.isSuccessful) {
                                navController.popBackStack()
                            } else {
                                errorMessage = "Failed to add assignment: ${response.errorBody()?.string()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Exception occurred: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            LocalContext.current,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                submissionDeadline = dateFormat.format(calendar.time)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddAssignment() {
    ProjectTheme {
        val navController = rememberNavController()
        AddAssignment(navController = navController, Fid = 101, class_ID = 101)
    }
}
