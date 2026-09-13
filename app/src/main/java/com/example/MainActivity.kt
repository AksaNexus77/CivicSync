package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.CivicSyncApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate900
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = Slate900
        ) {
          CivicSyncApp()
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String = "CivicSync", modifier: Modifier = Modifier) {
  CivicSyncApp(modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun CivicSyncAppPreview() {
  MyApplicationTheme {
    CivicSyncApp()
  }
}

