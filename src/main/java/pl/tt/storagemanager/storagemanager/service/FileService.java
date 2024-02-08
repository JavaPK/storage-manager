package pl.tt.storagemanager.storagemanager.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface FileService {

    UUID upload(MultipartFile file, String metadata) throws IOException;
}
