package pl.tt.storagemanager.storagemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.tt.storagemanager.storagemanager.api.RegisterInstanceDTO;
import pl.tt.storagemanager.storagemanager.service.RegisterStorageService;

@RequestMapping("/api/storage/register")
@RestController
@RequiredArgsConstructor
public class RegisterStorageController {

    private final RegisterStorageService registerStorageService;

    @PostMapping
    public ResponseEntity<Boolean> registerInstance(@RequestBody RegisterInstanceDTO registerInstanceDTO){
        return ResponseEntity.ok(registerStorageService.register(registerInstanceDTO));
    }
}
