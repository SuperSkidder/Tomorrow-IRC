package superskidder;

public class User {
	public String authName;
	public String password;
	public String GameID;
	public String hwid;
	public String head;
	public boolean connected;

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
