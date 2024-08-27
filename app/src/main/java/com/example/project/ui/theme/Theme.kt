import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.project.ui.theme.BlueToPurpleGradient
import com.example.project.ui.theme.CustomBlue
import com.example.project.ui.theme.CustomGray
import com.example.project.ui.theme.CustomWhite


// Dark theme color scheme
private val DarkColorScheme = ExtendedColorScheme(
    colorScheme = darkColorScheme(
        primary = CustomBlue,
        secondary = CustomGray,
        background = Color(0xFF121212),
        surface = Color(0xFF1F1F1F),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
    ),
    gradient = BlueToPurpleGradient
)

private val LightColorScheme = ExtendedColorScheme(
    colorScheme = lightColorScheme(
        primary = CustomBlue,
        secondary = CustomGray,
        background = CustomWhite,
        surface = Color(0xFFF5F5F5),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.Black
    ),
    gradient = BlueToPurpleGradient
)


data class ExtendedColorScheme(
    val colorScheme: ColorScheme,
    val gradient: Brush
)

@Composable
fun ProjectTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme.colorScheme,
        content = content
    )
}

