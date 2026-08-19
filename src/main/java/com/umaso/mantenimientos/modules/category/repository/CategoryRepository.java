package com.umaso.mantenimientos.modules.category.repository;

import com.umaso.mantenimientos.modules.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNombreIgnoreCase(String nombre);
}