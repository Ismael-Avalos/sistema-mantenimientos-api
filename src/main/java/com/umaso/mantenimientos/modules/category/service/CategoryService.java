package com.umaso.mantenimientos.modules.category.service;

import com.umaso.mantenimientos.modules.assets.repository.AssetRepository;
import com.umaso.mantenimientos.modules.category.dto.request.CreateCategoryRequest;
import com.umaso.mantenimientos.modules.category.dto.response.CategoryResponse;
import com.umaso.mantenimientos.modules.category.entity.Category;
import com.umaso.mantenimientos.modules.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AssetRepository assetRepository;

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        Category category = Category.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse update(UUID id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        category.setNombre(request.nombre());
        category.setDescripcion(request.descripcion());
        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    @Transactional
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada");
        }

        long equiposAsociados = assetRepository.countByCategoriaId(id);

        if (equiposAsociados > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar esta categoría porque tiene " + equiposAsociados + " equipo(s) asociado(s)."
            );
        }

        categoryRepository.deleteById(id);
    }

    @Transactional
    public void deleteAndReassign(UUID categoryIdToDelete, UUID targetCategoryId) {
        if (categoryIdToDelete.equals(targetCategoryId)) {
            throw new IllegalArgumentException("La categoría de destino debe ser diferente a la que deseas eliminar.");
        }

        Category targetCategory = categoryRepository.findById(targetCategoryId)
                .orElseThrow(() -> new RuntimeException("La categoría de destino no existe"));

        // Reasignar todos los equipos a la nueva categoría
        // Cambia targetCategory.getId() por targetCategory
        assetRepository.reassignCategory(categoryIdToDelete, targetCategory);

        // Eliminar la categoría original
        categoryRepository.deleteById(categoryIdToDelete);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        return mapToResponse(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getNombre(),
                category.getDescripcion()
        );
    }
}