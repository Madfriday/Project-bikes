package edu360.bike2bike.bike.Mapper;

import edu360.bike2bike.bike.DataSource.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    public User getById(Long id);

    public List<User> findAll();

    public void save(User user);

    public void deleteByIds(Long[] ids);

    public void update(User user);

    public User login(User user);

}
