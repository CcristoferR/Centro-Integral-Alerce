package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ActivityDetailActivity extends AppCompatActivity {

    private MaterialTextView activityNameTextView, activityDateLocationTextView;
    private MaterialTextView providerTextView, beneficiariesTextView;
    private MaterialTextView cupoTextView, capacitacionTextView;
    private MaterialButton fileButton, editActivityButton, rescheduleActivityButton, cancelActivityButton, shareActivityButton;
    private MaterialButton backButtonDetail;

    private DatabaseReference activityRef;
    private DatabaseReference cancellationsRef;
    private String activityId;
    private String userId;

    private EventActivity currentActivity; // Guardamos la actividad actual
    private Uri fileUri; // URI del archivo adjunto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Configuración del Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar_detail);

        // Inicializar el botón de regreso
        backButtonDetail = findViewById(R.id.backButtonDetail);
        backButtonDetail.setOnClickListener(v -> finish());

        // Vincular vistas
        activityNameTextView = findViewById(R.id.activityNameTextView);
        activityDateLocationTextView = findViewById(R.id.activityDateLocationTextView);
        providerTextView = findViewById(R.id.activityProviderTextView);
        beneficiariesTextView = findViewById(R.id.activityBeneficiariesTextView);
        cupoTextView = findViewById(R.id.activityCupoTextView);
        capacitacionTextView = findViewById(R.id.activityCapacitacionTextView);
        fileButton = findViewById(R.id.activityFileButton);
        editActivityButton = findViewById(R.id.editActivityButton);
        rescheduleActivityButton = findViewById(R.id.rescheduleActivityButton);
        cancelActivityButton = findViewById(R.id.cancelActivityButton);
        shareActivityButton = findViewById(R.id.shareActivityButton);

        // Obtener el activityId desde el Intent
        activityId = getIntent().getStringExtra("activityId");

        // Verifica que activityId no sea nulo para proceder
        if (activityId != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            activityRef = FirebaseDatabase.getInstance().getReference("activities").child(userId).child(activityId);
            cancellationsRef = FirebaseDatabase.getInstance().getReference("cancellations").child(userId).child(activityId);

            // Configurar el botón "Modificar" para navegar a EditActivity
            editActivityButton.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityDetailActivity.this, EditActivity.class);
                intent.putExtra("activityId", activityId);
                startActivityForResult(intent, 1);
            });

            // Configurar el botón "Reagendar"
            rescheduleActivityButton.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityDetailActivity.this, RescheduleActivity.class);
                intent.putExtra("activityId", activityId);
                startActivityForResult(intent, 2);
            });

            // Configurar el botón "Cancelar Actividad"
            cancelActivityButton.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityDetailActivity.this, CancelActivity.class);
                intent.putExtra("activityId", activityId);
                startActivityForResult(intent, 3);
            });

            // Configurar el botón "Compartir Actividad"
            shareActivityButton.setOnClickListener(v -> shareActivity());

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
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // La actividad ha sido eliminada (cancelada)
                    fetchCancellationReason();
                    return;
                }

                currentActivity = snapshot.getValue(EventActivity.class);
                if (currentActivity != null) {
                    // Actualizar los campos de la interfaz con los datos de Firebase
                    activityNameTextView.setText(currentActivity.getName() != null ? currentActivity.getName() : "Sin nombre");

                    String dateText = currentActivity.getFecha() != null && !currentActivity.getFecha().isEmpty() ? currentActivity.getFecha() : "Sin fecha";
                    String locationText = currentActivity.getLugar() != null && !currentActivity.getLugar().isEmpty() ? currentActivity.getLugar() : "Sin lugar";
                    activityDateLocationTextView.setText(String.format("%s • %s", dateText, locationText));

                    String providerText = currentActivity.getOferentes() != null && !currentActivity.getOferentes().isEmpty() ? currentActivity.getOferentes() : "Sin proveedor";
                    providerTextView.setText(providerText);

                    String beneficiariesText = currentActivity.getBeneficiarios() != null && !currentActivity.getBeneficiarios().isEmpty() ? currentActivity.getBeneficiarios() : "Sin beneficiarios";
                    beneficiariesTextView.setText(beneficiariesText);

                    String cupoText = currentActivity.getCupo() != null && !currentActivity.getCupo().isEmpty() ? currentActivity.getCupo() : "Sin cupo";
                    cupoTextView.setText(cupoText);

                    String capacitacionText = currentActivity.getCapacitacion() != null && !currentActivity.getCapacitacion().isEmpty() ? currentActivity.getCapacitacion() : "Sin tipo";
                    capacitacionTextView.setText(capacitacionText);

                    // Actividad no cancelada, habilitar botones
                    editActivityButton.setEnabled(true);
                    rescheduleActivityButton.setEnabled(true);
                    cancelActivityButton.setEnabled(true);
                    shareActivityButton.setEnabled(true);

                    // Configurar botón para abrir archivo si existe URL
                    if (currentActivity.getFileUrl() != null && !currentActivity.getFileUrl().isEmpty()) {
                        fileButton.setVisibility(View.VISIBLE);
                        fileButton.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentActivity.getFileUrl()));
                            startActivity(intent);
                        });

                        // Obtener URI del archivo para adjuntarlo al correo
                        fetchFileUri(currentActivity.getFileUrl());
                    } else {
                        fileButton.setVisibility(View.GONE);
                        fileUri = null;
                    }
                } else {
                    Toast.makeText(ActivityDetailActivity.this, "No se pudieron cargar los detalles.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ActivityDetailActivity.this, "Error de base de datos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCancellationReason() {
        // Si la actividad ha sido eliminada, revisamos la sección 'cancellations' para obtener el motivo
        cancellationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

                // Deshabilitar los botones
                editActivityButton.setEnabled(false);
                rescheduleActivityButton.setEnabled(false);
                cancelActivityButton.setEnabled(false);
                shareActivityButton.setEnabled(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ActivityDetailActivity.this, "Error al recuperar la información de cancelación.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFileUri(String fileUrl) {
        // Obtener el URI del archivo adjunto desde Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            fileUri = uri;
        }).addOnFailureListener(e -> {
            Toast.makeText(ActivityDetailActivity.this, "Error al obtener el archivo adjunto.", Toast.LENGTH_SHORT).show();
            fileUri = null;
        });
    }

    private void shareActivity() {
        if (currentActivity == null) {
            Toast.makeText(this, "No se puede compartir. Los detalles de la actividad no están cargados.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construir el texto del correo electrónico con los detalles de la actividad
        String emailSubject = "Detalles de la Actividad: " + currentActivity.getName();
        String emailBody = "Nombre de la Actividad: " + currentActivity.getName() + "\n" +
                "Fecha: " + (currentActivity.getFecha() != null ? currentActivity.getFecha() : "Sin fecha") + "\n" +
                "Hora: " + (currentActivity.getHora() != null ? currentActivity.getHora() : "Sin hora") + "\n" +
                "Lugar: " + (currentActivity.getLugar() != null ? currentActivity.getLugar() : "Sin lugar") + "\n" +
                "Oferentes: " + (currentActivity.getOferentes() != null ? currentActivity.getOferentes() : "Sin oferentes") + "\n" +
                "Beneficiarios: " + (currentActivity.getBeneficiarios() != null ? currentActivity.getBeneficiarios() : "Sin beneficiarios") + "\n" +
                "Cupo: " + (currentActivity.getCupo() != null ? currentActivity.getCupo() : "Sin cupo") + "\n" +
                "Tipo de Actividad: " + (currentActivity.getCapacitacion() != null ? currentActivity.getCapacitacion() : "Sin tipo") + "\n";

        // Intent para enviar correo electrónico
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("*/*");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        // Adjuntar el archivo si existe
        if (fileUri != null) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Compartir Actividad vía"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No hay aplicaciones de correo instaladas.", Toast.LENGTH_SHORT).show();
        }
    }
}
