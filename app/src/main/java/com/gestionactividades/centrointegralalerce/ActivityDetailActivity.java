package com.gestionactividades.centrointegralalerce;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Recibir los datos de la actividad seleccionada
        String name = getIntent().getStringExtra("name");
        String date = getIntent().getStringExtra("date");
        String location = getIntent().getStringExtra("location");
        String type = getIntent().getStringExtra("type");
        String provider = getIntent().getStringExtra("provider");

        // Configurar los TextViews con los datos recibidos
        TextView nameTextView = findViewById(R.id.activityNameTextView);
        TextView dateTextView = findViewById(R.id.activityDateTextView);
        TextView locationTextView = findViewById(R.id.activityLocationTextView);
        TextView typeTextView = findViewById(R.id.activityTypeTextView);
        TextView providerTextView = findViewById(R.id.activityProviderTextView);

        nameTextView.setText(name);
        dateTextView.setText(date);
        locationTextView.setText(location);
        typeTextView.setText(type);
        providerTextView.setText(provider);
    }
}
