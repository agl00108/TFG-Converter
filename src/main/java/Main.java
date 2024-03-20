import org.processors.*;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException {

        //Primero creamos un excel combinator que nos servirá para combinar todos los excel genrados
        ExcelCombinator comb=new ExcelCombinator();

        //PROCESAMIENTO INDICES DE VEGETACION
        String years[]={"2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023"};

       /* for (String year : years)
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
        /*
     SQLConvertor prueba=new SQLConvertor();
        prueba.generarInsercionDesdeExcel("src/main/archivosExcel/resultadoPRECIPITACIONES.xlsx", "src/main/insertDatos.sql");
*/
        //Para transformar el archivo generado por GEU al válido en nuestra BD
        GEUConverter.transformarArchivoSQL("src/main/J2Input.sql", "src/main/J2Output.sql");
        //Para obtener el archivo del punto medio
        GEUConverter.processSqlFile("src/main/J2Input.sql");

    /*

    for (String year : years)
    {
        String folderPath = "src/main/J2_OLIVOS/INDICES/" + year;
        String excelFile = "src/main/J2_OLIVOS/resultadoIndices/resultado" + year + ".xlsx";
        String oliveSql="src/main/J2_OLIVOS/datos.sql";
        CSVPIndicesO pro = new CSVPIndicesO();
        pro.readOliveIds(oliveSql);
        pro.processCSVFiles(folderPath);
        pro.writeExcelFile(excelFile);
    }
    comb.combineExcelFiles("src/main/J2_OLIVOS/resultadoIndices/","src/main/J2_OLIVOS/resultadoIndices/resultadoINDICES.xlsx");

    CSVPIndicesO pro = new CSVPIndicesO();
    pro.generateSQLFromExcel("src/main/J2_OLIVOS/resultadoIndices/resultadoINDICES.xlsx","src/main/J2_OLIVOS/resultadosjson","src/main/J2_OLIVOS/insercion.sql","Sentinel-2","Satelite");
*/
    }
}