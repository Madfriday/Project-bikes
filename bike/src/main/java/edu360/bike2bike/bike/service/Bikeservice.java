package edu360.bike2bike.bike.service;

import edu360.bike2bike.bike.DataSource.Bike;
import edu360.bike2bike.bike.DataSource.BikeInfornmation;
import org.springframework.data.geo.GeoResults;

import java.util.List;

public interface Bikeservice {
    public void save(BikeInfornmation bike);
    public void save(Bike l);
    public void save(String l);
    public List<Bike> findAll();
    BikeInfornmation getById(Long id);
    void deleteByIds(Long[] ids);
    public void save1(String bike);
    public void save2(String bike);
    void update(BikeInfornmation Bike);

    GeoResults<Bike> findnear(double longitude, double latitude);
}
