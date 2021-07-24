package lupogryph.exemple.quarkus;

import io.smallrye.mutiny.Uni;
import org.infinispan.manager.EmbeddedCacheManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/exemple")
public class App {

    @Inject
    ExempleObjectCacheService exempleObjectCache;

    @Inject
    EmbeddedCacheManager cacheManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ExempleObject> get() {
        return exempleObjectCache.get();
    }

    @Path("/key/{key}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ExempleObject getWithKey(@PathParam("key") Integer key) {
        return exempleObjectCache.get(key);
    }

    @Path("/nom/{nom}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ExempleObject getByNom(@PathParam("nom") String nom) {
        return exempleObjectCache.findByNom(nom);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExempleObject create(ExempleObject exempleObject) {
        return exempleObjectCache.put(exempleObject);
    }

    @Path("/key/{key}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExempleObject replace(@PathParam("key") Integer key, ExempleObject exempleObject) {
        return exempleObjectCache.update(key, exempleObject);
    }

    @Path("/key/{key}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String delete(@PathParam("key") Integer key) {
        exempleObjectCache.delete(key);
        return "exempleObject in cache with key : " + key + " deleted";
    }

    @Path("/cache/info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> cacheInfoJson() {
        return Uni.createFrom().item(cacheManager.getCacheManagerInfo().toJson().toPrettyString())
                .map(i -> Response.ok(i, MediaType.APPLICATION_JSON).build())
        ;
    }

}