package io.multiverse.smartrepo.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import io.multiverse.smartrepo.SmartrepoService;
import io.multiverse.smartrepo.common.JsonUtils;
import io.multiverse.smartrepo.common.Neo4jWrapper;
import io.multiverse.smartrepo.model.CreationReport;
import io.multiverse.smartrepo.model.DeviceState;
import io.multiverse.smartrepo.model.NetworkState;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Implementation of Smartrepo API
 */
public class SmartrepoServiceImpl extends Neo4jWrapper implements SmartrepoService {

	private static final Logger logger = LoggerFactory.getLogger(SmartrepoServiceImpl.class);

	private static final String MAIN_DB = "neo4j";

	public SmartrepoServiceImpl(Vertx vertx, JsonObject config) {
		super(vertx, config);
	}

	@Override
	public SmartrepoService initializePersistence(Handler<AsyncResult<Void>> resultHandler) {
		List<String> constraints = new ArrayList<String>();
		// constraints.add(CypherQuery.CLEAR_DB);
		constraints.add(CypherQuery.Constraints.UNIQUE_HOST);
		bulkExecute(MAIN_DB, constraints, res -> {
			if (res.succeeded()) {
				logger.info("Neo4j DB initialized");
				loadExampleNetwork(resultHandler);
			} else {
				logger.error(res.cause());
				resultHandler.handle(Future.failedFuture(res.cause()));
			}
		});
		return this;
	}

	@Override
	public SmartrepoService processNetworkRunningState(NetworkState netState,
			Handler<AsyncResult<CreationReport>> resultHandler) {
		CreationReport report = new CreationReport();
		resultHandler.handle(Future.succeededFuture(report));
		
		processNetworkState(netState, res -> {
			if (res.succeeded()) {
				logger.info("Running state processed. Report: " + res.result().toJson().encodePrettily());
			} else {
				logger.info("Failed to process running state: " + res.cause());
			}
		});
		return this;
	}
	
	/* Processing functions */
	private void processNetworkState(NetworkState netState, 
			Handler<AsyncResult<CreationReport>> resultHandler) {
		List<String> queries = new ArrayList<String>();
		CreationReport report = new CreationReport();
		vertx.executeBlocking(future -> {
			report.setTimestamp(OffsetDateTime.now().toLocalDateTime().toString());
			
			ConfigProcessor cp = new ConfigProcessor(netState);
			if (!cp.process()) {
				future.fail("Failed to process config");
				return;
			}
			report.setConfigProcessor(cp.getReport());

			GraphCreator gc = new GraphCreator(cp.getOutput());
			if (!gc.process()) {
				future.fail("Failed to create graph queries");
				return;
			}
			report.setQueriesGenerator(gc.getReport());
			
			queries.add(CypherQuery.CLEAR_DB);
			queries.addAll(gc.getOutput().stream().map(s -> s.split("@")[1]).collect(Collectors.toList()));
			
			future.complete();
		}, res -> {
			if (res.succeeded()) {
				logger.info("Create graph with "+queries.size()+" queries");
				createGraph(MAIN_DB, queries, done -> {
					if (res.succeeded()) {
						report.setGraphCreator(done.result());
						resultHandler.handle(Future.succeededFuture(report));
					} else {
						resultHandler.handle(Future.failedFuture(done.cause()));	
					}
				});
			} else {
				resultHandler.handle(Future.failedFuture(res.cause()));	
			}
		});
	}
	
	private void loadExampleNetwork(Handler<AsyncResult<Void>> resultHandler) {
		String stateColl = vertx.fileSystem().readFileBlocking("state-collection.json").toString();
		final TypeReference<HashMap<String,DeviceState>> typeRef 
				= new TypeReference<HashMap<String,DeviceState>>() {};
		final Map<String, DeviceState> configs 
				= JsonUtils.json2Pojo(stateColl, typeRef);
		final NetworkState netConfig = new NetworkState();
		netConfig.setConfigs(configs);
		processNetworkRunningState(netConfig, done -> {
			resultHandler.handle(Future.succeededFuture());
			if (done.succeeded()) {
				logger.info("Example network loaded");
			} else {
				logger.info("Failed to load example network: " + done.cause().getMessage());
			}
		});
	}
	
	/* private String joinQueries(List<String> queries) {
	String res = "";
	for (String q: queries) {
		res+=q.substring(0, q.length() - 1);
		res+=" ";
	}
	return res;
	} */

	/* private void processNetworkStateBlocking(NetworkState netState, 
		Handler<AsyncResult<CreationReport>> resultHandler) {
	List<String> queries = new ArrayList<String>();
	CreationReport report = new CreationReport();
	vertx.executeBlocking(future -> {
		report.setTimestamp(OffsetDateTime.now().toLocalDateTime().toString());
		
		ConfigProcessor cp = new ConfigProcessor(netState);
		if (!cp.process()) {
			future.fail("Failed to process config");
			// resultHandler.handle(Future.failedFuture("Failed to process config"));
			return;
		}
		report.setConfigProcessor(cp.getReport());

		GraphCreator gc = new GraphCreator(cp.getOutput());
		if (!gc.process()) {
			// resultHandler.handle(Future.failedFuture("Failed to create graph queries"));
			future.fail("Failed to create graph queries");
			return;
		}
		report.setQueriesGenerator(gc.getReport());
		
		queries.add(CypherQuery.CLEAR_DB);
		queries.addAll(gc.getOutput().stream().map(s -> s.split("@")[1]).collect(Collectors.toList()));
		
		future.complete();
	}, res -> {
		if (res.succeeded()) {
			logger.info("Create graph with "+queries.size()+" queries");
			createGraphBlocking(MAIN_DB, queries, done -> {
				if (done.succeeded()) {
					logger.info("Total queries: " + queries.size());
					// report.setGraphCreator(done.result());
					resultHandler.handle(Future.succeededFuture(report));

					// save queries for static views creation
					saveQueries(new JsonArray(queries));
				} else {
					resultHandler.handle(Future.failedFuture(done.cause()));	
				}
			});
		} else {
			resultHandler.handle(Future.failedFuture(res.cause()));	
		}
	});
	} */

}