# TFG-Converter
Este proyecto forma parte del TFG de Alba Gómez Liébana Programa para el grado de Ingeniería Informática de la Universidad de Jaén en el curso 2023/2024.
Se trata de una aplicación Java que procesa datos descargados mediante satélite usando Google Earth Engine o mediante dron sobre índices de vegetación y los convierte en formato .xlsx. Los datos incluyen índices de vegetación, temperatura y lluvia acumulada. Posteriormente, genera la sentencia SQL para incluirla directamente en la Base de Datos.

## Iniciar 
Para obtener una copia del proyecto en funcionamiento en tu máquina local para fines de desarrollo y pruebas, puedes clonar el repositorio de GitHub.  
### Prerrequisitos
Kit de Desarrollo de Java (JDK)
Maven
Un Entorno de Desarrollo Integrado (IDE) como IntelliJ IDEA
### Instalación
Clona el repositorio
Abre el proyecto en tu IDE
Construye el proyecto usando Maven

## Uso
La clase principal del proyecto es Main.java. Esta clase contiene el método principal que orquesta el procesamiento de los datos.  El proyecto procesa datos para diferentes años y combina los resultados en un solo archivo Excel para cada tipo de datos (índices de vegetación, temperatura y lluvia acumulada).  El proyecto también incluye funcionalidad para generar sentencias SQL insert desde los datos procesados.
