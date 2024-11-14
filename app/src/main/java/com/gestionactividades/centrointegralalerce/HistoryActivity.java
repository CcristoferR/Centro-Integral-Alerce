package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyItemList;
    private DatabaseReference activitiesRef;
    private DatabaseReference cancellationsRef;
    private String userId;

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Vincular vistas
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        backButton = findViewById(R.id.backButton);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        activitiesRef = FirebaseDatabase.getInstance().getReference("activities").child(userId);
        cancellationsRef = FirebaseDatabase.getInstance().getReference("cancellations").child(userId);

        // Configurar el RecyclerView
        historyItemList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(this, historyItemList);
        historyRecyclerView.setAdapter(historyAdapter);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Cargar historial
        loadHistory();

        // Configurar el botón "Volver"
        backButton.setOnClickListener(v -> {
            // Regresa a HomeActivity
            Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadHistory() {
        // Limpiar la lista para evitar duplicados
        historyItemList.clear();

        // Recuperar actividades actuales
        activitiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot activitySnapshot : snapshot.getChildren()) {
                    EventActivity activity = activitySnapshot.getValue(EventActivity.class);
                    if (activity != null) {
                        HistoryItem item = new HistoryItem();
                        item.setActivityId(activity.getActivityId());
                        item.setName(activity.getName());
                        item.setDate(activity.getFecha());
                        item.setTime(activity.getHora());
                        item.setCanceled(false);
                        item.setCancellationReason(null);
                        item.setReprogrammedReasons(null);
                        historyItemList.add(item);
                    }
                }

                // Recuperar actividades canceladas
                cancellationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot cancellationSnapshot) {
                        for (DataSnapshot canceledActivitySnapshot : cancellationSnapshot.getChildren()) {
                            String activityId = canceledActivitySnapshot.child("activityId").getValue(String.class);
                            String name = canceledActivitySnapshot.child("name").getValue(String.class);
                            String date = canceledActivitySnapshot.child("fecha").getValue(String.class);
                            String time = canceledActivitySnapshot.child("hora").getValue(String.class);
                            String cancellationReason = canceledActivitySnapshot.child("cancellationReason").getValue(String.class);
                            String cancellationDate = canceledActivitySnapshot.child("cancellationDate").getValue(String.class);

                            HistoryItem item = new HistoryItem();
                            item.setActivityId(activityId);
                            item.setName(name);
                            item.setDate(date);
                            item.setTime(time);
                            item.setCanceled(true);
                            item.setCancellationReason(String.format("Cancelada el %s. Motivo: %s", cancellationDate, cancellationReason));
                            item.setReprogrammedReasons(null);

                            historyItemList.add(item);
                        }

                        // Notificar al adaptador que los datos han cambiado
                        historyAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Manejo de errores
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de errores
            }
        });
    }
}
