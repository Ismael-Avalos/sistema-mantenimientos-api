package com.umaso.mantenimientos.modules.settings.repository;

import com.umaso.mantenimientos.modules.settings.entity.InstitutionalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InstitutionalConfigurationRepository
        extends JpaRepository<InstitutionalConfiguration, UUID> {
}