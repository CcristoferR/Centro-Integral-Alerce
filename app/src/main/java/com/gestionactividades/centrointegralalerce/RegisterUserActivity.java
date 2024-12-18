package com.gestionactividades.centrointegralalerce;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterUserActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, nameEditText;
    private Button registerButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Inicializar elementos de la interfaz
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Referencias a los elementos de la interfaz
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nameEditText = findViewById(R.id.nameEditText); // Asegúrate de que este ID exista en el XML
        registerButton = findViewById(R.id.registerButton);

        // Configuración del botón para registrar al usuario
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();

        // Validar campos
        if (name.isEmpty()) {
            nameEditText.setError("El nombre es obligatorio");
            nameEditText.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailEditText.setError("El correo electrónico es obligatorio");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("La contraseña es obligatoria");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            passwordEditText.requestFocus();
            return;
        }

        // Crear usuario en Firebase
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Obtener el usuario registrado
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Actualizar el perfil del usuario
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    Toast.makeText(RegisterUserActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Log.e("RegisterUser", "Error al actualizar perfil", profileTask.getException());
                                    Toast.makeText(RegisterUserActivity.this, "Error al actualizar perfil: " + profileTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterUserActivity.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("RegisterUser", "Error al registrar usuario", task.getException());
                            Toast.makeText(RegisterUserActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
