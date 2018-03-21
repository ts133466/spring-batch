package com.ifast.batch.dao;

import org.apache.commons.lang3.CharUtils;

class SqlBuilderUtil {

	public static String convertToTableField(String field) {

		StringBuffer fieldName = new StringBuffer();
		String classFieldName = field;

		int indexOfTableAlias = field.indexOf(".");
		String tableAlias = "";

		if (indexOfTableAlias >= 0) {
			tableAlias = field.substring(0, indexOfTableAlias);
			fieldName.append(tableAlias);
			fieldName.append(".");

		}

		int lastIndexOfDot = field.lastIndexOf(".");
		if (lastIndexOfDot >= 0) {
			classFieldName = field.substring(lastIndexOfDot + 1);
		}
		for (char c : classFieldName.toCharArray()) {
			if (CharUtils.isAsciiAlphaUpper(c)) {
				fieldName.append("_");
			}
			fieldName.append(c);
		}
		return fieldName.toString();
	}

	public static String convertToTableName(String clazzName) {
		StringBuffer tableName = new StringBuffer();

		int lastIndexOfDot = clazzName.lastIndexOf(".");
		clazzName = clazzName.substring(lastIndexOfDot + 1);

		if (clazzName.endsWith("Bean")) {
			clazzName = clazzName.substring(0, clazzName.length() - 4);
		}

		for (char c : clazzName.toCharArray()) {
			if (CharUtils.isAsciiAlphaUpper(c)) {
				if (tableName.length() > 0) {
					tableName.append("_");
				}
			}
			tableName.append(c);
		}
		return tableName.toString();
	}
}
