package org.igae.controlador;

import javax.inject.Inject;

import javax.ws.rs.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.igae.servicio.DocumentoService;

@Path("/documentos")
@Produces("application/json")
@Consumes("application/json")
public class DocumentoEndPoint {

    @Inject
    DocumentoService service;

    @POST
    public void reset() {
        service.eliminarDocumentos();
    }
}