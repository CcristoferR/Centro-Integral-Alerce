package com.gestionactividades.centrointegralalerce;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {

    private List<EventActivity> activities;
    private Context context;
    private OnActivityClickListener listener; // Listener opcional para manejar clics

    // Constructor modificado que acepta un Listener adicional opcional
    public ActivitiesAdapter(Context context, List<EventActivity> activities, OnActivityClickListener listener) {
        this.context = context;
        this.activities = activities;
        this.listener = listener;
    }

    public ActivitiesAdapter(Context context, List<EventActivity> activities) {
        this(context, activities, null); // Constructor sin Listener
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        EventActivity activity = activities.get(position);
        holder.activityNameTextView.setText(activity.getName());
        holder.activityDateTextView.setText(activity.getFecha());
        holder.activityLocationTextView.setText(activity.getLugar());

        // Configurar clic para abrir detalles o llamar al listener si está presente
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActivityClick(activity); // Llamar al listener si está configurado
            } else {
                Intent intent = new Intent(context, ActivityDetailActivity.class);
                intent.putExtra("activityId", activity.getActivityId());
                intent.putExtra("name", activity.getName());
                intent.putExtra("fecha", activity.getFecha());
                intent.putExtra("lugar", activity.getLugar());
                intent.putExtra("oferentes", activity.getOferentes());
                intent.putExtra("beneficiarios", activity.getBeneficiarios());
                intent.putExtra("fileUrl", activity.getFileUrl());
                intent.putExtra("cupo", activity.getCupo());
                intent.putExtra("capacitacion", activity.getCapacitacion());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public void updateActivities(List<EventActivity> newActivities) {
        if (newActivities == null) return;
        this.activities.clear();
        this.activities.addAll(newActivities);
        notifyDataSetChanged();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView activityNameTextView;
        TextView activityDateTextView;
        TextView activityLocationTextView;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            activityNameTextView = itemView.findViewById(R.id.activityNameTextView);
            activityDateTextView = itemView.findViewById(R.id.activityDateTextView);
            activityLocationTextView = itemView.findViewById(R.id.activityLocationTextView);
        }
    }

    // Interfaz para manejar clics
    public interface OnActivityClickListener {
        void onActivityClick(EventActivity activity);
    }
}
