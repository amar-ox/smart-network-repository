package io.multiverse.smartrepo;

import static io.multiverse.smartrepo.SmartrepoService.SERVICE_ADDRESS;

import io.multiverse.smartrepo.api.RestSmartrepoAPIVerticle;
import io.multiverse.smartrepo.common.BaseMicroserviceVerticle;
import io.multiverse.smartrepo.impl.SmartrepoServiceImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.serviceproxy.ServiceBinder;


/**
 * A verticle publishing the smartrepo service.
 */
public class SmartrepoVerticle extends BaseMicroserviceVerticle {

	@Override
	public void start(Future<Void> future) throws Exception {
		super.start();

		// create the service instance
		SmartrepoService smartrepoService = new SmartrepoServiceImpl(vertx, config());

		// register the service proxy on event bus
		new ServiceBinder(vertx)
				.setAddress(SERVICE_ADDRESS)
				.register(SmartrepoService.class, smartrepoService);

		initsmartrepoDatabase(smartrepoService)
				.compose(r -> deployRestVerticle(smartrepoService))
				.onComplete(future);
	}

	private Future<Void> initsmartrepoDatabase(SmartrepoService service) {
		Promise<Void> initPromise = Promise.promise();
		service.initializePersistence(initPromise);
		return initPromise.future();
	}

	private Future<Void> deployRestVerticle(SmartrepoService service) {
		Promise<String> promise = Promise.promise();
		vertx.deployVerticle(new RestSmartrepoAPIVerticle(service),
				new DeploymentOptions().setConfig(config()), promise);
		return promise.future().map(r -> null);
	}
}