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
 * Consumer-side Pact tests for AdminRouteController.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "nexus-admin-api", pactVersion = PactSpecVersion.V3)
class AdminRouteContractTest {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String AUTH_HEADER = "Bearer test-admin-token";
    private static final String TENANT_ID = "00000000-0000-0000-0000-000000000001";
    private static final String ROUTE_ID   = "00000000-0000-0000-0000-000000000002";

    // --- POST /admin/tenants/{id}/routes ---

    @Pact(consumer = "admin-console")
    public RequestResponsePact createRoutePact(PactDslWithProvider builder) {
        return builder
                .given("tenant with id 00000000-0000-0000-0000-000000000001 exists")
                .uponReceiving("create a route for tenant")
                    .method("POST")
                    .path("/admin/tenants/" + TENANT_ID + "/routes")
                    .matchHeader("Authorization", "Bearer .+", AUTH_HEADER)
                    .matchHeader("Content-Type", "application/json.*", "application/json")
                    .body(new PactDslJsonBody()
                            .stringType("pathPattern", "/api/**")
                            .stringType("targetUrl", "http://backend")
                            .stringType("method", "ANY")
                            .stringType("authType", "NONE")
                            .booleanType("active", true))
                .willRespondWith()
                    .status(201)
                    .matchHeader("Content-Type", "application/json.*", "application/json")
                    .body(new PactDslJsonBody()
                            .uuid("id")
                            .uuid("tenantId")
                            .stringType("pathPattern", "/api/**")
                            .stringType("targetUrl", "http://backend")
                            .stringType("method", "ANY")
                            .stringType("authType", "NONE")
                            .booleanType("active", true))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createRoutePact")
    void testCreateRoute(MockServer mockServer) throws Exception {
        var body = """
                {"pathPattern":"/api/**","targetUrl":"http://backend","method":"ANY","authType":"NONE","active":true}
                """;
        var response = HTTP.send(
                HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .uri(URI.create(mockServer.getUrl() + "/admin/tenants/" + TENANT_ID + "/routes"))
                        .header("Authorization", AUTH_HEADER)
                        .header("Content-Type", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(201);
    }

    // --- DELETE /admin/tenants/{id}/routes/{routeId} ---

    @Pact(consumer = "admin-console")
    public RequestResponsePact deleteRoutePact(PactDslWithProvider builder) {
        return builder
                .given("tenant with id 00000000-0000-0000-0000-000000000001 has route 00000000-0000-0000-0000-000000000002")
                .uponReceiving("delete a route")
                    .method("DELETE")
                    .path("/admin/tenants/" + TENANT_ID + "/routes/" + ROUTE_ID)
                    .matchHeader("Authorization", "Bearer .+", AUTH_HEADER)
                .willRespondWith()
                    .status(204)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "deleteRoutePact")
    void testDeleteRoute(MockServer mockServer) throws Exception {
        var response = HTTP.send(
                HttpRequest.newBuilder()
                        .DELETE()
                        .uri(URI.create(mockServer.getUrl()
                                + "/admin/tenants/" + TENANT_ID + "/routes/" + ROUTE_ID))
                        .header("Authorization", AUTH_HEADER)
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(204);
    }
}
