package edu360.bike2bike.bike.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class Logserviceimp implements Logservice {
    @Autowired
    private MongoTemplate db;

    @Override
    public void save(String log) {
        db.save(log,"logs");
    }
}
