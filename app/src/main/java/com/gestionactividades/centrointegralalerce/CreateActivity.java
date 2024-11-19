package com.gestionactividades.centrointegralalerce;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CreateActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private EditText activityNameEditText, providerEditText, beneficiariesEditText, cupoEditText;
    private Spinner locationSpinner, capacitacionSpinner, frequencySpinner;
    private TextView selectedDatesTextView, selectedEndDateTextView;
    private LinearLayout endDateLayout;
    private Button uploadFileButton, saveActivityButton, selectDateButton, selectEndDateButton;
    private Uri fileUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth auth;

    private String[] locationOptions = {"Oficina del centro", "Lugares del territorio", "Otro lugar"};
    private String[] capacitacionOptions = {
            "Capacitación",
            "Taller",
            "Charlas",
            "Atenciones",
            "Operativo en oficina",
            "Operativo rural",
            "Operativo",
            "Práctica profesional",
            "Diagnóstico"
    };
    private String[] frequencyOptions = {"Una vez", "Diaria", "Semanal", "Mensual"};

    private String selectedDate; // Fecha en formato "dd/MM/yyyy"
    private String selectedTime; // Hora en formato "HH:mm"
    private String selectedFrequency = "Una vez";
    private String selectedEndDate; // Fecha de finalización

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Inicializar FirebaseAuth y obtener el UID del usuario
        auth = FirebaseAuth.getInstance();

        // Vincular variables con los elementos de la vista
        activityNameEditText = findViewById(R.id.activityNameEditText);
        providerEditText = findViewById(R.id.providerEditText);
        beneficiariesEditText = findViewById(R.id.beneficiariesEditText);
        cupoEditText = findViewById(R.id.cupoEditText);
        locationSpinner = findViewById(R.id.locationSpinner);
        capacitacionSpinner = findViewById(R.id.capacitacionSpinner);
        frequencySpinner = findViewById(R.id.frequencySpinner);
        selectedDatesTextView = findViewById(R.id.selectedDatesTextView);
        selectedEndDateTextView = findViewById(R.id.selectedEndDateTextView);
        endDateLayout = findViewById(R.id.endDateLayout);

        uploadFileButton = findViewById(R.id.uploadFileButton);
        saveActivityButton = findViewById(R.id.saveActivityButton);
        selectDateButton = findViewById(R.id.selectDateButton);
        selectEndDateButton = findViewById(R.id.selectEndDateButton);

        // Inicializar referencias de Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("activities");
        storageReference = FirebaseStorage.getInstance().getReference("activity_files");

        // Configurar adaptador para el Spinner de lugares
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locationOptions);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        // Configurar adaptador para el Spinner de capacitación
        ArrayAdapter<String> capacitacionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, capacitacionOptions);
        capacitacionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capacitacionSpinner.setAdapter(capacitacionAdapter);

        // Configurar adaptador para el Spinner de frecuencia
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencyOptions);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);

        // Configurar listeners
        selectDateButton.setOnClickListener(v -> showDateTimeDialog());
        selectEndDateButton.setOnClickListener(v -> showEndDateDialog());
        uploadFileButton.setOnClickListener(v -> openFilePicker());
        saveActivityButton.setOnClickListener(v -> saveActivity());

        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                selectedFrequency = frequencyOptions[i];
                if (selectedFrequency.equals("Una vez")) {
                    endDateLayout.setVisibility(View.GONE);
                    selectedEndDate = null;
                    selectedEndDateTextView.setText("Fecha de finalización no seleccionada");
                } else {
                    endDateLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedFrequency = "Una vez";
                endDateLayout.setVisibility(View.GONE);
            }
        });
    }

    private void showDateTimeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_periodicity_options, null);
        Button dateButton = dialogView.findViewById(R.id.selectDateButton);
        Button timeButton = dialogView.findViewById(R.id.selectTimeButton);

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
                dateButton.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                timeButton.setText(selectedTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Configurar Fecha y Hora")
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    if (selectedDate != null && selectedTime != null) {
                        selectedDatesTextView.setText(String.format("Fecha: %s Hora: %s", selectedDate, selectedTime));
                    } else {
                        Toast.makeText(this, "Por favor selecciona una fecha y hora", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    private void showEndDateDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedEndDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
            selectedEndDateTextView.setText("Fecha de finalización: " + selectedEndDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Manejar resultado de selección de archivo
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            Toast.makeText(this, "Archivo seleccionado: " + fileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveActivity() {
        String activityName = activityNameEditText.getText().toString().trim();
        String date = selectedDate;
        String time = selectedTime;
        String lugar = locationSpinner.getSelectedItem() != null ? locationSpinner.getSelectedItem().toString() : "Sin lugar";
        String oferentes = providerEditText.getText().toString().trim();
        String beneficiarios = beneficiariesEditText.getText().toString().trim();
        String cupo = cupoEditText.getText().toString().trim();
        String capacitacion = capacitacionSpinner.getSelectedItem() != null ? capacitacionSpinner.getSelectedItem().toString() : "Sin capacitación";
        String frequency = selectedFrequency;
        String endDate = selectedEndDate;

        if (activityName.isEmpty() || date == null || date.isEmpty() || time == null || time.isEmpty() || cupo.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!frequency.equals("Una vez") && (endDate == null || endDate.isEmpty())) {
            Toast.makeText(this, "Por favor selecciona una fecha de finalización", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userActivitiesRef = databaseReference.child(userId);
        String activityId = userActivitiesRef.push().getKey();

        Map<String, Object> activityData = new HashMap<>();
        activityData.put("activityId", activityId);
        activityData.put("name", activityName);
        activityData.put("fecha", date);
        activityData.put("hora", time);
        activityData.put("lugar", lugar);
        activityData.put("oferentes", oferentes.isEmpty() ? "Sin proveedor" : oferentes);
        activityData.put("beneficiarios", beneficiarios.isEmpty() ? "Sin beneficiarios" : beneficiarios);
        activityData.put("cupo", cupo);
        activityData.put("capacitacion", capacitacion);
        activityData.put("frequency", frequency);
        activityData.put("endDate", endDate);

        if (fileUri != null) {
            Toast.makeText(this, "Subiendo archivo a Firebase Storage...", Toast.LENGTH_SHORT).show();
            StorageReference fileRef = storageReference.child(fileUri.getLastPathSegment());
            fileRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        activityData.put("fileUrl", uri.toString());
                        saveActivityToFirebase(userActivitiesRef, activityId, activityData);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al subir archivo: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            saveActivityToFirebase(userActivitiesRef, activityId, activityData);
        }
    }

    private void saveActivityToFirebase(DatabaseReference userActivitiesRef, String activityId, Map<String, Object> activityData) {
        if (activityId != null) {
            userActivitiesRef.child(activityId).setValue(activityData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Programar notificaciones
                            scheduleNotifications(activityId, activityData.get("name").toString(), activityData.get("fecha").toString(), activityData.get("hora").toString(), activityData.get("frequency").toString(), activityData.get("endDate") != null ? activityData.get("endDate").toString() : null);

                            Toast.makeText(this, "Actividad guardada correctamente en Firebase", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(CreateActivity.this, HomeActivity.class);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Error al guardar la actividad en Firebase Database", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error específico al guardar en Firebase Database: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            Toast.makeText(this, "Error: no se pudo generar el ID de la actividad", Toast.LENGTH_LONG).show();
        }
    }

    private void scheduleNotifications(String activityId, String activityName, String startDate, String startTime, String frequency, String endDate) {
        try {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date startDateObj = sdfDate.parse(startDate);
            Date endDateObj = endDate != null ? sdfDate.parse(endDate) : startDateObj;

            // Validar que la fecha de finalización es posterior a la fecha de inicio
            if (endDateObj.before(startDateObj)) {
                Toast.makeText(this, "La fecha de finalización debe ser posterior a la fecha de inicio", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDateObj);

            // Lista de frecuencias válidas
            List<String> validFrequencies = Arrays.asList(frequencyOptions);

            // Validar frecuencia
            if (!validFrequencies.contains(frequency)) {
                Toast.makeText(this, "Frecuencia no válida", Toast.LENGTH_SHORT).show();
                return;
            }

            // Loop through dates based on frequency
            while (!calendar.getTime().after(endDateObj)) {
                String occurrenceDate = sdfDate.format(calendar.getTime());
                String fechaHora = occurrenceDate + " " + startTime;
                Date activityDateTime = sdfDateTime.parse(fechaHora);
                long activityTimeMillis = activityDateTime.getTime();

                long currentTimeMillis = System.currentTimeMillis();

                // Programar notificación un día antes
                long oneDayBeforeMillis = activityTimeMillis - TimeUnit.DAYS.toMillis(1);
                if (oneDayBeforeMillis > currentTimeMillis) {
                    scheduleNotification(
                            activityId,
                            "La actividad \"" + activityName + "\" es mañana.",
                            oneDayBeforeMillis,
                            activityId + "_one_day_before_" + occurrenceDate
                    );
                }

                // Programar notificación un minuto antes
                long oneMinuteBeforeMillis = activityTimeMillis - TimeUnit.MINUTES.toMillis(1);
                if (oneMinuteBeforeMillis > currentTimeMillis) {
                    scheduleNotification(
                            activityId,
                            "La actividad \"" + activityName + "\" está por comenzar.",
                            oneMinuteBeforeMillis,
                            activityId + "_one_minute_before_" + occurrenceDate
                    );
                }

                // Incrementar la fecha según la frecuencia
                switch (frequency) {
                    case "Diaria":
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        break;
                    case "Semanal":
                        calendar.add(Calendar.WEEK_OF_YEAR, 1);
                        break;
                    case "Mensual":
                        calendar.add(Calendar.MONTH, 1);
                        break;
                    default:
                        // Para "Una vez", salimos del bucle
                        break;
                }

                // Detener el bucle si la frecuencia es "Una vez"
                if (frequency.equals("Una vez")) {
                    break;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al programar notificaciones. Formato de fecha u hora inválido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleNotification(String activityId, String message, long triggerAtMillis, String uniqueId) {
        long delay = triggerAtMillis - System.currentTimeMillis();
        if (delay < 0) {
            // Si el tiempo ya pasó, no programamos la notificación
            return;
        }

        // Crear datos para pasar al Worker
        Data notificationData = new Data.Builder()
                .putString("title", "Recordatorio de Actividad")
                .putString("message", message)
                .build();

        // Crear solicitud de trabajo única
        OneTimeWorkRequest notificationRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(notificationData)
                .addTag(uniqueId) // Agregar una etiqueta única para poder cancelar si es necesario
                .build();

        // Encolar el trabajo
        WorkManager.getInstance(getApplicationContext()).enqueue(notificationRequest);
    }
}
