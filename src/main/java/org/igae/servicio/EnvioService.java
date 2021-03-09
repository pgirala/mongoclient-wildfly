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

import org.igae.modelo.Envio;

@ApplicationScoped
public class EnvioService {
    @Inject
    FormService formService;

    @Inject
    MongoDatabase mongoDB;

    public void addEnvio(Envio envio) {
        List<Document> listaDocumentos = getListaDocumentosPoseidos(envio.getIdRemitente());
        Hashtable<ObjectId, ObjectId> equivalencias = obtenerNuevosIds(listaDocumentos);
        List<Document> listaReplicas = replicarDocumentos(listaDocumentos, envio.getIdRemitente(),
                envio.getIdDestinatario(), equivalencias);
        eliminarDocumentosEnviosPrevios(envio.getIdRemitente(), envio.getIdDestinatario());
        getCollection().insertMany(listaReplicas);
        // insertar envío para que lo vea el receptor
    }

    private Hashtable<ObjectId, ObjectId> obtenerNuevosIds(List<Document> listaDocumentos) {
        Hashtable<ObjectId, ObjectId> equivalencias = new Hashtable<ObjectId, ObjectId>();

        for (Document documento : listaDocumentos) {
            equivalencias.put(documento.getObjectId("_id"), new ObjectId());
        }

        return equivalencias;
    }

    private List<Document> replicarDocumentos(List<Document> listaDocumentos, String antiguoPropietario,
            String nuevoPropietario, Hashtable<ObjectId, ObjectId> equivalencias) {

        List<Document> resultado = new ArrayList<Document>();

        for (Document documento : listaDocumentos) {
            Document replica = replicarDocumento(documento, antiguoPropietario, nuevoPropietario, equivalencias);
            resultado.add(replica);
        }

        return resultado;
    }

    private Document replicarDocumento(Document documento, String antiguoPropietario, String nuevoPropietario,
            Hashtable<ObjectId, ObjectId> equivalencias) {
        Document duplicado = getClone(documento, equivalencias);
        duplicado.replace("_id", documento.getObjectId("_id"), equivalencias.get(documento.getObjectId("_id")));
        duplicado.replace("owner", new ObjectId(nuevoPropietario));
        duplicado.append("sender", new ObjectId(antiguoPropietario));
        return duplicado;
    }

    private Document getClone(Document documento, Hashtable<ObjectId, ObjectId> equivalencias) {
        String documentoConReferenciasObsoletas = documento.toJson();
        String documentoConReferenciasActualizadas = actualizarReferencias(documentoConReferenciasObsoletas,
                documento.getObjectId("_id").toHexString(), equivalencias);
        return Document.parse(documentoConReferenciasActualizadas);
    }

    private String actualizarReferencias(String documento, String id, Hashtable<ObjectId, ObjectId> equivalencias) {
        String resultado = documento;

        for (ObjectId objectId : equivalencias.keySet()) {
            if (objectId.toHexString().compareTo(id) == 0)
                continue;

            resultado = resultado.replaceAll(objectId.toHexString(), equivalencias.get(objectId).toHexString());
        }

        return resultado;
    }

    private List<Document> getListaDocumentosRemitidos(String idRemitente, String idDestinatario) {
        Bson filtro = and(eq("sender", new ObjectId(idRemitente)), eq("owner", new ObjectId(idDestinatario)),
                ne("form", formService.getIdFormularioUsuario()), ne("form", formService.getIdFormularioAdmin()),
                ne("form", formService.getIdFormularioEnvio()), eq("deleted", null));
        return this.getListaDocumentos(filtro);
    }

    private void eliminarDocumentosEnviosPrevios(String idRemitente, String idDestinatario) {
        Bson filtro = and(eq("sender", new ObjectId(idRemitente)), eq("owner", new ObjectId(idDestinatario)),
                ne("form", formService.getIdFormularioUsuario()), ne("form", formService.getIdFormularioAdmin()),
                ne("form", formService.getIdFormularioEnvio()), eq("deleted", null));
        DeleteResult dr = getCollection().deleteMany(filtro);
    }

    private List<Document> getListaDocumentosPoseidos(String idPropietario) {
        Bson filtro = and(eq("owner", new ObjectId(idPropietario)), ne("form", formService.getIdFormularioUsuario()),
                ne("form", formService.getIdFormularioAdmin()), ne("form", formService.getIdFormularioEnvio()),
                eq("deleted", null));
        return this.getListaDocumentos(filtro);
    }

    private List<Document> getListaDocumentos(Bson filtro) {
        List<Document> resultado = new ArrayList<>();

        MongoCursor<Document> cursor = getCollection().find(filtro).iterator();

        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                resultado.add(document);
            }
        } finally {
            cursor.close();
        }

        return resultado;
    }

    private MongoCollection getCollection() {
        return mongoDB.getCollection("submissions");
    }
}