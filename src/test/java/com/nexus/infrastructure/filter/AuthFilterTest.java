package com.nexus.infrastructure.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nexus.AbstractIntegrationTest;
import com.nexus.api.dto.CreateApiKeyRequest;
import com.nexus.api.dto.CreateRouteRequest;
import com.nexus.api.dto.CreateTenantRequest;
import com.nexus.api.dto.TenantResponse;
import com.nexus.application.service.ApiKeyService;
import com.nexus.application.service.RouteCommandService;
import com.nexus.application.service.TenantService;
import com.nexus.domain.model.enums.AuthType;
import com.nexus.domain.model.enums.Plan;

class AuthFilterTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private RouteCommandService routeCommandService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Test
    void missingTenantSlugHeader_returns400() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/some/path", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unknownTenantSlug_returns404() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Slug", "nonexistent-" + UUID.randomUUID());

        ResponseEntity<String> response = restTemplate.exchange(
                "/some/path", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void invalidApiKey_returns401() {
        TenantResponse tenant = tenantService.createTenant(CreateTenantRequest.builder()
                .name("Auth Test")
                .slug("auth-test-" + UUID.randomUUID())
                .plan(Plan.PRO)
                .build());

        routeCommandService.createRoute(tenant.getId(), CreateRouteRequest.builder()
                .pathPattern("/secure/**")
                .targetUrl("http://backend:8080")
                .method("ANY")
                .stripPrefix(false)
                .authType(AuthType.API_KEY)
                .retryAttempts(0)
                .build());

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Slug", tenant.getSlug());
        headers.set("X-API-Key", "wrong-key");

        ResponseEntity<String> response = restTemplate.exchange(
                "/secure/resource", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validApiKey_passesAuthFilter() {
        TenantResponse tenant = tenantService.createTenant(CreateTenantRequest.builder()
                .name("Valid Key Tenant")
                .slug("valid-key-" + UUID.randomUUID())
                .plan(Plan.PRO)
                .build());

        routeCommandService.createRoute(tenant.getId(), CreateRouteRequest.builder()
                .pathPattern("/open/**")
                .targetUrl("http://backend:8080")
                .method("ANY")
                .stripPrefix(false)
                .authType(AuthType.API_KEY)
                .retryAttempts(0)
                .build());

        String rawKey = apiKeyService.createApiKey(tenant.getId(),
                CreateApiKeyRequest.builder().name("test-key").build()).getRawKey();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Slug", tenant.getSlug());
        headers.set("X-API-Key", rawKey);

        ResponseEntity<String> response = restTemplate.exchange(
                "/open/resource", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        // Auth passed — downstream will 502 because backend isn't real, but not 401
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
