package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityDetailActivity extends AppCompatActivity {

    private TextView activityNameTextView, activityDateLocationTextView;
    private TextView providerTextView, beneficiariesTextView;
    private TextView cupoTextView, capacitacionTextView;
    private Button fileButton, backButton, editActivityButton, rescheduleActivityButton, cancelActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Vincular vistas
        activityNameTextView = findViewById(R.id.activityNameTextView);
        activityDateLocationTextView = findViewById(R.id.activityDateLocationTextView);
        providerTextView = findViewById(R.id.activityProviderTextView);
        beneficiariesTextView = findViewById(R.id.activityBeneficiariesTextView);
        cupoTextView = findViewById(R.id.activityCupoTextView);
        capacitacionTextView = findViewById(R.id.activityCapacitacionTextView);
        fileButton = findViewById(R.id.activityFileButton);

        // Botones de acción
        backButton = findViewById(R.id.backButton);
        editActivityButton = findViewById(R.id.editActivityButton);
        rescheduleActivityButton = findViewById(R.id.rescheduleActivityButton);
        cancelActivityButton = findViewById(R.id.cancelActivityButton);

        // Obtener datos del Intent
        String name = getIntent().getStringExtra("name");
        String fecha = getIntent().getStringExtra("fecha");
        String lugar = getIntent().getStringExtra("lugar");
        String oferentes = getIntent().getStringExtra("oferentes");
        String beneficiarios = getIntent().getStringExtra("beneficiarios");
        String cupo = getIntent().getStringExtra("cupo");
        String capacitacion = getIntent().getStringExtra("capacitacion");
        String fileUrl = getIntent().getStringExtra("fileUrl");

        // Configurar vistas con datos
        activityNameTextView.setText(name != null ? name : "Sin nombre");
        activityDateLocationTextView.setText(String.format("%s • %s",
                fecha != null ? fecha : "Sin fecha",
                lugar != null ? lugar : "Sin lugar"));
        providerTextView.setText(oferentes != null ? oferentes : "Sin proveedor");
        beneficiariesTextView.setText(beneficiarios != null ? beneficiarios : "Sin beneficiarios");
        cupoTextView.setText(cupo != null ? cupo : "Sin cupo");
        capacitacionTextView.setText(capacitacion != null ? capacitacion : "Sin capacitación");

        // Configurar botón de archivo adjunto
        if (fileUrl != null && !fileUrl.isEmpty()) {
            fileButton.setVisibility(View.VISIBLE);
            fileButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                startActivity(intent);
            });
        } else {
            fileButton.setVisibility(View.GONE);
        }

        // Configurar el botón "Volver" para regresar al HomeActivity
        backButton.setOnClickListener(v -> finish());

        // Configurar el botón "Modificar" para navegar a EditActivity
        editActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDetailActivity.this, EditActivity.class);
            // Pasar datos de la actividad actual a EditActivity
            intent.putExtra("name", name);
            intent.putExtra("fecha", fecha);
            intent.putExtra("lugar", lugar);
            intent.putExtra("oferentes", oferentes);
            intent.putExtra("beneficiarios", beneficiarios);
            intent.putExtra("cupo", cupo);
            intent.putExtra("capacitacion", capacitacion);
            intent.putExtra("fileUrl", fileUrl);
            startActivity(intent);
        });

        // Configuración de otros botones (opcional)
        rescheduleActivityButton.setOnClickListener(v -> {
            // Aquí puedes agregar la lógica para reagendar la actividad
        });

        cancelActivityButton.setOnClickListener(v -> {
            // Aquí puedes agregar la lógica para cancelar la actividad
        });
    }
}
