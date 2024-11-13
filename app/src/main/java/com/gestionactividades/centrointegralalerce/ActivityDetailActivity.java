package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase; // Asegúrate de tener esta importación
import com.google.firebase.database.ValueEventListener;

public class ActivityDetailActivity extends AppCompatActivity {

    private TextView activityNameTextView, activityDateLocationTextView;
    private TextView providerTextView, beneficiariesTextView;
    private TextView cupoTextView, capacitacionTextView;
    private Button fileButton, backButton, editActivityButton;

    private DatabaseReference databaseReference;
    private String activityId;
    private String userId; // Agregamos esta variable para mantener el userId

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
        backButton = findViewById(R.id.backButton);
        editActivityButton = findViewById(R.id.editActivityButton);

        // Obtener el activityId desde el Intent
        activityId = getIntent().getStringExtra("activityId");

        // Verifica que activityId no sea nulo para proceder
        if (activityId != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("activities").child(userId).child(activityId);

            Log.d("ActivityDetailActivity", "activityId: " + activityId);
            Log.d("ActivityDetailActivity", "userId: " + userId);

            // Configurar el botón "Volver"
            backButton.setOnClickListener(v -> finish());

            // Configurar el botón "Modificar" para navegar a EditActivity
            editActivityButton.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityDetailActivity.this, EditActivity.class);
                intent.putExtra("activityId", activityId);
                startActivityForResult(intent, 1);
            });

            // Cargar los detalles de la actividad
            loadActivityDetails();
        } else {
            finish();
            Toast.makeText(this, "Error al cargar detalles de la actividad.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            loadActivityDetails(); // Recargar detalles si hubo cambios
            setResult(RESULT_OK); // Indicar a HomeActivity que debe actualizarse
        }
    }

    private void loadActivityDetails() {
        // Asegurarse de que la referencia incluye el userId y activityId correctos
        DatabaseReference activityRef = FirebaseDatabase.getInstance().getReference("activities").child(userId).child(activityId);

        activityRef.addValueEventListener(new ValueEventListener() { // Cambiamos a addValueEventListener para escuchar cambios en tiempo real
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EventActivity activity = dataSnapshot.getValue(EventActivity.class);
                if (activity != null) {
                    Log.d("ActivityDetailActivity", "Datos de la actividad cargados: " + activity.getName());

                    // Actualizar los campos de la interfaz con los datos de Firebase
                    activityNameTextView.setText(activity.getName() != null ? activity.getName() : "Sin nombre");
                    activityDateLocationTextView.setText(String.format("%s • %s",
                            activity.getFecha() != null ? activity.getFecha() : "Sin fecha",
                            activity.getLugar() != null ? activity.getLugar() : "Sin lugar"));
                    providerTextView.setText(activity.getOferentes() != null ? activity.getOferentes() : "Sin proveedor");
                    beneficiariesTextView.setText(activity.getBeneficiarios() != null ? activity.getBeneficiarios() : "Sin beneficiarios");
                    cupoTextView.setText(activity.getCupo() != null ? activity.getCupo() : "Sin cupo");
                    capacitacionTextView.setText(activity.getCapacitacion() != null ? activity.getCapacitacion() : "Sin capacitación");

                    // Configurar botón para abrir archivo si existe URL
                    if (activity.getFileUrl() != null && !activity.getFileUrl().isEmpty()) {
                        fileButton.setVisibility(View.VISIBLE);
                        fileButton.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getFileUrl()));
                            startActivity(intent);
                        });
                    } else {
                        fileButton.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ActivityDetailActivity.this, "No se pudieron cargar los detalles.", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad si no se encuentran los detalles
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ActivityDetailActivity.this, "Error de base de datos.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
