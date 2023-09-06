package io.multiverse.smartrepo.model;

public enum HostTypeEnum {

	SpineRouter("SpineRouter"),
	LeafRouter("LeafRouter"),
	BorderRouter("BorderRouter"),
	Firewall("Firewall"),
	Server("Server"),
	Switch("Switch");

	private String value;
	private HostTypeEnum(String value) { this.value = value; }
	public String getValue() { return this.value; }
}
