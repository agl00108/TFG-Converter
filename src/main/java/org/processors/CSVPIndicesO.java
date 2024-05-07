package org.processors;
/**
 * @brief Clase para pasar los datos descargados de Google Earth Engine sobre Índices de Vegetación a .xlsl
 * @author Alba Gómez Liébana   agl00108
 * @date 21/02/2024
 */

import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class CSVPIndicesO
{
    private Map<String, Map<String, String>> dataMap;
    private Set<String> headers;
    private Map<String, String> oliveIds; //Mapa para almacenar los IDs de los olivos
    private int indice;

    /**
     * @brief constructor
     */
    public CSVPIndicesO() {
        this.dataMap = new HashMap<>();
        this.headers = new LinkedHashSet<>();
        this.oliveIds = new HashMap<>();
        this.indice = 0;
    }

    /**
     * @param folderPath carpeta donde se encuentran los csv
     * @brief función para procesar los ficheros csv
     */
    public void processCSVFiles(String folderPath)
    {
        File folder1 = new File(folderPath);
        File[] listOfFolders = folder1.listFiles();
        if (listOfFolders != null)
        {
            for (File folder : listOfFolders)
            {
                File[] listOfFiles = folder.listFiles();
                assert listOfFiles != null;
                for (File file : listOfFiles)
                {
                    if (file.isFile() && file.getName().endsWith(".csv"))
                    {
                        processCSV(file);
                    }
                }
            }
        }
    }

    /**
     * @param excelFile ruta del excel
     * @brief función para escribir los resultados en el excel
     */
    public void writeExcelFile(String excelFile)
    {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("INDICES");

        // Añadir encabezados a la hoja Excel
        Row headerRow = sheet.createRow(0);

        Cell cellOliveId = headerRow.createCell(0);
        Cell cellX = headerRow.createCell(1);
        Cell cellY = headerRow.createCell(2);
        Cell cellYear = headerRow.createCell(3);
        Cell cellMonth = headerRow.createCell(4);

        cellOliveId.setCellValue("ID");
        cellX.setCellValue("CentroX");
        cellY.setCellValue("CentroY");
        cellYear.setCellValue("Año");
        cellMonth.setCellValue("Mes");

        int headerIndex = 5;
        for (String header : headers)
        {
            Cell headerCell = headerRow.createCell(headerIndex++);
            headerCell.setCellValue(header);
        }

        // Añadir datos a la hoja Excel
        int rowNum = 1;
        for (Map.Entry<String, Map<String, String>> entry : dataMap.entrySet())
        {
            String[] keyParts = entry.getKey().split(",");
            String centroX = keyParts[0];
            String centroY = keyParts[1];
            String year = keyParts[2];
            String month = keyParts[3];

            Row row = sheet.createRow(rowNum++);
            Cell cellOliveIdData = row.createCell(0);
            Cell cellCentroX = row.createCell(1);
            Cell cellCentroY = row.createCell(2);
            Cell cellYearData = row.createCell(3);
            Cell cellMonthData = row.createCell(4);

            // Establecer los valores en las celdas correspondientes
            // Obtener el ID del olivo de los datos del mapa
            String oliveId = entry.getValue().get("ID_OBJETO");
            if (oliveId != null) {
                cellOliveIdData.setCellValue(oliveId);
                setCell(cellCentroX, centroX);
                setCell(cellCentroY, centroY);
                cellYearData.setCellValue(year);
                cellMonthData.setCellValue(month);

                Map<String, String> meanValues = entry.getValue();
                headerIndex = 5;
                for (String header : headers)
                {
                    Cell cellMean = row.createCell(headerIndex++);
                    String meanValue = meanValues.get(header);
                    if (meanValue != null)
                    {
                        setCell(cellMean, meanValue.replace(".", ","));
                    }
                }
            }
        }
        try (FileOutputStream fileOut = new FileOutputStream(excelFile))
        {
            workbook.write(fileOut);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @brief función para procesar un archivo CSV
     * @param file archivo
     */
    /**
     * @param file archivo
     * @brief función para procesar un archivo CSV
     */
    private void processCSV(File file)
    {
        try (CSVReader csvReader = new CSVReader(new FileReader(file)))
        {
            List<String[]> csvData = csvReader.readAll();

            int indiceCentroX = 1;
            int indiceCentroY = 2;
            int indiceMean = 3;

            String fileName = file.getName().replace(".csv", "");

            // Obtener el nombre de la carpeta contenedora
            String folderName = file.getParentFile().getName();

            // Extraer año y mes del nombre de la carpeta
            String[] folderNameParts = folderName.split("_");
            String year = folderNameParts[2].substring(3, 7);
            String monthAbbreviation = folderNameParts[2].substring(0, 3).toLowerCase();
            String month = getMonthName(monthAbbreviation);

            for (String[] rowData : csvData)
            {
                String centroX = rowData[indiceCentroX];
                String centroY = rowData[indiceCentroY];
                String mean = rowData[indiceMean];

                if (!Objects.equals(centroX, "field_1")) //Para no repetir el encabezado
                {

                    String key = centroX + "," + centroY;
                    // Verificar si hay un ID de olivo correspondiente para el punto medio x e y actual
                    if (oliveIds.containsKey(key))
                    {
                        String oliveId = oliveIds.get(key);
                        dataMap.computeIfAbsent(key + "," + year + "," + month, k -> new HashMap<>()).put(fileName, mean);
                        dataMap.get(key + "," + year + "," + month).put("ID_OBJETO", oliveId);
                    }

                    headers.add(fileName);
                }
            }
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param month mes
     * @return string con el mes
     * @brief Función para obtener el mes a poner en la columna
     */
    private String getMonthName(String month)
    {
        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("ene", "Enero");
        monthMap.put("feb", "Febrero");
        monthMap.put("mar", "Marzo");
        monthMap.put("abr", "Abril");
        monthMap.put("may", "Mayo");
        monthMap.put("jun", "Junio");
        monthMap.put("jul", "Julio");
        monthMap.put("ago", "Agosto");
        monthMap.put("sep", "Septiembre");
        monthMap.put("oct", "Octubre");
        monthMap.put("nov", "Noviembre");
        monthMap.put("dic", "Diciembre");

        return monthMap.getOrDefault(month, "Desconocido");
    }

    /**
     * @param cell
     * @param value
     * @brief función para establecer la celda
     */
    private void setCell(Cell cell, String value)
    {
        try {
            double numericValue = Double.parseDouble(value);
            cell.setCellValue(numericValue);
        } catch (NumberFormatException e) {
            cell.setCellValue(value);
        }
    }

    /**
     * @param sqlFile Ruta del archivo SQL
     * @brief Método para leer los IDs de los olivos desde un archivo SQL
     */
    public void readOliveIds(String sqlFile)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(sqlFile)))
        {
            String line;
            while ((line = br.readLine()) != null) {
                //if (line.startsWith("Insert into ALBAGOMEZ.OBJETO"))
                if (line.startsWith("Insert into OBJETO")) {
                    String id = line.split("'")[1];
                    String[] parts = line.split("SDO_POINT_TYPE\\(")[1].split(",");
                    String x = parts[0].trim();
                    String y = parts[1].split(",")[0].trim();
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
     * Genera un archivo SQL con las sentencias INSERT INTO para cada fila del Excel.
     *
     * @param excelFilePath  Ruta del archivo Excel.
     * @param jsonFolderPath Ruta de la carpeta que contiene los archivos JSON.
     * @param sqlFilePath    Ruta donde se guardará el archivo SQL generado.
     * @param nombreFuente   Nombre de la fuente.
     * @param tipoFuente     Tipo de fuente.
     * @throws IOException Excepción de E/S si hay problemas al leer/escribir archivos.
     */
    public void generateSQLFromExcel(String excelFilePath, String jsonFolderPath, String sqlFilePath, String nombreFuente, String tipoFuente)
    {
        try (FileInputStream fileInputStream = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream);
             BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFilePath)))
        {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet)
            {
                if (row.getRowNum() > 0)
                {
                    // Obtenemos todos los datos necesarios del archivo Excel
                    String ID = row.getCell(0).getStringCellValue();
                    String year = row.getCell(3).getStringCellValue();
                    String month = row.getCell(4).getStringCellValue();
                    String fecha = year + "-" + obtenerNumeroMes(month) + "-01"; // Suponiendo que month sea el nombre del mes
                    String jsonFileName = "/indice_20240506_" + indice + ".json";
                    indice++;
                    String jsonFilePath = jsonFolderPath + jsonFileName;
                    String jsonContent = readJSON(jsonFilePath);
                    String sqlStatement = "INSERT INTO HISTORICO_DATOS(FECHA,VOLUMEN,REFLECTANCIA,ID_OBJETO,NOMBRE_FUENTE,TIPO_FUENTE) VALUES " +
                            "(TO_DATE('" + fecha + "', 'YYYY-MM-DD'), NULL, utl_raw.cast_to_raw('" + jsonContent + "')," + ID + ",'" + nombreFuente + "','" + tipoFuente + "');";

                    writer.write(sqlStatement);
                    writer.newLine();
                }
            }
        } catch (IOException e)
        {
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

    /**
     * Obtiene el número correspondiente a un mes a partir de su nombre.
     * @param mes
     * @return Número correspondiente al mes.
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
}
