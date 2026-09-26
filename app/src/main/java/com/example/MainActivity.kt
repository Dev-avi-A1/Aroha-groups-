package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.ui.navigation.ArohaAppNavigation
import com.example.ui.theme.ArohaDeepBackground
import com.example.ui.theme.ArohaTheme
import com.example.ui.viewmodel.ArohaViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: ArohaViewModel by viewModels {
        val app = application as ArohaApplication
        ArohaViewModel.provideFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ArohaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ArohaDeepBackground
                ) {
                    LaunchedEffect(Unit) {
                        viewModel.toastEvent.collectLatest { msg ->
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    }

                    ArohaAppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
