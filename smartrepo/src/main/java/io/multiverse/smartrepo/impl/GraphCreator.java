package io.multiverse.smartrepo.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class GraphCreator {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphCreator.class);
	
	private JsonObject input;
	private List<String> output;
	private List<String> report;
	
	// sort acl tables
	// private JsonArray aclInInterface = null;
	// private JsonArray aclInGlobal = null;
	
	public GraphCreator(JsonObject input) {
		this.input = input;
		this.output = new ArrayList<String>();
	}
	
	public List<String> getOutput() {
		return output;
	}
	
	public List<String> getReport() {
		return report;
	}

	public boolean process() {
		if (input == null) {
			logger.info("Input object is null");
			return false;
		}
		report = new ArrayList<String>();
		logger.info("GraphDB creation... ");
		Instant start = Instant.now();
		
		output = new ArrayList<String>();
		
		JsonArray hosts = input.getJsonArray("host");
	    processHosts(hosts); 
	    
	    JsonArray ltps = input.getJsonArray("ltp");
	    processLtps(ltps);
	    
	    JsonArray etherCtps = input.getJsonArray("etherCtp");
	    processEtherCtps(etherCtps);
	    
	    JsonArray ip4Ctps = input.getJsonArray("ip4Ctp");
	    processIp4Ctps(ip4Ctps);
	    
	    JsonArray sviCtps = input.getJsonArray("sviCtp");
	    processSviCtps(sviCtps);
		
		// processHostsAgg();
	    
	    processAutoVlanMembers();
	    
	    JsonArray links = input.getJsonArray("link");
	    processLinks(links);

	    // processAutoLc();
	    
	    JsonArray ipConns = input.getJsonArray("ipConn");
	    processIpConns(ipConns);
	    
	    JsonArray bgpNeighbors = input.getJsonArray("bgp");
	    processBgpNeighbors(bgpNeighbors);
	    connectBgpNeighbors();

		JsonArray bgpFilters = input.getJsonArray("bgpFilter");
	    processBgpFilters(bgpFilters);

	    // JsonArray aclTables = input.getJsonArray("aclTable");
	    // sortAclTables(aclTables);
	    // processAclTables();

	    JsonArray aclRules = input.getJsonArray("aclRule");
	    // JsonArray sortedAclRules = sortAclRules(aclRules);
	    processAclRules(aclRules);
	    
	    JsonArray ipRoutes = input.getJsonArray("ipRoute");
	    processFibRoutes(ipRoutes);
	    
	    Instant end = Instant.now();
		logger.info("2- Queries creation time: " + Duration.between(start, end).toMillis() + " ms.");
	    return true;
	}    

	private void processHosts(JsonArray hosts) {
		String query = "T4@" + CypherQuery.Graph.CREATE_HOST;
		hosts.forEach(e -> {
	    	JsonObject host = (JsonObject) e;
	    	String result = String.format(query, host.getString("label"),
	    			host.getString("name"), host.getString("hostname"),
    				host.getString("type"), host.getString("mac"),
    				host.getString("platform"),
    				host.getString("bgpAsn"), host.getString("bgpStatus"), 
					host.getString("bgpId"), host.getString("hwsku"));
	    	output.add(result);
	    });
	}

	private void processLtps(JsonArray ltps) {
		String query = "T5@" + CypherQuery.Graph.CREATE_LTP;
		ltps.forEach(e -> {
			JsonObject ltp = (JsonObject) e;
	    	String result = String.format(query, 
    				ltp.getString("host"), 
    				ltp.getString("name"), ltp.getString("type"), ltp.getString("adminStatus"), ltp.getString("operStatus"),
    				ltp.getString("index"), ltp.getString("speed"), ltp.getString("mtu"));
    		output.add(result);
	    });
	}

	private void processEtherCtps(JsonArray ctps) {
		String q = "T6@" + CypherQuery.Graph.SET_ETHERCTP;
		ctps.forEach(e -> {
			JsonObject ctp = (JsonObject) e;
	    	String result = String.format(q, 
	    			ctp.getString("host"), 
	    			ctp.getString("interface"), 
	    			ctp.getString("macAddr"), 
	    			ctp.getString("vlan"),
	    			ctp.getString("mode"));
	    	output.add(result);
	    });
	}
	 
	private void processIp4Ctps(JsonArray ctps) {
		String q = "T7@" + CypherQuery.Graph.CREATE_IP4CTP;
		ctps.forEach(e -> {
			JsonObject ctp = (JsonObject) e;
	    	String result = String.format(q, 
	    			ctp.getString("host"), 
	    			ctp.getString("interface"), 
	    			ctp.getString("ipAddr"),
	    			ctp.getString("netMask"),
	    			ctp.getString("netAddr"));
	    	output.add(result);
	    });
	}
	
	private void processSviCtps(JsonArray ctps) {
		String q = "T8@" + CypherQuery.Graph.CREATE_SVICTP;
		ctps.forEach(e -> {
			JsonObject ctp = (JsonObject) e;
	    	String result = String.format(q, 
	    			ctp.getString("host"), 
	    			ctp.getString("interface"), 
	    			ctp.getString("ipAddr"),
	    			ctp.getString("netMask"),
	    			ctp.getString("netAddr"),
	    			ctp.getString("svi"),
	    			ctp.getString("vlan"),
	    			ctp.getString("mtu"));
	    	output.add(result);
	    });
	}
	
	/* private void processLinks(JsonArray links) {
		String q = "T9@" + CypherQuery.Graph.CREATE_LINK;
		links.forEach(e -> {
			JsonObject link = (JsonObject) e;
	    	String result = String.format(q, 
	    			link.getString("srcHost"),
	    			link.getString("srcInterface"),
	    			link.getString("destHost"),
	    			link.getString("destInterface"),
	    			link.getString("name"),
	    			link.getString("name"),
	    			link.getString("name"));
	    	output.add(result);
	    });
	} */
	
	private void processLinks(JsonArray links) {
		String q = "T9@" + CypherQuery.Graph.CREATE_LINK;
		links.forEach(e -> {
			JsonObject link = (JsonObject) e;
	    	String result = String.format(q, 
	    			link.getString("srcHost"),
	    			link.getString("srcInterface"),
	    			link.getString("destHost"),
	    			link.getString("destInterface"),
	    			link.getString("name"));
	    	output.add(result);
	    });
	}
	
	private void processAutoLc() {
		String q = "T10@" + CypherQuery.Graph.AUTO_LINKCONN;
    	output.add(q);
	}
	
	private void processAutoVlanMembers() {
		String q = "T17@" + CypherQuery.Graph.AUTO_VLAN_MEMBER;
    	output.add(q);
	}
	
	private void processIpConns(JsonArray ipConns) {
		String q = "T11@" + CypherQuery.Graph.CREATE_IPCONN;
		ipConns.forEach(e -> {
			JsonObject ipConn = (JsonObject) e;
	    	String result = String.format(q, 
	    			ipConn.getString("host"), 
	    			ipConn.getString("interface"),
	    			ipConn.getString("macAddr"),
	    			ipConn.getString("ipAddr"));
	    	output.add(result);
	    });
	}

	private void processBgpNeighbors(JsonArray bgpNeighbors) {
		String q = "T12@" + CypherQuery.Graph.CREATE_BGP;
		bgpNeighbors.forEach(e -> {
			JsonObject bgpN = (JsonObject) e;
	    	String result = String.format(q, 
	    			bgpN.getString("host"), 
	    			bgpN.getString("localAddr"),
	    			bgpN.getString("remoteAddr"), 
	    			bgpN.getString("remoteId"),
	    			bgpN.getString("remoteAsn"),
	    			bgpN.getString("state"),
	    			bgpN.getString("holdTime"),
	    			bgpN.getString("keepAlive"));
	    	output.add(result);
	    });
	}

	private void processBgpFilters(JsonArray bgpFilters) {
		// TODO: support optional fields
		String q = "T14@" + CypherQuery.Graph.CREATE_BGPFILTER;
		bgpFilters.forEach(e -> {
			JsonObject bgpF = (JsonObject) e;
	    	String result = String.format(q, 
	    			bgpF.getString("host"), 
	    			bgpF.getString("type"),
	    			bgpF.getString("prefix"), 
					bgpF.getInteger("filterAs"),
	    			bgpF.getInteger("priority"),
	    			bgpF.getString("action"));
	    	output.add(result);
	    });
	}
	
	private void connectBgpNeighbors() {
		String q = "T13@" + CypherQuery.Graph.AUTO_BGP_NEIGHBORS;
    	output.add(q);
	}
	
	// ACL table
	/* private void sortAclTables(JsonArray aclTables) {
		aclInGlobal = new JsonArray();
		aclTables.forEach(e -> {
			JsonObject table = (JsonObject) e;
	    	String stage = table.getString("stage");
	    	// String binding = table.getString("binding");
	    	if (stage.equals(AclStageEnum.ingress.getValue())) {
	    		aclInGlobal.add(table);
	    	}
	    });
	} */
	
	/* private void processAclTables() {
		// ingress only
		String qInGbl = "T14@" + CypherQuery.Graph.CREATE_ACLTABLE;
		aclInGlobal.forEach(e -> {
			JsonObject table = (JsonObject) e;
	    	String result = String.format(qInGbl, 
	    			table.getString("stage"),
	    			table.getString("name"),
	    			table.getString("binding"),
	    			table.getString("type"),
	    			table.getString("description"),
	    			table.getString("host"));
	    	output.add(result);
	    });
	} */
	
	// ACL rule
	/* private JsonArray sortAclRules(JsonArray aclRules) {	
		JsonArray sortedAclRules = new JsonArray();

		List<JsonObject> jsonValues = new ArrayList<JsonObject>();
		for (int i = 0; i < aclRules.size(); i++) {
		    jsonValues.add(aclRules.getJsonObject(i));
		}
		Collections.sort(jsonValues, new Comparator<JsonObject>() {
		    private static final String KEY = "priority";

		    @Override
		    public int compare(JsonObject a, JsonObject b) {
		        long valA, valB;
		        valA = a.getLong(KEY);
		        valB = b.getLong(KEY);
		        return (int) (valB - valA);
		    }
		});

		for (int i = 0; i < jsonValues.size(); i++) {
		    sortedAclRules.add((JsonObject)jsonValues.get(i));
		}
		return sortedAclRules;
	} */

	private void processAclRules(JsonArray aclRules) {
		String q = "T15@" + CypherQuery.Graph.CREATE_ACLRULE;
		aclRules.forEach(e -> {
			JsonObject aclRule = (JsonObject) e;
	    	String result = String.format(q, 
					aclRule.getString("host"),
	    			aclRule.getString("name"), 
					aclRule.getString("table"),
	    			aclRule.getInteger("priority"), 
	    			aclRule.getString("action"),
	    			aclRule.getString("direction"),
					aclRule.getString("dIP"),
					aclRule.getString("dPort"),
					aclRule.getString("sIP"),
					aclRule.getString("sPort"),
					aclRule.getString("protocol"));
	    	output.add(result);
	    });
	}
	
	private void processFibRoutes(JsonArray routes) {
		String qItf = "T16@" + CypherQuery.Graph.CREATE_FIB_ROUTE_ITF;
		String qSvi = "T17@" + CypherQuery.Graph.CREATE_FIB_ROUTE_SVI;
		routes.forEach(e -> {
			JsonObject route = (JsonObject) e;
	    	String itfName = route.getString("interface");
	    	if (itfName.startsWith("Vlan")) {
	    		String result = String.format(qSvi, 
						route.getString("host"),
		    			itfName,
	    				route.getString("to"),
		    			route.getString("via"),
		    			route.getString("type"),
						route.getString("cost"));
		    	output.add(result);
	    	} else {
	    		String result = String.format(qItf, 
						route.getString("host"),
		    			itfName,
	    				route.getString("to"), 
		    			route.getString("via"),
		    			route.getString("type"),
						route.getString("cost"));
		    	output.add(result);
	    	}
	    });
	}
	
	/* private void processHostsAgg() {
	String CREATE_HOST = "T4@CREATE (h:Host:%s {name: '%s', hostname: '%s', type: '%s', mac: '%s', platform: '%s', "
			+ "bgpAsn: '%s', bgpStatus: '%s', hwsku: '%s'})";
	String CREATE_LTP = " CREATE (h)-[:CONTAINS]->(:Ltp {name: '%s', type: '%s', adminStatus: '%s', operStatus: '%s', "
			+ "index: '%s', speed: '%s', mtu: '%s'})";
	String CREATE_ETHERCTP = "-[:CONTAINS]->(:EtherCtp {macAddr: '%s', vlan: '%s', mode: '%s'})";
	String CREATE_IP4CTP = "-[:CONTAINS]->(:Ip4Ctp {ipAddr:'%s', netMask:'%s', netAddr: '%s'})";
	String CREATE_SVICTP = "-[:CONTAINS]->(:SviCtp {ipAddr:'%s', netMask:'%s', netAddr: '%s', svi:'%s', vlan:'%s', mtu:'%s'})";
	
	JsonArray hosts = input.getJsonArray("host");
	String[] hostQuery = {""};
	hosts.forEach(e -> {
    	JsonObject host = (JsonObject) e;
    	hostQuery[0] = String.format(CREATE_HOST, host.getString("type"),
    			host.getString("name"), host.getString("hostname"),
				host.getString("type"), host.getString("mac"),
				host.getString("platform"),
				host.getString("bgpAsn"), host.getString("bgpStatus"), host.getString("hwsku"));
    	hostQuery[0]+=" WITH h as h";
    	// add Ltps
    	getLtpsOfHost(host.getString("name")).forEach(ee -> {
    		JsonObject ltp = (JsonObject) ee;
    		hostQuery[0]+= String.format(CREATE_LTP,
    				ltp.getString("name"), ltp.getString("type"), ltp.getString("adminStatus"), ltp.getString("operStatus"),
    				ltp.getString("index"), ltp.getString("speed"), ltp.getString("mtu"));
    		// add etherCtps
    		getEtherCtpsOfLtp(host.getString("name"), ltp.getString("name")).forEach(eee -> {
	    		JsonObject ectp = (JsonObject) eee;
	    		hostQuery[0]+= String.format(CREATE_ETHERCTP, 
		    			ectp.getString("macAddr"), ectp.getString("vlan"), ectp.getString("mode"));
	    		// add ip4Ctps
	    		getIp4CtpsOfEtherCtp(host.getString("name"), ectp.getString("interface")).forEach(eeee -> {
		    		JsonObject ip4ctp = (JsonObject) eeee;
		    		hostQuery[0]+= String.format(CREATE_IP4CTP, 
		    				ip4ctp.getString("ipAddr"), ip4ctp.getString("netMask"), ip4ctp.getString("netAddr"));
		    	});
	    		// add sviCtps
	    		getSviCtpsOfEtherCtp(host.getString("name"), ectp.getString("interface")).forEach(eeee -> {
		    		JsonObject svictp = (JsonObject) eeee;
		    		hostQuery[0]+= String.format(CREATE_SVICTP,
		    				svictp.getString("ipAddr"), svictp.getString("netMask"), svictp.getString("netAddr"), 
		    				svictp.getString("svi"), svictp.getString("vlan"), svictp.getString("mtu"));
		    	});
	    	});
    	});

    	// add one host query
    	output.add(hostQuery[0]+=";");
    });
}

private JsonArray getLtpsOfHost(String host) {
	JsonArray ltps = input.getJsonArray("ltp");
	JsonArray res = new JsonArray();
	ltps.forEach(e -> {
    	JsonObject ltp = (JsonObject) e;
    	if (ltp.getString("host").equals(host)) {
    		res.add(ltp);
    	}
	});
	return res;
}
private JsonArray getEtherCtpsOfLtp(String host, String ltp) {
	JsonArray ctps = input.getJsonArray("etherCtp");
	JsonArray res = new JsonArray();
	ctps.forEach(e -> {
    	JsonObject ctp = (JsonObject) e;
    	if (ctp.getString("host").equals(host) && ctp.getString("interface").equals(ltp)) {
    		res.add(ctp);
    	}
	});
	return res;
}
private JsonArray getIp4CtpsOfEtherCtp(String host, String ltp) {
	JsonArray ctps = input.getJsonArray("ip4Ctp");
	JsonArray res = new JsonArray();
	ctps.forEach(e -> {
    	JsonObject ctp = (JsonObject) e;
    	if (ctp.getString("host").equals(host) && ctp.getString("interface").equals(ltp)) {
    		res.add(ctp);
    	}
	});
	return res;
}
private JsonArray getSviCtpsOfEtherCtp(String host, String ltp) {
	JsonArray ctps = input.getJsonArray("sviCtp");
	JsonArray res = new JsonArray();
	ctps.forEach(e -> {
    	JsonObject ctp = (JsonObject) e;
    	if (ctp.getString("host").equals(host) && ctp.getString("interface").equals(ltp)) {
    		res.add(ctp);
    	}
	});
	return res;
} */
}
