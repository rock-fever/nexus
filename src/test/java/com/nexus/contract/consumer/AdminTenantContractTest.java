package com.nexus.contract.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer-side Pact tests for AdminTenantController.
 * Defines the contract the "admin-console" consumer expects from the "nexus-admin-api" provider.
 * Pact files are written to target/pacts/ and verified by NexusPactProviderTest.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "nexus-admin-api", pactVersion = PactSpecVersion.V3)
class AdminTenantContractTest {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String AUTH_HEADER = "Bearer test-admin-token";

    // --- POST /admin/tenants ---

    @Pact(consumer = "admin-console")
    public RequestResponsePact createTenantPact(PactDslWithProvider builder) {
        return builder
                .given("admin user is authenticated")
                .uponReceiving("create a new tenant")
                    .method("POST")
                    .path("/admin/tenants")
                    .matchHeader("Authorization", "Bearer .+", AUTH_HEADER)
                    .matchHeader("Content-Type", "application/json.*", "application/json")
                    .body(new PactDslJsonBody()
                            .stringType("name", "Acme Corp")
                            .stringType("slug", "acme")
                            .stringType("plan", "FREE"))
                .willRespondWith()
                    .status(201)
                    .matchHeader("Content-Type", "application/json.*", "application/json")
                    .body(new PactDslJsonBody()
                            .uuid("id")
                            .stringType("name", "Acme Corp")
                            .stringType("slug", "acme")
                            .stringType("plan", "FREE")
                            .stringType("status", "ACTIVE"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createTenantPact")
    void testCreateTenant(MockServer mockServer) throws Exception {
        var body = """
                {"name":"Acme Corp","slug":"acme","plan":"FREE"}
                """;
        var response = HTTP.send(
                HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .uri(URI.create(mockServer.getUrl() + "/admin/tenants"))
                        .header("Authorization", AUTH_HEADER)
                        .header("Content-Type", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(201);
    }

    // --- GET /admin/tenants/{id} ---

    @Pact(consumer = "admin-console")
    public RequestResponsePact getTenantPact(PactDslWithProvider builder) {
        return builder
                .given("tenant with id 00000000-0000-0000-0000-000000000001 exists")
                .uponReceiving("get tenant by id")
                    .method("GET")
                    .path("/admin/tenants/00000000-0000-0000-0000-000000000001")
                    .matchHeader("Authorization", "Bearer .+", AUTH_HEADER)
                .willRespondWith()
                    .status(200)
                    .matchHeader("Content-Type", "application/json.*", "application/json")
                    .body(new PactDslJsonBody()
                            .uuid("id")
                            .stringType("name")
                            .stringType("slug")
                            .stringType("plan")
                            .stringType("status"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getTenantPact")
    void testGetTenant(MockServer mockServer) throws Exception {
        var response = HTTP.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(mockServer.getUrl() + "/admin/tenants/00000000-0000-0000-0000-000000000001"))
                        .header("Authorization", AUTH_HEADER)
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
    }

    // --- PATCH /admin/tenants/{id}/status ---

    @Pact(consumer = "admin-console")
    public RequestResponsePact updateStatusPact(PactDslWithProvider builder) {
        return builder
                .given("tenant with id 00000000-0000-0000-0000-000000000001 exists")
                .uponReceiving("update tenant status to SUSPENDED")
                    .method("PATCH")
                    .path("/admin/tenants/00000000-0000-0000-0000-000000000001/status")
                    .query("status=SUSPENDED")
                    .matchHeader("Authorization", "Bearer .+", AUTH_HEADER)
                .willRespondWith()
                    .status(200)
                    .matchHeader("Content-Type", "application/json.*", "application/json")
                    .body(new PactDslJsonBody()
                            .uuid("id")
                            .stringValue("status", "SUSPENDED"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "updateStatusPact")
    void testUpdateStatus(MockServer mockServer) throws Exception {
        var response = HTTP.send(
                HttpRequest.newBuilder()
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .uri(URI.create(mockServer.getUrl()
                                + "/admin/tenants/00000000-0000-0000-0000-000000000001/status?status=SUSPENDED"))
                        .header("Authorization", AUTH_HEADER)
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
    }
}
