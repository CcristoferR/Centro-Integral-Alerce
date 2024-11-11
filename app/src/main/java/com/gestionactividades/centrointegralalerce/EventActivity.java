package com.gestionactividades.centrointegralalerce;

public class EventActivity {
    private String name;
    private String fecha;
    private String lugar;  // Cambiado de `location` a `lugar`
    private String description;
    private String oferentes;  // Cambiado de `provider` a `oferentes`
    private String type;
    private String fileUrl;
    private String beneficiarios;

    // Constructor vacío necesario para Firebase
    public EventActivity() {}

    public EventActivity(String name, String fecha, String lugar, String description, String oferentes, String type, String fileUrl, String beneficiarios) {
        this.name = name;
        this.fecha = fecha;
        this.lugar = lugar;
        this.description = description;
        this.oferentes = oferentes;
        this.type = type;
        this.fileUrl = fileUrl;
        this.beneficiarios = beneficiarios;
    }

    // Getters
    public String getName() { return name; }
    public String getFecha() { return fecha; }
    public String getLugar() { return lugar; }  // Método actualizado
    public String getDescription() { return description; }
    public String getOferentes() { return oferentes; }  // Método actualizado
    public String getType() { return type; }
    public String getFileUrl() { return fileUrl; }
    public String getBeneficiarios() { return beneficiarios; }
}
