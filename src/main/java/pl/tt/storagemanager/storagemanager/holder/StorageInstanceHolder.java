package pl.tt.storagemanager.storagemanager.holder;

import pl.tt.storagemanager.storagemanager.api.RegisterInstanceDTO;

import java.util.Map;

public interface StorageInstanceHolder {
    Boolean register(RegisterInstanceDTO registerInstanceDTO);

    Map<StorageInstanceKey, StorageInstanceInfo> getStorageInstances();

    void unregister(StorageInstanceKey storageInstanceKey);
}
