package com.nexus.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Scenario: Concurrent load from 3 tenants on different plans.
 * FREE  (60/min)  → hits 429 after threshold.
 * PRO   (600/min) → never hits 429 under this load.
 * ENTERPRISE (unlimited) → never hits 429.
 * Verifies no cross-tenant rate-limit bleed.
 * Run: ./mvnw gatling:test -Dgatling.simulationClass=com.nexus.simulation.MultiTenantIsolationSimulation
 */
public class MultiTenantIsolationSimulation extends Simulation {

    private static final String FREE_SLUG       = "mt-free";
    private static final String PRO_SLUG        = "mt-pro";
    private static final String ENTERPRISE_SLUG = "mt-enterprise";

    @Override
    public void before() {
        try {
            String jwt = SimulationHelper.login();

            String freeId = SimulationHelper.createTenant(jwt, "MT Free", FREE_SLUG, "FREE");
            SimulationHelper.createRoute(jwt, freeId, "/anything/**", "https://httpbin.org", "ANY", "NONE");

            String proId = SimulationHelper.createTenant(jwt, "MT Pro", PRO_SLUG, "PRO");
            SimulationHelper.createRoute(jwt, proId, "/anything/**", "https://httpbin.org", "ANY", "NONE");

            String entId = SimulationHelper.createTenant(jwt, "MT Enterprise", ENTERPRISE_SLUG, "ENTERPRISE");
            SimulationHelper.createRoute(jwt, entId, "/anything/**", "https://httpbin.org", "ANY", "NONE");

            System.out.println("[setup] multi-tenant isolation tenants ready");
        } catch (Exception e) {
            System.err.println("[setup] skipped (may already exist): " + e.getMessage());
        }
    }

    HttpProtocolBuilder protocol = http.baseUrl(SimulationHelper.BASE_URL);

    // FREE: 80 req in 60s — exceeds 60/min cap, will produce 429s
    ScenarioBuilder freeScn = scenario("FREE tenant")
            .exec(http("FREE request")
                    .get("/anything/hello")
                    .header("X-Tenant-Slug", FREE_SLUG)
                    .check(status().in(200, 429)));

    // PRO: 80 req in 60s — well within 600/min cap
    ScenarioBuilder proScn = scenario("PRO tenant")
            .exec(http("PRO request")
                    .get("/anything/hello")
                    .header("X-Tenant-Slug", PRO_SLUG)
                    .check(status().is(200)));

    // ENTERPRISE: 80 req in 60s — unlimited
    ScenarioBuilder entScn = scenario("ENTERPRISE tenant")
            .exec(http("ENTERPRISE request")
                    .get("/anything/hello")
                    .header("X-Tenant-Slug", ENTERPRISE_SLUG)
                    .check(status().is(200)));

    {
        setUp(
                freeScn.injectOpen(constantUsersPerSec(1.4).during(60)),
                proScn.injectOpen(constantUsersPerSec(1.4).during(60)),
                entScn.injectOpen(constantUsersPerSec(1.4).during(60))
        )
        .protocols(protocol)
        .assertions(
                // PRO and ENTERPRISE must have zero failures
                details("PRO request").failedRequests().count().is(0L),
                details("ENTERPRISE request").failedRequests().count().is(0L)
        );
    }
}
