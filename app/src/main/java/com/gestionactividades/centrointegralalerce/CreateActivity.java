package com.gestionactividades.centrointegralalerce;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private EditText activityNameEditText, providerEditText, beneficiariesEditText, cupoEditText;
    private Spinner locationSpinner, capacitacionSpinner;
    private TextView selectedDatesTextView;
    private Button uploadFileButton, saveActivityButton, selectDateButton;
    private Uri fileUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private String[] locationOptions = {"Oficina del centro", "Lugares del territorio", "Otro lugar"};
    private String[] capacitacionOptions = {"Capacitación A", "Capacitación B", "Capacitación C"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Vincular variables con los elementos de la vista
        activityNameEditText = findViewById(R.id.activityNameEditText);
        providerEditText = findViewById(R.id.providerEditText);
        beneficiariesEditText = findViewById(R.id.beneficiariesEditText);
        cupoEditText = findViewById(R.id.cupoEditText);
        locationSpinner = findViewById(R.id.locationSpinner);
        capacitacionSpinner = findViewById(R.id.capacitacionSpinner);
        selectedDatesTextView = findViewById(R.id.selectedDatesTextView);

        uploadFileButton = findViewById(R.id.uploadFileButton);
        saveActivityButton = findViewById(R.id.saveActivityButton);
        selectDateButton = findViewById(R.id.selectDateButton);

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

        // Configurar listeners para los botones
        selectDateButton.setOnClickListener(v -> showPeriodicityDialog());
        uploadFileButton.setOnClickListener(v -> openFilePicker());
        saveActivityButton.setOnClickListener(v -> saveActivityWithFile());
    }

    private void showPeriodicityDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_periodicity_options, null);
        Spinner frequencySpinner = dialogView.findViewById(R.id.frequencySpinner);
        Button dateButton = dialogView.findViewById(R.id.selectDateButton);
        Button timeButton = dialogView.findViewById(R.id.selectTimeButton);

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                dateButton.setText(day + "/" + (month + 1) + "/" + year);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                timeButton.setText(hour + ":" + minute);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Configurar Fecha, Hora y Periodicidad")
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String selectedDate = dateButton.getText().toString();
                    String selectedTime = timeButton.getText().toString();
                    if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                        selectedDatesTextView.setText("Fecha: " + selectedDate + " Hora: " + selectedTime);
                    } else {
                        Toast.makeText(this, "Por favor selecciona una fecha y hora", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
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

    private void saveActivityWithFile() {
        String activityName = activityNameEditText.getText().toString();
        String fecha = selectedDatesTextView.getText().toString();
        String lugar = locationSpinner.getSelectedItem() != null ? locationSpinner.getSelectedItem().toString() : "Sin lugar";
        String oferentes = providerEditText.getText().toString();
        String beneficiarios = beneficiariesEditText.getText().toString();
        String cupo = cupoEditText.getText().toString();
        String capacitacion = capacitacionSpinner.getSelectedItem() != null ? capacitacionSpinner.getSelectedItem().toString() : "Sin capacitación";

        if (activityName.isEmpty() || fileUri == null || fecha.isEmpty() || cupo.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Subiendo archivo a Firebase Storage...", Toast.LENGTH_SHORT).show();

        // Subir el archivo a Firebase Storage
        StorageReference fileRef = storageReference.child(fileUri.getLastPathSegment());
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String fileUrl = uri.toString();
                    Toast.makeText(this, "Archivo subido con éxito", Toast.LENGTH_SHORT).show();

                    // Guardar la información de la actividad en Firebase Realtime Database
                    String activityId = databaseReference.push().getKey();
                    Map<String, Object> activityData = new HashMap<>();
                    activityData.put("name", activityName);
                    activityData.put("fileUrl", fileUrl);
                    activityData.put("fecha", fecha);
                    activityData.put("lugar", lugar);
                    activityData.put("oferentes", oferentes.isEmpty() ? "Sin proveedor" : oferentes);
                    activityData.put("beneficiarios", beneficiarios.isEmpty() ? "Sin beneficiarios" : beneficiarios);
                    activityData.put("cupo", cupo);
                    activityData.put("capacitacion", capacitacion);

                    if (activityId != null) {
                        databaseReference.child(activityId).setValue(activityData)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Actividad y archivo guardados correctamente en Firebase", Toast.LENGTH_LONG).show();
                                        finish(); // Finaliza la actividad y regresa a la anterior
                                    } else {
                                        Toast.makeText(this, "Error al guardar la actividad en Firebase Database", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error específico al guardar en Firebase Database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Toast.makeText(this, "Error: no se pudo generar el ID de la actividad", Toast.LENGTH_LONG).show();
                    }
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir archivo a Firebase Storage: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
