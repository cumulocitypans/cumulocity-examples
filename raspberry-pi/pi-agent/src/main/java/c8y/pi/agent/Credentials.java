package c8y.pi.agent;

public class Credentials {
	public Credentials(String host, String tenant, String user, String password, String key) {
		this.host = host;
		this.tenant = tenant;
		this.user = user;
		this.password = password;
		this.key = key;
	}

	public String getHost() {
		return host;
	}

	public String getTenant() {
		return tenant;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
	
	public String getKey() {
		return key;
	}

	private String host;
	private String tenant;
	private String user;
	private String password;
	private String key;
}
