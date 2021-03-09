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

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import com.mongodb.client.result.DeleteResult;

import org.igae.modelo.Envio;

@ApplicationScoped
public class EnvioService {

    @Inject
    MongoDatabase mongoDB;

    public void addEnvio(Envio envio) {
        eliminarDocumentosEnviosPrevios(envio.getIdRemitente(), envio.getIdDestinatario());
        List<Document> listaDocumentos = getListaDocumentosPoseidos(envio.getIdRemitente());
        System.out.println("=====>Número de documentos poseidos: " + listaDocumentos.size());
    }

    public List<Document> getListaDocumentosRemitidos(String idRemitente, String idDestinatario) {
        Bson filtro = and(eq("sender", new ObjectId(idRemitente)), eq("owner", new ObjectId(idDestinatario)),
                eq("deleted", null));
        return this.getListaDocumentos(filtro);
    }

    public void eliminarDocumentosEnviosPrevios(String idRemitente, String idDestinatario) {
        Bson filtro = and(eq("sender", new ObjectId(idRemitente)), eq("owner", new ObjectId(idDestinatario)),
                eq("deleted", null));
        DeleteResult dr = getCollection().deleteMany(filtro);
        System.out.println("====================>Borrados: " + dr.getDeletedCount());
    }

    public List<Document> getListaDocumentosPoseidos(String idPropietario) {
        Bson filtro = and(eq("owner", new ObjectId(idPropietario)), eq("deleted", null));
        return this.getListaDocumentos(filtro);
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

    /*
     * public List<Document> list(){ List<Usuario> list = new ArrayList<>(); Bson
     * filter = exists("data.password"); MongoCursor<Document> cursor =
     * getCollection().find(filter).iterator();
     * 
     * try { while (cursor.hasNext()) { Document document = cursor.next(); Usuario
     * usuario = new Usuario(); usuario.setEmail(document.get("data",
     * Document.class).getString("email"));
     * System.out.println("===============================");
     * System.out.println(document.toString());
     * System.out.println("==============================="); list.add(usuario); } }
     * finally { cursor.close(); }
     * 
     * return list; }
     * 
     * publicvoid add(Usuario usuario){ Document document = new Document()
     * .append("name", customer.getName()) .append("surname", customer.getSurname())
     * .append("id", customer.getId()); getCollection().insertOne(document); }
     * 
     * public void update(Customer customer){ // update one document
     * 
     * Bson filter = eq("id", customer.getId()); Bson updateOperation = set("name",
     * customer.getName()); getCollection().updateOne(filter, updateOperation); }
     * 
     * public void delete(Customer customer){ // delete one document
     * 
     * Bson filter = eq("id", customer.getId()); getCollection().deleteOne(filter);
     * }
     * 
     */
    private MongoCollection getCollection() {
        return mongoDB.getCollection("submissions");
    }
}