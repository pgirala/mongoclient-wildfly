package org.igae.servicio;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import java.util.Date;

import org.igae.modelo.Envio;

@ApplicationScoped
public class EnvioService {
    @Inject
    FormService formService;

    @Inject
    DocumentoService documentoService;

    @Inject
    MongoDatabase mongoDB;

    private Envio obtenerEnvio(String idEnvio) {
        Document documento = documentoService.getDocumento(idEnvio);

        Envio envio = new Envio();
        envio.setId(documento.getObjectId("_id").toString());
        envio.setIdRemitente(documento.getObjectId("owner").toString());

        Document datos = (Document) documento.get("data");
        Document destinatario = (Document) datos.get("destinatario");
        envio.setIdDestinatario(destinatario.getObjectId("_id").toString());
        envio.setComentario(datos.getString("comentario"));
        envio.setMomentoEnvio(documento.getDate("created"));
        return envio;
    }

    private void imprimirListaDocumentos(List<Document> listaDocumentos) {
        System.out.println("====================>");
        for (Document d : listaDocumentos) {
            System.out.println(d);
            System.out.println("++++++++");
        }
        System.out.println("====================>");
    }

    public void perfeccionarEnvio(String idEnvio) {
        Envio envio = this.obtenerEnvio(idEnvio);

        List<Document> listaDocumentos = documentoService.getListaDocumentosPoseidos(envio.getIdRemitente());
        Hashtable<ObjectId, ObjectId> equivalencias = documentoService.obtenerNuevosIds(listaDocumentos);
        List<Document> listaReplicas = documentoService.replicarDocumentos(listaDocumentos, envio.getIdRemitente(),
                envio.getIdDestinatario(), equivalencias);
        documentoService.eliminarDocumentos(envio.getIdRemitente(), envio.getIdDestinatario());
        documentoService.insertarDocumentos(listaReplicas);

        documentoService.actualizar(envio.getId(), "data.momentoEnvio", envio.getMomentoEnvio());
    }

    private MongoCollection getCollection() {
        return mongoDB.getCollection("submissions");
    }
}