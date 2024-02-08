package pl.tt.storagemanager.storagemanager.api;

import lombok.Builder;

@Builder
public record InstanceInfo(int id, String host, int port) {
}
