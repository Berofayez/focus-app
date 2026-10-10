package com.focusapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.focusapp.ui.FocusAppRoot
import com.focusapp.ui.theme.FocusAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusAppTheme {
                FocusAppRoot()
            }
        }
    }
}
