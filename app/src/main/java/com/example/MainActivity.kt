package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.PikPakApp
import com.example.ui.theme.PikPakTheme
import com.example.ui.viewmodel.PikPakViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      PikPakTheme {
        val viewModel: PikPakViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        PikPakApp(viewModel = viewModel)
      }
    }
  }
}
