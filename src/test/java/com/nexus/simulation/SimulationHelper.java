package com.nexus.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class SimulationHelper {

    static final String BASE_URL = "http://localhost:8081";
    static final HttpClient HTTP = HttpClient.newHttpClient();
    static final ObjectMapper MAPPER = new ObjectMapper();

    public static String login() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("username", "admin", "password", "admin"));
        var response = HTTP.send(
                post("/admin/auth/login", body, null),
                HttpResponse.BodyHandlers.ofString());
        return MAPPER.readTree(response.body()).get("token").asText();
    }

    public static String createTenant(String jwt, String name, String slug, String plan) throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("name", name, "slug", slug, "plan", plan));
        var response = HTTP.send(post("/admin/tenants", body, jwt), HttpResponse.BodyHandlers.ofString());
        return MAPPER.readTree(response.body()).get("id").asText();
    }

    public static String createRoute(String jwt, String tenantId, String pattern,
                                     String target, String method, String authType) throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "pathPattern", pattern, "targetUrl", target,
                "method", method, "authType", authType, "active", true));
        var response = HTTP.send(
                post("/admin/tenants/" + tenantId + "/routes", body, jwt),
                HttpResponse.BodyHandlers.ofString());
        return MAPPER.readTree(response.body()).get("id").asText();
    }

    public static String createApiKey(String jwt, String tenantId) throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("name", "load-test-key"));
        var response = HTTP.send(
                post("/admin/tenants/" + tenantId + "/keys", body, jwt),
                HttpResponse.BodyHandlers.ofString());
        return MAPPER.readTree(response.body()).get("rawKey").asText();
    }

    private static HttpRequest post(String path, String body, String jwt) {
        var builder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json");
        if (jwt != null) builder.header("Authorization", "Bearer " + jwt);
        return builder.build();
    }
}
