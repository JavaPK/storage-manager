package pl.tt.storagemanager.storagemanager.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pl.tt.storagemanager.storagemanager.holder.StorageInstanceHolder;
import pl.tt.storagemanager.storagemanager.holder.StorageInstanceInfo;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstanceHeathCheckScheduler {

    @Value("${storage.protocol}")
    private String storageProtocol;

    @Value("${storage.endpoint-url.health-check}")
    private String healthCheckEndpointUrl;

    private final String HEALTH_CHECK_URL_PATTERN = "%s://%s:%d%s";

    private final RestTemplate restTemplate;
    private final StorageInstanceHolder storageInstanceHolder;

    @Scheduled(fixedRateString = "${storagemanager.health-check.heartbeat}")
    public void checkInstanceHealth() {
        var storageInstancesMap = storageInstanceHolder.getStorageInstances();

        for (var entry : storageInstancesMap.entrySet()) {
            try {
                var response = restTemplate.getForEntity(getStorageUrl(entry.getValue()), Void.class);

                if(!response.getStatusCode().is2xxSuccessful()){
                    log.debug("Storage instance id: {}, intance type : {} has been unregistered ", entry.getKey().id(), entry.getKey().instatnceType());
                    storageInstanceHolder.unregister(entry.getKey());
                }
            } catch (RestClientException e) {
                log.error("{}. Storage instance id: {}, intance type : {} has been unregistered ", e.getMessage(), entry.getKey().id(), entry.getKey().instatnceType());
                storageInstanceHolder.unregister(entry.getKey());
            }
        }
    }

    private String getStorageUrl(StorageInstanceInfo value) {
        return HEALTH_CHECK_URL_PATTERN.formatted(storageProtocol, value.host(), value.port(), healthCheckEndpointUrl);
    }
}
