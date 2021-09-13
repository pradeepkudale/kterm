package com.pradale.kterm.db.repository;

import com.pradale.kterm.db.entity.Server;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<Server, Integer> {
}