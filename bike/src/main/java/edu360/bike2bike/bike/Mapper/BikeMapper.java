package edu360.bike2bike.bike.Mapper;

import edu360.bike2bike.bike.DataSource.Bike;
import edu360.bike2bike.bike.DataSource.BikeInfornmation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BikeMapper {
    public void save(BikeInfornmation bike);

    public void save(Bike l);

    public List<Bike> findAll();

    public BikeInfornmation getById(Long id);
    public void deleteByIds(Long[] ids);
    public void update(BikeInfornmation Bike);

    public BikeInfornmation login(BikeInfornmation Bike);

}

