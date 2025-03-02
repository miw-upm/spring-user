## [Máster en Ingeniería Web por la Universidad Politécnica de Madrid (miw-upm)](http://miw.etsisi.upm.es)

## Back-end con Tecnologías de Código Abierto (BETCA).

> Este proyecto es un apoyo docente de la asignatura y contiene un proyecto completo con Spring

## Tecnologías necesarias

`Java` `Maven` `GitHub` `Spring-boot` `Sonarcloud` `PostgreSQL` `Docker`

### :gear: Instalación del proyecto

1. Clonar el repositorio en tu equipo, **mediante consola**:

```sh
> cd <folder path>
> git clone https://github.com/miw-upm/spring-api-rest
```

2. Importar el proyecto mediante **IntelliJ IDEA**
    * **Open**, y seleccionar la carpeta del proyecto.

### :gear: Ejecución en local

* Ejecutar en el proyecto la siguiente secuencia de comandos de Docker ( :warning: **incluir el punto final** ):

```sh
> docker build -t api .
> docker run -d --name api-app  -p 8080:8080 api
```
