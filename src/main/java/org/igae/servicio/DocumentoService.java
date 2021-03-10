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

@ApplicationScoped
public class DocumentoService {
    @Inject
    FormService formService;

    @Inject
    MongoDatabase mongoDB;

    public Hashtable<ObjectId, ObjectId> obtenerNuevosIds(List<Document> listaDocumentos) {
        Hashtable<ObjectId, ObjectId> equivalencias = new Hashtable<ObjectId, ObjectId>();

        for (Document documento : listaDocumentos) {
            equivalencias.put(documento.getObjectId("_id"), new ObjectId());
        }

        return equivalencias;
    }

    public List<Document> replicarDocumentos(List<Document> listaDocumentos, String antiguoPropietario,
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
        duplicado.append("previousowner", new ObjectId(antiguoPropietario));
        return duplicado;
    }

    private Document getClone(Document documento, Hashtable<ObjectId, ObjectId> equivalencias) {
        String documentoConReferenciasObsoletas = documento.toJson();
        String documentoConReferenciasActualizadas = actualizarReferencias(documentoConReferenciasObsoletas,
                documento.getObjectId("_id").toHexString(), equivalencias);
        return Document.parse(documentoConReferenciasActualizadas);
    }

    public String actualizarReferencias(String documento, String id, Hashtable<ObjectId, ObjectId> equivalencias) {
        String resultado = documento;

        for (ObjectId objectId : equivalencias.keySet()) {
            if (objectId.toHexString().compareTo(id) == 0)
                continue;

            resultado = resultado.replaceAll(objectId.toHexString(), equivalencias.get(objectId).toHexString());
        }

        return resultado;
    }

    public List<Document> getListaDocumentos(String idAntPropietario, String idNuevoPropietario) {
        Bson filtro = and(eq("previousowner", new ObjectId(idAntPropietario)),
                eq("owner", new ObjectId(idNuevoPropietario)), ne("form", formService.getIdFormularioEnvio()),
                getFiltroSalvaguarda());
        return this.getListaDocumentos(filtro);
    }

    public void eliminarDocumentos() {
        Bson filtro = getFiltroSalvaguarda();
        DeleteResult dr = getCollection().deleteMany(filtro);
    }

    public void eliminarDocumentos(String idAntPropietario, String idNuevoPropietario) {
        Bson filtro = and(eq("previousowner", new ObjectId(idAntPropietario)),
                eq("owner", new ObjectId(idNuevoPropietario)), ne("form", formService.getIdFormularioEnvio()),
                getFiltroSalvaguarda());
        DeleteResult dr = getCollection().deleteMany(filtro);
    }

    public List<Document> getListaDocumentosPoseidos(String idPropietario) {
        Bson filtro = and(eq("owner", new ObjectId(idPropietario)), ne("form", formService.getIdFormularioEnvio()),
                getFiltroSalvaguarda());
        return this.getListaDocumentos(filtro);
    }

    private Bson getFiltroSalvaguarda() {
        // evita que se borren los datos de los usuarios y de los administradores
        // además solo considera los borrados
        return and(ne("form", formService.getIdFormularioUsuario()), ne("form", formService.getIdFormularioAdmin()),
                eq("deleted", null));
    }

    public Document getDocumento(String id) {
        try {
            Bson filtro = eq("_id", new ObjectId(id));
            return (Document) getCollection().find(filtro).first();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Document> getListaDocumentos(Bson filtro) {
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

    public void insertarDocumentos(List<Document> lista) {
        getCollection().insertMany(lista);
    }

    public void actualizar(String id, String atributo, Object valor) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(id));

        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put(atributo, valor);

        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", newDocument);

        getCollection().updateOne(query, updateObject);
    }

    private MongoCollection getCollection() {
        return mongoDB.getCollection("submissions");
    }

}