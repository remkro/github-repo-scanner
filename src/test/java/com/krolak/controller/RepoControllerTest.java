package com.krolak.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureWireMock
@TestPropertySource(properties = {
        "app.github.api.base-url=http://localhost:${wiremock.server.port}"
})
class RepoControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Value("${app.security.api-key}")
    private String apiKey;

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @Test
    void testGetUserRepositories_shouldReturnRepositoryInfo() {
        WireMock.stubFor(get(urlEqualTo("/users/testUser/repos?per_page=5&page=0"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"repo1\",\"owner\":{\"login\":\"testUser\"},\"fork\":false}]")));

        WireMock.stubFor(get(urlEqualTo("/repos/testUser/repo1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"main\",\"commit\":{\"sha\":\"abc123\"}}]")));

        webTestClient.get().uri(uriBuilder ->
                        uriBuilder.path("/github/repos/{username}")
                                .queryParam("page", 0)
                                .queryParam("size", 5)
                                .build("testUser"))
                .header("x-api-key", apiKey) // Add the x-api-key header
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("testUser")
                .jsonPath("$.repositories[0].repositoryName").isEqualTo("repo1")
                .jsonPath("$.repositories[0].branches[0].name").isEqualTo("main")
                .jsonPath("$.repositories[0].branches[0].lastCommitSha").isEqualTo("abc123")
                .jsonPath("$.elements").isEqualTo(1)
                .jsonPath("$.pageNumber").isEqualTo(0)
                .jsonPath("$.pageSize").isEqualTo(5);
    }

    @Test
    void testGetUserRepositories_shouldReturnNotFound_forNonExistentUser() {
        WireMock.stubFor(get(urlEqualTo("/users/nonExistentUser/repos?per_page=5&page=0"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Not Found\",\"documentation_url\":\"https://developer.github.com/v3\"}")));

        webTestClient.get().uri(uriBuilder ->
                        uriBuilder.path("/github/repos/{username}")
                                .queryParam("page", 0)
                                .queryParam("size", 5)
                                .build("nonExistentUser"))
                .header("x-api-key", apiKey)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("User doesn't exist: nonExistentUser");
    }

    @Test
    void testGetUserRepositories_shouldReturnInternalServerError_onGitHubApiException() {
        WireMock.stubFor(get(urlEqualTo("/users/testUser/repos?per_page=5&page=0"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Internal Server Error\",\"documentation_url\":\"https://developer.github.com/v3\"}")));

        webTestClient.get().uri(uriBuilder ->
                        uriBuilder.path("/github/repos/{username}")
                                .queryParam("page", 0)
                                .queryParam("size", 5)
                                .build("testUser"))
                .header("x-api-key", apiKey)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.message").isEqualTo("GitHub API failed: 500 Server Error: \"{\"message\":\"Internal Server Error\",\"documentation_url\":\"https://developer.github.com/v3\"}\"");
    }
}