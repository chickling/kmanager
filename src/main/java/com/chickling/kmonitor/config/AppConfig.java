package com.chickling.kmonitor.config;

/**
 * @author Hulva Luva.H
 *
 */
public class AppConfig {
	private String esHosts;
	private String esIndex;

	private Integer dataCollectFrequency = 1;

	private String zkHosts;
	private Integer zkSessionTimeout = 30 * 1000;
	private Integer zkConnectionTimeout = 30 * 1000;

	private Boolean isAlertEnabled = false;

	private Integer offsetInfoCacheQueue = 500;
	private Integer offsetInfoHandler;
	private String taskFolder = "tasks";

	private Boolean smtpAuth = false;
	private String smtpUser;
	private String smtpPasswd;
	private String smtpServer;
	private String mailSender;
	private String mailSubject;

	private Long excludeByLastSeen = 2592000L;

	public String getZkHosts() {
		return zkHosts;
	}

	public void setZkHosts(String zkHosts) {
		this.zkHosts = zkHosts;
	}

	public Boolean getIsAlertEnabled() {
		return isAlertEnabled;
	}

	public void setIsAlertEnabled(Boolean isAlertEnabled) {
		this.isAlertEnabled = isAlertEnabled;
	}

	public String getEsHosts() {
		return esHosts;
	}

	public void setEsHosts(String esHosts) {
		this.esHosts = esHosts;
	}

	public String getEsIndex() {
		return esIndex;
	}

	public void setEsIndex(String esIndex) {
		this.esIndex = esIndex;
	}

	public Integer getDataCollectFrequency() {
		return dataCollectFrequency;
	}

	public void setDataCollectFrequency(Integer dataCollectFrequency) {
		this.dataCollectFrequency = dataCollectFrequency;
	}

	public Integer getZkSessionTimeout() {
		return zkSessionTimeout;
	}

	public void setZkSessionTimeout(Integer zkSessionTimeout) {
		this.zkSessionTimeout = zkSessionTimeout;
	}

	public Integer getZkConnectionTimeout() {
		return zkConnectionTimeout;
	}

	public void setZkConnectionTimeout(Integer zkConnectionTimeout) {
		this.zkConnectionTimeout = zkConnectionTimeout;
	}

	public Integer getOffsetInfoCacheQueue() {
		return offsetInfoCacheQueue;
	}

	public void setOffsetInfoCacheQueue(Integer offsetInfoCacheQueue) {
		this.offsetInfoCacheQueue = offsetInfoCacheQueue;
	}

	public Integer getOffsetInfoHandler() {
		return offsetInfoHandler;
	}

	public void setOffsetInfoHandler(Integer offsetInfoHandler) {
		this.offsetInfoHandler = offsetInfoHandler;
	}

	public Boolean getSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(Boolean smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	public String getSmtpPasswd() {
		return smtpPasswd;
	}

	public void setSmtpPasswd(String smtpPasswd) {
		this.smtpPasswd = smtpPasswd;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getMailSender() {
		return mailSender;
	}

	public void setMailSender(String mailSender) {
		this.mailSender = mailSender;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	public String getTaskFolder() {
		return taskFolder;
	}

	public void setTaskFolder(String taskFolder) {
		this.taskFolder = taskFolder;
	}

	public Long getExcludeByLastSeen() {
		return excludeByLastSeen;
	}

	public void setExcludeByLastSeen(Long excludeByLastSeen) {
		this.excludeByLastSeen = excludeByLastSeen;
	}

}
