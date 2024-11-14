package com.gestionactividades.centrointegralalerce;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CancelActivity extends AppCompatActivity {

    private EditText cancellationReasonEditText;
    private Button saveCancellationButton;

    private String activityId;
    private String userId;
    private DatabaseReference activityRef;
    private DatabaseReference cancellationsRef;

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

        // Referencia a la actividad en la base de datos y a la rama cancellations
        activityRef = FirebaseDatabase.getInstance().getReference("activities").child(userId).child(activityId);
        cancellationsRef = FirebaseDatabase.getInstance().getReference("cancellations").child(userId).child(activityId);

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

        // Antes de eliminar la actividad, recuperamos su información
        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                EventActivity activity = snapshot.getValue(EventActivity.class);
                if (activity != null) {
                    // Almacenar información de la actividad cancelada en la sección cancellations
                    Map<String, Object> cancellationData = new HashMap<>();
                    cancellationData.put("activityId", activity.getActivityId());
                    cancellationData.put("name", activity.getName());
                    cancellationData.put("fecha", activity.getFecha());
                    cancellationData.put("hora", activity.getHora());
                    cancellationData.put("cancellationReason", reason);
                    cancellationData.put("cancellationDate", currentDate);

                    cancellationsRef.setValue(cancellationData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Eliminar la actividad
                            activityRef.removeValue().addOnCompleteListener(removeTask -> {
                                if (removeTask.isSuccessful()) {
                                    Toast.makeText(CancelActivity.this, "Actividad cancelada y eliminada con éxito", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    Toast.makeText(CancelActivity.this, "Error al eliminar la actividad", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(CancelActivity.this, "Error al guardar el motivo de cancelación", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(CancelActivity.this, "No se pudo encontrar la actividad.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CancelActivity.this, "Error al acceder a los datos de la actividad.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
