package com.ifast.batch.criteria;

public enum Operator {

	EQ("="), NE("!="), IN("in"), NOTIN("not in"), GE(">="), LE("<="), GT(">"), LT("<"), 
	LIKE("like"), NULL("is null"),NOTNULL("is not null"), EQPROP("="),NEPROP("!=");
	
	private String criteria;
	
	private Operator(String criteria) {
		this.criteria = criteria;
	}
	
	public String getOperator() {
		return criteria;
	}
	
}
