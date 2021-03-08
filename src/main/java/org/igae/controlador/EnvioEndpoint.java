package org.igae.controlador;

import javax.inject.Inject;

import javax.ws.rs.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

//import org.igae.servicio.EnvioService;
import org.igae.modelo.Envio;

@Path("/envios")
@Produces("application/json")
@Consumes("application/json")
public class EnvioEndpoint {

//    @Inject EnvioService service;

    @POST
    public List<Envio> add(HashMap mensaje) {
        System.out.println("=========================<");
        System.out.println(mensaje.toString());
        // remitente
        HashMap request = (HashMap)mensaje.get("request");
        HashMap data = (HashMap)request.get("data");
        HashMap destinatario = (HashMap)data.get("destinatario");
        HashMap submission = (HashMap)mensaje.get("submission");
        if (submission != null) {
            System.out.println("momento envío" + (String) submission.get("created"));
        }
        System.out.println("remitente: " + (String)request.get("owner"));
        System.out.println("destinatario: " + (String)destinatario.get("_id"));
        System.out.println("comentario: " + (String)data.get("comentario"));
        //
        System.out.println("=========================>");
//            service.add(envio);
        return new ArrayList<Envio>();
    }
}