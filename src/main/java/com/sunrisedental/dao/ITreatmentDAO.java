package com.sunrisedental.dao;

import com.sunrisedental.model.Treatment;
import java.util.List;
import java.util.Optional;

public interface ITreatmentDAO {
    Optional<Treatment> findById(int id);
    Optional<Treatment> findByCode(String code);
    List<Treatment> findAll();
}
