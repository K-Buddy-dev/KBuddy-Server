package com.example.kbuddy_backend.user.constant;

public enum Country {
	KR("South Korea", "KR"),
	US("United States", "US"),
	JP("Japan", "JP"),
	CN("China", "CN"),
	GB("United Kingdom", "GB"),
	DE("Germany", "DE"),
	FR("France", "FR"),
	IN("India", "IN"),
	BR("Brazil", "BR"),
	AU("Australia", "AU"),
	CA("Canada", "CA"),
	IT("Italy", "IT"),
	MX("Mexico", "MX"),
	RU("Russia", "RU"),
	ES("Spain", "ES"),
	NL("Netherlands", "NL"),
	SE("Sweden", "SE"),
	CH("Switzerland", "CH"),
	TR("Turkey", "TR");

	private final String name; // Display name like "South Korea"
	private final String code; // Country code like "KR"

	// Constructor
	Country(String name, String code) {
		this.name = name;
		this.code = code;
	}

	// Getter for the display name
	public String getName() {
		return name;
	}

	// Getter for the country code
	public String getCode() {
		return code;
	}
}
