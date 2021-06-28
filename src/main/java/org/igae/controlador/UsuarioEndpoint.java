package org.igae.controlador;

import javax.inject.Inject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.tagext.BodyContent;

import java.util.Base64;
import java.util.HashMap;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.client.*;
import javax.ws.rs.client.Entity;
import java.util.List;
import com.mongodb.BasicDBObject;

import org.igae.servicio.UsuarioService;
import org.igae.modelo.Acreditacion;
import org.igae.modelo.Usuario;
import org.igae.modelo.Payload;

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
    public String getTokenFormioUsuario() {
        String tokenKC = httpServletRequest.getHeader("authorization");
        if (tokenKC == null) {
            try {
                httpServletResponse.sendError(401, "Token invalido");
            } catch (Exception e) {

            }
            return null;
        }
        String codigoUsuario = this.getCodigoUsuario(tokenKC);
        httpServletResponse.setHeader("x-jwt-token", this.getToken(codigoUsuario));
        return "{\"codigoUsuario\": \"" + codigoUsuario + "\"}";
    }

    @GET
    @Path("/token/organizacion")
    @Produces("application/json")
    public String getTokenFormioOrganizacion() {
        String tokenKC = httpServletRequest.getHeader("authorization");
        if (tokenKC == null) {
            try {
                httpServletResponse.sendError(401, "Token invalido");
            } catch (Exception e) {

            }
            return null;
        }
        String codigoUsuario = this.getCodigoOrganizacion(tokenKC);
        httpServletResponse.setHeader("x-jwt-token", this.getToken(codigoUsuario));
        return "{\"codigoUsuario\": \"" + codigoUsuario + "\"}";
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
    private String getCodigoUsuario(String token) {
        String resultado = "Anónimo";
        String tokenKC = token.replace("Bearer ", "");
        String[] chunks = tokenKC.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        BasicDBObject basicDBObject = (BasicDBObject) BasicDBObject.parse(payload);
        if (basicDBObject != null)
            resultado = basicDBObject.getString("preferred_username");
        return resultado;
    }

    private String getCodigoOrganizacion(String token) {
        String resultado = "Anónimo";
        String tokenKC = token.replace("Bearer ", "");
        String[] chunks = tokenKC.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        BasicDBObject basicDBObject = (BasicDBObject) BasicDBObject.parse(payload);
        if (basicDBObject != null) {
            String user = basicDBObject.getString("user");
            if (user != null) {
                basicDBObject = (BasicDBObject) BasicDBObject.parse(user);
                resultado = basicDBObject.getString("organization");
            }
        }
        return resultado;
    }

    private String getToken(String codigoUsuario) {
        try {
            Client cliente = ClientBuilder.newClient();
            WebTarget recurso = cliente.target("http://formio:3001/user/login"); // TODO a un fichero de properties
            Payload payload = new Payload();
            payload.setData(this.getAcreditacion(codigoUsuario));
            Response respuesta = recurso.request().post(Entity.json(payload.toString()));
            return respuesta.getHeaderString("x-jwt-token");
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private Acreditacion getAcreditacion(String codigoUsuario) {
        Acreditacion acreditacion = new Acreditacion();
        acreditacion.setEmail(codigoUsuario + "@gob.es"); // TODO a un fichero de properties
        return acreditacion;
    }
}
