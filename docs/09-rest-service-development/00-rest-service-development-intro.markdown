# Rest Service Development [](id=rest-service-development)

HyperIoTBase project contains the HyperIoTBase-rest-service module. 
In this module there are the base classes to automatically expose REST services for CRUD operations for entities (HyperIoTBaseEntityRestApi), to register the module as an OSGi bundle (HyperIoTBaseRestActivator), and to automatically generate Swagger documentation of the services(HyperIoTSwaggerIntentProvider).

The REST services layer of HyperIoT Framework is based on Apache CFX.

HyperIoT Framework, as we have seen, decouples the service layer (HyperIoTBase-service module) from the REST service layer (HyperIoTBase-rest-service module); this makes the REST service layer non-mandatory allowing projects to be created without the *rest-service module. At any time it will be possible to insert such a module using the yo hyperiot:new-rest-module generator command.

The *rest-service module exposes through a *RestApi class methods for REST calls.
The class generated through the generator extends the HyperIoTBaseEntityRestApi class, thus automatically exposing REST services for CRUD operations.
The *RestApi classes are annotated with the @Component annotation thus making them OSGi components, and through the property service.exported.intents=swagger the swagger documentation of the REST services reachable at the URL http:ip:port/hyperiot/*/api-docs?url=/hyperiot/*/swagger.json is also generated automatically. 
In addition, a REST service is also generated with swagger documentation in json or yaml format.

Let us now see, through a simple example, how to add a new REST service different from the automatically generated ones.
Let us consider such a project:

Book

* Book-api
* Book-service
* ...

Let's suppose that a service has already been implemented that allows retrieving entities of type Book through the author id.

We want to expose, at this point, the findLibriByAuthor service as a REST service. To do this we need to generate, if it is not already present, the Book-service-rest project within which is the LibroRestApi class.
Obviously in LibroRestApi there are, by the above, already methods for exposing as REST services the CRUD operations on the Book entity.

To expose the REST service findLibriByAuthor simply add in LibroRestApi the method:

```
...
@GET
@Path("/byAuthorId/{authorId}")
@Produces(MediaType.APPLICATION_JSON)
@LoggedIn
@ApiOperation(value = "/hyperiot/books/authors/{autoreId}", notes = "Service for finding books by author id", httpMethod = "GET", produces = "application/json",authorizations = @Authorization("jwt-auth"))
@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
      @ApiResponse(code = 403, message = "Not authorized"),
      @ApiResponse(code = 500, message = "Internal error") })
@JsonView(HyperIoTJSONView.Public.class)
public Response findBooksByAuthorId(@ApiParam(value = "The author id", required = true) @PathParam("authorId") long authorId) {
   ...
   try {
      return Response.ok(this.entityService.findBooksByAuthorId(authorId)).build();
   } catch (Throwable t) {
      return this.handleException(t);
   }
}
...
```

Note how the REST service layer connects to the service layer through the line 13 statement this.entityService.findLibriByAuthor(authorId), defining a situation that can be represented in this way:

![Rest service invocation pipe](../images/framework-invocation-pipe.png)

If we look at the implementation of the reported findLibriByAuthor method, we notice how all the information necessary for the service to function is reported through annotations in the javax.ws.rs package.
The @GET annotation for example makes the service invocable in GET; similarly there are the @POST, @PUT, @DELETE annotations.
The @Path annotation, on the other hand, allows specifying to which endpoint the service is exposed; in the example case, the service is exposed to the URL http:ip:port/hyperiot/libros/byAuthorId/{authorId} .

The @ApiOperation @ApiResponse annotations are used instead for generating Swagger documentation.

One important annotation is @LoggedIn which is not part of the javax.ws.rs package, but is defined within the HyperIoTBase-security module.
The @LoggedIn annotation allows you to specify that the REST service is invocable only if the user who is invoking the service is an authenticated user.

