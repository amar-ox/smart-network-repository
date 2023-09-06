package io.multiverse.smartrepo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.multiverse.smartrepo.common.JsonUtils;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class Vlan {
	
	public static enum VlanModeEnum {
		tagged("tagged"),
		untagged("untagged"),
		undefined("undefined");
		private String value;
		private VlanModeEnum(String value) { this.value = value; }
		public String getValue() { return this.value; }
	}

	@JsonProperty("NAME")
	private String name;
	
	@JsonProperty("VID")
	private String vid;
	
	@JsonProperty("MEMBER")
	private String member;
	
	@JsonProperty("MODE")
	private VlanModeEnum mode;
	
	/*-----------------------------------------------*/

	public Vlan() {}
	public Vlan(JsonObject json) {
		JsonUtils.fromJson(json, this, Vlan.class);
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
		return Objects.equals(toString(), ((Vlan) obj).toString());
	}
	@Override
	public int hashCode() {
		return Objects.hash(name+vid+member);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVid() {
		return vid;
	}
	public void setVid(String vid) {
		this.vid = vid;
	}
	public String getMember() {
		return member;
	}
	public void setMember(String member) {
		this.member = member;
	}
	public VlanModeEnum getMode() {
		return mode;
	}
	public void setMode(VlanModeEnum mode) {
		this.mode = mode;
	}

	/*-----------------------------------------------*/
}
