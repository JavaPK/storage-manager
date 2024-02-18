package pl.tt.storagemanager.storagemanager.service;

import lombok.RequiredArgsConstructor;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pl.tt.storagemanager.storagemanager.api.InstanceInfo;
import pl.tt.storagemanager.storagemanager.config.RestTemplateConfig;
import pl.tt.storagemanager.storagemanager.model.FileDocument;
import pl.tt.storagemanager.storagemanager.repository.FileDocumentRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;

@Transactional
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final LoadBalancerService loadBalancerService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FileDocumentRepository fileDocumentRepository;

    public static final String HTTP_UPLOAD_PATTERN_URL = "http://%s:%d/api/storage/upload";

    @Override
    public ResponseEntity<UUID> forwardUpload(MultipartFile file, String metadata) throws IOException {
        Map<String, String> metadataMap = new HashMap<>(objectMapper.readValue(metadata, Map.class));

        UUID id  = UUID.randomUUID();
        metadataMap.put("id", id.toString());

        InstanceInfo instanceInfo = loadBalancerService.getNextInstanceInfo();
        ResponseEntity<UUID> response = forwardRequest(file, objectMapper.writeValueAsString(metadataMap), instanceInfo);

        if(response.getStatusCode().is2xxSuccessful()){

            int lastIndexOf = file.getOriginalFilename().lastIndexOf(".");
            String extension = file.getOriginalFilename().substring(lastIndexOf);
            String fileName = file.getOriginalFilename().substring(0, lastIndexOf);
            // sha powinno być zwracane ze storage
            String sha = Hashing.sha512().hashBytes(file.getBytes()).toString();

            FileDocument fileDocument = FileDocument.builder()
                    .id(id)
                    .name(fileName)
                    .extension(extension)
                    .contentLength(file.getSize())
                    .sha(sha)
                    .metadata(metadataMap)
                    .build();

            fileDocumentRepository.save(fileDocument);
        }

        return response;
    }

    private ResponseEntity<UUID> forwardRequest(MultipartFile file, String metadata, InstanceInfo instanceInfo) throws IOException {
        //TODO if not null

        ResponseEntity<UUID> response = ResponseEntity.internalServerError().build();
        final UUID uuid = UUID.randomUUID();
        if (instanceInfo != null) {
            final InputStream inputStream = file.getInputStream();
            final RequestCallback requestCallback = new RequestCallback() {
                @Override
                public void doWithRequest(final ClientHttpRequest request) throws IOException {
                    request.getHeaders().add("Content-type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
                    request.getHeaders().add("metadata", objectMapper.writeValueAsString(Map.of("id", uuid.toString())));
                    IOUtils.copy(inputStream, request.getBody());
                }
            };

            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            restTemplate.setRequestFactory(requestFactory);
            final HttpMessageConverterExtractor responseExtractor =
                    new HttpMessageConverterExtractor(UUID.class, restTemplate.getMessageConverters());

            String url = HTTP_UPLOAD_PATTERN_URL.formatted(instanceInfo.host(), instanceInfo.port());
            restTemplate.execute(url, HttpMethod.POST, requestCallback, responseExtractor);
        }

        return ResponseEntity.ok(uuid);
    }
}
