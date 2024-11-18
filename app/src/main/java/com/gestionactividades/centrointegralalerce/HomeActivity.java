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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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
        createActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateActivity.class);
            startActivityForResult(intent, 1);
        });
        historyButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, HistoryActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
    }

    private void onActivitySelected(EventActivity activity) {
        Intent intent = new Intent(this, ActivityDetailActivity.class);
        intent.putExtra("activityId", activity.getActivityId());
        startActivityForResult(intent, 2);
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
        List<CalendarDay> dates = getRecurringDates(activity);
        decoratedDates.addAll(dates);
    }

    private void refreshCalendarAndActivities() {
        calendarView.removeDecorators();
        calendarView.addDecorator(new EventDecorator(0xFFE57373, decoratedDates));
        calendarView.invalidateDecorators(); // Forzar actualización del calendario

        // Actualizar las actividades mostradas para la fecha seleccionada
        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate != null) {
            List<EventActivity> activitiesForDate = getActivitiesForDate(selectedDate);
            activitiesAdapter.updateActivities(activitiesForDate);
        } else {
            activitiesAdapter.updateActivities(new ArrayList<>(activitiesList));
        }
    }

    private List<EventActivity> getActivitiesForDate(CalendarDay date) {
        List<EventActivity> activitiesOnDate = new ArrayList<>();
        String selectedDateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", date.getDay(), date.getMonth() + 1, date.getYear());

        for (EventActivity activity : activitiesList) {
            List<String> occurrenceDates = getOccurrenceDates(activity);
            if (occurrenceDates.contains(selectedDateStr)) {
                activitiesOnDate.add(activity);
            }
        }
        return activitiesOnDate;
    }

    private List<CalendarDay> getRecurringDates(EventActivity activity) {
        List<CalendarDay> dates = new ArrayList<>();

        if (activity.getFrequency() == null || activity.getFrequency().equals("Una vez")) {
            // Actividad no recurrente
            CalendarDay date = parseDate(activity.getFecha());
            if (date != null) {
                dates.add(date);
            }
            return dates;
        }

        // Parsear fechas
        Date startDate = parseDateString(activity.getFecha());
        Date endDate = parseDateString(activity.getEndDate());

        if (startDate == null || endDate == null) {
            return dates;
        }

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        // Generar fechas según la frecuencia
        while (!startCal.after(endCal)) {
            CalendarDay day = CalendarDay.from(startCal);
            dates.add(day);

            switch (activity.getFrequency()) {
                case "Diaria":
                    startCal.add(Calendar.DATE, 1);
                    break;
                case "Semanal":
                    startCal.add(Calendar.DATE, 7);
                    break;
                case "Mensual":
                    startCal.add(Calendar.MONTH, 1);
                    break;
                default:
                    // Si la frecuencia no es reconocida, salimos del bucle
                    startCal.add(Calendar.DATE, 1);
                    break;
            }
        }

        return dates;
    }

    private List<String> getOccurrenceDates(EventActivity activity) {
        List<String> dates = new ArrayList<>();

        if (activity.getFrequency() == null || activity.getFrequency().equals("Una vez")) {
            dates.add(activity.getFecha());
            return dates;
        }

        // Parsear fechas
        Date startDate = parseDateString(activity.getFecha());
        Date endDate = parseDateString(activity.getEndDate());

        if (startDate == null || endDate == null) {
            return dates;
        }

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        // Generar fechas según la frecuencia
        while (!startCal.after(endCal)) {
            String dateStr = dateFormat.format(startCal.getTime());
            dates.add(dateStr);

            switch (activity.getFrequency()) {
                case "Diaria":
                    startCal.add(Calendar.DATE, 1);
                    break;
                case "Semanal":
                    startCal.add(Calendar.DATE, 7);
                    break;
                case "Mensual":
                    startCal.add(Calendar.MONTH, 1);
                    break;
                default:
                    startCal.add(Calendar.DATE, 1);
                    break;
            }
        }

        return dates;
    }

    private CalendarDay parseDate(String dateStr) {
        if (dateStr != null && !dateStr.isEmpty()) {
            String[] dateParts = dateStr.split("/");
            if (dateParts.length == 3) {
                try {
                    int day = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]) - 1; // Mes en base 0
                    int year = Integer.parseInt(dateParts[2]);
                    return CalendarDay.from(year, month, day);
                } catch (NumberFormatException e) {
                    Log.e("HomeActivity", "Error al parsear la fecha: " + dateStr, e);
                }
            }
        }
        return null;
    }

    private Date parseDateString(String dateStr) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                return dateFormat.parse(dateStr);
            } catch (ParseException e) {
                Log.e("HomeActivity", "Error al parsear la fecha: " + dateStr, e);
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Si volvemos de CreateActivity o ActivityDetailActivity y la actividad se creó o modificó, actualizamos la UI
        if ((requestCode == 1 || requestCode == 2) && resultCode == RESULT_OK) {
            refreshCalendarAndActivities();
        }
    }
}
