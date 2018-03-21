package com.ifast.batch.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ifast.batch.criteria.CriteriaSet;
import com.ifast.batch.criteria.Operator;

public class ConditionBuilder {

	private static final String MODE_AND = "AND";
	private static final String MODE_OR = "OR";
	private String mode = MODE_AND;
	private List<CriteriaSet> andCriterias;

	private List<CriteriaSet> orCriterias;


	ConditionBuilder() {
	}


	public ConditionBuilder eq(String col, String namedParam) {
		addCriteria(new CriteriaSet(Operator.EQ, mode, col, namedParam));
		return this;
	}

	public ConditionBuilder eqProperty(String col, String colEq) {
		addCriteria(new CriteriaSet(Operator.EQPROP, mode, col, colEq));
		return this;
	}

	public ConditionBuilder notEq(String col, String namedParam) {
		addCriteria(new CriteriaSet(Operator.NE, mode, col, namedParam));
		return this;
	}

	public ConditionBuilder notIn(String col, String namedParam) {
		addCriteria(new CriteriaSet(Operator.NOTIN, mode, col, namedParam));
		return this;
	}

	public ConditionBuilder in(String col, String namedParam) {
		addCriteria(new CriteriaSet(Operator.IN, mode, col, namedParam));
		return this;
	}

	public ConditionBuilder like(String col, String namedParam) {
		addCriteria(new CriteriaSet(Operator.LIKE, mode, col, namedParam));
		return this;
	}

	public ConditionBuilder greaterEq(String col, String namedParam) {
		addCriteria(new CriteriaSet(Operator.GE, mode, col, namedParam));
		return this;
	}

	public ConditionBuilder lessEq(String col, String namedParam) {
		addCriteria(new CriteriaSet(Operator.LE, mode, col, namedParam));
		return this;
	}

	public ConditionBuilder greaterThan(String col, String namedParam) {
		addCriteria(new CriteriaSet(Operator.GT, mode, col, namedParam));
		return this;
	}

	public ConditionBuilder lessThan(String col, String namedParam) {
		addCriteria(new CriteriaSet(Operator.LT, mode, col, namedParam));
		return this;
	}

	public ConditionBuilder isNotNull(String col) {
		addCriteria(new CriteriaSet(Operator.NOTNULL, mode, col, null));
		return this;
	}

	public ConditionBuilder isNull(String col) {
		addCriteria(new CriteriaSet(Operator.NULL, mode, col, null));
		return this;
	}

	public ConditionBuilder or(ConditionBuilder criteria) {
		mode = MODE_OR;
		addCriteria(new CriteriaSet(MODE_OR, criteria));
		return this;
	}

	public ConditionBuilder and(ConditionBuilder criteria) {
		mode = MODE_AND;
		addCriteria(new CriteriaSet(MODE_AND, criteria));
		return this;
	}

	public ConditionBuilder or() {
		mode = MODE_OR;
		return this;
	}

	public ConditionBuilder and() {
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

	private void addCriteria(CriteriaSet criteriaSet) {
		if (MODE_AND.equals(mode)) {
			getAndCriterias().add(criteriaSet);
		} else {
			getOrCriterias().add(criteriaSet);
		}
	}

	public String toSql() {
		StringBuffer conditionSql = new StringBuffer();
		if (andCriterias != null) {
			Iterator<CriteriaSet> andCriteriaSets = andCriterias.iterator();
			while (andCriteriaSets.hasNext()) {
				CriteriaSet criteriaSet = andCriteriaSets.next();
				String criteriaSql = createCriteria(criteriaSet);
				conditionSql.append(criteriaSql);

				if (andCriteriaSets.hasNext()) {
					conditionSql.append(" and ");
				}
			}
		}
		if (orCriterias != null) {

			Iterator<CriteriaSet> orCriteriaSets = orCriterias.iterator();
			if (StringUtils.isNotEmpty(conditionSql) && orCriteriaSets.hasNext()) {
				conditionSql.append(" or ");
			}

			while (orCriteriaSets.hasNext()) {
				CriteriaSet criteriaSet = orCriteriaSets.next();
				String criteriaSql = createCriteria(criteriaSet);
				conditionSql.append(criteriaSql);
				if (orCriteriaSets.hasNext()) {
					conditionSql.append(" or ");
				}
			}
		}
		return conditionSql.toString();
	}

	private String createCriteria(CriteriaSet set) {
		if (set.getOperator() != null) {
			return getCriteriaString(set.getOperator(), set.getCol(), set.getVal());
		} else {
			if (set.getConditionBuilder() != null) {
				ConditionBuilder nestedCondition = set.getConditionBuilder();
				StringBuffer buffer = new StringBuffer();
				buffer.append("(");
				buffer.append(nestedCondition.toSql());
				buffer.append(")");
				return buffer.toString();
			}
		}
		return "";
	}

	private String getCriteriaString(Operator operator, String col, Object namedParam) {
		switch (operator) {
		case NULL:
		case NOTNULL:
			return SqlBuilderUtil.convertToTableField(col) + " " + operator.getOperator();
		case IN:
		case NOTIN:
			return SqlBuilderUtil.convertToTableField(col) + " " + operator.getOperator() + " (:" + namedParam + ")";
		case EQPROP:
		case NEPROP:
			return SqlBuilderUtil.convertToTableField(col) + " " + operator.getOperator() + " "
					+ SqlBuilderUtil.convertToTableField(namedParam.toString());
		default:
			return SqlBuilderUtil.convertToTableField(col) + " " + operator.getOperator() + " :" + namedParam;
		}
	}

	public boolean isEmpty() {
		return getAndCriterias().isEmpty() && getOrCriterias().isEmpty();
	}

}
