package pl.tt.storagemanager.storagemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        return new RestTemplate(requestFactory);
    }
}
