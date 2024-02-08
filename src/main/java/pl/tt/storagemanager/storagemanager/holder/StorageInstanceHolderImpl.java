package pl.tt.storagemanager.storagemanager.holder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.tt.storagemanager.storagemanager.api.RegisterInstanceDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
final class StorageInstanceHolderImpl implements StorageInstanceHolder {

    private final Map<StorageInstanceKey, StorageInstanceInfo> STORAGE_INSTANCES = new ConcurrentHashMap<>();

    @Override
    public Boolean register(RegisterInstanceDTO registerInstanceDTO) {
        var storageInstatnceKey = new StorageInstanceKey(registerInstanceDTO.id(), registerInstanceDTO.instatnceType());
        var currentInstanceInfo = STORAGE_INSTANCES.get(storageInstatnceKey);
        var newInstanceInfo = new StorageInstanceInfo(registerInstanceDTO.host(), registerInstanceDTO.port());

        if (isInstanceAlreadyRegistered(currentInstanceInfo, newInstanceInfo)) {
            return Boolean.FALSE;
        }

        STORAGE_INSTANCES.put(storageInstatnceKey, newInstanceInfo);

        log.debug("New storage instance registered id: {}, instance type: {}, host: {}, port: {}",
                registerInstanceDTO.id(), registerInstanceDTO.instatnceType(), registerInstanceDTO.host(), registerInstanceDTO.port());

        return Boolean.TRUE;
    }

    @Override
    public Map<StorageInstanceKey, StorageInstanceInfo> getStorageInstances() {
        return Map.copyOf(STORAGE_INSTANCES);
    }

    @Override
    public void unregister(StorageInstanceKey storageInstanceKey) {
        STORAGE_INSTANCES.remove(storageInstanceKey);

        log.debug("Unregister storage instance id: {}, instance type: {}",
                storageInstanceKey.id(), storageInstanceKey.instatnceType());
    }


    private boolean isInstanceAlreadyRegistered(StorageInstanceInfo currentInstanceInfo, StorageInstanceInfo newInstanceInfo) {
        return currentInstanceInfo != null && currentInstanceInfo.equals(newInstanceInfo);
    }
}
