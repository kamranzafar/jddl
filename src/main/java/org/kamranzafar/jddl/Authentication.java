package org.kamranzafar.jddl;

public class Authentication {
	public static enum AuthType {
		BASIC
	}

	private AuthType authType = AuthType.BASIC;
	private String username;
	private String password;

	public Authentication() {
	}

	public Authentication(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public AuthType getAuthType() {
		return authType;
	}

	public void setAuthType(AuthType authType) {
		this.authType = authType;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
