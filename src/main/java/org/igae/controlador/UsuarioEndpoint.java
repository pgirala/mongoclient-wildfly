package org.igae.controlador;

import javax.inject.Inject;

import javax.ws.rs.*;
import java.util.List;

import org.igae.servicio.UsuarioService;
import org.igae.modelo.Usuario;

@Path("/usuarios")
@Produces("application/json")
@Consumes("application/json")
public class UsuarioEndpoint {

    @Inject UsuarioService service;

    @GET
    public List<Usuario> list() {
        return service.list();
    }
/*
    @POST
    public List<Usuario> add(Usuario usuario) {
        service.add(usuario);
        return list();
    }

    @PUT
    public List<Usuario> put(Usuario usuario) {
        service.update(usuario);
        return list();
    }

    @DELETE
    public List<Usuario> delete(Usuario usuario) {
        service.delete(usuario);
        return list();
    }
*/
}