import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.project.R
import com.example.project.ui.theme.CustomBlue
import com.example.project.ui.theme.CustomWhite

@Composable
fun LoginForm(
    title: String,
    buttonText: String,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisibility: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    isLoading: Boolean,
    onSubmit: () -> Unit,
    errorMessage: String,
    imageResId: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x3B41CF))
    ) {
        Image(
            painter = painterResource(id = R.drawable.bgm),
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .background(Color(0x3B41CF)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginImage(imageResId)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = CustomBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    EmailField(email, onEmailChange)
                    Spacer(modifier = Modifier.height(16.dp))
                    PasswordField(
                        password = password,
                        passwordVisibility = passwordVisibility,
                        onPasswordChange = onPasswordChange,
                        onPasswordVisibilityChange = onPasswordVisibilityChange
                    )

                    AnimatedVisibility(
                        visible = errorMessage.isNotEmpty(),
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Forgot Password?",
                        modifier = Modifier
                            .clickable { /* Handle forgot password click */ }
                            .padding(8.dp),
                        color = CustomBlue,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .alpha(if (isLoading) 0.6f else 1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomBlue,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = buttonText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun EmailField(
    email: String,
    onEmailChange: (String) -> Unit
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = CustomBlue,
            focusedLabelColor = CustomBlue,
            cursorColor = CustomBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PasswordField(
    password: String,
    passwordVisibility: Boolean,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit
) {
    val visualTransformation: VisualTransformation =
        if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = visualTransformation,
        trailingIcon = {
            IconButton(onClick = onPasswordVisibilityChange) {
                Icon(
                    imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                )
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = CustomBlue,
            focusedLabelColor = CustomBlue,
            cursorColor = CustomBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LoginImage(imageResId: Int) {
    Image(
        painter = painterResource(id = imageResId),
        contentDescription = null,
        modifier = Modifier
            .size(200.dp)
            .padding(bottom = 24.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginForm() {
    LoginForm(
        title = "Welcome Back",
        buttonText = "Login",
        email = "email@example.com",
        onEmailChange = {},
        password = "password123",
        onPasswordChange = {},
        passwordVisibility = false,
        onPasswordVisibilityChange = { /* Toggle visibility */ },
        isLoading = false,
        onSubmit = { /* Handle submit */ },
        errorMessage = "",
        imageResId = R.drawable.im2 // Replace with your image resource ID
    )
}
