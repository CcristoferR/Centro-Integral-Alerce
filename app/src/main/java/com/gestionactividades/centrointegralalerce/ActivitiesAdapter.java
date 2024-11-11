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
            holder.activityNameTextView.setText(activity.getName());
            holder.activityDateTextView.setText(activity.getFecha());
            holder.activityLocationTextView.setText(activity.getLugar()); // Usar `getLugar` para obtener la ubicación

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ActivityDetailActivity.class);
                intent.putExtra("name", activity.getName());
                intent.putExtra("fecha", activity.getFecha());
                intent.putExtra("lugar", activity.getLugar());  // Usar `getLugar`
                intent.putExtra("description", activity.getDescription());
                intent.putExtra("oferentes", activity.getOferentes());  // Usar `getOferentes`
                intent.putExtra("type", activity.getType());
                intent.putExtra("beneficiarios", activity.getBeneficiarios());
                intent.putExtra("fileUrl", activity.getFileUrl());
                context.startActivity(intent);
            });
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
            TextView activityLocationTextView; // Nuevo TextView para la ubicación

            public ActivityViewHolder(@NonNull View itemView) {
                super(itemView);
                activityNameTextView = itemView.findViewById(R.id.activityNameTextView);
                activityDateTextView = itemView.findViewById(R.id.activityDateTextView);
                activityLocationTextView = itemView.findViewById(R.id.activityLocationTextView); // Conectar con el TextView del layout
            }
        }
    }
