package io.multiverse.smartrepo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.multiverse.smartrepo.common.JsonUtils;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class AclRule {
	
	public enum AclActionEnum {
		DROP("DROP"),
		ACCEPT("ACCEPT");
		private String value;
		private AclActionEnum(String value) { this.value = value; }
		public String getValue() { return this.value; }
	}

	@JsonProperty("TABLE")
	private String tableName;
	
	@JsonProperty("RULE")
	private String ruleName;
	
	@JsonProperty("PRIORITY")
	private String priority;
	
	@JsonProperty("ACTION")
	private AclActionEnum action;
	
	@JsonProperty(value = "SRC_IP", required = false)
	private String srcIp = "ANY";

	@JsonProperty(value = "SRC_PORT", required = false)
	private String srcPort = "ANY";

	@JsonProperty(value = "DST_IP", required = false)
	private String dstIp = "ANY";

	@JsonProperty(value = "DST_PORT", required = false)
	private String dstPort = "ANY";

	@JsonProperty(value = "PROTOCOL", required = false)
	private String protocol = "ANY";
	
	/*-----------------------------------------------*/

	public AclRule() {}
	public AclRule(JsonObject json) {
		JsonUtils.fromJson(json, this, AclRule.class);
	}

	/*-----------------------------------------------*/

	public JsonObject toJson() {
		return new JsonObject(JsonUtils.pojo2Json(this, false));
	}
	@Override
	public String toString() {
		return JsonUtils.pojo2Json(this, false);
	}
	@Override
	public boolean equals(Object obj) {
		return Objects.equals(toString(), ((AclRule) obj).toString());
	}
	@Override
	public int hashCode() {
		return Objects.hash(tableName+ruleName);
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public AclActionEnum getAction() {
		return action;
	}
	public void setAction(AclActionEnum action) {
		this.action = action;
	}
	public String getSrcIp() {
		return srcIp;
	}
	public void setSrcIp(String srcIp) {
		this.srcIp = srcIp;
	}
	public String getSrcPort() {
		return srcPort;
	}
	public void setSrcPort(String srcPort) {
		this.srcPort = srcPort;
	}
	public String getDstIp() {
		return dstIp;
	}
	public void setDstIp(String dstIp) {
		this.dstIp = dstIp;
	}
	public String getDstPort() {
		return dstPort;
	}
	public void setDstPort(String dstPort) {
		this.dstPort = dstPort;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}	
}
