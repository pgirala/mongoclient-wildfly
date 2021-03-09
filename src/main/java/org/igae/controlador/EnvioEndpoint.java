package org.igae.controlador;

import javax.inject.Inject;

import javax.ws.rs.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.igae.servicio.EnvioService;
import org.igae.modelo.Envio;

@Path("/envios")
@Produces("application/json")
@Consumes("application/json")
public class EnvioEndpoint {

    @Inject EnvioService service;

    @POST
    public List<Envio> add(HashMap mensaje) {
        List<Envio> resultado = new ArrayList<Envio>();
        HashMap request = (HashMap)mensaje.get("request");
        HashMap data = (HashMap)request.get("data");
        HashMap destinatario = (HashMap)data.get("destinatario");
        HashMap submission = (HashMap)mensaje.get("submission");

        Envio envio = new Envio();
        envio.setIdRemitente((String)request.get("owner"));
        envio.setIdDestinatario((String)destinatario.get("_id"));
        envio.setComentario((String)data.get("comentario"));

        if (submission != null)  
            try {
                envio.setMomentoEnvio(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse((String)submission.get("created")));
            } catch (Exception e) {
                System.out.println("=============> Error de formateo con fechas");
                e.printStackTrace();
            }

        service.addEnvio(envio);

        resultado.add(envio);
        return resultado;
    }
}