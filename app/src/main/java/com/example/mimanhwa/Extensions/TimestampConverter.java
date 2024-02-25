package com.example.mimanhwa.Extensions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampConverter {
    // Método para convertir timestamp de Firebase a cadena en formato dd/mm/yyyy
    public String convertTimestamp(long timestamp) {

        try {
            // Crear un objeto Date utilizando el timestamp
            Date date = new Date(timestamp);

            // Especificar el formato de fecha deseado
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            // Convertir la fecha a cadena en el formato especificado
            String fechaFormateada = dateFormat.format(date);

            return fechaFormateada;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
