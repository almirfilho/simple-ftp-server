import ftp.*;

public class TestDrive {

	public static void main( String args[] ){
		
		FtpServer server = new FtpServer( "/Users/almirfilho/Desktop/ftp_folder", 12345 );
		server.start();
	}
}
