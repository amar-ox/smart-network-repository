package io.multiverse.smartrepo.common;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;

/**
 * An abstract base verticle that provides several helper methods for REST API.
 */
public abstract class RestAPIVerticle extends BaseMicroserviceVerticle {

	protected Future<Void> createHttpServer(Router router, String host, int port) {
		Promise<HttpServer> httpServerFuture = Promise.promise();
		vertx.createHttpServer()
		.requestHandler(router)
		.listen(port, host, httpServerFuture);
		return httpServerFuture.future().map(r -> null);
	}
	protected void enableCorsSupport(Router router) {
		Set<String> allowHeaders = new HashSet<>();
		allowHeaders.add("x-requested-with");
		allowHeaders.add("Access-Control-Allow-Origin");
		allowHeaders.add("Authorization");
		allowHeaders.add("origin");
		allowHeaders.add("Content-Type");
		allowHeaders.add("accept");
		Set<HttpMethod> allowMethods = new HashSet<>();
		allowMethods.add(HttpMethod.GET);
		allowMethods.add(HttpMethod.PUT);
		allowMethods.add(HttpMethod.OPTIONS);
		allowMethods.add(HttpMethod.POST);
		allowMethods.add(HttpMethod.DELETE);
		allowMethods.add(HttpMethod.PATCH);

		router.route().handler(CorsHandler.create("*")
				.allowedHeaders(allowHeaders)
				.allowedMethods(allowMethods));
	}
	protected void enableLocalSession(Router router) {
		// router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(
				LocalSessionStore.create(vertx, "nms.user.session")));
	}
	protected void enableClusteredSession(Router router) {
		// router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(
				ClusteredSessionStore.create(vertx, "nms.user.session")));
	}

	// Auth helper methods
	protected void requireLogin(RoutingContext context, BiConsumer<RoutingContext, JsonObject> biHandler) {
		Optional<JsonObject> principal = Optional.ofNullable(context.request().getHeader("user-principal"))
				.map(JsonObject::new);
		if (principal.isPresent()) {
			biHandler.accept(context, principal.get());
		} else {
			context.response()
			.setStatusCode(401)
			.end(new JsonObject().put("message", "need_auth").encode());
		}
	}
	// Authorization handlers
	protected void checkAdminRole(RoutingContext context) {
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		if (principal.getString("role", "").contains("admin")) {
			context.next();
		} else {
			forbidden(context);
		}
	}
	protected void checkOwnerRole(RoutingContext context) {
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		String role = principal.getString("role", "");
		if (role.contains("owner")) {
			context.next();
		} else {
			forbidden(context);
		}
	}
	protected void checkUserRole(RoutingContext context) {
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		String role = principal.getString("role", "");
		if (role.contains("user")) {
			context.next();
		} else {
			forbidden(context);
		}
	}
	protected void checkReaderRole(RoutingContext context) {
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		String role = principal.getString("role", "");
		if (role.contains("reader")) {
			context.next();
		} else {
			forbidden(context);
		}
	}
	protected void checkAgentRole(RoutingContext context) {
		JsonObject principal = new JsonObject(context.request().getHeader("user-principal"));
		if (principal.getString("role", "").contains("agent")) {
			context.next();
		} else {
			forbidden(context);
		}
	}
	protected void responseToken(RoutingContext context, String tokenStr) {
		JsonObject token = new JsonObject().put("token", tokenStr);
		context.response()
				.putHeader("content-type", "application/json")
				.setStatusCode(200)
				.end(token.encodePrettily());
	}

	// helper result handler within a request context
	protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Handler<T> handler) {
		return res -> {
			if (res.succeeded()) {
				handler.handle(res.result());
			} else {
				handleFailedOperation(context, res.cause());
			}
		};
	}
	protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Function<T, String> converter) {
		return ar -> {
			if (ar.succeeded()) {
				T res = ar.result();
				if (res == null) {
					internalError(context, new Throwable("invalid result"));
				} else {
					context.response()
							.putHeader("content-type", "application/json")
							.end(converter.apply(res));
				}
			} else {
				handleFailedOperation(context, ar.cause());
			}
		};
	}
	protected <T> Handler<AsyncResult<T>> resultHandlerNonEmpty(RoutingContext context) {
		return ar -> {
			if (ar.succeeded()) {
				T res = ar.result();
				if (res == null) {
					notFound(context);
				} else {
					context.response()
							.putHeader("content-type", "application/json")
							.end(res.toString());
				}
			} else {
				handleFailedOperation(context, ar.cause());
			}
		};
	}
	protected <T> Handler<AsyncResult<T>> rawResultHandler(RoutingContext context) {
		return ar -> {
			if (ar.succeeded()) {
				T res = ar.result();
				context.response()
				.end(res == null ? "" : res.toString());
			} else {
				handleFailedOperation(context, ar.cause());
			}
		};
	}
	protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context, int status) {
		return ar -> {
			if (ar.succeeded()) {
				context.response()
						.setStatusCode(status == 0 ? 200 : status)
						.putHeader("content-type", "application/json")
						.end();
			} else {
				handleFailedOperation(context, ar.cause());
			}
		};
	}
	protected Handler<AsyncResult<Integer>> createResultHandler(RoutingContext context, String location) {
		return ar -> {
			if (ar.succeeded()) {
				Integer id = ar.result();
				context.response()
						.setStatusCode(201)
						.putHeader("content-type", "application/json")
 						.putHeader("Location", location + "/" + id)
						.end(new JsonObject().put("id", id).encode());
			} else {
				handleFailedOperation(context, ar.cause());
			}
		};
	}
	protected Handler<AsyncResult<Void>> createResultHandler(RoutingContext context) {
		return ar -> {
			if (ar.succeeded()) {
				context.response().setStatusCode(201).end();
			} else {
				handleFailedOperation(context, ar.cause());
			}
		};
	}
	protected Handler<AsyncResult<Void>> updateResultHandler(RoutingContext context) {
		return ar -> {
			if (ar.succeeded()) {
				context.response().setStatusCode(200).end();
			} else {
				handleFailedOperation(context, ar.cause());
			}
		};
	}
	protected Handler<AsyncResult<Void>> deleteResultHandler(RoutingContext context) {
		return res -> {
			if (res.succeeded()) {
				context.response()
						.setStatusCode(204)
						.putHeader("content-type", "application/json")
						.end();
			} else {
				handleFailedOperation(context, res.cause());
			}
		};
	}

	protected void handleFailedOperation(RoutingContext context, Throwable cause) {
		// TODO: use accurate messages
		if (cause.getMessage().toUpperCase().contains("CONFLICT")) {
			conflict(context);
		} else if (cause.getMessage().toUpperCase().contains("NOT_FOUND")) {
			notFound(context);
		} else if (cause.getMessage().toUpperCase().contains("INVALID")) {
			badRequest(context, cause);
			// cause.printStackTrace();
		} else if (cause.getMessage().toUpperCase().contains("NO_CHANGE")) {
			notChanged(context);
		} else {
			internalError(context, cause);
		}
	}

	// helper method for HTTP responses  
	protected void notChanged(RoutingContext context) {
		context.response().setStatusCode(304).end();
	}
	protected void conflict(RoutingContext context) {
		context.response().setStatusCode(409).end();
	}
	protected void badRequest(RoutingContext context, Throwable ex) {
		context.response().setStatusCode(400)
				.putHeader("content-type", "application/json")
				.end(new JsonObject().put("message", ex.getMessage()).encodePrettily());
	}
	protected void unauthorized(RoutingContext context) {
		context.response().setStatusCode(401).end();
	}
	protected void forbidden(RoutingContext context) {
		context.response().setStatusCode(403).end();
	}
	protected void notFound(RoutingContext context) {
		context.response().setStatusCode(404)
		.putHeader("content-type", "application/json")
		.end(new JsonObject().put("message", "not_found").encodePrettily());
	}
	protected void internalError(RoutingContext context, Throwable ex) {
		context.response().setStatusCode(500)
		.putHeader("content-type", "application/json")
		.end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
	}
	protected void notImplemented(RoutingContext context) {
		context.response().setStatusCode(501)
		.putHeader("content-type", "application/json")
		.end(new JsonObject().put("message", "not_implemented").encodePrettily());
	}
	protected void badGateway(Throwable ex, RoutingContext context) {
		ex.printStackTrace();
		context.response()
		.setStatusCode(502)
		.putHeader("content-type", "application/json")
		.end(new JsonObject().put("error", "bad_gateway")
				.put("message", ex.getMessage())
				.encodePrettily());
	}
	protected void serviceUnavailable(RoutingContext context) {
		context.fail(503);
	}
	protected void serviceUnavailable(RoutingContext context, Throwable ex) {
		context.response().setStatusCode(503)
		.putHeader("content-type", "application/json")
		.end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
	}
	protected void serviceUnavailable(RoutingContext context, String cause) {
		context.response().setStatusCode(503)
		.putHeader("content-type", "application/json")
		.end(new JsonObject().put("error", cause).encodePrettily());
	}
}
