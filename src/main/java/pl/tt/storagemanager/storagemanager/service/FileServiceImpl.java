package pl.tt.storagemanager.storagemanager.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.tt.storagemanager.storagemanager.api.InstanceInfo;
import pl.tt.storagemanager.storagemanager.api.InstatnceType;
import pl.tt.storagemanager.storagemanager.exception.AnyStorageInstanceNotAvailableException;
import pl.tt.storagemanager.storagemanager.holder.StorageInstanceHolder;
import pl.tt.storagemanager.storagemanager.holder.StorageInstanceKey;
import pl.tt.storagemanager.storagemanager.model.FileDocument;
import pl.tt.storagemanager.storagemanager.repository.FileDocumentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

	private static final String HTTP_UPLOAD_PATTERN_URL = "http://%s:%d/api/storage/upload";
	private static final String ID_METADATA_KEY = "id";
	private static final String EMPTY_STRING = "";
	private static final String CONTENT_TYPE_HEADER = "Content-type";
	private static final String METADATA_HEADER = "X-METADATA";
	private static final HttpStatusCode HTTP_STATUS_CODE_NOT_FOUND = HttpStatusCode.valueOf(404);
	private final LoadBalancerService loadBalancerService;
	private final StorageInstanceHolder storageInstanceHolder;
	private final ObjectMapper objectMapper;
	private final FileDocumentRepository fileDocumentRepository;
	private final RestTemplate restTemplate;

	@Override
	public ResponseEntity<UUID> forwardUpload(MultipartFile multipartFile, String metadataString) throws IOException {
		var metadataMap = new HashMap<String, String>(readMetadataFromString(metadataString));
		var id = UUID.randomUUID();
		var instanceInfo = loadBalancerService.getNextInstanceInfo();
		var response = forwardRequest(multipartFile, id, instanceInfo);

		// save temporary file
		File tempFile = File.createTempFile("tmp", "random");
		saveRequestInputStreamToTempFile(multipartFile, tempFile);

		if (response.getStatusCode().is2xxSuccessful()) {
			fileDocumentRepository.save(prepareFileDocument(multipartFile, metadataMap, id));
		}

		if(HTTP_STATUS_CODE_NOT_FOUND == response.getStatusCode()){
			storageInstanceHolder.unregister(new StorageInstanceKey(instanceInfo.id(), InstatnceType.MAIN));
			instanceInfo = loadBalancerService.getNextInstanceInfo();
			response = forwardRequest(multipartFile, id, instanceInfo);
		}

		tempFile.delete();
		return response;
	}

	private void saveRequestInputStreamToTempFile(MultipartFile multipartFile, File tempFile) {

	}

	private Map<String, String> readMetadataFromString(String metadataString) {
		try {
			return objectMapper.readValue(metadataString, Map.class);
		} catch (Exception e) {
			log.debug("Unable to map metadataString to map.");
			return Maps.newHashMap();
		}
	}

	private ResponseEntity<UUID> forwardRequest(MultipartFile file, UUID id, InstanceInfo instanceInfo)
			throws IOException {
		if (instanceInfo != null) {
			final var inputStream = file.getInputStream();
			final RequestCallback requestCallback = request -> {
				request.getHeaders().add(CONTENT_TYPE_HEADER, MediaType.APPLICATION_OCTET_STREAM_VALUE);
				request.getHeaders()
						.add(METADATA_HEADER, objectMapper.writeValueAsString(Map.of(ID_METADATA_KEY, id.toString())));
				IOUtils.copy(inputStream, request.getBody());
			};

			final var responseExtractor =
					new HttpMessageConverterExtractor(UUID.class, restTemplate.getMessageConverters());

			String url = HTTP_UPLOAD_PATTERN_URL.formatted(instanceInfo.host(), instanceInfo.port());
			restTemplate.execute(url, HttpMethod.POST, requestCallback, responseExtractor);
			return ResponseEntity.ok(id);
		} else {
			throw new AnyStorageInstanceNotAvailableException();
		}
	}

	private FileDocument prepareFileDocument(MultipartFile file, Map<String, String> metadataMap, UUID id) {
		int lastIndexOf = file.getOriginalFilename().lastIndexOf(".");
		String extension = lastIndexOf != -1 ? file.getOriginalFilename().substring(lastIndexOf) : EMPTY_STRING;
		String fileName = lastIndexOf != -1 ? file.getOriginalFilename().substring(0, lastIndexOf) : EMPTY_STRING;

		return FileDocument.builder()
				.id(id)
				.name(fileName)
				.extension(extension)
				.contentLength(file.getSize())
				.metadata(metadataMap)
				.build();
	}
}
