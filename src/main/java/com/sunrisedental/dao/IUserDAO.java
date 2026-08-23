package com.sunrisedental.dao;

import com.sunrisedental.model.User;
import java.util.List;
import java.util.Optional;

public interface IUserDAO {
    Optional<User> findByUsername(String username);
    Optional<User> findById(int id);
    List<User> findAll();
    boolean createUser(User user);
    boolean updateUser(User user);
}
