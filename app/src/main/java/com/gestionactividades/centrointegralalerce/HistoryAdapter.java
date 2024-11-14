package com.gestionactividades.centrointegralalerce;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<HistoryItem> historyItemList;

    public HistoryAdapter(Context context, List<HistoryItem> historyItemList) {
        this.context = context;
        this.historyItemList = historyItemList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyItemList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return historyItemList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView activityNameTextView;
        private TextView activityDateTimeTextView;
        private TextView activityStatusTextView;
        private TextView activityCancellationReasonTextView;
        private TextView activityReschedulesTextView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            activityNameTextView = itemView.findViewById(R.id.activityNameTextView);
            activityDateTimeTextView = itemView.findViewById(R.id.activityDateTimeTextView);
            activityStatusTextView = itemView.findViewById(R.id.activityStatusTextView);
            activityCancellationReasonTextView = itemView.findViewById(R.id.activityCancellationReasonTextView);
            activityReschedulesTextView = itemView.findViewById(R.id.activityReschedulesTextView);
        }

        public void bind(HistoryItem item) {
            activityNameTextView.setText(item.getName() != null ? item.getName() : "Sin nombre");
            activityDateTimeTextView.setText(String.format("Fecha: %s Hora: %s",
                    item.getDate() != null ? item.getDate() : "Sin fecha",
                    item.getTime() != null ? item.getTime() : "Sin hora"));

            if (item.isCanceled()) {
                activityStatusTextView.setText("Estado: Cancelada");
                activityCancellationReasonTextView.setVisibility(View.VISIBLE);
                activityCancellationReasonTextView.setText(item.getCancellationReason());

                activityReschedulesTextView.setVisibility(View.GONE);
            } else {
                activityStatusTextView.setText("Estado: Activa");
                activityCancellationReasonTextView.setVisibility(View.GONE);

                // Mostrar información de reprogramaciones si existen
                if (item.getReprogrammedReasons() != null && !item.getReprogrammedReasons().isEmpty()) {
                    StringBuilder reprogramInfo = new StringBuilder();
                    for (EventActivity.RescheduleInfo reschedule : item.getReprogrammedReasons()) {
                        reprogramInfo.append(String.format("- %s: %s\n", reschedule.getDateTime(), reschedule.getReason()));
                    }
                    activityReschedulesTextView.setVisibility(View.VISIBLE);
                    activityReschedulesTextView.setText(String.format("Reprogramaciones:\n%s", reprogramInfo.toString()));
                } else {
                    activityReschedulesTextView.setVisibility(View.GONE);
                }
            }
        }
    }
}
