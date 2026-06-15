package com.umaso.mantenimientos.modules.attachments.repository;

import com.umaso.mantenimientos.modules.attachments.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
}