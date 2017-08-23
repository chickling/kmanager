package com.chickling.kmanager.config;

/**
 * @author Hulva Luva.H
 *
 */
public class DataSourceConfig {
	private String dbName;
	private String tableName;

	private String driverClassName;
	private String url;
	private String username;
	private String password;

	public DataSourceConfig() {
		super();
	}

	public DataSourceConfig(String driverClassName, String url, String username, String password) {
		super();
		this.driverClassName = driverClassName;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public DataSourceConfig(String dbName, String tableName, String driverClassName, String url, String username,
			String password) {
		super();
		this.dbName = dbName;
		this.tableName = tableName;
		this.driverClassName = driverClassName;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
