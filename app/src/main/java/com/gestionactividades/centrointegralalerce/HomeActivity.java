// HomeActivity.java
package com.gestionactividades.centrointegralalerce;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private RecyclerView activitiesRecyclerView;
    private Button createActivityButton;
    private ActivitiesAdapter activitiesAdapter;
    private List<EventActivity> activitiesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        calendarView = findViewById(R.id.calendarView);
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView);
        createActivityButton = findViewById(R.id.createActivityButton);

        // Inicializar datos de prueba
        activitiesList = getSampleActivities();

        // Configura el RecyclerView
        activitiesAdapter = new ActivitiesAdapter(new ArrayList<>());
        activitiesRecyclerView.setAdapter(activitiesAdapter);
        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Selección de fechas en el calendario
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            List<EventActivity> activities = getActivitiesForDate(date);
            activitiesAdapter.updateActivities(activities);
        });

        // Botón para crear una nueva actividad
        createActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateActivity.class);
            startActivity(intent);
        });

        // Marcar fechas con eventos en el calendario
        markEventsOnCalendar();
    }

    // Método para obtener actividades en la fecha seleccionada
    private List<EventActivity> getActivitiesForDate(CalendarDay date) {
        List<EventActivity> activitiesOnDate = new ArrayList<>();
        String selectedDate = date.getYear() + "-" + (date.getMonth() + 1) + "-" + date.getDay();
        for (EventActivity activity : activitiesList) {
            if (activity.getDate().equals(selectedDate)) {
                activitiesOnDate.add(activity);
            }
        }
        return activitiesOnDate;
    }

    // Método para agregar datos de prueba
    private List<EventActivity> getSampleActivities() {
        List<EventActivity> sampleActivities = new ArrayList<>();
        sampleActivities.add(new EventActivity("Reunión", "2024-11-10", "Sala A"));
        sampleActivities.add(new EventActivity("Taller de Capacitación", "2024-11-12", "Sala B"));
        sampleActivities.add(new EventActivity("Charla Informativa", "2024-11-15", "Auditorio"));
        return sampleActivities;
    }

    // Método para marcar fechas con eventos en el calendario
    private void markEventsOnCalendar() {
        for (EventActivity activity : activitiesList) {
            String[] dateParts = activity.getDate().split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Mes comienza en 0
            int day = Integer.parseInt(dateParts[2]);
            calendarView.setDateSelected(CalendarDay.from(year, month, day), true);
        }
    }
}
