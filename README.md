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

Para incorporar la protección basada en un bearer token generado por Keycloak:
- Ejemplo base tomado de https://wjw465150.gitbooks.io/keycloak-documentation/content/securing_apps/topics/oidc/java/jboss-adapter.html

- Descargar el adaptador de Keycloak para WildFly: https://www.keycloak.org/downloads
- Subirlo al servidor Wildfly: docker cp keycloak-oidc-wildfly-adapter-13.0.1.zip wildfly:/opt/jboss/wildfly
- Desempaquetarlo: unzip keycloak-oidc-wildfly-adapter-13.0.1.zip
- Eliminar el fichero zip.
- Configurar el adaptador cambiando al subdirectorio bin y ejecutando: ./jboss-cli.sh --file=adapter-elytron-install-offline.cli
- Reiniciar el servidor y desplegar Clifford-back cuando el servidor esté totalmente levantado (tarda un rato).
- Hay que cambiar el issuer del token que genera Keycloak. Para ello basta con poner en la solapa general de Clifford, la Frontend URL: http://keycloak:8080/auth/
Para descubrir este problema fue fundamental hacer un dump de todas las peticiones http a wildfly basándose en http://alloutfornoloss.com/wildfly-dump-http-request-and-response/ con lo que el standalone.xml del servidor Wildfly debe quedar así en este apartado:
        <subsystem xmlns="urn:jboss:domain:undertow:10.0" default-server="default-server" default-virtual-host="default-host" default-servlet-container="default" default-security-domain="other">
            <buffer-cache name="default"/>
            <server name="default-server">
                <http-listener name="default" socket-binding="http" redirect-socket="https" enable-http2="true"/>
                <https-listener name="https" socket-binding="https" security-realm="ApplicationRealm" enable-http2="true"/>
                <host name="default-host" alias="localhost">
                    <location name="/" handler="welcome-content"/>
                    <http-invoker security-realm="ApplicationRealm"/>
					<filter-ref name="request-dumper"/>
                </host>
            </server>
            <servlet-container name="default">
                <jsp-config/>
                <websockets/>
            </servlet-container>
            <handlers>
                <file name="welcome-content" path="${jboss.home.dir}/welcome-content"/>
            </handlers>
            <application-security-domains>
                <application-security-domain name="other" http-authentication-factory="keycloak-http-authentication"/>
            </application-security-domains>
			<filters>
				<filter name="request-dumper" class-name="io.undertow.server.handlers.RequestDumpingHandler" module="io.undertow.core"/>
			</filters>
        </subsystem>
- Crear, como administrador en Keycloak, en el dominio Clifford, el cliente clifford-back-end con el protocolo openid-connect.
- Tras crearlo, cambiar el tipo de acceso por "Bearer only".
- Seleccionar "Instalación", seleccionar el formato JSON, añadir la línea   "enable-cors": true y descargar el fichero colocándolo en webapp\WEB-INF
- Para que los dos grupos de contenedores trabajen en la misma red hay que hacer lo siguiente:
- 1. Crear una red: docker network create clifford-net
- 2. Asegurarse de que en los docker-compose.yml se incluya una referencia a la red creada:
networks:
  default:
    external:
      name: clifford-net


```
