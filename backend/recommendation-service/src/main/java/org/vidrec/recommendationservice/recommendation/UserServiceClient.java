package org.vidrec.recommendationservice.recommendation;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Reads the follow graph from user-service at scoring time so recommendations
 * can factor in what the people a user follows are watching. Mirrors
 * {@link SvdClient}: short timeouts and a graceful empty-list fallback so a
 * user-service outage simply drops the social signal instead of failing.
 */
@Slf4j
@Service
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${USER_SERVICE_URL:${user.service.url:http://user-service:8081}}") String userServiceUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
            .baseUrl(userServiceUrl)
            .requestFactory(requestFactory)
            .build();
    }

    public List<UUID> getFollowing(UUID userId) {
        try {
            List<UUID> following = restClient.get()
                .uri("/users/{userId}/following", userId.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<List<UUID>>() {});
            return following == null ? List.of() : following;
        } catch (Exception ex) {
            log.warn("user-service unavailable; dropping social signal for userId={}", userId, ex);
            return List.of();
        }
    }
}
