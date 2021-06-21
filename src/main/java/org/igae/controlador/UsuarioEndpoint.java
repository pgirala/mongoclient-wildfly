package org.igae.controlador;

import javax.inject.Inject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import com.mongodb.BasicDBObject;

import org.igae.servicio.UsuarioService;
import org.igae.modelo.Usuario;

@Path("/usuarios")
// @Produces("application/json")
// @Consumes("application/json")
public class UsuarioEndpoint {
    @Context
    private ServletConfig servletConfig;
    @Context
    private ServletContext servletContext;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;

    @Inject
    UsuarioService service;

    @GET
    public List<Usuario> list() {
        return service.list();
    }

    @GET
    @Path("/token/usuario")
    @Produces("application/json")
    public String getTokenFormio() {
        String resultado = "ERROR";
        String tokenKC = httpServletRequest.getHeader("authorization");
        if (tokenKC == null) {
            try {
                httpServletResponse.sendError(401, "Token invalido");
            } catch (Exception e) {

            }
            return null;
        }

        tokenKC = tokenKC.replace("Bearer ", "");
        String[] chunks = tokenKC.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        BasicDBObject basicDBObject = (BasicDBObject) BasicDBObject.parse(payload);
        if (basicDBObject != null)
            resultado = basicDBObject.getString("preferred_username");
        return resultado;
    }

    @GET
    @Path("/token/organizacion")
    @Produces("application/json")
    public String getTokenFormioOrganizacion() {
        String resultado = "ERROR";
        String tokenKC = httpServletRequest.getHeader("authorization");
        if (tokenKC == null) {
            try {
                httpServletResponse.sendError(401, "Token invalido");
            } catch (Exception e) {

            }
            return null;
        }

        tokenKC = tokenKC.replace("Bearer ", "");
        String[] chunks = tokenKC.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        BasicDBObject basicDBObject = (BasicDBObject) BasicDBObject.parse(payload);
        String user = basicDBObject.getString("user");
        if (user != null) {
            basicDBObject = (BasicDBObject) BasicDBObject.parse(user);
            resultado = basicDBObject.getString("organization");
        }
        return resultado;
    }

    /*
     * @POST public List<Usuario> add(Usuario usuario) { service.add(usuario);
     * return list(); }
     * 
     * @PUT public List<Usuario> put(Usuario usuario) { service.update(usuario);
     * return list(); }
     * 
     * @DELETE public List<Usuario> delete(Usuario usuario) {
     * service.delete(usuario); return list(); }
     */
}