package io.multiverse.smartrepo;

import io.multiverse.smartrepo.model.CreationReport;
import io.multiverse.smartrepo.model.NetworkState;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A service interface managing Smartrepo.
 */
@VertxGen
@ProxyGen
public interface SmartrepoService {

	/**
	 * The name of the event bus service.
	 */
	String SERVICE_NAME = "smartrepo-eb-service";

	/**
	 * The address on which the service is published.
	 */
	String SERVICE_ADDRESS = "service.smartrepo";

	String FROTNEND_ADDRESS = "mvs.to.frontend";

	String EVENT_ADDRESS = "smartrepo.event";

	@Fluent	
	SmartrepoService initializePersistence(Handler<AsyncResult<Void>> resultHandler);

	@Fluent	
	SmartrepoService processNetworkRunningState(NetworkState netState, Handler<AsyncResult<CreationReport>> resultHandler);
}