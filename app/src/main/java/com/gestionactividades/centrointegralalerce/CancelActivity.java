package com.gestionactividades.centrointegralalerce;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CancelActivity extends AppCompatActivity {

    private EditText cancellationReasonEditText;
    private Button saveCancellationButton;

    private String activityId;
    private String userId;
    private DatabaseReference activityRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel);

        // Vincular vistas
        cancellationReasonEditText = findViewById(R.id.cancellationReasonEditText);
        saveCancellationButton = findViewById(R.id.saveCancellationButton);

        // Obtener activityId y userId
        activityId = getIntent().getStringExtra("activityId");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Referencia a la actividad en Firebase
        activityRef = FirebaseDatabase.getInstance().getReference("activities").child(userId).child(activityId);

        // Configurar botón para guardar la cancelación
        saveCancellationButton.setOnClickListener(v -> saveCancellation());
    }

    private void saveCancellation() {
        String reason = cancellationReasonEditText.getText().toString().trim();

        if (reason.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el motivo de la cancelación.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la fecha y hora actuales
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        // Actualizar la actividad en Firebase
        activityRef.child("isCancelled").setValue(true);
        activityRef.child("cancellationReason").setValue(reason);
        activityRef.child("cancellationDate").setValue(currentDate).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CancelActivity.this, "Actividad cancelada con éxito", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(CancelActivity.this, "Error al cancelar la actividad", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
