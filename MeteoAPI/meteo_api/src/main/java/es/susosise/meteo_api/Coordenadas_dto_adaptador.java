package es.susosise.meteo_api;

public class Coordenadas_dto_adaptador {
    private String latitud;
    private String longitud;

    public Coordenadas_dto_adaptador(String latitud, String longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    String getLatitud() {
        return latitud;
    }

    String getLongitud() {
        return longitud;
    }
}
