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
import org.igae.modelo.Usuario;
import org.igae.servicio.UsuarioService;

@ApplicationScoped
public class EnvioService {
    @Inject
    FormService formService;

    @Inject
    DocumentoService documentoService;

    @Inject
    UsuarioService usuarioService;

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
        envio.setDominio(datos.getString("dominio"));
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

    public void asociarEnvio(List<Document> listaDocumentos, Envio envio) {
        for (Document documento : listaDocumentos) {
            documento.append("envio", new ObjectId(envio.getId()));
        }
    }

    public void enviar(String idEnvio) {
        // envío en el lado del remitente
        Envio envio = this.obtenerEnvio(idEnvio);
        Usuario remitente = usuarioService.findOne(envio.getIdRemitente());
        Usuario destinatario = usuarioService.findOne(envio.getIdDestinatario());
        // se incluye en el resumen el remitente
        documentoService.actualizar(envio.getId(), "data.resumen",
                "De " + ((remitente == null || remitente.getName() == null) ? " - " : remitente.getName()) + " a "
                        + ((destinatario == null || destinatario.getName() == null) ? " - " : destinatario.getName()));
        documentoService.actualizar(envio.getId(), "data.momentoEnvio", envio.getMomentoEnvio());

        // se replican los documentos enviados por el remitente
        List<Document> listaDocumentos = documentoService.getListaDocumentosPoseidos(envio.getIdRemitente(),
                envio.getDominio());
        Hashtable<ObjectId, ObjectId> equivalenciasRemitente = documentoService.obtenerNuevosIds(listaDocumentos);
        List<Document> listaReplicasRemitente = documentoService.replicarDocumentos(listaDocumentos,
                envio.getIdRemitente(), equivalenciasRemitente);
        asociarEnvio(listaReplicasRemitente, envio);
        documentoService.insertarDocumentos(listaReplicasRemitente);

        // envío en el lado del destinatario

        // se replica el envío pero poniendo como propietario al receptor para que lo
        // vea en su lista de envíos

        Document documentoEnvio = documentoService.getDocumento(idEnvio);
        Hashtable<ObjectId, ObjectId> correspondenciaId = new Hashtable<ObjectId, ObjectId>();
        correspondenciaId.put(new ObjectId(idEnvio), new ObjectId());
        Document envioDuplicado = documentoService.replicarDocumento(documentoEnvio, envio.getIdDestinatario(),
                correspondenciaId);
        envioDuplicado.append("envio", new ObjectId(envio.getId())); // mantiene el nexo con el original
        documentoService.insertarDocumento(envioDuplicado);

        // se replican los documentos pendientes de recibir por el destinatario
        Hashtable<ObjectId, ObjectId> equivalenciasDestinatario = documentoService.obtenerNuevosIds(listaDocumentos);
        List<Document> listaReplicasDestinatario = documentoService.replicarDocumentos(listaDocumentos,
                envio.getIdDestinatario(), equivalenciasDestinatario);
        Envio envioReplica = this.obtenerEnvio(envioDuplicado.getObjectId("_id").toString());
        asociarEnvio(listaReplicasDestinatario, envioReplica);
        documentoService.insertarDocumentos(listaReplicasDestinatario);
    }

    public void recibir(String idEnvio) {
        Envio envio = this.obtenerEnvio(idEnvio);
        Envio envioOriginal = this.obtenerEnvio(envio.getId());

        List<Document> listaDocumentos = documentoService.getListaDocumentosPoseidos(envio.getIdRemitente(),
                envio.getDominio());
        Hashtable<ObjectId, ObjectId> equivalencias = documentoService.obtenerNuevosIds(listaDocumentos);
        List<Document> listaReplicas = documentoService.replicarDocumentos(listaDocumentos, envio.getIdRemitente(),
                envio.getIdDestinatario(), equivalencias);
        documentoService.eliminarDocumentos(envio.getIdRemitente(), envio.getIdDestinatario(), envio.getDominio());
        documentoService.insertarDocumentos(listaReplicas);

        Usuario remitente = usuarioService.findOne(envio.getIdRemitente());
        Usuario destinatario = usuarioService.findOne(envio.getIdDestinatario());

        // se incluye en el resumen el remitente
        documentoService.actualizar(envio.getId(), "data.resumen",
                "De " + ((remitente == null || remitente.getName() == null) ? " - " : remitente.getName()) + " a "
                        + ((destinatario == null || destinatario.getName() == null) ? " - " : destinatario.getName()));
        documentoService.actualizar(envio.getId(), "data.momentoEnvio", envio.getMomentoEnvio());
        // de momento se recibe automáticamente
        documentoService.actualizar(envio.getId(), "data.momentoRecepcion", envio.getMomentoEnvio());

        // se replica el envío pero poniendo como propietario al receptor para que lo
        // vea en su lista de envíos

        Document documentoEnvio = documentoService.getDocumento(idEnvio);
        Hashtable<ObjectId, ObjectId> correspondenciaId = new Hashtable<ObjectId, ObjectId>();
        correspondenciaId.put(new ObjectId(idEnvio), new ObjectId());
        Document envioDuplicado = documentoService.replicarDocumento(documentoEnvio, envio.getIdRemitente(),
                envio.getIdDestinatario(), correspondenciaId);
        documentoService.insertarDocumento(envioDuplicado);
    }

    private MongoCollection getCollection() {
        return mongoDB.getCollection("submissions");
    }
}