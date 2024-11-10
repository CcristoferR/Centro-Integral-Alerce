// ActivitiesAdapter.java
package com.gestionactividades.centrointegralalerce;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {

    private List<EventActivity> activities;

    public ActivitiesAdapter(List<EventActivity> activities) {
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
        holder.activityNameTextView.setText(activity.getName());
        holder.activityDateTextView.setText(activity.getDate());
        holder.activityLocationTextView.setText(activity.getLocation());
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public void updateActivities(List<EventActivity> newActivities) {
        this.activities = newActivities;
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
}
