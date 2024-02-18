package pl.tt.storagemanager.storagemanager.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import pl.tt.storagemanager.storagemanager.model.FileDocument;

@Repository
public interface FileDocumentRepository extends MongoRepository<FileDocument, UUID> {
}
