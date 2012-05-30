package ftp;

public abstract class FtpCommandType {

	public static final String USER = "USER";
	public static final String PASS = "PASS";
	public static final String SYST = "SYST";
	public static final String FEAT = "FEAT";
	public static final String PWD = "PWD";
	public static final String TYPE = "TYPE";
	public static final String PASV = "PASV";
	public static final String LIST = "LIST";
	public static final String CWD = "CWD";
	public static final String CDUP = "CDUP";
	public static final String EPSV = "EPSV";
	public static final String STOR = "STOR";
	public static final String RETR = "RETR";
	
	public static final String NONE = "NONE";
	public static final String UNKNOWN = "UNKNOWN COMMAND";

}
