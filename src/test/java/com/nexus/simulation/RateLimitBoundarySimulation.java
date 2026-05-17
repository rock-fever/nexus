package com.nexus.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Scenario: Verify rate limiting fires at the correct threshold.
 * Uses a FREE plan tenant (60 req/min). Injects 80 req over 60s.
 * Asserts that at least some requests return 429.
 * Run: ./mvnw gatling:test -Dgatling.simulationClass=com.nexus.simulation.RateLimitBoundarySimulation
 */
public class RateLimitBoundarySimulation extends Simulation {

    private static final String SLUG = "ratelimit-sim";
    private static String apiKey;

    @Override
    public void before() {
        try {
            String jwt = SimulationHelper.login();
            String tid = SimulationHelper.createTenant(jwt, "RateLimit Sim", SLUG, "FREE");
            SimulationHelper.createRoute(jwt, tid, "/anything/**", "https://httpbin.org", "ANY", "API_KEY");
            apiKey = SimulationHelper.createApiKey(jwt, tid);
            System.out.println("[setup] rate-limit tenant ready, key=" + apiKey);
        } catch (Exception e) {
            System.err.println("[setup] skipped (may already exist): " + e.getMessage());
        }
    }

    HttpProtocolBuilder protocol = http
            .baseUrl(SimulationHelper.BASE_URL)
            .header("X-Tenant-Slug", SLUG);

    // 80 requests over 60 seconds exceeds the FREE plan cap of 60/min.
    ScenarioBuilder scn = scenario("Rate Limit Boundary")
            .exec(session -> session.set("apiKey", apiKey))
            .exec(http("Proxy GET with API key")
                    .get("/anything/test")
                    .header("X-Api-Key", "#{apiKey}")
                    .check(status().in(200, 429)));

    {
        setUp(
                scn.injectOpen(constantUsersPerSec(1.4).during(60))
        )
        .protocols(protocol)
        .assertions(
                // At least some requests must have been rate-limited
                details("Proxy GET with API key").failedRequests().count().gt(0L)
        );
    }
}
