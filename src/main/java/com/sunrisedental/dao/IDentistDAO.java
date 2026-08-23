package com.sunrisedental.dao;

import com.sunrisedental.model.Dentist;
import java.util.List;
import java.util.Optional;

public interface IDentistDAO {
    Optional<Dentist> findById(int id);
    List<Dentist> findAllAvailable();
    List<Dentist> findAll();
}
