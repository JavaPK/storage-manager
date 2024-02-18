package pl.tt.storagemanager.storagemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pl.tt.storagemanager.storagemanager.model.FileDocument;
import pl.tt.storagemanager.storagemanager.repository.FileDocumentRepository;
import pl.tt.storagemanager.storagemanager.service.FileService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")
public class FileController {

    private final FileService fileService;
    private final FileDocumentRepository fileDocumentRepository;

    @PostMapping("/upload")
    public ResponseEntity<UUID> upload(@RequestParam(name = "file") MultipartFile file,
                                       @RequestParam(name = "metadata") String metadata) throws IOException {

        return fileService.forwardUpload(file, metadata);
    }

    @GetMapping
    public ResponseEntity<List<FileDocument>> get(){
        return ResponseEntity.ok(fileDocumentRepository.findAll());
    }
}
