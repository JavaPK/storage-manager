package pl.tt.storagemanager.storagemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.tt.storagemanager.storagemanager.api.InstanceInfo;
import pl.tt.storagemanager.storagemanager.api.InstatnceType;
import pl.tt.storagemanager.storagemanager.holder.StorageInstanceHolder;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoadBalancerServiceImpl implements LoadBalancerService{

    private static final AtomicInteger CURRENT_ITERATION = new AtomicInteger(1);
    private final StorageInstanceHolder storageInstanceHolder;

    @Override
    public InstanceInfo getNextInstanceInfo() {
        var instanceMap = storageInstanceHolder.getStorageInstances();
        var entryList = instanceMap.entrySet().stream()
                .filter(entry -> entry.getKey().instatnceType() == InstatnceType.MAIN)
                .sorted(Comparator.comparing(entry -> entry.getKey().id()))
                .collect(Collectors.toList());
        var index = CURRENT_ITERATION.getAndIncrement() % entryList.size();
        var entryValue = entryList.get(index).getValue();
        var entryKey = entryList.get(index).getKey();
        return InstanceInfo.builder()
                .port(entryValue.port())
                .host(entryValue.host())
                .id(entryKey.id())
                .build();
    }
}
