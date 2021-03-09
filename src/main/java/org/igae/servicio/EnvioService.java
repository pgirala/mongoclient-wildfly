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
        eliminarDocumentosEnviosPrevios(envio.getIdRemitente(), envio.getIdDestinatario());
        Hashtable<ObjectId, ObjectId> equivalencias = replicarDocumentos(envio.getIdDestinatario(),
                getListaDocumentosPoseidos(envio.getIdRemitente()));
    }

    private Hashtable<ObjectId, ObjectId> replicarDocumentos(String nuevoPropietario, List<Document> listaDocumentos) {
        Hashtable<ObjectId, ObjectId> equivalencias = new Hashtable<ObjectId, ObjectId>();

        for (Document documento : listaDocumentos) {
            Document replica = replicarDocumento(documento, nuevoPropietario);

            System.out.println("*****************");
            System.out.println("ORIGINAL======>" + documento.toJson());
            System.out.println("REPLICA======>" + replica.toJson());

            if (replica != null)
                equivalencias.put(documento.getObjectId("_id"), replica.getObjectId("_id"));

            // getCollection().insertOne(replica);
        }

        return equivalencias;
    }

    private Document replicarDocumento(Document documento, String nuevoPropietario) {
        Document duplicado = getClone(documento);
        duplicado.append("_id", new ObjectId());
        return duplicado;
    }

    private Document getClone(Document documento) {
        return Document.parse(documento.toJson());
    }

    private List<Document> getListaDocumentosRemitidos(String idRemitente, String idDestinatario) {
        Bson filtro = and(eq("sender", new ObjectId(idRemitente)), eq("owner", new ObjectId(idDestinatario)),
                ne("form", formService.getIdFormularioUsuario()), ne("form", formService.getIdFormularioEnvio()),
                eq("deleted", null));
        return this.getListaDocumentos(filtro);
    }

    private void eliminarDocumentosEnviosPrevios(String idRemitente, String idDestinatario) {
        Bson filtro = and(eq("sender", new ObjectId(idRemitente)), eq("owner", new ObjectId(idDestinatario)),
                ne("form", formService.getIdFormularioUsuario()), ne("form", formService.getIdFormularioEnvio()),
                eq("deleted", null));
        DeleteResult dr = getCollection().deleteMany(filtro);
    }

    private List<Document> getListaDocumentosPoseidos(String idPropietario) {
        Bson filtro = and(eq("owner", new ObjectId(idPropietario)), ne("form", formService.getIdFormularioUsuario()),
                ne("form", formService.getIdFormularioEnvio()), eq("deleted", null));
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