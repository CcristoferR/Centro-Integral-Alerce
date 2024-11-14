package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Importaciones necesarias
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ActivityDetailActivity extends AppCompatActivity {

    private TextView activityNameTextView, activityDateLocationTextView;
    private TextView providerTextView, beneficiariesTextView;
    private TextView cupoTextView, capacitacionTextView;
    private Button fileButton, backButton, editActivityButton, rescheduleActivityButton, cancelActivityButton;

    private DatabaseReference activityRef;
    private DatabaseReference cancellationsRef;
    private String activityId;
    private String userId;

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
        rescheduleActivityButton = findViewById(R.id.rescheduleActivityButton);
        cancelActivityButton = findViewById(R.id.cancelActivityButton);

        // Obtener el activityId desde el Intent
        activityId = getIntent().getStringExtra("activityId");

        // Verifica que activityId no sea nulo para proceder
        if (activityId != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            activityRef = FirebaseDatabase.getInstance().getReference("activities").child(userId).child(activityId);
            cancellationsRef = FirebaseDatabase.getInstance().getReference("cancellations").child(userId).child(activityId);

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

            // Configurar el botón "Reagendar" para navegar a RescheduleActivity (Si la funcionalidad existe)
            rescheduleActivityButton.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityDetailActivity.this, RescheduleActivity.class);
                intent.putExtra("activityId", activityId);
                startActivityForResult(intent, 2);
            });

            // Configurar el botón "Cancelar Actividad" para navegar a CancelActivity
            cancelActivityButton.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityDetailActivity.this, CancelActivity.class);
                intent.putExtra("activityId", activityId);
                startActivityForResult(intent, 3);
            });

            // Cargar los detalles de la actividad
            loadActivityDetails();
        } else {
            finish();
            Toast.makeText(this, "Error al cargar detalles de la actividad.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1 || requestCode == 2 || requestCode == 3) && resultCode == RESULT_OK) {
            loadActivityDetails(); // Recargar detalles si hubo cambios, reprogramación o cancelación
            setResult(RESULT_OK); // Indicar a HomeActivity que debe actualizarse
        }
    }

    private void loadActivityDetails() {
        // Revisar si la actividad existe todavía
        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // La actividad ha sido eliminada (cancelada)
                    fetchCancellationReason();
                    return;
                }

                EventActivity activity = dataSnapshot.getValue(EventActivity.class);
                if (activity != null) {
                    Log.d("ActivityDetailActivity", "Datos de la actividad cargados: " + activity.getName());

                    // Actualizar los campos de la interfaz con los datos de Firebase
                    activityNameTextView.setText(activity.getName() != null ? activity.getName() : "Sin nombre");

                    String dateText = activity.getFecha() != null && !activity.getFecha().isEmpty() ? activity.getFecha() : "Sin fecha";
                    String locationText = activity.getLugar() != null && !activity.getLugar().isEmpty() ? activity.getLugar() : "Sin lugar";
                    activityDateLocationTextView.setText(String.format("%s • %s", dateText, locationText));

                    String providerText = activity.getOferentes() != null && !activity.getOferentes().isEmpty() ? activity.getOferentes() : "Sin proveedor";
                    providerTextView.setText(providerText);

                    String beneficiariesText = activity.getBeneficiarios() != null && !activity.getBeneficiarios().isEmpty() ? activity.getBeneficiarios() : "Sin beneficiarios";
                    beneficiariesTextView.setText(beneficiariesText);

                    String cupoText = activity.getCupo() != null && !activity.getCupo().isEmpty() ? activity.getCupo() : "Sin cupo";
                    cupoTextView.setText(cupoText);

                    String capacitacionText = activity.getCapacitacion() != null && !activity.getCapacitacion().isEmpty() ? activity.getCapacitacion() : "Sin capacitación";
                    capacitacionTextView.setText(capacitacionText);

                    // Actividad no cancelada, habilitar botones
                    editActivityButton.setEnabled(true);
                    rescheduleActivityButton.setEnabled(true);
                    cancelActivityButton.setEnabled(true);

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
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ActivityDetailActivity.this, "Error de base de datos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCancellationReason() {
        // Si la actividad ha sido eliminada, revisamos la sección 'cancellations' para obtener el motivo
        DatabaseReference cancellationRef = FirebaseDatabase.getInstance().getReference("cancellations").child(userId).child(activityId);
        cancellationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // No hay información de cancelación
                    Toast.makeText(ActivityDetailActivity.this, "La actividad ha sido cancelada y eliminada.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String reason = snapshot.child("cancellationReason").getValue(String.class);
                String date = snapshot.child("cancellationDate").getValue(String.class);

                if (reason == null || reason.isEmpty()) {
                    reason = "Sin motivo de cancelación";
                }
                if (date == null || date.isEmpty()) {
                    date = "Fecha desconocida";
                }

                // Mostrar la información de cancelación
                Toast.makeText(ActivityDetailActivity.this, "Esta actividad ha sido cancelada.", Toast.LENGTH_LONG).show();

                // Puedes mostrar esta información en un TextView si lo deseas
                // Por ejemplo: cancellationInfoTextView.setText(String.format("Motivo: %s\nFecha: %s", reason, date));
                // cancellationInfoTextView.setVisibility(View.VISIBLE);

                // Deshabilitar los botones
                editActivityButton.setEnabled(false);
                rescheduleActivityButton.setEnabled(false);
                cancelActivityButton.setEnabled(false);

                // Concluimos la actividad si lo deseas
                // finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ActivityDetailActivity.this, "Error al recuperar la información de cancelación.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
