package pl.tt.storagemanager.storagemanager.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("file_document")
public class FileDocument {

    @Id
    private UUID id;

    private String name;

    private String extension;

    private Long contentLength;

    private String sha;

    private Map<String, String> metadata;

}
