package org.processors;
/**
 * @brief Clase para pasar los datos descargados de Dron e sobre Índices de Vegetación a .xlsl
 * @author Alba Gómez Liébana   agl00108
 * @date 06/05/2024
 */
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLConvertorDron
{
    private String carpeta; //Carpeta donde se encuentran los archivos
    private Map<String, String> oliveIds; //Mapa para almacenar los IDs de los olivos

    public Map<String, String> getOliveIds()
    {
        return oliveIds;
    }

    public SQLConvertorDron(String carpeta)
    {
        this.carpeta = carpeta;
        oliveIds = new HashMap<>();
    }

    /**
     * @brief Método para procesar los archivos de la carpeta y guardar los datos en un archivo final
     * @param rutaGuardar Ruta donde se guardará el archivo final
     */
    public void procesarArchivos(String rutaGuardar)
    {
        Map<String, List<Double>> datosPorPar = new HashMap<>();
        try {
            for (File archivo : new File(carpeta).listFiles())
            {
                if (archivo.isFile() && archivo.getName().endsWith(".xlsx"))
                {
                    FileInputStream fis = new FileInputStream(archivo);
                    Workbook libro = new XSSFWorkbook(fis);
                    fis.close();

                    Sheet hoja = libro.getSheetAt(0);

                    procesarHoja(hoja, archivo.getName(), datosPorPar);
                }
            }
            escribirArchivoFinal(datosPorPar, rutaGuardar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Método para procesar una hoja de un archivo XLSX
     * @param hoja Hoja a procesar
     * @param nombreArchivo Nombre del archivo
     * @param datosPorPar Mapa donde se guardan los datos
     */
    private void procesarHoja(Sheet hoja, String nombreArchivo, Map<String, List<Double>> datosPorPar)
    {
        for (int i = 1; i < hoja.getLastRowNum(); i++)
        {
            Row filaField1 = hoja.getRow(i);

            if (filaField1 != null)
            {
                Cell celdaField1 = filaField1.getCell(0);
                Cell celdaField2 = filaField1.getCell(1);
                Cell indice=filaField1.getCell(2);

                if(celdaField1 == null || celdaField2 == null || indice == null)
                {
                    break;
                }

                String key= String.format("%.3f", celdaField1.getNumericCellValue()) + "," + String.format("%.3f",celdaField2.getNumericCellValue());
                if(!oliveIds.containsKey(key))
                {
                    System.out.println("No se ha encontrado el olivo con coordenadas " + key);
                }else
                {
                    //Añadimos tanto la fecha como el ID del olivo para el posterior INSERT INTO
                    String id= oliveIds.get(key);
                    String fecha= nombreArchivo.substring(8, nombreArchivo.indexOf("."));
                    String par = celdaField1 + "_" + celdaField2 + "_" + fecha + "_" + id;
                    if(!datosPorPar.containsKey(par))
                    {
                        datosPorPar.put(par, new ArrayList<>());
                        datosPorPar.get(par).add(indice.getNumericCellValue());
                    }else
                    {
                        datosPorPar.get(par).add(indice.getNumericCellValue());
                    }
                }
            }
        }
    }

    /**
     * @brief Método para escribir los datos en un archivo final
     * @param datosPorPar Mapa con los datos
     * @param rutaGuardar Ruta donde se guardará el archivo final
     */
    private void escribirArchivoFinal(Map<String, List<Double>> datosPorPar, String rutaGuardar)
    {
        XSSFWorkbook resultadoFinal = new XSSFWorkbook();
        Sheet hojaFinal = resultadoFinal.createSheet();

        Row encabezados = hojaFinal.createRow(0);
        encabezados.createCell(0).setCellValue("Field_1");
        encabezados.createCell(1).setCellValue("Field_2");
        encabezados.createCell(2).setCellValue("FECHA");
        encabezados.createCell(3).setCellValue("ID");
        encabezados.createCell(4).setCellValue("NDVI");
        encabezados.createCell(5).setCellValue("NDWI");
        encabezados.createCell(6).setCellValue("SAVI");

        int filaDestinoIndex = 1;
        for (Map.Entry<String, List<Double>> entry : datosPorPar.entrySet())
        {
            Row filaDestino = hojaFinal.createRow(filaDestinoIndex++);
            String[] campos = entry.getKey().split("_");
            filaDestino.createCell(0).setCellValue(Double.parseDouble(campos[0]));
            filaDestino.createCell(1).setCellValue(Double.parseDouble(campos[1]));
            filaDestino.createCell(2).setCellValue(campos[2]);
            filaDestino.createCell(3).setCellValue(campos[3]);
            int celdaDestinoIndex = 4;
            List<Double> valores = entry.getValue();
            for (Double valor : valores)
            {
                filaDestino.createCell(celdaDestinoIndex++).setCellValue(valor);
            }
        }
        try
        {
            FileOutputStream fos = new FileOutputStream(rutaGuardar);
            resultadoFinal.write(fos);
            fos.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @brief Método para leer los IDs de los olivos desde un archivo SQL
     * @param sqlFile Ruta del archivo SQL
     */
    public void readOliveIds(String sqlFile)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(sqlFile)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                if (line.startsWith("Insert into ALBAGOMEZ.OBJETO")) {
                //if (line.startsWith("Insert into OBJETO")) {
                    String id = line.split("'")[1];
                    String[] parts = line.split("SDO_POINT_TYPE\\(")[1].split(",");
                    String x = parts[0].trim();
                    String y = parts[1].split(",")[0].trim();

                    //Lo guardamos con 3 decimales
                    double xCoord = Double.parseDouble(x);
                    double yCoord = Double.parseDouble(y);
                    x = String.format("%.3f", xCoord);
                    y = String.format("%.3f", yCoord);
                    String key = x + "," + y;
                    oliveIds.put(key, id);
                }
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @brief Método para generar un archivo SQL a partir de un archivo Excel
     * @param excelFilePath Ruta del archivo Excel
     * @param jsonFolderPath Ruta de la carpeta con los archivos JSON
     * @param sqlFilePath Ruta del archivo SQL de salida
     * @param nombreFuente Nombre de la fuente
     * @param tipoFuente   Tipo de la fuente
     */
    public void generarSQLParaDron(String excelFilePath, String jsonFolderPath, String sqlFilePath, String nombreFuente, String tipoFuente)
    {
        int indice = 1;
        try (FileInputStream fileInputStream = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream);
             BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFilePath)))
        {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet)
            {
                if (row.getRowNum() > 0)
                {
                    String fecha = row.getCell(2).getStringCellValue();
                    String id= row.getCell(3).getStringCellValue();

                    String jsonFileName = "/indice_20240507_" + indice + ".json";
                    indice++;
                    String jsonFilePath = jsonFolderPath + jsonFileName;
                    String jsonContent = readJSON(jsonFilePath);
                    String sqlStatement = "INSERT INTO HISTORICO_DATOS(FECHA,VOLUMEN,REFLECTANCIA,ID_OBJETO,NOMBRE_FUENTE,TIPO_FUENTE) VALUES " +
                            "(TO_DATE('" + fecha + "', 'DD-MM-YYYY'), NULL, utl_raw.cast_to_raw('" + jsonContent + "')," + id + ",'" + nombreFuente + "','" + tipoFuente + "');";

                    writer.write(sqlStatement);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lee el contenido de un archivo JSON y lo retorna como una cadena de texto.
     *
     * @param jsonFilePath Ruta del archivo JSON.
     * @return Contenido del archivo JSON.
     * @throws IOException Excepción de E/S si hay problemas al leer el archivo.
     */
    private String readJSON(String jsonFilePath) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(jsonFilePath)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
