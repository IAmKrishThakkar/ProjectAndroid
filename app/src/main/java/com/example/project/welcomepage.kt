package com.example.project.ui.welcomepage

import ProjectTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.project.R
import com.example.project.ui.theme.CustomBlue

@Composable
fun WelcomePage(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bgm1),
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x3B41CF)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Add padding if needed
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            WelcomeText()
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeButtons(navController)
            Spacer(modifier = Modifier.height(25.dp))
            WelcomeImage()
        }
    }
}


@Composable
fun WelcomeImage() {
    Image(
        painter = painterResource(id = R.drawable.im1),
        contentDescription = "College Building",
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
    )
}

@Composable
fun WelcomeText() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to",
            fontSize = 50.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Scholarly",
            fontSize = 50.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Let access all work from here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun WelcomeButtons(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = { navController.navigate("Slogin") },
            modifier = Modifier
                .padding(8.dp)
                .width(150.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CustomBlue,
                )
        ) {
            Text("Student Login", color = MaterialTheme.colorScheme.onPrimary)
        }
        OutlinedButton(
            onClick = { navController.navigate("Flogin") },
            modifier = Modifier
                .padding(8.dp)
                .width(150.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CustomBlue,
            )
        ) {
            Text("Faculty Login", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Preview
@Composable
fun WelcomePagePreview() {
    ProjectTheme {
        val navController = rememberNavController()
        WelcomePage(navController)
    }
}
