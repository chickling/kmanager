package com.chickling.kmanager.config;

/**
 * @author Hulva Luva.H
 *
 */
public class MailServerConfig {
	private String smtpHost;
	private Boolean authenticate;
	private String user;
	private String password;

	public MailServerConfig() {
		super();
	}

	public MailServerConfig(String smtpHost, Boolean authenticate, String user, String password) {
		super();
		this.smtpHost = smtpHost;
		this.authenticate = authenticate;
		this.user = user;
		this.password = password;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public Boolean getAuthenticate() {
		return authenticate;
	}

	public void setAuthenticate(Boolean authenticate) {
		this.authenticate = authenticate;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
