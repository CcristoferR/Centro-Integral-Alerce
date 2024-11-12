package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Inicializar vistas
        calendarView = findViewById(R.id.calendarView);
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView);
        createActivityButton = findViewById(R.id.createActivityButton);
        historyButton = findViewById(R.id.historyButton);
        settingsButton = findViewById(R.id.settingsButton);

        // Inicializar lista y adaptador para RecyclerView
        activitiesList = new ArrayList<>();
        activitiesAdapter = new ActivitiesAdapter(this, new ArrayList<>());
        activitiesRecyclerView.setAdapter(activitiesAdapter);
        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Referencia a Firebase para cargar actividades del usuario autenticado
        String userId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("activities").child(userId);

        // Configurar escucha para cambio de fecha en el calendario
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            List<EventActivity> activitiesForDate = getActivitiesForDate(date);
            activitiesAdapter.updateActivities(activitiesForDate);
            activitiesAdapter.notifyDataSetChanged();
        });

        // Configurar navegación
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        createActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateActivity.class);
            startActivity(intent);
        });

        // Cargar actividades de Firebase
        loadActivitiesFromFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActivitiesFromFirebase();
    }

    private void loadActivitiesFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activitiesList.clear();
                Set<CalendarDay> dates = new HashSet<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventActivity activity = snapshot.getValue(EventActivity.class);
                    if (activity != null) {
                        activitiesList.add(activity);

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
                                        dates.add(CalendarDay.from(year, month, day));
                                    } catch (NumberFormatException e) {
                                        Log.e("HomeActivity", "Error en el formato de la fecha: " + fullDate);
                                    }
                                }
                            }
                        }
                    }
                }

                calendarView.removeDecorators();
                calendarView.addDecorator(new EventDecorator(0xFFE57373, dates));
                Log.d("HomeActivity", "Fechas decoradas: " + dates);
                activitiesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Error al cargar las actividades", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<EventActivity> getActivitiesForDate(CalendarDay date) {
        List<EventActivity> activitiesOnDate = new ArrayList<>();
        String selectedDate = date.getDay() + "/" + (date.getMonth() + 1) + "/" + date.getYear();
        Log.d("HomeActivity", "Fecha seleccionada: " + selectedDate);

        for (EventActivity activity : activitiesList) {
            String fullDate = activity.getFecha();
            if (fullDate != null && fullDate.startsWith("Fecha:")) {
                String activityDate = fullDate.split(" ")[1]; // Extrae solo la fecha
                Log.d("HomeActivity", "Comparando fecha de actividad: " + activityDate);
                if (activityDate.equals(selectedDate)) {
                    activitiesOnDate.add(activity);
                }
            }
        }
        Log.d("HomeActivity", "Actividades en la fecha seleccionada: " + activitiesOnDate.size());
        return activitiesOnDate;
    }
}
