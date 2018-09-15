package com.chickling.kmanager.config;

/**
 * @author Hulva Luva.H
 *
 */
public class AppConfig {
  private String clusterName;
  private String bootstrapServers;
  private String apiType = "";
  private String esHosts;
  private String esIndex = "";
  private String esTempName = "";
  private Integer dataCollectFrequency = 1;

  private String zkHosts;
  private Integer zkSessionTimeout = 30 * 1000;
  private Integer zkConnectionTimeout = 30 * 1000;

  private Boolean isAlertEnabled = false;

  private Integer offsetInfoCacheQueue = 5000;
  private Integer offsetInfoHandler;
  private String taskFolder = "tasks";

  private Boolean smtpAuth = false;
  private String smtpUser;
  private String smtpPasswd;
  private String smtpServer;
  private String mailSender;
  private String mailSubject;

  private Long excludeByLastSeen = 2592000L;

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public void setBootstrapServers(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  public String getApiType() {
    return apiType;
  }

  public void setApiType(String apiType) {
    this.apiType = apiType;
  }

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
    return esIndex.toLowerCase();
  }

  public void setEsIndex(String esIndex) {
    this.esIndex = esIndex;
  }

  public String getEsTempName() {
    return esTempName.toLowerCase();
  }

  public void setEsTempName(String esTempName) {
    this.esTempName = esTempName;
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

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getClusterName() {
    if (this.clusterName == null) {
      return "";
    }
    return this.clusterName;
  }

  @Override
  public String toString() {
    return "AppConfig [clusterName=" + clusterName + ", bootstrapServers=" + bootstrapServers + ", apiType=" + apiType + ", esHosts="
        + esHosts + ", esIndex=" + esIndex + ", dataCollectFrequency=" + dataCollectFrequency + ", zkHosts=" + zkHosts
        + ", zkSessionTimeout=" + zkSessionTimeout + ", zkConnectionTimeout=" + zkConnectionTimeout + ", isAlertEnabled=" + isAlertEnabled
        + ", offsetInfoCacheQueue=" + offsetInfoCacheQueue + ", offsetInfoHandler=" + offsetInfoHandler + ", taskFolder=" + taskFolder
        + ", smtpAuth=" + smtpAuth + ", smtpUser=" + smtpUser + ", smtpServer=" + smtpServer + ", mailSender=" + mailSender
        + ", mailSubject=" + mailSubject + ", excludeByLastSeen=" + excludeByLastSeen + "]";
  }


}
