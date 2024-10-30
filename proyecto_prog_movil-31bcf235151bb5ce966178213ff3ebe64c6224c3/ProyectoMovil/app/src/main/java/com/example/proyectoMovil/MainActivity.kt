package com.example.avance_proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectoMovil.ui.theme.AvanceTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Buscar...",
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(text = placeholder) },
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        singleLine = true,
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.DoneAll, contentDescription = "Limpiar")
                }
            }
        },
    )
}

// En tu actividad principal, añade el controlador de navegación
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(tonalElevation = 5.dp) {
                AvanceTheme {
                    //val navController =  // Devuelve NavHostController
                    MainAppNavHost(navController = rememberNavController())
                }
            }
        }
    }
}

@Composable
fun StatusBoxes(navController: NavHostController) {
    Column {
        StatusItem(
            title = "Notificaciones",
            count = 0,
            onClick = { navController.navigate("notifications") } // Navegar a la pantalla de notificaciones
        )
        // Aquí podrías agregar más StatusItems si los necesitas.
    }
}

@Composable
fun StatusItem(title: String, count: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick), // Aquí está el onClic
        color = Color(0xFFF1F1F1),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Usamos NavHostController en lugar de NavController
@Composable
fun MainApp(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }, // Actualiza la consulta al escribir
            placeholder = "Buscar..."
        )
        Spacer(modifier = Modifier.height(16.dp))
        StatusBoxes(navController) // Pasar navController aquí
        Spacer(modifier = Modifier.height(16.dp))
        MyListsSection(navController)
    }
}

@Composable
fun MyListsSection(navController: NavHostController) {

    Text(
        text = "Notas a Agregar",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    Column {
        MyListItem("Notas", navController)
    }
}

@Composable
fun MyListItem(title: String, navController: NavHostController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { navController.navigate("add_note") }, // Navegar a la pantalla para añadir notas
        color = Color(0xFFF1F1F1),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddNoteScreen(navController: NavHostController) {
    var noteTitle by remember { mutableStateOf("") }
    var noteDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Añadir Nota",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = noteTitle,
            onValueChange = { noteTitle = it },
            label = { Text("Título de la Nota") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = noteDescription,
            onValueChange = { noteDescription = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* Aquí se manejará el archivo multimedia */ }) {
            Icon(imageVector = Icons.Default.AttachFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir Archivo Multimedia")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("main") },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Volver a la Pantalla Principal")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Aquí puedes manejar la lógica para guardar la nota si lo deseas
                navController.popBackStack() // Regresa a la pantalla anterior
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Guardar Nota")
        }
    }
}

@Composable
fun NotificationsScreen(navController: NavHostController) {
    val notifications = listOf(
        "Notificación 1: Tienes una nueva tarea.",
        "Notificación 2: Recuerda la reunión mañana.",
        "Notificación 3: Se ha actualizado una nota."
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Notificaciones",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        notifications.forEach { notification ->
            Text(
                text = notification,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun MainAppNavHost(navController: NavHostController) {
    Surface(tonalElevation = 15.dp) {
        AvanceTheme {
            NavHost(navController = navController, startDestination = "main") {
                composable("main") { MainApp(navController) }
                composable("add_note") { AddNoteScreen(navController) }
                composable("notifications") { NotificationsScreen(navController) } // Nueva pantalla de notificaciones
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AvanceTheme {
        // Vista previa del contenido
        MainApp(rememberNavController())
    }
}
