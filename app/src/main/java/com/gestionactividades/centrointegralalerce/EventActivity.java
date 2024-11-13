package com.gestionactividades.centrointegralalerce;

import java.util.List;

public class EventActivity {
    private String activityId; // ID de la actividad
    private String name;
    private String fecha;
    private String lugar;
    private String description;
    private String oferentes;
    private String type;
    private String fileUrl;
    private String beneficiarios;
    private String cupo;
    private String capacitacion;
    private List<RescheduleInfo> reschedules; // Lista de reprogramaciones

    // Campos para cancelación
    private boolean isCancelled; // Indica si la actividad está cancelada
    private String cancellationReason; // Motivo de la cancelación
    private String cancellationDate; // Fecha de la cancelación

    // Constructor vacío necesario para Firebase
    public EventActivity() {}

    // Constructor completo que incluye todos los campos
    public EventActivity(String activityId, String name, String fecha, String lugar, String description, String oferentes,
                         String type, String fileUrl, String beneficiarios, String cupo, String capacitacion,
                         List<RescheduleInfo> reschedules, boolean isCancelled, String cancellationReason, String cancellationDate) {
        this.activityId = activityId; // Asignar el activityId
        this.name = name;
        this.fecha = fecha;
        this.lugar = lugar;
        this.description = description;
        this.oferentes = oferentes;
        this.type = type;
        this.fileUrl = fileUrl;
        this.beneficiarios = beneficiarios;
        this.cupo = cupo;
        this.capacitacion = capacitacion;
        this.reschedules = reschedules;
        this.isCancelled = isCancelled;
        this.cancellationReason = cancellationReason;
        this.cancellationDate = cancellationDate;
    }

    // Getter y Setter para activityId
    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    // Otros getters y setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOferentes() { return oferentes; }
    public void setOferentes(String oferentes) { this.oferentes = oferentes; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getBeneficiarios() { return beneficiarios; }
    public void setBeneficiarios(String beneficiarios) { this.beneficiarios = beneficiarios; }

    public String getCupo() { return cupo; }
    public void setCupo(String cupo) { this.cupo = cupo; }

    public String getCapacitacion() { return capacitacion; }
    public void setCapacitacion(String capacitacion) { this.capacitacion = capacitacion; }

    public List<RescheduleInfo> getReschedules() { return reschedules; }
    public void setReschedules(List<RescheduleInfo> reschedules) { this.reschedules = reschedules; }

    // Getters y Setters para cancelación
    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) { isCancelled = cancelled; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getCancellationDate() { return cancellationDate; }
    public void setCancellationDate(String cancellationDate) { this.cancellationDate = cancellationDate; }

    // Clase interna para la información de reprogramación
    public static class RescheduleInfo {
        private String dateTime;
        private String reason;

        // Constructor vacío necesario para Firebase
        public RescheduleInfo() {}

        // Constructor con parámetros
        public RescheduleInfo(String dateTime, String reason) {
            this.dateTime = dateTime;
            this.reason = reason;
        }

        // Getters y Setters
        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
