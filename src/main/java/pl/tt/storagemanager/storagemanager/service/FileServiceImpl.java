package pl.tt.storagemanager.storagemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService{

    private final LoadBalancerService loadBalancerService;

    @Override
    public UUID upload(MultipartFile file, String metadata) throws IOException {
        var instanceInfo = loadBalancerService.getNextInstanceInfo();
        return null;
    }
}
