package com.gestionactividades.centrointegralalerce;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class EditActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private EditText nameEditText, providerEditText, beneficiariesEditText, cupoEditText;
    private Spinner locationSpinner, capacitacionSpinner;
    private TextView selectedDatesTextView;
    private Button selectDateButton, uploadFileButton, saveChangesButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Vincular vistas
        nameEditText = findViewById(R.id.editActivityNameEditText);
        providerEditText = findViewById(R.id.editProviderEditText);
        beneficiariesEditText = findViewById(R.id.editBeneficiariesEditText);
        cupoEditText = findViewById(R.id.editCupoEditText);
        locationSpinner = findViewById(R.id.editLocationSpinner);
        capacitacionSpinner = findViewById(R.id.editCapacitacionSpinner);
        selectedDatesTextView = findViewById(R.id.editSelectedDatesTextView);
        selectDateButton = findViewById(R.id.editSelectDateButton);
        uploadFileButton = findViewById(R.id.editUploadFileButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        // Configurar los Spinners
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locationOptions);
        locationSpinner.setAdapter(locationAdapter);

        ArrayAdapter<String> capacitacionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, capacitacionOptions);
        capacitacionSpinner.setAdapter(capacitacionAdapter);

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("activities");
        storageReference = FirebaseStorage.getInstance().getReference("activity_files");

        // Obtener datos del Intent
        activityId = getIntent().getStringExtra("activityId"); // Asegúrate de enviar activityId desde ActivityDetailActivity
        String name = getIntent().getStringExtra("name");
        String fecha = getIntent().getStringExtra("fecha");
        String lugar = getIntent().getStringExtra("lugar");
        String oferentes = getIntent().getStringExtra("oferentes");
        String beneficiarios = getIntent().getStringExtra("beneficiarios");
        String cupo = getIntent().getStringExtra("cupo");
        String capacitacion = getIntent().getStringExtra("capacitacion");
        fileUrl = getIntent().getStringExtra("fileUrl");

        // Establecer los datos en los campos de edición
        nameEditText.setText(name);
        selectedDatesTextView.setText(fecha);
        providerEditText.setText(oferentes);
        beneficiariesEditText.setText(beneficiarios);
        cupoEditText.setText(cupo);

        // Seleccionar las opciones en los Spinners
        if (lugar != null) {
            int locationPosition = locationAdapter.getPosition(lugar);
            locationSpinner.setSelection(locationPosition);
        }
        if (capacitacion != null) {
            int capacitacionPosition = capacitacionAdapter.getPosition(capacitacion);
            capacitacionSpinner.setSelection(capacitacionPosition);
        }

        // Configurar el botón de seleccionar fecha y hora
        selectDateButton.setOnClickListener(v -> showDateTimePicker());

        // Configurar el botón de subir archivo
        uploadFileButton.setOnClickListener(v -> openFilePicker());

        // Configurar el botón de guardar cambios
        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = day + "/" + (month + 1) + "/" + year;

            new TimePickerDialog(this, (timeView, hour, minute) -> {
                String dateTime = "Fecha: " + date + " Hora: " + hour + ":" + minute;
                selectedDatesTextView.setText(dateTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

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
        String updatedName = nameEditText.getText().toString();
        String updatedFecha = selectedDatesTextView.getText().toString();
        String updatedLugar = locationSpinner.getSelectedItem().toString();
        String updatedProvider = providerEditText.getText().toString();
        String updatedBeneficiaries = beneficiariesEditText.getText().toString();
        String updatedCupo = cupoEditText.getText().toString();
        String updatedCapacitacion = capacitacionSpinner.getSelectedItem().toString();

        // Crear mapa con los valores actualizados
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", updatedName);
        updates.put("fecha", updatedFecha);
        updates.put("lugar", updatedLugar);
        updates.put("oferentes", updatedProvider);
        updates.put("beneficiarios", updatedBeneficiaries);
        updates.put("cupo", updatedCupo);
        updates.put("capacitacion", updatedCapacitacion);

        if (fileUri != null) {
            StorageReference fileRef = storageReference.child(fileUri.getLastPathSegment());
            fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updates.put("fileUrl", uri.toString());
                saveToDatabase(updates);
            }));
        } else {
            saveToDatabase(updates);
        }
    }

    private void saveToDatabase(Map<String, Object> updates) {
        if (activityId != null) {
            databaseReference.child(activityId).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditActivity.this, "Actividad actualizada con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditActivity.this, "Error al actualizar la actividad", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
