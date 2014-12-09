/**
 * 
 */
package com.sfpay.tools.database;

/**
 * @author 625288
 *
 * 2014年11月15日
 */
public class MapperData {
	private String sqlName;
	private String fieldName;
	private String jdbcType;
	private boolean isPrimaryKey;
	
	public MapperData(String sqlName, String fieldName, String jdbcType) {
		this.sqlName = sqlName;
		this.fieldName = fieldName;
		this.jdbcType = jdbcType;
	}
	
	public MapperData(String sqlName, String fieldName, String jdbcType, boolean isPrimaryKey) {
		this.sqlName = sqlName;
		this.fieldName = fieldName;
		this.jdbcType = jdbcType;
		this.isPrimaryKey = isPrimaryKey;
	}

	public String getSqlName() {
		return sqlName;
	}

	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getJdbcType() {
		return jdbcType;
	}

	public void setJdbcType(String jdbcType) {
		this.jdbcType = jdbcType;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public void setPrimaryKey(boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}
}
