import org.processors.*;

public class Main
{
    public static void main(String[] args)
    {
        /*
        //Primero creamos un excel combinator que nos servirá para combinar todos los excel genrados
        ExcelCombinator comb=new ExcelCombinator();

        //PROCESAMIENTO INDICES DE VEGETACION
        String years[]={"2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023"};
        for (String year : years)
        {
            String folderPath = "src/main/DATOS_INDICES/" + year;
            String excelFile = "src/main/archivosExcel/resultadoIndices/resultado" + year + ".xlsx";
            CSVPIndices csv = new CSVPIndices();
            csv.processCSVFiles(folderPath);
            csv.writeExcelFile(excelFile, year);
        }
        comb.combineExcelFiles("src/main/archivosExcel/resultadoIndices/","src/main/archivosExcel/resultadoINDICES.xlsx");

        //PROCESAMIENTO INDICES DE TEMPERATURA
        for (String year : years)
        {
            String folderPath = "src/main/DATOS_TEMPERATURA/" + year;
            String excelFile = "src/main/archivosExcel/resultadoTemperatura/resultado" + year + ".xlsx";
            CSVPTemperatura ind = new CSVPTemperatura();
            ind.processCSVFiles(folderPath);
            ind.writeExcelFile(excelFile, year);
        }
        comb.combineExcelFiles("src/main/archivosExcel/resultadoTemperatura/","src/main/archivosExcel/resultadoTEMPERATURA.xlsx");

        //PROCESAMIENTO ACUMULADO DE LLUVIA
        for (String year : years)
        {
            String folderPath = "src/main/DATOS_PRECIPITACIONES/" + year;
            String excelFile = "src/main/archivosExcel/resultadoPrecipitaciones/resultado" + year + ".xlsx";
            CSVPrecip ind = new CSVPrecip();
            ind.processCSVFiles(folderPath);
            ind.writeExcelFile(excelFile, year);
        }
        comb.combineExcelFiles("src/main/archivosExcel/resultadoPrecipitaciones/","src/main/archivosExcel/resultadoPRECIPITACIONES.xlsx");
        */
        SQLConvertor prueba=new SQLConvertor();
        prueba.generarInsercionDesdeExcel("src/main/archivosExcel/resultadoPRECIPITACIONES.xlsx", "src/main/insertDatos.sql");
    }
}