package io.multiverse.smartrepo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.multiverse.smartrepo.common.JsonUtils;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class BgpFilter {
	
	public enum FilterTypeEnum {
		inbound("inbound"),
		outbound("outbound");
		private String value;
		private FilterTypeEnum(String value) { this.value = value; }
		public String getValue() { return this.value; }
	}
	
	public enum FilterActionEnum {
		PERMIT("PERMIT"),
		DENY("DENY");
		private String value;
		private FilterActionEnum(String value) { this.value = value; }
		public String getValue() { return this.value; }
	}

	@JsonProperty("ACTION")
	private FilterActionEnum action;
	
	@JsonProperty("TYPE")
	private FilterTypeEnum type;
	
	@JsonProperty("PRIORITY")
	private int priority;
	
	@JsonProperty("PREFIX")
	private String prefix;

	@JsonProperty("FILTER_AS")
	private int filterAs;
	
	@JsonProperty(value = "SET_COMMUNITIES", required = false)
	private String setCommunities = null;
	
	@JsonProperty(value = "SET_LOCALPREF", required = false)
	private String setLocalPref = null;

	/*-----------------------------------------------*/

	public BgpFilter() {}
	public BgpFilter(JsonObject json) {
		JsonUtils.fromJson(json, this, BgpFilter.class);
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
		return Objects.equals(toString(), ((BgpFilter) obj).toString());
	}
	@Override
	public int hashCode() {
		return Objects.hash(action.getValue()+type.getValue()+priority+prefix);
	}
	public FilterActionEnum getAction() {
		return action;
	}
	public void setAction(FilterActionEnum action) {
		this.action = action;
	}
	public FilterTypeEnum getType() {
		return type;
	}
	public void setType(FilterTypeEnum type) {
		this.type = type;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public int getFilterAs() {
		return filterAs;
	}
	public void setFilterAs(int filterAs) {
		this.filterAs = filterAs;
	}
	public String getSetCommunities() {
		return setCommunities;
	}
	public void setSetCommunities(String setCommunities) {
		this.setCommunities = setCommunities;
	}
	public String getSetLocalPref() {
		return setLocalPref;
	}
	public void setSetLocalPref(String setLocalPref) {
		this.setLocalPref = setLocalPref;
	}
}
