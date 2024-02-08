package pl.tt.storagemanager.storagemanager.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.tt.storagemanager.storagemanager.api.RegisterInstanceDTO;
import pl.tt.storagemanager.storagemanager.holder.StorageInstanceHolder;

@Service
@AllArgsConstructor
class RegisterStorageServiceImpl implements RegisterStorageService {

    private final StorageInstanceHolder storageInstanceHolder;

    @Override
    public Boolean register(RegisterInstanceDTO registerInstanceDTO) {
        return storageInstanceHolder.register(registerInstanceDTO);
    }
}
