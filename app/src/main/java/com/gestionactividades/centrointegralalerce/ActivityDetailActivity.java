package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Recibir los datos de la actividad seleccionada
        String name = getIntent().getStringExtra("name");
        String fecha = getIntent().getStringExtra("fecha");
        String lugar = getIntent().getStringExtra("lugar");
        String oferentes = getIntent().getStringExtra("oferentes");
        String beneficiarios = getIntent().getStringExtra("beneficiarios");
        String fileUrl = getIntent().getStringExtra("fileUrl");
        String cupo = getIntent().getStringExtra("cupo");
        String capacitacion = getIntent().getStringExtra("capacitacion");

        // Configurar los TextViews con los datos recibidos
        TextView nameTextView = findViewById(R.id.activityNameTextView);
        TextView dateTextView = findViewById(R.id.activityDateTextView);
        TextView locationTextView = findViewById(R.id.activityLocationTextView);
        TextView providerTextView = findViewById(R.id.activityProviderTextView);
        TextView beneficiariesTextView = findViewById(R.id.activityBeneficiariesTextView);
        TextView cupoTextView = findViewById(R.id.activityCupoTextView);
        TextView capacitacionTextView = findViewById(R.id.activityCapacitacionTextView);
        TextView fileLinkTextView = findViewById(R.id.activityFileLinkTextView);

        nameTextView.setText(name != null ? name : "Sin nombre");
        dateTextView.setText(fecha != null ? fecha : "Sin fecha");
        locationTextView.setText(lugar != null ? lugar : "Sin lugar");
        providerTextView.setText(oferentes != null ? oferentes : "Sin proveedor");
        beneficiariesTextView.setText(beneficiarios != null ? beneficiarios : "Sin beneficiarios");
        cupoTextView.setText(cupo != null ? cupo : "Sin cupo");
        capacitacionTextView.setText(capacitacion != null ? capacitacion : "Sin capacitación");

        // Configurar el enlace del archivo si está disponible
        if (fileUrl != null && !fileUrl.isEmpty()) {
            fileLinkTextView.setVisibility(View.VISIBLE);
            fileLinkTextView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                startActivity(intent);
            });
        } else {
            fileLinkTextView.setVisibility(View.GONE);
        }
    }
}
