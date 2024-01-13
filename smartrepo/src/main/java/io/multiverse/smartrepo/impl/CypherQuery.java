package io.multiverse.smartrepo.impl;

public class CypherQuery {
	public static final String CLEAR_DB = "MATCH (n) DETACH DELETE n;";
	
	public static class Graph {
		public static final String CREATE_HOST = "CREATE (h:Host:%s {name: '%s', hostname: '%s', type: '%s', macAddress: '%s', platform: '%s', "
				+ "asn: '%s', bgpStatus: '%s', bgpId: '%s', hwsku: '%s'});";
		public static final String CREATE_LTP = "MATCH (r:Host) WHERE r.name = '%s' "
				+ "CREATE (r)-[:HAS_PORT]->(:Port {name: '%s', type: '%s', adminStatus: '%s', operStatus: '%s', "
				+ "index: '%s', speed: %s, mtu: %s});";
		public static final String SET_ETHERCTP = "MATCH (h:Host {name:'%s'})-[:HAS_PORT]->(l:Port {name:'%s'}) "
				+ "SET l.macAddress = '%s', l.inVlan = '%s', l.mode = '%s';";
		public static final String CREATE_IP4CTP = "MATCH (:Host {name:'%s'})-[:HAS_PORT]->(l:Port {name:'%s'}) "
				+ "CREATE (l)-[:HAS_INTERFACE]->(ipc:IPv4Endpoint {ipAddr:'%s', netMask:'%s', netAddr: '%s'});";
		public static final String CREATE_SVICTP = "MATCH (:Host {name:'%s'})-[:HAS_PORT]->(l:Port {name:'%s'}) "
				+ "CREATE (l)-[:CONTAINS_VLAN]->(ipc:Vlan {ipAddr:'%s', netMask:'%s', netAddr: '%s', svi:'%s', vlanID:'%s', mtu:%s});";
		public static final String CREATE_LINK = "MATCH (sR:Host {name:'%s'})-[:HAS_PORT]->(src:Port {name:'%s'})\r\n"
				+ "MATCH (tR:Host {name:'%s'})-[:HAS_PORT]->(dst:Port {name:'%s'})\r\n"
				+ "WITH DISTINCT sR,tR,src,dst\r\n"
				+ "CREATE (src)-[r:HAS_LINK {name: '%s'}]->(dst);";
		public static final String CREATE_IPCONN = "MATCH (:Host {name:'%s'})-[:HAS_PORT]->(:Port {name:'%s'})-[:HAS_INTERFACE]->(s:IPv4Endpoint) "
				+ "MATCH (:Port {macAddress:'%s'})-[:HAS_INTERFACE]->(d:IPv4Endpoint {ipAddr:'%s'}) "
				+ "CREATE (s)-[ip:HAS_IP_CONNECTION]->(d);";
		public static final String CREATE_BGP = "MATCH(r:Host {name:'%s'})-[]-(:Port)-[HAS_INTERFACE]->(c:IPv4Endpoint {ipAddr:'%s'})\r\n"
				+ "CREATE (c)-[:HAS_BGP_PEER]->(b:BgpPeer {peerIp:'%s', peerId:'%s', peerAs:'%s', "
				+ "state: '%s', holdTime: '%s', keepAlive: '%s'});";
		public static final String CREATE_BGPFILTER = "MATCH (h:Host {name:'%s'})\r\n" 
				+ "CREATE (h)-[:HAS_BGP_FILTER]->(f:BgpFilter {type:'%s', prefix:'%s', filter_as: '%s', priority: %s, action:'%s'});";
		public static final String CREATE_ACLRULE = "MATCH (h:Host {name:'%s'})\r\n" 
				+ "CREATE (h)-[:HAS_ACL_RULE]->(nr:AclRule {name:'%s', aclTable:'%s', priority: %s, action:'%s', "
				+ "direction: '%s', dIP: '%s', dPort: '%s', sIP: '%s', sPort: '%s', protocol: '%s'});";
		public static final String CREATE_FIB_ROUTE_ITF = ""
				+ "MATCH (h:Host {name:'%s'})-[:HAS_PORT]->(l:Port {name:'%s'})-[:HAS_INTERFACE]->(rEx:IPv4Endpoint)\r\n"
				+ "CREATE (h)-[:HAS_FIB_ROUTE]->(r:FibRoute {to: '%s', via: '%s', type: '%s', cost: %s})-[:EGRESS_INTERFACE]->(rEx);";
		public static final String CREATE_FIB_ROUTE_SVI = ""
				+ "MATCH (h:Host {name:'%s'})-[:HAS_PORT]->(:Port)-[:CONTAINS_VLAN]->(rEx:Vlan {svi:'%s'})\r\n"
				+ "CREATE (h)-[:HAS_FIB_ROUTE]->(r:FibRoute {to: '%s', via: '%s', type: '%s', cost: %s})-[:EGRESS_INTERFACE]->(rEx);";
		public static final String AUTO_LINKCONN = "MATCH (sC:EtherCtp)<-[:CONTAINS]-(sL:Ltp)-[:HAS_LINK]->(dL:Ltp)-[:CONTAINS]->(dC:EtherCtp) "
				+ "WHERE NOT (sC)-[:LINK_CONN]-() AND NOT (dC)-[:LINK_CONN]-() "
				+ "AND NOT (sL)-[:CONTAINS]-(:Switch) AND NOT (dL)-[:CONTAINS]-(:Switch) "
				+ "CREATE (sC)-[r:LINK_CONN]->(dC);";
		public static final String AUTO_VLAN_MEMBER = "MATCH (h:Host)\r\n"
				+ "MATCH (h)-[:HAS_PORT]-(:Port)-[:CONTAINS_VLAN]->(svi:Vlan)\r\n"
				+ "MATCH (h)-[:HAS_PORT]->(l:Port) WHERE l.inVlan = svi.vlanID AND NOT (l)-[:HAS_INTERFACE]->(:IPv4Endpoint)\r\n"
				+ "CREATE (l)-[b:IS_VLAN_MEMBER]->(svi)\r\n"
				+ "RETURN h, svi, b;";
		public static final String AUTO_BGP_NEIGHBORS = "MATCH (b1:BgpPeer)<-[:HAS_BGP_PEER]-(c1:IPv4Endpoint)-[:HAS_IP_CONNECTION]->(c2:IPv4Endpoint)-[:HAS_BGP_PEER]->(b2:BgpPeer)\r\n"
				+ "WHERE EXISTS((c2)-[:HAS_IP_CONNECTION]->(c1)) AND b1.peerIp=c2.ipAddr AND b2.peerIp=c1.ipAddr\r\n"
				+ "WITH DISTINCT b1,b2\r\n"
				+ "CREATE (b1)-[:HAS_BGP_SESSION]->(b2);";
	}
	
	public static class Constraints {
		public static final String UNIQUE_HOST = "CREATE CONSTRAINT unique_host IF NOT EXISTS FOR (h:Host) REQUIRE (h.name) IS UNIQUE";
		public static final String UNIQUE_HOSTNAME = "CREATE CONSTRAINT unique_hostname IF NOT EXISTS FOR (h:Host) REQUIRE (h.hostname) IS UNIQUE";
		public static final String UNIQUE_LTP = "";
		public static final String UNIQUE_ETHERCTP = "";
		public static final String UNIQUE_IP4CTP = "CREATE CONSTRAINT unique_ip_address IF NOT EXISTS FOR (c:IPv4Endpoint) REQUIRE (c.ipAddr) IS UNIQUE";
		public static final String UNIQUE_LINKCONN = "";
		public static final String UNIQUE_IPCONN = "";
		public static final String UNIQUE_BGP = "";
		public static final String UNIQUE_ROUTE = "";
		public static final String UNIQUE_ACLTABLE = "";
		public static final String UNIQUE_ACLRULE = "";
	}
	
	public static class Internal {
		public static final String DISCONNECT_BGP = "MATCH ()-[b:HAS_BGP_SESSION]-() DELETE b";
		public static final String RECONNECT_BGP = Graph.AUTO_BGP_NEIGHBORS;
		public static final String CHECK_HOST = "MATCH (h:Host{name:$deviceName}) RETURN h.name";
	}
}