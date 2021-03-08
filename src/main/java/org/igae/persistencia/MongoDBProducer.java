package org.igae.persistencia;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

import com.mongodb.client.MongoDatabase;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class MongoDBProducer {

    @Produces
    public MongoClient createMongo() {
        return new MongoClient("mongo", 27017);
    }

    @Produces
    public MongoDatabase createDB(MongoClient client) {
        return client.getDatabase("formio");
    }

    public void close(@Disposes MongoClient toClose) {
        toClose.close();
    }
}