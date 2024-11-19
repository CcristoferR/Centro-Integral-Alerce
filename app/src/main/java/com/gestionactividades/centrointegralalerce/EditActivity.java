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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EditActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private TextInputEditText nameEditText, providerEditText, beneficiariesEditText, cupoEditText;
    private Spinner locationSpinner, capacitacionSpinner, frequencySpinner;
    private TextView selectedDatesTextView, selectedEndDateTextView;
    private LinearLayout endDateLayout;
    private Button selectDateButton, selectEndDateButton, uploadFileButton, saveChangesButton, backButtonEdit;
    private Uri fileUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private String activityId; // ID de la actividad que estamos editando
    private String fileUrl; // URL del archivo actual

    private String[] locationOptions = {"Oficina del centro", "Lugares del territorio", "Otro lugar"};
    private String[] capacitacionOptions = {
            "Capacitación", "Taller", "Charlas", "Atenciones",
            "Operativo en oficina", "Operativo rural", "Operativo",
            "Práctica profesional", "Diagnóstico"
    };
    private String[] frequencyOptions = {"Una vez", "Diaria", "Semanal", "Mensual"};

    private String selectedDate; // Fecha seleccionada
    private String selectedTime; // Hora seleccionada
    private String selectedFrequency = "Una vez";
    private String selectedEndDate;

    private String previousStartDate;
    private String previousEndDate;
    private String previousFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Configuración del Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar_edit);

        // Inicializar el botón de regreso
        backButtonEdit = findViewById(R.id.backButtonEdit);
        backButtonEdit.setOnClickListener(v -> finish());

        // Vincular vistas
        nameEditText = findViewById(R.id.editActivityNameEditText);
        providerEditText = findViewById(R.id.editProviderEditText);
        beneficiariesEditText = findViewById(R.id.editBeneficiariesEditText);
        cupoEditText = findViewById(R.id.editCupoEditText);
        locationSpinner = findViewById(R.id.editLocationSpinner);
        capacitacionSpinner = findViewById(R.id.editCapacitacionSpinner);
        frequencySpinner = findViewById(R.id.editFrequencySpinner);
        selectedDatesTextView = findViewById(R.id.editSelectedDatesTextView);
        selectedEndDateTextView = findViewById(R.id.editSelectedEndDateTextView);
        endDateLayout = findViewById(R.id.editEndDateLayout);
        selectDateButton = findViewById(R.id.editSelectDateButton);
        selectEndDateButton = findViewById(R.id.editSelectEndDateButton);
        uploadFileButton = findViewById(R.id.editUploadFileButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        // Configurar los Spinners
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locationOptions);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        ArrayAdapter<String> capacitacionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, capacitacionOptions);
        capacitacionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capacitacionSpinner.setAdapter(capacitacionAdapter);

        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencyOptions);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);

        // Obtener el UID del usuario actual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("activities").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference("activity_files");

        // Obtener el activityId del Intent
        activityId = getIntent().getStringExtra("activityId");

        // Cargar los detalles de la actividad desde Firebase
        loadActivityDetails();

        // Configurar el botón para seleccionar fecha y hora
        selectDateButton.setOnClickListener(v -> showDateTimePicker());

        // Configurar el botón para seleccionar fecha de finalización
        selectEndDateButton.setOnClickListener(v -> showEndDateDialog());

        // Configurar el botón para subir archivo
        uploadFileButton.setOnClickListener(v -> openFilePicker());

        // Configurar el botón para guardar cambios
        saveChangesButton.setOnClickListener(v -> saveChanges());

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

    private void loadActivityDetails() {
        // Obtenemos la referencia específica de la actividad
        DatabaseReference activityRef = databaseReference.child(activityId);

        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                EventActivity activity = snapshot.getValue(EventActivity.class);
                if (activity != null) {
                    // Precargar datos en los campos de edición
                    nameEditText.setText(activity.getName());
                    selectedDatesTextView.setText(String.format("Fecha: %s Hora: %s", activity.getFecha(), activity.getHora()));
                    providerEditText.setText(activity.getOferentes());
                    beneficiariesEditText.setText(activity.getBeneficiarios());
                    cupoEditText.setText(activity.getCupo());

                    // Guardar la fecha y hora seleccionadas
                    selectedDate = activity.getFecha();
                    selectedTime = activity.getHora();

                    // Seleccionar opciones en los Spinners
                    ArrayAdapter<String> locationAdapter = (ArrayAdapter<String>) locationSpinner.getAdapter();
                    int locationPosition = locationAdapter.getPosition(activity.getLugar());
                    if (locationPosition >= 0) {
                        locationSpinner.setSelection(locationPosition);
                    }

                    ArrayAdapter<String> capacitacionAdapter = (ArrayAdapter<String>) capacitacionSpinner.getAdapter();
                    int capacitacionPosition = capacitacionAdapter.getPosition(activity.getCapacitacion());
                    if (capacitacionPosition >= 0) {
                        capacitacionSpinner.setSelection(capacitacionPosition);
                    }

                    // Seleccionar frecuencia
                    ArrayAdapter<String> frequencyAdapter = (ArrayAdapter<String>) frequencySpinner.getAdapter();
                    int frequencyPosition = frequencyAdapter.getPosition(activity.getFrequency());
                    if (frequencyPosition >= 0) {
                        frequencySpinner.setSelection(frequencyPosition);
                    }

                    selectedFrequency = activity.getFrequency();
                    selectedEndDate = activity.getEndDate();
                    selectedEndDateTextView.setText("Fecha de finalización: " + (selectedEndDate != null ? selectedEndDate : "No seleccionada"));

                    // Mostrar u ocultar el layout de fecha de finalización
                    if (selectedFrequency.equals("Una vez")) {
                        endDateLayout.setVisibility(View.GONE);
                    } else {
                        endDateLayout.setVisibility(View.VISIBLE);
                    }

                    // Guardar el fileUrl actual
                    fileUrl = activity.getFileUrl();

                    // Guardar valores anteriores para cancelar notificaciones
                    previousStartDate = activity.getFecha();
                    previousEndDate = activity.getEndDate();
                    previousFrequency = activity.getFrequency();
                } else {
                    Toast.makeText(EditActivity.this, "No se pudo cargar la actividad.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditActivity.this, "Error al cargar datos.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showDateTimePicker() {
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

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            Toast.makeText(this, "Archivo seleccionado: " + fileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() {
        // Obtener valores actualizados
        String updatedName = nameEditText.getText().toString().trim();
        String updatedFecha = selectedDate;
        String updatedHora = selectedTime;
        String updatedLugar = locationSpinner.getSelectedItem() != null ? locationSpinner.getSelectedItem().toString() : "Sin lugar";
        String updatedProvider = providerEditText.getText().toString().trim();
        String updatedBeneficiaries = beneficiariesEditText.getText().toString().trim();
        String updatedCupo = cupoEditText.getText().toString().trim();
        String updatedCapacitacion = capacitacionSpinner.getSelectedItem() != null ? capacitacionSpinner.getSelectedItem().toString() : "Sin capacitación";
        String updatedFrequency = selectedFrequency;
        String updatedEndDate = selectedEndDate;

        // Validar campos obligatorios
        if (updatedName.isEmpty() || updatedFecha == null || updatedHora == null || updatedCupo.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!updatedFrequency.equals("Una vez") && (updatedEndDate == null || updatedEndDate.isEmpty())) {
            Toast.makeText(this, "Por favor selecciona una fecha de finalización", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear mapa con los valores actualizados
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", updatedName);
        updates.put("fecha", updatedFecha);
        updates.put("hora", updatedHora);
        updates.put("lugar", updatedLugar);
        updates.put("oferentes", updatedProvider.isEmpty() ? "Sin proveedor" : updatedProvider);
        updates.put("beneficiarios", updatedBeneficiaries.isEmpty() ? "Sin beneficiarios" : updatedBeneficiaries);
        updates.put("cupo", updatedCupo);
        updates.put("capacitacion", updatedCapacitacion);
        updates.put("frequency", updatedFrequency);
        updates.put("endDate", updatedEndDate);

        if (fileUri != null) {
            // Si hay un nuevo archivo, lo subimos y actualizamos el fileUrl
            StorageReference fileRef = storageReference.child(fileUri.getLastPathSegment());
            fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("fileUrl", uri.toString());
                        saveToDatabase(updates);
                    })
            ).addOnFailureListener(e -> {
                Toast.makeText(EditActivity.this, "Error al subir el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // Si no se seleccionó un nuevo archivo, conservamos el fileUrl existente
            if (fileUrl != null) {
                updates.put("fileUrl", fileUrl);
            }
            saveToDatabase(updates);
        }
    }

    private void saveToDatabase(Map<String, Object> updates) {
        if (activityId != null) {
            databaseReference.child(activityId).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Cancelar notificaciones previas
                    cancelScheduledNotifications(activityId, previousStartDate, previousEndDate, previousFrequency);

                    // Programar nuevas notificaciones
                    scheduleNotifications(
                            activityId,
                            updates.get("name").toString(),
                            updates.get("fecha").toString(),
                            updates.get("hora").toString(),
                            updates.get("frequency").toString(),
                            updates.get("endDate") != null ? updates.get("endDate").toString() : null
                    );

                    Toast.makeText(EditActivity.this, "Actividad actualizada con éxito", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Indica que los cambios se realizaron con éxito
                    finish(); // Cierra EditActivity y vuelve a ActivityDetailActivity
                } else {
                    Toast.makeText(EditActivity.this, "Error al actualizar la actividad", Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            Toast.makeText(EditActivity.this, "Error: activityId es nulo.", Toast.LENGTH_SHORT).show();
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

    private void cancelScheduledNotifications(String activityId, String startDate, String endDate, String frequency) {
        try {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            Date startDateObj = sdfDate.parse(startDate);
            Date endDateObj = endDate != null ? sdfDate.parse(endDate) : startDateObj;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDateObj);

            // Lista de frecuencias válidas
            List<String> validFrequencies = Arrays.asList(frequencyOptions);

            // Validar frecuencia
            if (!validFrequencies.contains(frequency)) {
                return;
            }

            while (!calendar.getTime().after(endDateObj)) {
                String occurrenceDate = sdfDate.format(calendar.getTime());

                // Cancelar notificación un día antes
                WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(activityId + "_one_day_before_" + occurrenceDate);

                // Cancelar notificación un minuto antes
                WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(activityId + "_one_minute_before_" + occurrenceDate);

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

                if (frequency.equals("Una vez")) {
                    break;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
