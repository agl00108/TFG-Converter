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
    //FUNCIONES PARA OBTENER EL PUNTO MEDIO
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

    public static void convertAndSave(String insertStatement)
    {
        // Extrayendo las coordenadas del insert statement
        double[] coordinates = extractCoordinates(insertStatement);

        // Calculando el punto medio
        double centerX = (coordinates[0] + coordinates[4]) / 2;
        double centerY = (coordinates[1] + coordinates[3]) / 2;

        // Creando una cadena para el punto medio
        String centralPoint =  centerX + ", " + centerY ;

        // Insertando el punto medio en un archivo .txt
        writeToTxtFile(centralPoint);
    }

    private static void writeToTxtFile(String centralPoint)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/puntoMedio/ptoMedioJ2.txt", true)))
        {
            writer.write(centralPoint);
            writer.newLine();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
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

    public static String transformarSentenciaSQL(String sqlStatement)
    {
        // Patrón para encontrar la parte VALUES de la sentencia INSERT
        Pattern pattern = Pattern.compile("INSERT INTO OBJETO \\(TIPO_OBJETO, ZONA_UBICACION, ZONA_MUNICIPIO_CODIGO, POLIGONO_ENVOLVENTE\\) VALUES \\((.*?)\\);", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement);

        if (matcher.find())
        {
            String valuesPart = matcher.group(1);

            // Realizar la transformación según el formato deseado
            String tipoObjeto = valuesPart.split(",")[0].trim();
            String zonaUbicacion = valuesPart.split(",")[1].trim();
            String zonaMunicipioCodigo = valuesPart.split(",")[2].trim();
            String poligonoEnvolvente = valuesPart.substring(valuesPart.indexOf("SDO_GEOMETRY"));  // Captura todo después de "SDO_GEOMETRY"
            int endIndex = sqlStatement.indexOf(';', sqlStatement.indexOf("SDO_GEOMETRY"));
            if (endIndex != -1)
            {
                // Eliminar el último paréntesis de poligonoEnvolvente
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
            // Crear la nueva cadena con los valores transformados
            String transformedValues = String.format("INSERT INTO OBJETO (TIPO_OBJETO, ZONA_UBICACION, ZONA_MUNICIPIO_CODIGO, ZONA_PROVINCIA_CODIGO, POLIGONO_ENVOLVENTE, PUNTO_MEDIO) VALUES ('%s', '%s', %s, %s, %s, %s);", tipoObjeto, zonaUbicacion, zonaMunicipioCodigo, zonaProvinciaCodigo, poligonoEnvolvente, puntoMedio);

            // Reemplazar la parte original con la nueva cadena
            return transformedValues;
        }

        // Devolver la sentencia original si no se encontraron coincidencias
        return sqlStatement;
    }

    private static double[] extractCoordinates(String insertStatement)
    {
        // Aquí necesitarás implementar la lógica para extraer las coordenadas del insert statement
        // Puedes usar expresiones regulares, StringTokenizer, u otras técnicas dependiendo de la consistencia de tus datos
        // Por simplicidad, asumiré que siempre se proporcionan 8 coordenadas consecutivas en el formato dado.

        // Ejemplo básico para ilustrar
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
