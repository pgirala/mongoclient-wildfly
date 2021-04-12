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

    @Inject
    EnvioService service;

    @POST
    public HashMap add(HashMap mensaje) {
        HashMap resultado = new HashMap();
        String idEnvio = (String) mensaje.get("submissionId");
        service.perfeccionarEnvio(idEnvio);
        resultado.put("success", true);
        resultado.put("message", "Envío realizado correctamente.");
        return resultado;
    }
}