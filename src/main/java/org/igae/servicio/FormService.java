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

@ApplicationScoped
public class FormService {

    @Inject
    MongoDatabase mongoDB;

    public Document getFormulario(String tipo, String nombre) {
        Bson filtro = and(eq("type", tipo), eq("name", nombre), eq("deleted", null));
        return (Document) getCollection().find(filtro).first();
    }

    public ObjectId getIdFormularioUsuario() {
        Document formularioUsuario = getFormulario("resource", "user");
        if (formularioUsuario == null)
            return null;
        else
            return (ObjectId) formularioUsuario.get("_id");
    }

    public ObjectId getIdFormularioAdmin() {
        Document formularioUsuario = getFormulario("resource", "admin");
        if (formularioUsuario == null)
            return null;
        else
            return (ObjectId) formularioUsuario.get("_id");
    }

    public ObjectId getIdFormularioEnvio() {
        Document formularioUsuario = getFormulario("resource", "envio");
        if (formularioUsuario == null)
            return null;
        else
            return (ObjectId) formularioUsuario.get("_id");
    }

    public List<ObjectId> getIdFormularios(String dominio) {
        List<ObjectId> resultado = new ArrayList<>();

        MongoCursor<Document> cursor = getCollection().find(Filters.regex("path", dominio)).iterator();

        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                resultado.add(document.getObjectId("_id"));
            }
        } finally {
            cursor.close();
        }

        return resultado;
    }

    private MongoCollection getCollection() {
        return mongoDB.getCollection("forms");
    }
}