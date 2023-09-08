package io.multiverse.smartrepo.impl;

public class CypherQuery {
	public static final String CLEAR_DB = "MATCH (n) DETACH DELETE n;";
	
	public static class Graph {
		public static final String CREATE_HOST = "CREATE (h:Host:%s {name: '%s', hostname: '%s', type: '%s', mac: '%s', platform: '%s', "
				+ "asn: '%s', bgpStatus: '%s', bgpId: '%s', hwsku: '%s'});";
		public static final String CREATE_LTP = "MATCH (r:Host) WHERE r.name = '%s' "
				+ "CREATE (r)-[:CONTAINS]->(:Ltp {name: '%s', type: '%s', adminStatus: '%s', operStatus: '%s', "
				+ "index: '%s', speed: %s, mtu: %s});";
		public static final String CREATE_ETHERCTP = "MATCH (h:Host {name:'%s'})-[:CONTAINS]->(l:Ltp{name:'%s'}) "
				+ "CREATE (l)-[:CONTAINS]->(c:EtherCtp {macAddr: '%s', vlan: '%s', mode: '%s'});";
		public static final String CREATE_IP4CTP = "MATCH (r:Host {name:'%s'})-[:CONTAINS]->(:Ltp{name:'%s'})-[:CONTAINS]->(c:EtherCtp) "
				+ "CREATE (c)-[:CONTAINS]->(ipc:Ip4Ctp {ipAddr:'%s', netMask:'%s', netAddr: '%s'});";
		public static final String CREATE_SVICTP = "MATCH (r:Host {name:'%s'})-[:CONTAINS]->(:Ltp{name:'%s'})-[:CONTAINS]->(c:EtherCtp) "
				+ "CREATE (c)-[:CONTAINS]->(ipc:SviCtp {ipAddr:'%s', netMask:'%s', netAddr: '%s', svi:'%s', vlan:'%s', mtu:%s});";
		/* public static final String CREATE_LINK = "MATCH (sR:Host {name:'%s'})-[:CONTAINS]->(src:Ltp{name:'%s'})\r\n"
				+ "MATCH (tR:Host {name:'%s'})-[:CONTAINS]->(dst:Ltp{name:'%s'})\r\n"
				+ "WITH DISTINCT sR,tR,src,dst\r\n"
				+ "CALL apoc.do.case([\r\n"
				+ "(sR.type = 'LeafRouter' AND tR.type = 'LeafRouter'),\r\n"
				+ "\"CREATE (src)-[r:LINKED_VLT {name: '%s'}]->(dst)\",\r\n"
				+ "(sR.type = 'Server' OR tR.type ='Server'),\r\n"
				+ "\"CREATE (src)-[r:LINKED_L2 {name: '%s'}]->(dst)\"],\r\n"
				+ "\"CREATE (src)-[r:LINKED_L3 {name: '%s'}]->(dst)\", {src:src, dst:dst})\r\n"
				+ "YIELD value\r\n"
				+ "RETURN value;"; */
		public static final String CREATE_LINK = "MATCH (sR:Host {name:'%s'})-[:CONTAINS]->(src:Ltp{name:'%s'})\r\n"
				+ "MATCH (tR:Host {name:'%s'})-[:CONTAINS]->(dst:Ltp{name:'%s'})\r\n"
				+ "WITH DISTINCT sR,tR,src,dst\r\n"
				+ "CREATE (src)-[r:HAS_LINK {name: '%s'}]->(dst);";
		public static final String CREATE_IPCONN = "MATCH (r:Host {name:'%s'})-[:CONTAINS]->(sL:Ltp{name:'%s'})-[:CONTAINS*2]->(s:Ip4Ctp) "
				+ "MATCH (dC:EtherCtp{macAddr:'%s'})-[:CONTAINS]->(d:Ip4Ctp{ipAddr:'%s'}) "
				+ "CREATE (s)-[ip:IP_CONN]->(d);";
		public static final String CREATE_BGP = "MATCH(r:Host {name:'%s'})-[:CONTAINS*3]->(c:Ip4Ctp{ipAddr:'%s'})\r\n"
				+ "CREATE (c)-[:HAS_BGP_PEER]->(b:BgpPeer {peerIp:'%s', peerId:'%s', peerAs:'%s', "
				+ "state: '%s', holdTime: '%s', keepAlive: '%s'});";
		public static final String CREATE_BGPFILTER = "MATCH (h:Host {name:'%s'})\r\n" 
				+ "CREATE (h)-[:HAS_FILTER]->(f:Filter {type:'%s', prefix:'%s', filter_as: '%s', priority: %s, action:'%s'});";
		/* public static final String CREATE_ACLTABLE = "CREATE (a:Acl{stage: '%s', name: '%s', binding: '%s', type: '%s', description: '%s', rules:[]})\r\n"
				+ "WITH a\r\n"
				+ "MATCH (h:Host {name:'%s'})\r\n"
				+ "CREATE (h)-[:ACL]->(a);";
		public static final String CREATE_ACLRULE = "CREATE (nr:AclRule {name:'%s', priority: %s, action:'%s', matching:'%s'})\r\n"
				+ "WITH nr\r\n"
				+ "MATCH (h:Host {name:'%s'})-[:ACL]->(t:Acl{name:'%s'})\r\n"
				+ "OPTIONAL MATCH (t)-[:NEXT_RULE*]->(r:AclRule) WHERE NOT EXISTS ((r)-[:NEXT_RULE]->())\r\n"
				+ "WITH DISTINCT nr, t, r\r\n"
				+ "CALL apoc.do.case([\r\n"
				+ "  (t IS NOT null AND r IS NOT null AND nr.action = 'ACCEPT'),\r\n"
				+ "  'CREATE (r)-[:NEXT_RULE]->(nr)-[:ACCEPT]->(t)',\r\n"
				+ "  (t IS NOT null AND r IS NOT null AND nr.action = 'DROP'),\r\n"
				+ "  'CREATE (r)-[:NEXT_RULE]->(nr)',\r\n"
				+ "  (t IS NOT null AND r IS null AND nr.action = 'ACCEPT'),\r\n"
				+ "  'CREATE (t)-[:NEXT_RULE]->(nr)-[:ACCEPT]->(t)',\r\n"
				+ "  (t IS NOT null AND r IS null AND nr.action = 'DROP'),\r\n"
				+ "  'CREATE (t)-[:NEXT_RULE]->(nr)'\r\n"
				+ "  ],\r\n"
				+ "  'DELETE nr',{nr:nr, t:t, r:r})\r\n"
				+ "YIELD value\r\n"
				+ "RETURN value;"; */
		public static final String CREATE_ACLRULE = "MATCH (h:Host {name:'%s'})\r\n" 
				+ "CREATE (h)-[:HAS_ACL]->(nr:AclRule {name:'%s', aclTable:'%s', priority: %s, action:'%s', "
				+ "direction: '%s', dIP: '%s', dPort: '%s', sIP: '%s', sPort: '%s', protocol: '%s'});";
		public static final String CREATE_FIB_ROUTE_ITF = ""
				+ "MATCH (h:Host {name:'%s'})-[:CONTAINS]->(l:Ltp {name:'%s'})-[:CONTAINS*2]->(rEx:Ip4Ctp)\r\n"
				+ "CREATE (h)-[:HAS_FIB]->(r:FibRoute {to: '%s', via: '%s', type: '%s', cost: %s})-[:EGRESS]->(rEx);";
		/* public static final String CREATE_ROUTE_ITF = "CREATE(r:Route {to: '%s', via: '%s', type: '%s'}) \r\n"
				+ "WITH r\r\n"
				+ "MATCH (h:Host {name:'%s'})-[:CONTAINS]->(l:Ltp{name:'%s'})-[:CONTAINS*2]->(rEx:Ip4Ctp)\r\n"
				+ "CREATE (r)-[:EGRESS]->(rEx)\r\n"
				+ "WITH r, h\r\n"
				+ "OPTIONAL MATCH (h)-[:ACL]->(ag:Acl)\r\n"
				+ "WITH DISTINCT h, ag, r\r\n"
				+ "CALL apoc.do.case([\r\n"
				+ "  (ag IS NOT null),\r\n"
				+ "  'CREATE (ag)-[:TO_ROUTE]->(r)'\r\n"
				+ "],\r\n"
				+ "'CREATE (h)-[:TO_ROUTE]->(r)',{h:h, ag:ag, r:r})\r\n"
				+ "YIELD value\r\n"
				+ "RETURN value;"; */
		public static final String CREATE_FIB_ROUTE_SVI = ""
				+ "MATCH (h:Host {name:'%s'})-[:CONTAINS*3]->(rEx:SviCtp{svi:'%s'})\r\n"
				+ "CREATE (h)-[:HAS_FIB]->(r:FibRoute {to: '%s', via: '%s', type: '%s', cost: %s})-[:EGRESS]->(rEx);";
		/* public static final String CREATE_ROUTE_SVI = "CREATE(r:Route {to: '%s', via: '%s', type: '%s'}) \r\n"
				+ "WITH r\r\n"
				+ "MATCH (h:Host {name:'%s'})-[:CONTAINS*3]->(rEx:Ip4Ctp{svi:'%s'})\r\n"
				+ "CREATE (r)-[:EGRESS]->(rEx)\r\n"
				+ "WITH r, h\r\n"
				+ "OPTIONAL MATCH (h)-[:ACL]->(ag:Acl)\r\n"
				+ "WITH DISTINCT h, ag, r\r\n"
				+ "CALL apoc.do.case([\r\n"
				+ "  (ag IS NOT null),\r\n"
				+ "  'CREATE (ag)-[:TO_ROUTE]->(r)'\r\n"
				+ "],\r\n"
				+ "'CREATE (h)-[:TO_ROUTE]->(r)',{h:h, ag:ag, r:r})\r\n"
				+ "YIELD value\r\n"
				+ "RETURN value;"; */

		/* public static final String AUTO_LINKCONN = "MATCH (sC:EtherCtp)<-[:CONTAINS]-(sL:Ltp)-[:LINKED_L2|:LINKED_L3|:LINKED_VLT]->(dL:Ltp)-[:CONTAINS]->(dC:EtherCtp) "
				+ "WHERE NOT (sC)-[:LINK_CONN]-() AND NOT (dC)-[:LINK_CONN]-() "
				+ "AND NOT (sL)-[:CONTAINS]-(:Switch) AND NOT (dL)-[:CONTAINS]-(:Switch) "
				+ "CREATE (sC)-[r:LINK_CONN]->(dC);"; */
		public static final String AUTO_LINKCONN = "MATCH (sC:EtherCtp)<-[:CONTAINS]-(sL:Ltp)-[:HAS_LINK]->(dL:Ltp)-[:CONTAINS]->(dC:EtherCtp) "
				+ "WHERE NOT (sC)-[:LINK_CONN]-() AND NOT (dC)-[:LINK_CONN]-() "
				+ "AND NOT (sL)-[:CONTAINS]-(:Switch) AND NOT (dL)-[:CONTAINS]-(:Switch) "
				+ "CREATE (sC)-[r:LINK_CONN]->(dC);";
		public static final String AUTO_VLAN_MEMBER = "MATCH (h:Host)\r\n"
				+ "MATCH (h)-[:CONTAINS*3]->(svi:SviCtp)\r\n"
				+ "MATCH (h)-[:CONTAINS]->(l:Ltp)-[:CONTAINS]->(e:EtherCtp) WHERE e.vlan = svi.vlan AND NOT (e)-[:CONTAINS]->(:Ip4Ctp)\r\n"
				+ "CREATE (e)-[b:VLAN_MEMBER]->(svi)\r\n"
				+ "RETURN h, svi, b, e;";
		public static final String AUTO_BGP_NEIGHBORS = "MATCH (b1:BgpPeer)<-[:HAS_BGP_PEER]-(c1:Ip4Ctp)-[:IP_CONN]->(c2:Ip4Ctp)-[:HAS_BGP_PEER]->(b2:BgpPeer)\r\n"
				+ "WHERE EXISTS((c2)-[:IP_CONN]->(c1)) AND b1.peerIp=c2.ipAddr AND b2.peerIp=c1.ipAddr\r\n"
				+ "WITH DISTINCT b1,b2\r\n"
				+ "CREATE (b1)-[:PEERS_WITH]->(b2);";
	}
	
	public static class Constraints {
		public static final String UNIQUE_HOST = "CREATE CONSTRAINT unique_host IF NOT EXISTS ON (h:Host) ASSERT h.name IS UNIQUE";
		public static final String UNIQUE_HOSTNAME = "CREATE CONSTRAINT unique_hostname IF NOT EXISTS ON (h:Host) ASSERT h.hostname IS UNIQUE";
		public static final String UNIQUE_LTP = "";
		public static final String UNIQUE_ETHERCTP = "";
		public static final String UNIQUE_IP4CTP = "CREATE CONSTRAINT unique_ip_address IF NOT EXISTS ON (c:Ip4Ctp) ASSERT c.ipAddr IS UNIQUE";
		public static final String UNIQUE_LINKCONN = "";
		public static final String UNIQUE_IPCONN = "";
		public static final String UNIQUE_BGP = "";
		public static final String UNIQUE_ROUTE = "";
		public static final String UNIQUE_ACLTABLE = "";
		public static final String UNIQUE_ACLRULE = "";
	}
	
	public static class Internal {
		public static final String DISCONNECT_BGP = "MATCH ()-[b:PEERS_WITH]-() DELETE b";
		public static final String RECONNECT_BGP = Graph.AUTO_BGP_NEIGHBORS;
		public static final String CHECK_HOST = "MATCH (h:Host{name:$deviceName}) RETURN h.name";
	}
}