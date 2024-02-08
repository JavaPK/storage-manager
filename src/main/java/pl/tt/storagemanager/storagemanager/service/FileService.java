package pl.tt.storagemanager.storagemanager.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface FileService {

    ResponseEntity<UUID> forwardUpload(MultipartFile file, String metadata) throws IOException;
}
