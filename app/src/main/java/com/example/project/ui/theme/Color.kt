package com.example.project.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val CustomBlue = Color(0xFF161697)
val CustomGray = Color(0xFF9E9E9E)
val CustomWhite = Color(0xFFFFFFFF)
val BlueToPurpleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF161697), Color(0xFF6A0D91)),
    start = Offset(0f, 0f),
    end = Offset(1000f, 0f)
)