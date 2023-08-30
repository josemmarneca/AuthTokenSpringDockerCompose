package com.example.app.database.repositories;


import com.example.app.database.entities.Role;
import com.example.app.database.entities.User;
import com.example.app.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}
