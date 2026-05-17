package com.nexus.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Scenario: Sustained load on a proxied route.
 * Ramp to 20 users over 30s, hold for 2 minutes.
 * Asserts p95 < 3s and error rate < 5%.
 * Run: ./mvnw gatling:test -Dgatling.simulationClass=com.nexus.simulation.BaselineThroughputSimulation
 */
public class BaselineThroughputSimulation extends Simulation {

    private static final String SLUG = "baseline-load";

    @Override
    public void before() {
        try {
            String jwt = SimulationHelper.login();
            String tid = SimulationHelper.createTenant(jwt, "Baseline Load", SLUG, "PRO");
            SimulationHelper.createRoute(jwt, tid, "/anything/**", "https://httpbin.org", "ANY", "NONE");
            System.out.println("[setup] baseline tenant ready");
        } catch (Exception e) {
            System.err.println("[setup] skipped (may already exist): " + e.getMessage());
        }
    }

    HttpProtocolBuilder protocol = http
            .baseUrl(SimulationHelper.BASE_URL)
            .header("X-Tenant-Slug", SLUG);

    ScenarioBuilder scn = scenario("Baseline Throughput")
            .exec(http("Proxy GET").get("/anything/hello").check(status().not(500)));

    {
        setUp(
                scn.injectOpen(
                        rampUsers(10).during(30),
                        constantUsersPerSec(20).during(120)
                )
        )
        .protocols(protocol)
        .assertions(
                global().responseTime().percentile(95).lt(3000),
                global().failedRequests().percent().lt(5.0)
        );
    }
}
