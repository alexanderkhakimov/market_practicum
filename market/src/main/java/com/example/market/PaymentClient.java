package com.example.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PaymentClient {

    private final WebClient webClient;

    @Autowired
    public PaymentClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    public Mono<String> processPayment(@RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient, Long orderId) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        return webClient.post()
                .uri("/payments/process/{id}", orderId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class);
    }
}
