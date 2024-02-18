package pl.tt.storagemanager.storagemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import pl.tt.storagemanager.storagemanager.api.InstanceInfo;
import pl.tt.storagemanager.storagemanager.config.RestTemplateConfig;
import pl.tt.storagemanager.storagemanager.model.FileDocument;
import pl.tt.storagemanager.storagemanager.repository.FileDocumentRepository;

import java.io.IOException;
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
    private final RestTemplateConfig restTemplateConfig;
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
        var requestEntity = getRequestEntity(file, metadata);

        if (instanceInfo != null) {
            String url = HTTP_UPLOAD_PATTERN_URL.formatted(instanceInfo.host(), instanceInfo.port());
            response = restTemplateConfig.restTemplate().exchange(url, HttpMethod.POST, requestEntity, UUID.class);
        }

        return response;
    }

    private static HttpEntity<LinkedMultiValueMap<String, Object>> getRequestEntity(MultipartFile file,
                                                                                    String metadata) throws IOException {
        byte[] fileBytes = file.getBytes();
        //TODO przerobić aby wszystkie headery się przepisywały


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, String> pdfHeaderMap = new LinkedMultiValueMap<>();
        pdfHeaderMap.add("Content-disposition", "form-data; name=file; filename=" + file.getOriginalFilename());
        pdfHeaderMap.add("Content-type", file.getContentType());
        var fileHttpEntity = new HttpEntity<>(fileBytes, pdfHeaderMap);

        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add("file", fileHttpEntity);
        multipartReqMap.add("metadata", metadata);


        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity = new HttpEntity<>(multipartReqMap, headers);
        return reqEntity;
    }
}
