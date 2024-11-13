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
    private TextView rescheduleHistoryTextView; // TextView para el historial o motivo de cancelación
    private Button fileButton, backButton, editActivityButton, rescheduleActivityButton, cancelActivityButton;

    private DatabaseReference databaseReference;
    private String activityId;
    private String userId; // Variable para mantener el userId

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
        rescheduleHistoryTextView = findViewById(R.id.rescheduleHistoryTextView); // TextView para mostrar motivo de cancelación o reprogramaciones
        fileButton = findViewById(R.id.activityFileButton);
        backButton = findViewById(R.id.backButton);
        editActivityButton = findViewById(R.id.editActivityButton);
        rescheduleActivityButton = findViewById(R.id.rescheduleActivityButton);
        cancelActivityButton = findViewById(R.id.cancelActivityButton); // Nuevo botón "Cancelar Actividad"

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

            // Configurar el botón "Reagendar" para navegar a RescheduleActivity
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
        // Asegurarse de que la referencia incluye el userId y activityId correctos
        DatabaseReference activityRef = databaseReference;

        activityRef.addListenerForSingleValueEvent(new ValueEventListener() { // Escuchar una única vez
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

                    // Verificar si la actividad está cancelada
                    if (activity.isCancelled()) {
                        // Mostrar mensaje de cancelación
                        Toast.makeText(ActivityDetailActivity.this, "Esta actividad ha sido cancelada.", Toast.LENGTH_LONG).show();

                        // Deshabilitar botones que no aplican
                        editActivityButton.setEnabled(false);
                        rescheduleActivityButton.setEnabled(false);
                        cancelActivityButton.setEnabled(false);

                        // Mostrar motivo y fecha de cancelación
                        String cancellationInfo = "Actividad cancelada el " + activity.getCancellationDate() +
                                "\nMotivo: " + activity.getCancellationReason();
                        rescheduleHistoryTextView.setText(cancellationInfo);
                        rescheduleHistoryTextView.setVisibility(View.VISIBLE);
                    } else {
                        // Mostrar el historial de reprogramaciones si existe
                        if (activity.getReschedules() != null && !activity.getReschedules().isEmpty()) {
                            StringBuilder history = new StringBuilder();
                            for (EventActivity.RescheduleInfo reschedule : activity.getReschedules()) {
                                history.append("Fecha: ").append(reschedule.getDateTime())
                                        .append("\nMotivo: ").append(reschedule.getReason())
                                        .append("\n\n");
                            }
                            rescheduleHistoryTextView.setText(history.toString());
                            rescheduleHistoryTextView.setVisibility(View.VISIBLE);
                        } else {
                            rescheduleHistoryTextView.setVisibility(View.GONE); // Ocultar si no hay reprogramaciones
                        }
                    }

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
