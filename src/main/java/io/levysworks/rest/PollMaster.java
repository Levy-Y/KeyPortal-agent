package io.levysworks.rest;

import jakarta.ws.rs.HeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;

@RegisterRestClient(configKey = "poll")
@Path("/api/v1")
public interface PollMaster {

    @GET
    @Path("/poll")
    List<String> pollPublicKeys(@HeaderParam("X-Agent-Name") String agentName, @HeaderParam("X-Poll-Key") String pollKey);
}
