package isis.ISISCapitalist.classes;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("generic")
public class WebService {

    Services services;

    public WebService() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) // pb affichage XML
    public Response getWorld() {
        return Response.ok(services.getWorld()).build();
    }
}
