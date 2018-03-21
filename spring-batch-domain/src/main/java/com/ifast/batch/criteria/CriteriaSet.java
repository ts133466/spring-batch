package com.ifast.batch.criteria;

import com.ifast.batch.dao.ConditionBuilder;

public class CriteriaSet {

	private String col;
	private Operator operator;
	private Object val;
	private String jointType;
	private SearchCriteria sc;
	private ConditionBuilder conditionBuilder;
	private String alias;

	public CriteriaSet(Operator operator, String jointType, String col, Object val) {
		this.col = col;
		this.operator = operator;
		this.val = val;
		this.jointType = jointType;
	}

	public CriteriaSet(String jointType, SearchCriteria sc) {
		this.jointType = jointType;
		this.sc = sc;
	}

	public CriteriaSet(Operator operator, String jointType, String col) {
		this.operator = operator;
		this.col = col;
		this.jointType = jointType;
	}
	
	public CriteriaSet(String alias) {
	    this.alias = alias;
	}

	public CriteriaSet(String jointType, ConditionBuilder conditionBuilder) {
        this.jointType = jointType;
        this.conditionBuilder = conditionBuilder;
    }

	public String getCol() {
		return col;
	}

	public Operator getOperator() {
		return operator;
	}

	public Object getVal() {
		return val;
	}

	public String getJointType() {
		return jointType;
	}

	public String getAlias() {
	    return alias;
	}

	public SearchCriteria getSc() {
		return sc;
	}

	public ConditionBuilder getConditionBuilder() {
        return conditionBuilder;
    }
	
}
