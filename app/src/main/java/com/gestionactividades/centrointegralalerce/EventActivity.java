package com.gestionactividades.centrointegralalerce;

public class EventActivity {
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

    // Constructor vacío necesario para Firebase
    public EventActivity() {}

    public EventActivity(String name, String fecha, String lugar, String description, String oferentes,
                         String type, String fileUrl, String beneficiarios, String cupo, String capacitacion) {
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
    }

    // Getters
    public String getName() { return name; }
    public String getFecha() { return fecha; }
    public String getLugar() { return lugar; }
    public String getDescription() { return description; }
    public String getOferentes() { return oferentes; }
    public String getType() { return type; }
    public String getFileUrl() { return fileUrl; }
    public String getBeneficiarios() { return beneficiarios; }
    public String getCupo() { return cupo; }
    public String getCapacitacion() { return capacitacion; }
}
