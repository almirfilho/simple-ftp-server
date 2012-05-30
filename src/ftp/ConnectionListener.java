package ftp;

import java.io.IOException;
import java.net.Socket;

public class ConnectionListener implements Runnable {

	FtpServer ftpServer;
	
	public ConnectionListener( FtpServer ftpServer ){
		
		this.ftpServer = ftpServer;
	}
	
	public void run(){
		
		try {
			
			System.out.println( "Listener: Iniciado e esperando nova conexão.\n" );
			Socket newSocket = ftpServer.getServerSocket().accept();
			System.out.println( "\nListener: Nova conexão aceita." );
			ftpServer.setNewSocket( newSocket );
			
		} catch (IOException e) {

			System.out.println( "Listener: Não foi possivel aceitar nova conexão." );
		}
	}
	
}