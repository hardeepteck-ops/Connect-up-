package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.ConnectUpDatabase
import com.example.data.repository.ConnectUpRepository
import com.example.ui.navigation.ConnectUpApp
import com.example.ui.viewmodel.ConnectUpViewModel
import com.example.ui.viewmodel.ConnectUpViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = ConnectUpDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = ConnectUpRepository(database.connectUpDao())
        val factory = ConnectUpViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[ConnectUpViewModel::class.java]

        setContent {
            ConnectUpApp(viewModel = viewModel)
        }
    }
}
