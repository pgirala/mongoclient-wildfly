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
import org.bson.conversions.Bson;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;

import org.igae.modelo.Usuario;

@ApplicationScoped
public class UsuarioService {

    @Inject
    MongoDatabase mongoDB;

    public List<Usuario> list() {
        List<Usuario> list = new ArrayList<>();
        Bson filter = exists("data.password");
        MongoCursor<Document> cursor = getCollection().find(filter).iterator();

        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                Usuario usuario = new Usuario();

                usuario.setId(document.getObjectId("_id").toString());
                usuario.setName(document.get("data", Document.class).getString("name"));
                usuario.setEmail(document.get("data", Document.class).getString("email"));
                list.add(usuario);
            }
        } finally {
            cursor.close();
        }

        return list;
    }

    public Usuario findOne(String idUsuario) {
        Usuario resultado = null;

        for (Usuario usuario : this.list())
            if (usuario.getId().compareTo(idUsuario) == 0)
                resultado = usuario;

        return resultado;
    }

    /*
     * public void add(Usuario usuario){ Document document = new Document()
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