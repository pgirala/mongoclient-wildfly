package org.igae.controlador;

import java.util.Properties;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

/*
* El propósito es que el front solo interactúe con el back y no con el servidor form.io
* En el front, en environment.ts hay que poner lo siguiente:
*  FI_HOST: 'http://localhost:8010/proxy/clifford-back/rest/formio', 
*  FI_BASE_URL: 'http://localhost:8010/proxy/clifford-back/rest/formio',
*  FI_PROJECT_URL: 'http://localhost:8010/proxy/clifford-back/rest/formio', 
*
* TODO: la llamada pierde los parámetros de consulta con la redirección. Probablemente pierda también headers, ...
* Se abandona esta línea hasta no encontrar como replicar la petición exacta.
*/

@Path("/formio")
@Produces("application/json")
@Consumes("application/json")
public class FormioEndpoint {
    @Context
    private ServletContext servletContext;
    @Context
    private HttpServletRequest httpServletRequest;

    @Path("/{suburl:.+}")
    @GET
    @POST
    @PUT
    @PATCH
    @DELETE
    public Response proxy(@PathParam("suburl") String path) {
        try {
            URI uri = new URI(this.getProperties().getProperty("FI_HOST") + "/" + path + "?"
                    + httpServletRequest.getQueryString()); // la llamada perderá los parámetros en la redirección por
                                                            // lo que hay que añadírselos
            return Response.temporaryRedirect(uri).build();
        } catch (Exception e) {
            return null;
        }
    }

    private Properties getProperties() {
        Properties prop = new Properties();
        try {
            InputStream inputStream = servletContext.getResourceAsStream("/WEB-INF/application.properties");
            prop.load(inputStream);
        } catch (Exception e) {
        }
        return prop;
    }
}