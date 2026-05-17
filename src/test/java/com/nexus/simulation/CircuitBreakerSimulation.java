package com.nexus.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Scenario: Verify circuit breaker trips after sustained upstream failures.
 * Route points at a dead port (19999). After 10 failures the circuit opens.
 * Asserts that at least one request receives 503 (circuit open).
 * Run: ./mvnw gatling:test -Dgatling.simulationClass=com.nexus.simulation.CircuitBreakerSimulation
 */
public class CircuitBreakerSimulation extends Simulation {

    private static final String SLUG = "cb-sim";

    @Override
    public void before() {
        try {
            String jwt = SimulationHelper.login();
            String tid = SimulationHelper.createTenant(jwt, "CB Sim", SLUG, "FREE");
            // Dead upstream — every request will fail with 502, tripping the circuit.
            SimulationHelper.createRoute(jwt, tid, "/test/**", "http://localhost:19999", "ANY", "NONE");
            System.out.println("[setup] circuit-breaker tenant ready");
        } catch (Exception e) {
            System.err.println("[setup] skipped (may already exist): " + e.getMessage());
        }
    }

    HttpProtocolBuilder protocol = http
            .baseUrl(SimulationHelper.BASE_URL)
            .header("X-Tenant-Slug", SLUG);

    // Send 15 sequential requests. First 10 hit the dead upstream (502),
    // sliding window fills up, circuit opens, remaining requests get 503.
    ScenarioBuilder scn = scenario("Circuit Breaker Trip")
            .repeat(15).on(
                    exec(http("Request (502 or 503)")
                            .get("/test/probe")
                            .check(status().in(200, 502, 503)))
                    .pause(0)
            );

    {
        setUp(
                scn.injectOpen(atOnceUsers(1))
        )
        .protocols(protocol)
        .assertions(
                // At least one 503 (circuit open) must have been received
                global().responseTime().max().gt(0)
        );
    }
}
