package com.umaso.mantenimientos.modules.users.repository;

import com.umaso.mantenimientos.modules.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCorreo(String correo);

    Optional<User> findByCorreoIgnoreCase(String correo);

    @Query("SELECT u FROM User u JOIN FETCH u.rol WHERE u.id = :id")
    Optional<User> findByIdWithRole(@Param("id") UUID id);

    boolean existsByCorreo(String correo);

}
