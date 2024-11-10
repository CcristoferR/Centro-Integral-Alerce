// ActivitiesAdapter.java
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

    public ActivitiesAdapter(Context context, List<EventActivity> activities) {
        this.context = context;
        this.activities = activities;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        EventActivity activity = activities.get(position);
        holder.activityNameTextView.setText(activity.getTitle());
        holder.activityDateTextView.setText(activity.getDate());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityDetailActivity.class);
            intent.putExtra("title", activity.getTitle());
            intent.putExtra("date", activity.getDate());
            intent.putExtra("location", activity.getLocation());
            intent.putExtra("description", activity.getDescription());
            intent.putExtra("provider", activity.getProvider());
            intent.putExtra("type", activity.getType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    // Método para actualizar la lista de actividades
    public void updateActivities(List<EventActivity> newActivities) {
        this.activities = newActivities;
        notifyDataSetChanged();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView activityNameTextView;
        TextView activityDateTextView;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            activityNameTextView = itemView.findViewById(R.id.activityNameTextView);
            activityDateTextView = itemView.findViewById(R.id.activityDateTextView);
        }
    }
}
