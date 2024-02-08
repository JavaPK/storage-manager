package pl.tt.storagemanager.storagemanager.service;

import pl.tt.storagemanager.storagemanager.api.RegisterInstanceDTO;

public interface RegisterStorageService {
    Boolean register(RegisterInstanceDTO registerInstanceDTO);
}
