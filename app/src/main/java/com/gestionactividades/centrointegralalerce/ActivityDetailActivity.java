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
        String lugar = getIntent().getStringExtra("lugar");  // Cambiado a "lugar"
        String oferentes = getIntent().getStringExtra("oferentes");  // Cambiado a "oferentes"
        String beneficiarios = getIntent().getStringExtra("beneficiarios");
        String description = getIntent().getStringExtra("description");
        String type = getIntent().getStringExtra("type");

        String fileUrl = getIntent().getStringExtra("fileUrl");

        // Configurar los TextViews con los datos recibidos
        TextView nameTextView = findViewById(R.id.activityNameTextView);
        TextView dateTextView = findViewById(R.id.activityDateTextView);
        TextView locationTextView = findViewById(R.id.activityLocationTextView);
        TextView providerTextView = findViewById(R.id.activityProviderTextView);
        TextView beneficiariesTextView = findViewById(R.id.activityBeneficiariesTextView);
        TextView fileLinkTextView = findViewById(R.id.activityFileLinkTextView);

        nameTextView.setText(name != null ? name : "Sin nombre");
        dateTextView.setText(fecha != null ? fecha : "Sin fecha");
        locationTextView.setText(lugar != null ? lugar : "Sin lugar");
        providerTextView.setText(oferentes != null ? oferentes : "Sin oferentes");
        beneficiariesTextView.setText(beneficiarios != null ? beneficiarios : "Sin beneficiarios");

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
