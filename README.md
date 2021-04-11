# Clifford-back

Cliente fácil de formularios dinámicos (back end).

"Back end" RESTful de Clifford.

Nace para suplir la falta de triggers en MongoDB (para "perfeccionar" los envíos, replicando los documentos para que tenga acceso el receptor).

El objetivo es evolucionarlo para intermediar entre el front y los tres servidores implicados (el de formularios: form.io, el de flujos de trabajo, jBPM/Kie server y el de base de datos de documentos MongoDB). Así se podrán aplicar políticas de autorización basadas en Keycloak y se ofrecerá un API REST al front (incluyendo las consultas a elementos de las listas desplegables que aparecen en los formularios form.io).

## Obtener el código

```
$ git clone https://github.com/pgirala/Clifford-back.git


## Ejecutar la aplicación (desarrollo)

Requiere de MongoDB funcionando en el puerto habitual (mongodb://localhost:27017/formio?readPreference=primary&appname=MongoDB%20Compass&ssl=false). Se puede acceder desde su cliente Compass.

El servidor Wildfly también en el puerto estándar (8080) con la consola de administración en http://localhost:9990/console/index.html

Para poder acceder al servidor de administración de Wildfly hay que hacer lo siguiente:

1. Habilitar puertos entrantes:
- Acceder en wildfly\standalone\configuration al fichero standalone.xml
- Editarlo al final, en la parte de interfaces, sustituyendo en todos 127.0.0.1 por 0.0.0.0 (vi, i para insertar, ESC para volver al modo normal, :wq para grabar y salir, :q para salir).

2. Crear usuario administrador:
- Situarse en wildfly\bin
- Ejecutar ./add-user --silent admin Admin#1967 ManagementRealm
- Otra opción es ejecutar desde Windows:
    docker exec <CONTAINER> /opt/jboss/keycloak/bin/add-user-keycloak.sh -u admin -p Admin#1967

La aplicación se instala con:

mvn install wildfly:deploy

```

Se invoca al método con:

`http://localhost:8090/clifford-back/rest/envios` (el puerto dependerá de la instalación.)

```

Nótese que no está protegido (hay que incorporar la protección de Keycloak).

```
