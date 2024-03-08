/**
 * @brief Clase para generar los INSERT INTO de los archivos excel y JSON
 * @author Alba Gómez Liébana   agl00108
 * @date 27/02/2024
 */
package org.processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SQLConvertor
{
    static boolean encontrado=true; //Booleano para ver si se ha encontrado el dato de índices
    static int lastRowIndices=0; //Último archivo procesado de índices

    /**
     * @brief Función para obtener los datos de la columna de Excel
     * @pre El archivo Excel y los datos en JSON deben ir a la par, es decir, que los datos de la primera fila correspondan
     * con el primer JSON de temperaturas e índices y así consecutivamente.
     * @param rutaArchivoExcel ruta del archivo de temperaturas
     * @param sqlFilePath ruta del archivo donde queremos escribir los resultados
     */
    public void generarInsercionDesdeExcel(String rutaArchivoExcel, String sqlFilePath)
    {
        try (FileInputStream fileInputStream = new FileInputStream(rutaArchivoExcel);
             Workbook workbook = new XSSFWorkbook(fileInputStream))
        {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet)
            {
                // Suponemos que la primera fila contiene encabezados y empezamos desde la segunda
                if (row.getRowNum() > 0)
                {
                    //Obtenemos todos los datos necesarios del archivo Excel
                    int provinciaCodigo = (int) row.getCell(0).getNumericCellValue();
                    int municipioCodigo = (int) row.getCell(1).getNumericCellValue();
                    int poligono = (int) row.getCell(2).getNumericCellValue();
                    int parcela = (int) row.getCell(3).getNumericCellValue();
                    int recinto = (int) row.getCell(4).getNumericCellValue();
                    int anno = Integer.parseInt(row.getCell(5).getStringCellValue());
                    String mes = row.getCell(6).getStringCellValue();
                    String lluviaStr = row.getCell(7).getStringCellValue().replace(",", ".");
                    double lluvia = Double.parseDouble(lluviaStr);

                    //Llamamos a la función que generará el INSERT INTO
                    generarInsertInto(row.getRowNum(),provinciaCodigo, municipioCodigo, poligono, parcela, recinto, lluvia,  anno, mes , sqlFilePath);
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @brief Función para generar el INSERT INTO final e incluirlo en el archivo .sql
     * @note Si no se encuentra el JSON correspondiente de índices, pondría NULL en ese campo
     * @param fila fila por la que vamos en el documento
     * @param provinciaCodigo código de la provincia
     * @param municipioCodigo código del municipio
     * @param poligono número de polígono de la finca
     * @param parcela número de parcela de la finca
     * @param recinto número de recinto de la finca
     * @param lluvia lluvia registrada en la fecha concreta
     * @param anno año del que tenemos constancia
     * @param mes mes del que tenemos constancia
     * @param sqlFilePath archivo sql donde queremos incluir el INSERT INTO
     */
    public static void generarInsertInto(int fila, int provinciaCodigo, int municipioCodigo, int poligono,
                                         int parcela, int recinto, double lluvia, int anno, String mes, String sqlFilePath)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFilePath, true)))
        {
            LocalDate fecha = LocalDate.of(anno, obtenerNumeroMes(mes), 1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = fecha.format(formatter);

            // Construir el INSERT INTO
            String jsonReflectancia = obtenerContenidoJSON(fila,"indices", anno,mes, provinciaCodigo, municipioCodigo, poligono, parcela, recinto);
            String jsonTemperatura = obtenerContenidoJSON(fila, "temperatura",anno,mes, provinciaCodigo, municipioCodigo, poligono, parcela, recinto);
            String lluviaStr = String.valueOf(lluvia).replace(',', '.');

            String insertInto = String.format("INSERT INTO HISTORICO_FINCA (FECHA, PROVINCIA_CODIGO, MUNICIPIO_CODIGO, " +
                            "POLIGONO, PARCELA, RECINTO, LLUVIA, REFLECTANCIA, TEMPERATURA)\nVALUES " +
                            "(TO_DATE('%s', 'YYYY-MM-DD'), %d, %d, %d, %d, %d, %s,\n" +
                            "    %s, -- Reflectancia\n" +
                            "    %s -- Temperatura\n);\n",
                    formattedDate, provinciaCodigo, municipioCodigo, poligono, parcela, recinto, lluviaStr,
                    jsonReflectancia != null ? "utl_raw.cast_to_raw('" + jsonReflectancia + "')" : "utl_raw.cast_to_raw('{}')",
                    jsonTemperatura != null ? "utl_raw.cast_to_raw('" + jsonTemperatura + "')" : "utl_raw.cast_to_raw('{}')");

            // Escribir el INSERT INTO en el archivo .sql
            writer.write(insertInto);
            writer.newLine();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @brief Función para obtener el número del mes correspondiente al mes que tenemos
     * @param mes mes del que debemos obtener su número para insertarlo en la BD
     * @return el número correspondiente o -1 si no lo encuentra
     */
    private static int obtenerNumeroMes(String mes)
    {
        switch (mes.toLowerCase())
        {
            case "enero":
                return 1;
            case "febrero":
                return 2;
            case "marzo":
                return 3;
            case "abril":
                return 4;
            case "mayo":
                return 5;
            case "junio":
                return 6;
            case "julio":
                return 7;
            case "agosto":
                return 8;
            case "septiembre":
                return 9;
            case "octubre":
                return 10;
            case "noviembre":
                return 11;
            case "diciembre":
                return 12;
            default:
                throw new IllegalArgumentException("Mes no válido: " + mes);
        }
    }

    /**
     * @brief Función para obtener el contenido JSON de la fila que estamos procesando
     * @param fila fila que estamos procesando
     * @param tipoJSON si es un JSON de índices o temperatura
     * @param anno de la fila para comparar datos
     * @param mes de la fila para comparar datos
     * @param provinciaCodigo de la fila para comparar datos
     * @param municipioCodigo de la fila para comparar datos
     * @param poligono de la fila para comparar datos
     * @param parcela de la fila para comparar datos
     * @param recinto de la fila para comparar datos
     * @return un String con el contenido del JSON
     */
    private static String obtenerContenidoJSON(int fila, String tipoJSON, int anno, String mes, int provinciaCodigo, int municipioCodigo, int poligono,
                                               int parcela, int recinto )
    {
        String rutaCompleta;
        try
        {
            String rutaBase = "src/main/archivosJSON/";
            //Primero obtenemos el contenido del JSON
        /*
            NOTA: aquí es donde tenemos en cuenta el atributo encontrado. Este se pone a falso en el momento en el que
            no coincide el número de fila con el JSON a procesar debido a que no tenemos un archivo. Se procesaría,
            por tanto el índice almacenado en lastRowIndices, que posteriormente se incrementa para seguir el órden
        */
            String nombreArchivo = tipoJSON.equals("indices") ?
                    String.format("JSONIndices/indice_%s_%d.json", "20240305", encontrado ? (fila - 1) : lastRowIndices) :
                    String.format("JSONTemperaturas/temperatura_%s_%d.json", "20240305", fila - 1);

            rutaCompleta = Paths.get(rutaBase, nombreArchivo).toString();

            ObjectMapper objectMapper = new ObjectMapper();
            File archivo = new File(rutaCompleta);

            JsonNode jsonNode = objectMapper.readTree(archivo);

            JsonNode dataNode = jsonNode.path("data");
            JsonNode primerElemento = dataNode.isArray() && !dataNode.isEmpty() ? dataNode.get(0) : null;
            JsonNode NDMI = dataNode.isArray() && !dataNode.isEmpty() ? dataNode.get(3) : null;

            if (primerElemento != null)
            {
                String primerCampo = primerElemento.path("ID").asText();
                String[] campos = primerCampo.split(":");
                int provinciaCodigoJson = Integer.parseInt(campos[0]);
                int municipioCodigoJson = Integer.parseInt(campos[1]);
                int poligonoJson = Integer.parseInt(campos[2]);
                int parcelaJson = Integer.parseInt(campos[3]);
                int recintoJson = Integer.parseInt(campos[4]);
                JsonNode annioNode = dataNode.get(1).path("Año");
                JsonNode mesNode = dataNode.get(2).path("Mes");

                String campoNDMI = primerElemento.path("ID").asText();

                int annioJson = annioNode.isNull() ? 0 : annioNode.asInt();
                String mesJson = mesNode.isNull() ? "" : mesNode.asText();

                if (provinciaCodigoJson == provinciaCodigo && municipioCodigoJson == municipioCodigo && poligonoJson == poligono
                        && parcelaJson == parcela && recintoJson == recinto && annioJson == anno && mesJson.equalsIgnoreCase(mes))
                {
                    if(encontrado)
                        lastRowIndices=fila-1;
                    else
                    {
                        if(tipoJSON.equals("indices"))
                            lastRowIndices++;
                    }
   /*                 if(Objects.equals(campoNDMI, ""))
                        return null;*/
                    return objectMapper.writeValueAsString(jsonNode);
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        //Para el control la primera vez que no se encuentra el archivo JSON de índices
        if(encontrado)
        {
            encontrado = false;
            lastRowIndices++;
        }
        return null; // Retorna null para indicar un valor nulo
    }

}

