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
    /**
     * @brief Procesa un archivo SQL y extrae las coordenadas de los polígonos
     * @param sqlFilePath Ruta del archivo SQL
     * @pre El archivo debe contener sentencias INSERT INTO OBJETO
     */
    public static void processSqlFile(String sqlFilePath)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("INSERT INTO OBJETO"))
                {
                    convertAndSave(line);
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @brief Convierte una sentencia INSERT INTO OBJETO a un punto medio y lo guarda en un archivo .txt
     * @param insertStatement Sentencia INSERT INTO OBJETO
     */
    public static void convertAndSave(String insertStatement)
    {
        double[] coordinates = extractCoordinates(insertStatement);
        // Calcula el punto medio
        double centerX = (coordinates[0] + coordinates[4]) / 2;
        double centerY = (coordinates[1] + coordinates[3]) / 2;
        String centralPoint =  centerX + ", " + centerY ;
        writeToTxtFile(centralPoint);
    }

    /**
     * @brief Escribe un punto medio en un archivo .txt
     * @param centralPoint Punto medio a escribir
     */
    private static void writeToTxtFile(String centralPoint)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/puntoMedio/ptoMedioJ1.txt", true)))
        {
            writer.write(centralPoint);
            writer.newLine();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @brief Extrae las coordenadas de un polígono de una sentencia INSERT INTO OBJETO
     * @param inputFile Ruta del archivo SQL
     * @param outputFile Ruta del archivo de salida
     */
    public static void transformarArchivoSQL(String inputFile, String outputFile) throws IOException
    {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                String transformedLine = transformarSentenciaSQL(line);
                bw.write(transformedLine);
                bw.newLine();
            }
        }
    }

    /**
     * @brief Transforma una sentencia SQL de INSERT INTO OBJETO a una nueva sentencia con ZONA_PROVINCIA_CODIGO y PUNTO_MEDIO
     * @param sqlStatement Sentencia SQL de INSERT INTO OBJETO
     * @return Nueva sentencia SQL con ZONA_PROVINCIA_CODIGO y PUNTO_MEDIO
     */
    public static String transformarSentenciaSQL(String sqlStatement)
    {
        Pattern pattern = Pattern.compile("INSERT INTO OBJETO \\(TIPO_OBJETO, ZONA_UBICACION, ZONA_MUNICIPIO_CODIGO, POLIGONO_ENVOLVENTE\\) VALUES \\((.*?)\\);", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement);

        if (matcher.find())
        {
            String valuesPart = matcher.group(1);
            String tipoObjeto = valuesPart.split(",")[0].trim();
            String zonaUbicacion = valuesPart.split(",")[1].trim();
            String zonaMunicipioCodigo = valuesPart.split(",")[2].trim();
            String poligonoEnvolvente = valuesPart.substring(valuesPart.indexOf("SDO_GEOMETRY"));  // Captura todo después de "SDO_GEOMETRY"
            int endIndex = sqlStatement.indexOf(';', sqlStatement.indexOf("SDO_GEOMETRY"));
            if (endIndex != -1)
            {
                poligonoEnvolvente = sqlStatement.substring(sqlStatement.indexOf("SDO_GEOMETRY"), endIndex).trim();
                poligonoEnvolvente = poligonoEnvolvente.substring(0, poligonoEnvolvente.lastIndexOf(")"));
            }

            //TENEMOS QUE AÑADIR EL PUNTO MEDIO A LA SENTENCIA SQL
            double[] coordinates = extractCoordinates(sqlStatement);
            double centerX = (coordinates[0] + coordinates[4]) / 2;
            double centerY = (coordinates[1] + coordinates[3]) / 2;
            //Cambiamos las , por puntos
            String centerXString = String.format("%.5f", centerX).replace(',', '.');
            String centerYString = String.format("%.5f", centerY).replace(',', '.');
            // Transformar el valor de ZONA_MUNICIPIO_CODIGO a ZONA_PROVINCIA_CODIGO
            String zonaProvinciaCodigo = "23";
            String puntoMedio = String.format("SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(%s, %s, NULL), NULL, NULL)", centerXString, centerYString);
            String transformedValues = String.format("INSERT INTO OBJETO (TIPO_OBJETO, ZONA_UBICACION, ZONA_MUNICIPIO_CODIGO, ZONA_PROVINCIA_CODIGO, POLIGONO_ENVOLVENTE, PUNTO_MEDIO) VALUES ('%s', '%s', %s, %s, %s, %s);", tipoObjeto, zonaUbicacion, zonaMunicipioCodigo, zonaProvinciaCodigo, poligonoEnvolvente, puntoMedio);
            return transformedValues;
        }
        return sqlStatement;
    }

    /**
     * @brief Extrae las coordenadas de un polígono de una sentencia INSERT INTO OBJETO
     * @param insertStatement Sentencia INSERT INTO OBJETO
     * @return Coordenadas del polígono
     */
    private static double[] extractCoordinates(String insertStatement)
    {
        String[] tokens = insertStatement.split("[(),]");
        double[] coordinates = new double[8];
        int j=0;
        for (int i = 18; i < 26; i++)
        {
            coordinates[j] = Double.parseDouble(tokens[i + 1]);
            j++;
        }
        return coordinates;
    }
}
