package com.gestionactividades.centrointegralalerce;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Importaciones necesarias
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth; // Asegúrate de tener esta importación
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase; // Asegúrate de tener esta importación
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RescheduleActivity extends AppCompatActivity {

    private TextView activityNameTextView, currentDateTimeTextView;
    private EditText reasonEditText;
    private Button selectNewDateTimeButton, saveRescheduleButton;

    private String activityId;
    private String userId;
    private DatabaseReference activityRef;

    private String newDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule);

        // Vincular vistas
        activityNameTextView = findViewById(R.id.activityNameTextView);
        currentDateTimeTextView = findViewById(R.id.currentDateTimeTextView);
        reasonEditText = findViewById(R.id.reasonEditText);
        selectNewDateTimeButton = findViewById(R.id.selectNewDateTimeButton);
        saveRescheduleButton = findViewById(R.id.saveRescheduleButton);

        // Obtener el activityId y userId
        activityId = getIntent().getStringExtra("activityId");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Referencia a la actividad en la base de datos
        activityRef = FirebaseDatabase.getInstance().getReference("activities").child(userId).child(activityId);

        // Cargar los detalles actuales de la actividad
        loadActivityDetails();

        // Configurar botón para seleccionar nueva fecha y hora
        selectNewDateTimeButton.setOnClickListener(v -> showDateTimePicker());

        // Configurar botón para guardar reprogramación
        saveRescheduleButton.setOnClickListener(v -> saveReschedule());
    }

    private void loadActivityDetails() {
        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            // Implementar onDataChange y onCancelled para cargar los datos actuales
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                EventActivity activity = snapshot.getValue(EventActivity.class);
                if (activity != null) {
                    activityNameTextView.setText(activity.getName());
                    currentDateTimeTextView.setText(activity.getFecha());
                } else {
                    Toast.makeText(RescheduleActivity.this, "No se pudo cargar la actividad.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RescheduleActivity.this, "Error al cargar datos.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = day + "/" + (month + 1) + "/" + year;

            new TimePickerDialog(this, (timeView, hour, minute) -> {
                newDateTime = "Fecha: " + date + " Hora: " + hour + ":" + minute;
                selectNewDateTimeButton.setText(newDateTime); // Mostrar la nueva fecha y hora seleccionada en el botón
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveReschedule() {
        String reason = reasonEditText.getText().toString();

        if (newDateTime == null || reason.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa la nueva fecha/hora y el motivo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un objeto RescheduleInfo
        EventActivity.RescheduleInfo rescheduleInfo = new EventActivity.RescheduleInfo(newDateTime, reason);

        // Actualizar la actividad en la base de datos
        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                EventActivity activity = snapshot.getValue(EventActivity.class);
                if (activity != null) {
                    // Obtener la lista de reprogramaciones existente o crear una nueva
                    List<EventActivity.RescheduleInfo> reschedules = activity.getReschedules();
                    if (reschedules == null) {
                        reschedules = new ArrayList<>();
                    }

                    // Agregar la nueva reprogramación a la lista
                    reschedules.add(rescheduleInfo);

                    // Actualizar los campos de la actividad
                    activity.setFecha(newDateTime);
                    activity.setReschedules(reschedules);

                    // Guardar los cambios en la base de datos
                    activityRef.setValue(activity).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RescheduleActivity.this, "Actividad reprogramada con éxito", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(RescheduleActivity.this, "Error al reprogramar la actividad", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(RescheduleActivity.this, "No se pudo encontrar la actividad.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RescheduleActivity.this, "Error al acceder a los datos.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
