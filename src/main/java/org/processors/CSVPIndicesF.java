package org.processors;
/**
 * @brief Clase para pasar los datos descargados de Google Earth Engine sobre Índices de Vegetación a .xlsl
 * @author Alba Gómez Liébana   agl00108
 * @date 21/02/2024
 */

import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class CSVPIndicesF
{
    private Map<String, Map<String, String>> dataMap;
    private Set<String> headers;

    /**
     * @brief constructor
     */
    public CSVPIndicesF()
    {
        this.dataMap = new HashMap<>();
        this.headers = new LinkedHashSet<>();
    }

    /**
     * @brief función para procesar los ficheros csv
     * @param folderPath carpeta donde se encuentran los csv
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
     * @brief función para escribir los resultados en el excel
     * @param excelFile ruta del excel
     * @param year año que se necesita
     */
    public void writeExcelFile(String excelFile, String year)
    {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Tabla");

        // Añadir encabezados a la hoja Excel
        Row headerRow = sheet.createRow(0);

        Cell cellProvincia = headerRow.createCell(0);
        Cell cellMunicipio = headerRow.createCell(1);
        Cell cellPoligono = headerRow.createCell(2);
        Cell cellParcela = headerRow.createCell(3);
        Cell cellRecinto = headerRow.createCell(4);
        Cell cellYear = headerRow.createCell(5);
        Cell cellMonth = headerRow.createCell(6);

        cellProvincia.setCellValue("Provincia");
        cellMunicipio.setCellValue("Municipio");
        cellParcela.setCellValue("Parcela");
        cellPoligono.setCellValue("Poligono");
        cellRecinto.setCellValue("Recinto");
        cellYear.setCellValue("Año");
        cellMonth.setCellValue("Mes");

        int headerIndex = 7;
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
            String provincia = keyParts[0];
            String municipio = keyParts[1];
            String poligono = keyParts[2];
            String parcela = keyParts[3];
            String recinto = keyParts[4];
            String month = keyParts[6];

            Row row = sheet.createRow(rowNum++);
            Cell cellProvinciaData= row.createCell(0);
            Cell cellMunicipioData= row.createCell(1);
            Cell cellPoligonoData = row.createCell(2);
            Cell cellParcelaData = row.createCell(3);
            Cell cellRecintoData = row.createCell(4);
            Cell cellYearData = row.createCell(5);
            Cell cellMonthData = row.createCell(6);

            setCell(cellProvinciaData, provincia);
            setCell(cellMunicipioData, municipio);
            setCell(cellParcelaData, parcela);
            setCell(cellPoligonoData, poligono);
            setCell(cellRecintoData, recinto);
            cellYearData.setCellValue(year);
            cellMonthData.setCellValue(month);

            Map<String, String> meanValues = entry.getValue();
            headerIndex = 7;
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

        // Guardar Excel
        try (FileOutputStream fileOut = new FileOutputStream(excelFile))
        {
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @brief función para procesar un archivo CSV
     * @param file archivo
     */
    private void processCSV(File file)
    {
        try (CSVReader csvReader = new CSVReader(new FileReader(file)))
        {
            List<String[]> csvData = csvReader.readAll();

            int indiceProvincia = 11;
            int indiceMunicipio = 5;
            int indiceParcela = 9;
            int indicePoligono = 10;
            int indiceRecinto = 12;
            int indiceMean = 4;

            String fileName = file.getName().replace(".csv", "");

            // Obtener el nombre de la carpeta contenedora
            String folderName = file.getParentFile().getName();

            // Extraer año y mes del nombre de la carpeta
            String[] folderNameParts = folderName.split("_");
            String year = folderNameParts[2].substring(3, 7);
            String monthAbbreviation = folderNameParts[2].substring(0, 3);
            String month = getMonthName(monthAbbreviation);

            for (String[] rowData : csvData)
            {
                String provincia=rowData[indiceProvincia];
                String municipio = rowData[indiceMunicipio];
                String parcela = rowData[indiceParcela];
                String poligono = rowData[indicePoligono];
                String recinto = rowData[indiceRecinto];
                String mean = rowData[indiceMean];

                if(!Objects.equals(municipio, "municipio"))
                {
                    // Ajustar la construcción de la clave para incluir
                    String key = provincia+ ","+ municipio + "," + poligono + "," + parcela + "," + recinto + "," + year + "," + month;

                    dataMap.computeIfAbsent(key, k -> new HashMap<>()).put(fileName, mean);

                    // Añadir el encabezado a la lista solo si no está presente
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
     * @brief Función para obtener el mes a poner en la columna
     * @param month mes
     * @return string con el mes
*/
    private String getMonthName(String month)
    {
    // Mapeo de las tres letras del mes a su nombre completo
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
     * @brief función para establecer la celda
     * @param cell
     * @param value
     */
    private void setCell(Cell cell, String value)
    {
        try
        {
            double numericValue = Double.parseDouble(value);
            cell.setCellValue(numericValue);
        } catch (NumberFormatException e)
        {
            cell.setCellValue(value);
        }
    }
}