package es.susosise.meteo_api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.configurationprocessor.json.JSONTokener;
import org.springframework.stereotype.Service;

@Service
public class ParteMetereologico_servicios_modelo {

    @Autowired
    ParteMetereologico_persistencia_adaptador persistencia;

    @Autowired
    MisPropiedades mispropiedades;

    public ParteMetereologico_dto_adaptador ObtenerDatosMetereologicos(String poblacion, String codigoPais) throws IOException, JSONException {
        Coordenadas_dto_adaptador coordenadas = getGeolocalizacion(poblacion, codigoPais);
        if (coordenadas != null) {
            ParteMetereologico_dto_adaptador metereologia = getMetereologia(coordenadas);
            return metereologia;
        }
        return null;
        //return getMetereologia(getGeolocalizacion(poblacion, codigoPais));
    }
    
    public ParteMetereologico_servicios_modelo(ParteMetereologico_persistencia_adaptador persistencia) {
        this.persistencia = persistencia;
    }
    
    public Long getCuantasHay() {
        return persistencia.count();
    }
    
    public List<ParteMetereologico_entidad_modelo> getTodas() {
        return persistencia.findAll();
    }
    
   
    public Object buscarPorIdentificador(Long idInterno) {
        Optional<ParteMetereologico_entidad_modelo> parteMeteorologico = persistencia.findById(idInterno);
        if (parteMeteorologico.isPresent()) {
            return parteMeteorologico.get();
        } else {
            return new ParteMetereologico_entidad_modelo();
        }
    }
    
    public void guardar(ParteMetereologico_entidad_modelo parteMeteorologico) {
            persistencia.save(parteMeteorologico);
    }
    
    public void eliminar(ParteMetereologico_entidad_modelo parteMeteorologico) {
        persistencia.delete(parteMeteorologico);
    }

    
    private Coordenadas_dto_adaptador getGeolocalizacion(String poblacion, String codigoPais) throws IOException, JSONException {
        //nota: Utiliza un servicio de OpenWeather
        // https://openweathermap.org/api/geocoding-api
        URL urlGeolocalizacion = new URL("http://api.openweathermap.org/geo/1.0/direct" 
        + "?q=" + poblacion + "," + codigoPais 
        + "&appid=" + mispropiedades.getWeatherAPIkey());
        String datosTexto = llamarALaApiYObtenerRespuesta(urlGeolocalizacion);
        JSONArray datos = (JSONArray) new JSONTokener(datosTexto).nextValue();
        JSONObject datosJson = datos.getJSONObject(0);
        String latitud = datosJson.getString("lat");
        String longitud = datosJson.getString("lon");
        return new Coordenadas_dto_adaptador(latitud, longitud);
    }

    private ParteMetereologico_dto_adaptador getMetereologia(Coordenadas_dto_adaptador coordenadas) throws IOException, JSONException{
        //nota: Utiliza un servicio de OpenWeather
        // https://openweathermap.org/current
        URL urlMetereologia = new URL("https://api.openweathermap.org/data/2.5/weather"
        + "?lat=" + coordenadas.getLatitud() 
        + "&lon=" + coordenadas.getLongitud ()
        + "&units=metric"
        + "&lang=es"
        + "&appid=" + mispropiedades.getWeatherAPIkey());
        String datosTexto = llamarALaApiYObtenerRespuesta(urlMetereologia);
        //JSONArray datos = (JSONArray) new JSONTokener(datosTexto).nextValue();
        JSONObject datosJson = (JSONObject) new JSONTokener(datosTexto).nextValue();
        Double temperaturaActual = datosJson.getJSONObject("main").getDouble("temp");
        Double humedadActual = datosJson.getJSONObject("main").getDouble("humidity");
        double vientoVelocidad = datosJson.getJSONObject("wind").getDouble("speed");
        Integer vientoDireccion = datosJson.getJSONObject("wind").getInt("deg");
        return new ParteMetereologico_dto_adaptador(temperaturaActual, humedadActual, vientoVelocidad, vientoDireccion);
    }
    
    private String llamarALaApiYObtenerRespuesta(URL url) throws IOException {
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");
            conexion.setConnectTimeout(3000);
            conexion.setReadTimeout(3000);
            int respuestaGCodigo = conexion.getResponseCode();
            if (respuestaGCodigo != HttpURLConnection.HTTP_OK) {
                BufferedReader receptor = new BufferedReader(new InputStreamReader(conexion.getErrorStream()));
                String unaLinea;
                StringBuffer respuestaError = new StringBuffer();
                while((unaLinea = receptor.readLine()) != null) {
                    respuestaError.append(unaLinea);
                }
                receptor.close();
                conexion.disconnect();
                return respuestaError.toString();
            }else {
                BufferedReader receptor = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                String unaLinea;
                StringBuffer respuestaOk = new StringBuffer();
                while((unaLinea = receptor.readLine()) != null) {
                    respuestaOk.append(unaLinea);
                }
                receptor.close();
                conexion.disconnect();
                return respuestaOk.toString();
            }
    }

 
}
