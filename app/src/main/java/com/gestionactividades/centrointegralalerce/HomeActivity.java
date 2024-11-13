package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private RecyclerView activitiesRecyclerView;
    private Button createActivityButton, historyButton, settingsButton;
    private ActivitiesAdapter activitiesAdapter;
    private List<EventActivity> activitiesList;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private Set<CalendarDay> decoratedDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar FirebaseAuth y vistas
        auth = FirebaseAuth.getInstance();
        calendarView = findViewById(R.id.calendarView);
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView);
        createActivityButton = findViewById(R.id.createActivityButton);
        historyButton = findViewById(R.id.historyButton);
        settingsButton = findViewById(R.id.settingsButton);

        // Configurar RecyclerView
        activitiesList = new ArrayList<>();
        activitiesAdapter = new ActivitiesAdapter(this, new ArrayList<>(), this::onActivitySelected);
        activitiesRecyclerView.setAdapter(activitiesAdapter);
        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar base de datos de Firebase
        String userId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("activities").child(userId);
        decoratedDates = new HashSet<>();

        // Escuchar cambios en la base de datos en tiempo real
        setupRealtimeUpdates();

        // Configurar escucha para cambio de fecha en el calendario
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            List<EventActivity> activitiesForDate = getActivitiesForDate(date);
            activitiesAdapter.updateActivities(activitiesForDate);
        });

        // Configurar botones de navegación
        createActivityButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CreateActivity.class)));
        historyButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, HistoryActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
    }

    private void onActivitySelected(EventActivity activity) {
        Intent intent = new Intent(this, ActivityDetailActivity.class);
        intent.putExtra("activityId", activity.getActivityId());
        startActivityForResult(intent, 2); // Cambiamos a 'startActivityForResult' para recibir un resultado
    }

    private void setupRealtimeUpdates() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activitiesList.clear();
                decoratedDates.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventActivity activity = snapshot.getValue(EventActivity.class);
                    if (activity != null) {
                        activity.setActivityId(snapshot.getKey());
                        activitiesList.add(activity);
                        updateDecoratedDates(activity);
                    }
                }

                // Actualizamos tanto el calendario como la lista de actividades
                refreshCalendarAndActivities();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Error al sincronizar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDecoratedDates(EventActivity activity) {
        // Agregar la fecha de la actividad actualizada al conjunto de decoraciones del calendario
        String fullDate = activity.getFecha();
        if (fullDate != null && fullDate.startsWith("Fecha:")) {
            String[] dateTimeParts = fullDate.split(" ");
            if (dateTimeParts.length >= 2) {
                String[] dateParts = dateTimeParts[1].split("/");
                if (dateParts.length == 3) {
                    try {
                        int day = Integer.parseInt(dateParts[0]);
                        int month = Integer.parseInt(dateParts[1]) - 1; // Mes en base 0
                        int year = Integer.parseInt(dateParts[2]);
                        decoratedDates.add(CalendarDay.from(year, month, day));
                    } catch (NumberFormatException e) {
                        Log.e("HomeActivity", "Error en el formato de la fecha: " + fullDate);
                    }
                }
            }
        }
    }

    private void refreshCalendarAndActivities() {
        calendarView.removeDecorators();
        calendarView.addDecorator(new EventDecorator(0xFFE57373, decoratedDates));
        activitiesAdapter.updateActivities(new ArrayList<>(activitiesList));
    }

    private List<EventActivity> getActivitiesForDate(CalendarDay date) {
        List<EventActivity> activitiesOnDate = new ArrayList<>();
        String selectedDate = date.getDay() + "/" + (date.getMonth() + 1) + "/" + date.getYear();
        for (EventActivity activity : activitiesList) {
            String fullDate = activity.getFecha();
            if (fullDate != null && fullDate.startsWith("Fecha:")) {
                String activityDate = fullDate.split(" ")[1];
                if (activityDate.equals(selectedDate)) {
                    activitiesOnDate.add(activity);
                }
            }
        }
        return activitiesOnDate;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            refreshCalendarAndActivities(); // Refrescar vista al volver desde el detalle
        }
    }
}
