package pl.tt.storagemanager.storagemanager.api;

import lombok.Builder;

@Builder
public record RegisterInstanceDTO(int id, String host, int port, InstatnceType instatnceType) {
}
