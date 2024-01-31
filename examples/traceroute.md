# BGP "what-if" scenario
Follow the steps below to run the example. 

1. Access the Neo4j console from a browser at http://localhost:7474.
Use `neo4j` `12345-password` if prompted to connect.

2. Run the following queries in the prompt:
```bash
// declare a placeholder one-hop forwarding to avoid recursive declaration error
CALL apoc.custom.declareProcedure("trHop(pkt_id::INT, itf_id::INT, hop::INT) :: (ignored::INT)", 
"RETURN null AS ignored", 'read')
```
```bash
// define the one-hop forwarding process
CALL apoc.custom.declareProcedure("trHop(pkt_id::INT, itf_id::INT, hop::INT) :: (ignored::INT)", 
"MATCH (packet:Packet) WHERE id(packet) = $pkt_id
MATCH (r:Host)-[:HAS_PORT]->(srcItf:Port)
WHERE id(srcItf) = $itf_id AND NOT (packet)-[:IN_PACKET]->(srcItf)
MERGE (srcItf)<-[:IN_PACKET {hop: $hop}]-(packet)
WITH r, srcItf, packet
// ACL IN:
MATCH (acl:AclRule {direction: 'ingress'})
WHERE ((acl)<-[:HAS_ACL_RULE]-(r) OR (acl)<-[:HAS_ACL_RULE]-(srcItf)) AND (acl.sIP = packet.srcIp OR acl.sIP = 'ANY') AND (acl.sPort = packet.srcPort OR acl.sPort = 'ANY') AND (acl.dIP = packet.dstIp OR acl.dIP = 'ANY') AND (acl.dPort = packet.dstPort OR acl.dPort = 'ANY') AND (acl.protocol = packet.protocol OR acl.protocol = 'ANY')
WITH r, packet, srcItf, acl ORDER BY acl.priority DESC
WITH r, packet, srcItf, head(collect(acl)) as inAclRule
MERGE (inAclRule)-[:APPLIES_IN_ACL {action: inAclRule.action, hop: $hop}]->(packet)
WITH r, packet, srcItf, inAclRule WHERE inAclRule.action = 'ACCEPT'
// DESTINATION:
OPTIONAL MATCH (r)-[:HAS_PORT]->(dest:Port {ipAddr: packet.dstIp})
WITH r, packet, srcItf, dest
FOREACH (x IN CASE WHEN dest IS NOT NULL THEN [dest] ELSE [] END |
    MERGE (packet)-[:REACHED {hop: $hop}]->(x)
)
// FORWARDING:
WITH r, packet, srcItf
MATCH (r)-[]->(d:Port)
CALL {
    WITH r, srcItf, packet, d
    MATCH (r)-[:HAS_FIB_ROUTE]->(fib:FibRoute)-[:EGRESS_INTERFACE]->(:Vlan)<-[:IS_VLAN_MEMBER]-(egrItf:Port)-[:HAS_LINK]->(i:Port)    
    WHERE i.ipAddr = packet.dstIp AND id(egrItf) <> id(srcItf) AND fib.to = packet.dstSubnet AND d.ipAddr <> packet.dstIp
    RETURN fib, egrItf
    UNION
    WITH r, srcItf, packet, d
    MATCH (r)-[:HAS_FIB_ROUTE]->(fib:FibRoute)-[:EGRESS_INTERFACE]->(egrItf:Port) 
    WHERE id(egrItf) <> id(srcItf) AND fib.to = packet.dstSubnet AND d.ipAddr <> packet.dstIp
    RETURN fib, egrItf
}
WITH DISTINCT r, packet, egrItf, fib
MERGE (fib)<-[:FWD_WITH {hop: $hop}]-(packet)
WITH r, packet, egrItf
// ACL OUT:
MATCH (acl:AclRule {direction: 'egress'})
WHERE ((acl)<-[:HAS_ACL_RULE]-(r) OR (acl)<-[:HAS_ACL_RULE]-(egrItf)) AND (acl.sIP = packet.srcIp OR acl.sIP = 'ANY') AND (acl.sPort = packet.srcPort OR acl.sPort = 'ANY') AND (acl.dIP = packet.dstIp OR acl.dIP = 'ANY') AND (acl.dPort = packet.dstPort OR acl.dPort = 'ANY') AND (acl.protocol = packet.protocol OR acl.protocol = 'ANY')
WITH packet, egrItf, acl ORDER BY acl.priority DESC
WITH packet, egrItf, head(collect(acl)) as outAclRule
MERGE (outAclRule)-[:APPLIES_OUT_ACL {action: outAclRule.action, hop: $hop}]->(packet)
WITH packet, egrItf, outAclRule WHERE outAclRule.action = 'ACCEPT'
MATCH (egrItf)-[:HAS_LINK]->(nh:Port)
CALL custom.trHop(id(packet),id(nh),$hop+1) YIELD ignored
RETURN null AS ignored", 'write')
```
```bash
// define the traceroute procedure 
CALL apoc.custom.declareProcedure("traceroute(src::STRING, dst_ip::STRING, dst_sn::STRING, dst_port::INT, protocol::STRING) :: (ignored::INT)", 
"MATCH (r:Router{name:$src})-[:HAS_PORT]->(srcItf:Port {name: 'Loopback0'})
MERGE (srcItf)<-[:IN_PACKET {hop: 0}]-(packet:Packet {srcIp: srcItf.ipAddr, srcPort: 49152, dstIp: $dst_ip, dstPort: $dst_port, protocol: $protocol, dstSubnet: $dst_sn})
WITH r, packet, srcItf
MATCH (r)-[]->(d:Port)
MATCH (r)-[:HAS_FIB_ROUTE]->(fib:FibRoute)-[:EGRESS_INTERFACE]->(egrItf:Port)
WHERE id(egrItf) <> id(srcItf) AND fib.to = packet.dstSubnet AND d.ipAddr <> packet.dstIp
WITH DISTINCT r, packet, egrItf, fib
MERGE (fib)<-[:FWD_WITH {hop: 0}]-(packet)
WITH r, egrItf, packet
MATCH (acl:AclRule {direction: 'egress'})
WHERE ((acl)<-[:HAS_ACL_RULE]-(r) OR (acl)<-[:HAS_ACL_RULE]-(egrItf)) AND (acl.sIP = packet.srcIp OR acl.sIP = 'ANY') AND (acl.sPort = packet.srcPort OR acl.sPort = 'ANY') AND (acl.dIP = packet.dstIp OR acl.dIP = 'ANY') AND (acl.dPort = packet.dstPort OR acl.dPort = 'ANY') AND (acl.protocol = packet.protocol OR acl.protocol = 'ANY')
WITH packet, egrItf, acl ORDER BY acl.priority DESC
WITH packet, egrItf, head(collect(acl)) as outAclRule
MERGE (outAclRule)-[:APPLIES_OUT_ACL {action: outAclRule.action, hop: 0}]->(packet)
WITH packet, egrItf, outAclRule WHERE outAclRule.action = 'ACCEPT'
MATCH (egrItf)-[:HAS_LINK]->(nh:Port)
CALL custom.trHop(id(packet), id(nh), 1) YIELD ignored
RETURN null AS ignored", 'write')
```

## Example

Clean results before running the example:
```bash
MATCH (n:Packet) DETACH DELETE n;
```

1. Simulate packet forwarding:
```bash
CALL custom.traceroute('leaf01', '10.0.104.104', '10.0.104.0/24', 33434, 'UDP') YIELD ignored
RETURN ignored;
```

2. Print detailed forwarding process:
```bash
MATCH (p:Packet)-[r]-(x)<-[:HAS_PORT|HAS_ACL_RULE|HAS_FIB_ROUTE*1..3]-(h:Host)
OPTIONAL MATCH (p)-[:REACHED]-(d)
WITH DISTINCT h,r,x ORDER BY r.hop, id(r)
RETURN h.name, r, x
```
