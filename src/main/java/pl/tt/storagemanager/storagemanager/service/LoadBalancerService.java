package pl.tt.storagemanager.storagemanager.service;

import pl.tt.storagemanager.storagemanager.api.InstanceInfo;

public interface LoadBalancerService {

    InstanceInfo getNextInstanceInfo();
}
