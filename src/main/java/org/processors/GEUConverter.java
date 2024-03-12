package org.processors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GEUConverter
{

    public static void transformarArchivoSQL(String inputFile, String outputFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = br.readLine()) != null) {
                String transformedLine = transformarSentenciaSQL(line);
                bw.write(transformedLine);
                bw.newLine();
            }
        }
    }

    public static String transformarSentenciaSQL(String sqlStatement) {
        // Patrón para encontrar la parte VALUES de la sentencia INSERT
        Pattern pattern = Pattern.compile("INSERT INTO OBJETO \\(TIPO_OBJETO, ZONA_UBICACION, ZONA_MUNICIPIO_CODIGO, POLIGONO_ENVOLVENTE\\) VALUES \\((.*?)\\);", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement);

        if (matcher.find()) {
            String valuesPart = matcher.group(1);

            // Realizar la transformación según el formato deseado
            String tipoObjeto = valuesPart.split(",")[0].trim();
            String zonaUbicacion = valuesPart.split(",")[1].trim();
            String zonaMunicipioCodigo = valuesPart.split(",")[2].trim();
            String poligonoEnvolvente = valuesPart.substring(valuesPart.indexOf("SDO_GEOMETRY"));  // Captura todo después de "SDO_GEOMETRY"
            int endIndex = sqlStatement.indexOf(';', sqlStatement.indexOf("SDO_GEOMETRY"));
            if (endIndex != -1) {
                poligonoEnvolvente = sqlStatement.substring(sqlStatement.indexOf("SDO_GEOMETRY"), endIndex + 1).trim();
            }

            // Transformar el valor de ZONA_MUNICIPIO_CODIGO a ZONA_PROVINCIA_CODIGO
            String zonaProvinciaCodigo = "23";

            // Crear la nueva cadena con los valores transformados
            String transformedValues = String.format("INSERT INTO OBJETO (TIPO_OBJETO, ZONA_UBICACION, ZONA_MUNICIPIO_CODIGO, ZONA_PROVINCIA_CODIGO, POLIGONO_ENVOLVENTE) VALUES ('%s', '%s', %s, %s, %s", tipoObjeto, zonaUbicacion, zonaMunicipioCodigo, zonaProvinciaCodigo, poligonoEnvolvente);

            // Reemplazar la parte original con la nueva cadena
            return transformedValues;
        }

        // Devolver la sentencia original si no se encontraron coincidencias
        return sqlStatement;
    }

}
