package org.processors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelCombinator
{
    /**
     * Combina archivos XLSX en un único archivo.
     *
     * @param folderPath  Ruta de la carpeta que contiene los archivos XLSX.
     * @param outputFile  Ruta del archivo de salida combinado.
     */
    public void combineExcelFiles(String folderPath, String outputFile)
    {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xlsx"));

        if (files == null || files.length == 0) {
            System.out.println("No se encontraron archivos XLSX en la carpeta especificada.");
            return;
        }

        Workbook combinedWorkbook = new XSSFWorkbook();
        Sheet combinedSheet = combinedWorkbook.createSheet("DatosCombinados");

        // Copiar encabezados de la primera hoja
        copyHeader(files[0], combinedSheet);

        // Copiar datos de todas las hojas
        for (File file : files)
        {
            copyData(file, combinedSheet);
        }

        // Guardar el archivo combinado
        try (FileOutputStream fileOut = new FileOutputStream(outputFile))
        {
            combinedWorkbook.write(fileOut);
        } catch (IOException e)
        {
            throw new RuntimeException("Error al guardar el archivo combinado.", e);
        } finally
        {
            try {
                combinedWorkbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @brief Copia los encabezados de la primera hoja del archivo especificado a la hoja combinada.
     *
     * @param file Archivo del cual se copian los encabezados.
     * @param combinedSheet Hoja en la que se copian los encabezados.
     */
    private void copyHeader(File file, Sheet combinedSheet)
    {
        try
        {
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0); // Suponiendo que la primera hoja contiene los datos

            Row headerRow = combinedSheet.createRow(0);
            for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(sheet.getRow(0).getCell(i).getStringCellValue());
            }
        } catch (IOException e)
        {
            throw new RuntimeException("Error al copiar encabezados.", e);
        }
    }

    /**
     * @brief Copia los datos de todas las hojas del archivo especificado a la hoja combinada.
     *
     * @param file Archivo del cual se copian los datos.
     * @param combinedSheet Hoja en la que se copian los datos.
     */
    private void copyData(File file, Sheet combinedSheet)
    {
        try {
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0);

            int lastRowIndex = combinedSheet.getLastRowNum();

            for (int i = 1; i <= sheet.getLastRowNum(); i++)
            {
                Row sourceRow = sheet.getRow(i);
                Row destinationRow = combinedSheet.createRow(lastRowIndex + i);

                for (int j = 0; j < sourceRow.getPhysicalNumberOfCells(); j++)
                {
                    Cell sourceCell = sourceRow.getCell(j);
                    Cell destinationCell = destinationRow.createCell(j);

                    if (sourceCell.getCellType() == CellType.NUMERIC)
                    {
                        destinationCell.setCellValue(sourceCell.getNumericCellValue());
                    } else if (sourceCell.getCellType() == CellType.STRING)
                    {
                        destinationCell.setCellValue(sourceCell.getStringCellValue());
                    } else if (sourceCell.getCellType() == CellType.FORMULA)
                    {
                        destinationCell.setCellFormula(sourceCell.getCellFormula());
                    }
                }
            }
        } catch (IOException e)
        {
            throw new RuntimeException("Error al copiar datos.", e);
        }
    }
}
