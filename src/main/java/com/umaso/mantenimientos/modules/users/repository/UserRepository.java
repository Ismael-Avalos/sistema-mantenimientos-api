package com.umaso.mantenimientos.modules.users.repository;

import com.umaso.mantenimientos.modules.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

}