package com.gestionactividades.centrointegralalerce;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RescheduleActivity extends AppCompatActivity {

    private TextView activityNameTextView, currentDateTimeTextView;
    private TextInputEditText reasonEditText;
    private Button selectNewDateTimeButton, saveRescheduleButton, backButtonReschedule;

    private String activityId;
    private String userId;
    private DatabaseReference activityRef;

    private String selectedDate; // Fecha seleccionada en formato "dd/MM/yyyy"
    private String selectedTime; // Hora seleccionada en formato "HH:mm"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule);

        // Configuración del Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar_reschedule);

        // Inicializar el botón de regreso
        backButtonReschedule = findViewById(R.id.backButtonReschedule);
        backButtonReschedule.setOnClickListener(v -> finish());

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

        // Configurar botón para guardar la reprogramación
        saveRescheduleButton.setOnClickListener(v -> saveReschedule());
    }

    private void loadActivityDetails() {
        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                EventActivity activity = snapshot.getValue(EventActivity.class);
                if (activity != null) {
                    activityNameTextView.setText(activity.getName() != null ? activity.getName() : "Sin nombre");

                    // Mostrar la fecha/hora actual de la actividad
                    String currentDate = activity.getFecha() != null ? activity.getFecha() : "Sin fecha";
                    String currentTime = (activity.getHora() != null && !activity.getHora().isEmpty()) ? activity.getHora() : "Sin hora";
                    currentDateTimeTextView.setText(String.format("Fecha Actual: %s\nHora Actual: %s", currentDate, currentTime));
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

        // Crear diálogo personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar nueva fecha y hora");

        // Inflar el layout personalizado
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_periodicity_options, null);
        builder.setView(dialogView);

        Button dateButton = dialogView.findViewById(R.id.selectDateButton);
        Button timeButton = dialogView.findViewById(R.id.selectTimeButton);

        dateButton.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                dateButton.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        timeButton.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                timeButton.setText(selectedTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            if (selectedDate != null && selectedTime != null) {
                selectNewDateTimeButton.setText(String.format("Nueva Fecha: %s\nNueva Hora: %s", selectedDate, selectedTime));
            } else {
                Toast.makeText(this, "Por favor, selecciona la fecha y hora.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveReschedule() {
        String reason = reasonEditText.getText().toString().trim();

        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Por favor, selecciona la nueva fecha y hora.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el motivo de la reprogramación.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un objeto RescheduleInfo con la nueva fecha y hora
        String newDateTime = String.format("Fecha: %s, Hora: %s", selectedDate, selectedTime);
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

                    // Actualizar los campos de la actividad con la nueva fecha/hora
                    activity.setFecha(selectedDate);
                    activity.setHora(selectedTime);
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
