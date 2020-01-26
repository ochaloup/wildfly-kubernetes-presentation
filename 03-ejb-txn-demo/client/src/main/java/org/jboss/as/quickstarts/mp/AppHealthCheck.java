package org.jboss.as.quickstarts.mp;

import org.eclipse.microprofile.health.HealthCheckResponse;

// @Liveness
// @ApplicationScoped
public class AppHealthCheck
        // implements HealthCheck
{
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("OK");
    }
}
