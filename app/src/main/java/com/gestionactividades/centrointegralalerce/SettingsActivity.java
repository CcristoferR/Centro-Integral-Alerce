package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();

        // Configurar la barra superior (Toolbar)
        MaterialToolbar toolbar = findViewById(R.id.settingsToolbar);
        toolbar.setNavigationOnClickListener(v -> {
            // Acción del botón de la toolbar para volver al Home
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Finaliza la actividad actual
        });

        // Botón Cerrar Sesión
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            // Cierra la sesión en Firebase y regresa al login
            auth.signOut();
            Toast.makeText(SettingsActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Botón Registrar Usuario
        Button registerUserButton = findViewById(R.id.registerUserButton);
        registerUserButton.setOnClickListener(v -> {
            // Navega a la actividad de registro de usuario
            Intent intent = new Intent(SettingsActivity.this, RegisterUserActivity.class);
            startActivity(intent);
        });

        // Botón Volver desde el contenido principal
        Button backButtonContent = findViewById(R.id.backButtonContent);
        backButtonContent.setOnClickListener(v -> {
            // Acción del botón para volver al Home
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Finaliza la actividad actual
        });
    }
}
