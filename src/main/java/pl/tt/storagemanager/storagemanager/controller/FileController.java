package pl.tt.storagemanager.storagemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.tt.storagemanager.storagemanager.service.FileService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<UUID> upload(@RequestParam(name = "file") MultipartFile file,
                                       @RequestParam(name = "metadata") String metadata) throws IOException {

        return fileService.forwardUpload(file, metadata);
    }
}
