package io.multiverse.smartrepo.model;

public enum HostTypeEnum {

	Spine("Spine"),
	Leaf("Leaf"),
	Border("Border"),
	Firewall("Firewall"),
	Server("Server"),
	Switch("Switch");

	private String value;
	private HostTypeEnum(String value) { this.value = value; }
	public String getValue() { return this.value; }
}
