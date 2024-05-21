import org.processors.*;

import java.io.IOException;
import java.util.Map;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        //GENERAR DATOS PARA LAS FINCAS

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


        SQLConvertorF prueba=new SQLConvertorF();
        prueba.generarInsercionDesdeExcel("src/main/archivosExcel/resultadoPRECIPITACIONES.xlsx", "src/main/insertDatos.sql");

        //PARA GENERAR LOS DATOS DE LOS OLIVOS

       //Para transformar el archivo generado por GEU al válido en nuestra BD
        GEUConverter.transformarArchivoSQL("src/main/J1Input.sql", "src/main/J1Output.sql");
        //Para obtener el archivo del punto medio
        GEUConverter.processSqlFile("src/main/J1Input.sql");


    for (String year : years)
    {
        String folderPath = "src/main/J1_OLIVOS/INDICES/" + year;
        String excelFile = "src/main/J1_OLIVOS/resultadoIndices/resultado" + year + ".xlsx";
        String oliveSql="src/main/J1_OLIVOS/datosJ1.sql";
        CSVPIndicesO pro = new CSVPIndicesO();
        pro.readOliveIds(oliveSql);
        pro.processCSVFiles(folderPath);
        pro.writeExcelFile(excelFile);
    }
    comb.combineExcelFiles("src/main/J1_OLIVOS/resultadoIndices/","src/main/J1_OLIVOS/resultadoIndices/resultadoINDICESJ1.xlsx");
*/

    CSVPIndicesO pro = new CSVPIndicesO();
    //pro.generateSQLFromExcel("src/main/J1_OLIVOS/resultadoIndices/resultadoINDICESJ1.xlsx","src/main/J1_OLIVOS/resultadosjson","src/main/J1_OLIVOS/insercionJ1.sql","Sentinel-2","Satelite");
    SQLConvertorDron dron=new SQLConvertorDron("src/main/J3_OLIVOS/resultadosDron/datosDron");
    //String oliveSql="src/main/J3_OLIVOS/datosJ3.sql";
    //String rutaGuardar="src/main/J3_OLIVOS/resultadosDron/resultado_final.xlsx";
    //dron.readOliveIds(oliveSql);
    //prueba.procesarArchivos(rutaGuardar);
    //dron.generarSQLParaDron("src/main/J3_OLIVOS/resultadosDron/resultado_final.xlsx","src/main/J3_OLIVOS/resultadosDron/resultadosJSONDron","src/main/J3_OLIVOS/resultadosDron/insercionJ3Dron.sql","DJI 210","Dron");
    //dron.generarTXT("src/main/J1_OLIVOS/resultadosDron/resultado_final.xlsx","src/main/J1_OLIVOS/resultadosDron/resultadosJSONDron","src/main/J1_OLIVOS/resultadosDron/mediaJ1.txt");
    //PARA GENERAR LA MEDIA DEL SATÉLITE
    String mediaPath="src/main/J3_OLIVOS/resultadosDron/mediaJ3.txt";
    Integer provincia=23;
    Integer municipio=5;
    String zona="J3";
    Integer poligono=8;
    Integer parcela=101;
    Integer recinto=2;
    String tipoFuente="Dron";
    String nombreFuente="DJI 210";
    dron.generarMedia(mediaPath, provincia, municipio, zona, poligono, parcela, recinto,tipoFuente,nombreFuente);


    }
}