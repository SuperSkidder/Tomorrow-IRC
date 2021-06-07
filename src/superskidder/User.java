package superskidder;

public class User {
	private String authName;
	private String password;
	private String GameID;
	private String hwid;

	public User(String authName, String password, String hwid, String GameID) {
		this.authName = authName;
		this.password = password;
		this.GameID = GameID;
		this.hwid = hwid;
	}
	
	public String getHwid() {
		return hwid;
	}

	public void setHwid(String hwid) {
		this.hwid = hwid;
	}
	
	public String getAuthName() {
		return authName;
	}

	public void setAuthName(String authName) {
		this.authName = authName;
	}

	public String getGameID() {
		return GameID;
	}

	public void setGameID(String GameID) {
		this.GameID = GameID;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
