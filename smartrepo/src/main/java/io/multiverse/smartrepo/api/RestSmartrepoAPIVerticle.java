package io.multiverse.smartrepo.api;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import io.multiverse.smartrepo.SmartrepoService;
import io.multiverse.smartrepo.common.JsonUtils;
import io.multiverse.smartrepo.common.RestAPIVerticle;
import io.multiverse.smartrepo.model.DeviceState;
import io.multiverse.smartrepo.model.NetworkState;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * This verticle exposes REST API endpoints to process smartrepo operation
 */
public class RestSmartrepoAPIVerticle extends RestAPIVerticle {

	private static final Logger logger = LoggerFactory.getLogger(RestSmartrepoAPIVerticle.class);

	public static final String SERVICE_NAME = "smartrepo-rest-api";

	private static final String API_VERSION = "/v";

	private static final String API_RUNNING_STATE = "/running/state";

	private SmartrepoService service;

	public RestSmartrepoAPIVerticle(SmartrepoService service) {
		this.service = service;
	}

	@Override
	public void start(Future<Void> future) throws Exception {
		super.start();
		final Router router = Router.router(vertx);

		// body handler
		router.route().handler(BodyHandler.create());

		// version
		router.get(API_VERSION).handler(this::apiVersion);

		router.post(API_RUNNING_STATE).handler(this::checkAdminRole).handler(this::apiProcessNetworkRunningState);

		// get HTTP host and port from configuration, or use default value
		String host = config().getString("smartrepo.http.address", "0.0.0.0");
		int port = config().getInteger("smartrepo.http.port", 7070);

		// create HTTP server and publish REST service
		createHttpServer(router, host, port)
				.compose(serverCreated -> publishHttpEndpoint(SERVICE_NAME, host, port))
				.onComplete(future);
	}

	/* Operations on running network */
	private void apiProcessNetworkRunningState(RoutingContext context) {
		try {
			TypeReference<HashMap<String,DeviceState>> typeRef 
					= new TypeReference<HashMap<String,DeviceState>>() {};
			final Map<String, DeviceState> deviceStates 
					= JsonUtils.json2Pojo(context.getBodyAsString(), typeRef);
			final NetworkState netState = new NetworkState();
			netState.setConfigs(deviceStates);
			service.processNetworkRunningState(netState, resultHandlerNonEmpty(context));
		} catch (Exception e) {
			logger.info("API input argument exception: " + e.getMessage());
			badRequest(context, e);
		}
	}

	/* API version */
	private void apiVersion(RoutingContext context) {
		context.response().end(new JsonObject()
				.put("name", SERVICE_NAME)
				.put("version", "v1").encodePrettily());
	}
}
