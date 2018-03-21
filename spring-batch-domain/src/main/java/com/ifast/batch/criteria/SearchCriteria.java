package com.ifast.batch.criteria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ifast.batch.util.BeanUtil;

public class SearchCriteria {

	private static final String MODE_AND = "AND";
	private static final String MODE_OR = "OR";
	private String mode = MODE_AND;

	private Map<String, Boolean> orderByMap;

	private List<CriteriaSet> andCriterias;

	private List<CriteriaSet> orCriterias;

	private List<CriteriaSet> aliasCriterias;

	private List<String> distincts;

	private List<String> projections;

	private int maxResults;

	private int firstResult;

	public SearchCriteria() {
	}

	public SearchCriteria eq(String col, final Object value) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.EQ, mode, col, value));
		return this;
	}

	public SearchCriteria notEq(String col, final Object value) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.NE, mode, col, value));
		return this;
	}

	public SearchCriteria notIn(String col, final Object[] values) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.NOTIN, mode, col, values));
		return this;
	}

//	public SearchCriteria notIn(String col, List<Object> values) {
//		col = BeanUtil.convertColumnToProperty(col);
//		addCriteria(new CriteriaSet(Operator.NOTIN, mode, col, values));
//		return this;
//	}

	public SearchCriteria inArray(String col, final Object[] values) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.IN, mode, col, values));
		return this;
	}

	public SearchCriteria in(String col, final Object...values) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.IN, mode, col, values));
		return this;
	}

//	public SearchCriteria in(String col, List<Object> values) {
//		col = BeanUtil.convertColumnToProperty(col);
//		addCriteria(new CriteriaSet(Operator.IN, mode, col, values));
//		return this;
//	}

	public SearchCriteria like(String col, final Object value) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.LIKE, mode, col, value));
		return this;
	}

	public SearchCriteria greaterEq(String col, final Object value) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.GE, mode, col, value));
		return this;
	}

	public SearchCriteria lessEq(String col, final Object value) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.LE, mode, col, value));
		return this;
	}

	public SearchCriteria greaterThan(String col, final Object value) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.LE, mode, col, value));
		return this;
	}

	public SearchCriteria lessThan(String col, final Object value) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.LT, mode, col, value));
		return this;
	}

	public SearchCriteria isNotNull(String col) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.NOTNULL, mode, col, null));
		return this;
	}

	public SearchCriteria isNull(String col) {
		col = BeanUtil.convertColumnToProperty(col);
		addCriteria(new CriteriaSet(Operator.NULL, mode, col, null));
		return this;
	}

	public SearchCriteria or(final SearchCriteria criteria) {
		mode = MODE_OR;
		addCriteria(new CriteriaSet(MODE_OR, criteria));
		return this;
	}

	public SearchCriteria and(final SearchCriteria criteria) {
		mode = MODE_AND;
		addCriteria(new CriteriaSet(MODE_AND, criteria));
		return this;
	}
	public SearchCriteria orderBy(String col) {
		col = BeanUtil.convertColumnToProperty(col);
		getOrderByMap().put(col, false);
		return this;
	}
	public SearchCriteria orderBy(String col, final boolean desc) {
		col = BeanUtil.convertColumnToProperty(col);
		getOrderByMap().put(col, desc);
		return this;
	}

	public SearchCriteria addAlias(final String aliasName) {
	    getAliasCriterias().add(new CriteriaSet(aliasName));
	    return this;
	}

	public SearchCriteria or() {
		mode = MODE_OR;
		return this;
	}

	public SearchCriteria and() {
		mode = MODE_AND;
		return this;
	}

	public List<CriteriaSet> getAndCriterias() {
		if (andCriterias == null) {
			andCriterias = new ArrayList<>();
		}
		return andCriterias;
	}

	public List<CriteriaSet> getOrCriterias() {
		if (orCriterias == null) {
			orCriterias = new ArrayList<>();
		}
		return orCriterias;
	}

	public List<CriteriaSet> getAliasCriterias() {
	    if (aliasCriterias == null) {
	        aliasCriterias = new ArrayList<>();
	    }
	    return aliasCriterias;
	}

	public Map<String, Boolean> getOrderByMap() {
		if(orderByMap == null){
			orderByMap = new LinkedHashMap<>();
		}
		return orderByMap;
	}

	public List<String> getDistincts() {
		if (distincts == null) {
			distincts = new ArrayList<>();
		}
		return distincts;
	}

	public List<String> getProjections() {
		if (projections == null) {
			projections = new ArrayList<>();
		}
		return projections;
	}

	public SearchCriteria distinct(final String... cols) {
		for(String col : cols) {
			getDistincts().add(BeanUtil.convertColumnToProperty(col));
		}
		return this;
	}

	public SearchCriteria projection(final String... cols) {
		for(String col : cols) {
			getProjections().add(BeanUtil.convertColumnToProperty(col));
		}
		return this;
	}

	private void addCriteria(final CriteriaSet criteriaSet) {
		if (MODE_AND.equals(mode)) {
			getAndCriterias().add(criteriaSet);
		} else {
			getOrCriterias().add(criteriaSet);
		}
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(final int maxResults) {
		this.maxResults = maxResults;
	}

	public int getFirstResult() {
		return firstResult;
	}

	public void setFirstResult(final int firstResult) {
		this.firstResult = firstResult;
	}

}
