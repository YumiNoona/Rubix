package com.cubeguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.cubeguide.ui.CubeApp

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
 super.onCreate(savedInstanceState)
  enableEdgeToEdge()
  setContent { CubeApp() }
 }
}
