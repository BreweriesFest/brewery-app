package com.brewery.app.scheduler;

import com.mongodb.client.MongoClient;
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    public MongoDBStorageProvider mongoDBStorageProvider(MongoClient mongoClient) {
        return new MongoDBStorageProvider(mongoClient);
    }
}
