package edu360.bike2bike.bike.service;


import edu360.bike2bike.bike.DataSource.Bike;
import edu360.bike2bike.bike.DataSource.BikeInfornmation;
import edu360.bike2bike.bike.Mapper.BikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class BikeServiceImpl implements Bikeservice{



    @Autowired
    private MongoTemplate db;
    @Autowired
    BikeMapper mapper;

    @Override
    public BikeInfornmation getById(Long id) {
        return mapper.getById(id);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        mapper.deleteByIds(ids);
    }

    @Override
    public void update(BikeInfornmation Bike) {
        mapper.update(Bike);
    }



    @Override
    public void save(BikeInfornmation bike) {
        mapper.save(bike);
    }

    @Override
    public void save(Bike bike) {
        db.save(bike);
    }

    @Override
    public void save1(String bike) {
        db.save(bike,"Bike1");
    }

    @Override
    public void save2(String bike) {
        db.save(bike,"Bike2");
    }

    @Override
    public List<Bike> findAll() {
        return db.findAll(Bike.class);
    }
    @Override
    public void save(String l) {
        db.save(l,"bike");
    }

    @Override
    public GeoResults<Bike> findnear(double longitude, double latitude) {
        NearQuery nr = NearQuery.near(longitude,latitude,Metrics.KILOMETERS);
        nr.maxDistance(1.2).query(new Query().addCriteria(Criteria.where("status").is(0)).limit(20));
         GeoResults<Bike> rs =   db.geoNear(nr,Bike.class);
         return rs;
    }
}
