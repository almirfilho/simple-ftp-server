package ftp;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FtpServer {
	
	/* ------------------------------
	 * --- atributos ----------------
	 ------------------------------*/
	
	private int controlPort;
	
	private int dataPort;
	
	private ServerSocket controlServerSocket = null;
	
	private ServerSocket dataServerSocket = null;
	
	private Socket controlSocket = null;
	
	private Socket controlSocket2 = null;
	
	private Socket dataSocket = null;
	
	private Thread connectionListener;
	
	private PrintWriter out;
	
	private BufferedReader in;
	
	private UserDataBase bd;
	
	private String user;
	
	private boolean logged;
	
	private FtpCommand currentCommand;
	
	private String buffer;
	
	private File fileManager;
	
	private String rootDirectory;
	
	private boolean canAcceptNewConnection = false;
	
	/* ------------------------------
	 * --- construtor ---------------
	 ------------------------------*/

	public FtpServer( String root, int port ){
		
		if( port < 0 )
			port = 0;
		
		this.controlPort = port;
		this.rootDirectory = root;
		this.bd = new UserDataBase();
		this.currentCommand = new FtpCommand();
		this.fileManager = new File( this.rootDirectory );
		
		try {
			
			this.controlServerSocket = new ServerSocket( this.controlPort );
			
		} catch( IOException e ){
			
			System.out.println( "Não foi possível atribuir listening para a porta " + this.controlPort );
			System.exit(-1);
		}
	}
	
	/* ------------------------------
	 * --- metodos ------------------
	 ------------------------------*/
	
	public void start(){
		
		while( true ){
			
			try {
				
				if( this.canAcceptNewConnection ){
					
					this.controlSocket = this.controlSocket2;
					this.canAcceptNewConnection = false;
					
				} else {
					
					System.out.println( "Esperando requisição de conexão..." );
					this.controlSocket = this.controlServerSocket.accept();
					System.out.println( "Requisição de conexão recebida." );
				}
				
				this.out = new PrintWriter( this.controlSocket.getOutputStream(), true );
				this.in = new BufferedReader( new InputStreamReader( this.controlSocket.getInputStream() ) );
				
				this.connectionListener = new Thread( new ConnectionListener( this ) );
				this.connectionListener.start();
				
			} catch( IOException e ){
				
				System.out.println( "Requisição falhou." );
				System.exit(-1);
			}
			
			this.out.println( "220 por favor informe seu username" );
			
			this.buffer = this.listenClient();
			this.currentCommand.set( this.buffer );

			while( !this.canAcceptNewConnection ){
				
				this.execute( this.currentCommand );
				this.buffer = this.listenClient();
				
				if( this.buffer == null )
					break;
				
				this.currentCommand.set( this.buffer );
			}
				
			this.disconnect();
		}
	}
	
	private boolean disconnect(){
		
		try {
			
			//this.controlServerSocket.close();
			if( this.dataServerSocket != null )
				this.dataServerSocket.close();
			
			this.out.close();
			this.in.close();
			System.out.println( "Conexões fechadas com sucesso." );
			
			return true;
			
		} catch( IOException e ){
			
			System.out.println( "Ocorreu um erro ao tentar fechar alguma conexão." );
		}
		
		return false;
	}
	
	private void execute( FtpCommand command ){
		
		System.out.println( "Cliente: " + command.toString() );
		
		if( command.getType().equals( FtpCommandType.USER ) )
			this.user( command.getParam(0) );
		
		else if( command.getType().equals( FtpCommandType.PASS ) )
			this.pass( command.getParam(0) );
		
		else if( this.logged ){
			
			if( command.getType().equals( FtpCommandType.SYST ) )
				this.syst();
			
			else if( command.getType().equals( FtpCommandType.FEAT ) )
				this.feat();
			
			else if( command.getType().equals( FtpCommandType.PWD ) )
				this.pwd();
			
			else if( command.getType().equals( FtpCommandType.TYPE ) )
				this.type( command.getParam(0) );
			
			else if( command.getType().equals( FtpCommandType.PASV ) )
				this.pasv();
			
			else if( command.getType().equals( FtpCommandType.LIST ) )
				this.list();
			
			else if( command.getType().equals( FtpCommandType.CWD ) )
				this.cwd( command.getParam(0) );
			
			else if( command.getType().equals( FtpCommandType.CDUP ) )
				this.cdup();
			
			else if( command.getType().equals( FtpCommandType.EPSV ) )
				this.out.println( "202 Comando nao implementado neste servidor (EPSV)." );
			
			else if( command.getType().equals( FtpCommandType.STOR ) )
				this.stor( command.getParam(0) );
			
			else if( command.getType().equals( FtpCommandType.RETR ) )
				this.retr( command.getParam(0) );
		}
	}
	
	private void user( String username ){
		
		this.user = username;
		this.out.println( "331 por favor informe sua senha" );
	}
	
	private void pass( String password ){
		
		if( this.bd.userPass( this.user, password ) ){
			
			this.logged = true;
			this.out.println( "230 login realizado com sucesso" );
		
		} else
			this.out.println( "530 username/password incorretos" );
	}
	
	private void syst(){
		
		this.out.println( "215 Mac OS 10.6" );
	}
	
	private void feat(){
		
		this.out.println( "211- Extecoes suportadas:" );
		this.out.println( "PASV" );
		this.out.println( "211 Fim." );
	}
	
	private void pwd(){
		
		this.out.println( "257 " + this.fileManager.getPath() );
	}
	
	private void type( String param ){
		
		String message = "";
		
		if( param.equals( "I" ) )
			message = "Tipo de transferencia agora eh 8-bits packed (bytes)";
		
		else if( param.equals( "A" ) )
			message = "Tipo de transferencia agora eh ASCII";
		
		this.out.println( "200 " + message );
	}
	
	private void pasv(){
		
		try {
			
			this.dataServerSocket = new ServerSocket( 0 );
			this.dataPort = this.dataServerSocket.getLocalPort();
			
			int p1 = this.dataPort / 256;
			int p2 = this.dataPort % 256;
			
//			System.out.println(this.dataPort);
			
			String str = new String();
	        try {
	            str = java.net.InetAddress.getLocalHost().getHostAddress();
	        } catch (UnknownHostException ex) {
	            System.out.println("fudeuu!");
	        }

	        str = str.replace('.', ',');
			
			this.out.println( "227 Entrando em modo passivo (" + str + "," + p1 + "," + p2 + ")" );
			this.dataSocket = this.dataServerSocket.accept();
			
		} catch( IOException e ){
			
			System.out.println( "Não foi possível criar socket para a porta " + this.dataPort );
			this.out.println( "425 Nao foi possivel abrir uma conexao de dados" );
		}
	}
	
	private void list(){
		
		this.out.println( "150 Abrindo conexao para listagem" );
		
		if( this.fileManager.isDirectory() ){
			
			File[] files = this.fileManager.listFiles();
			String list = "";
			
			if( files != null ){
			
				String type;
				DateFormat date = new SimpleDateFormat( "MMM  dd hh:mm" );
				
				for( File file : files ){
					
					if( file.isDirectory() )
						type = "d";
					else
						type = "-";
					
					list += type + "rwxrwxrw-   1 ftpuser  ftpuser  " + file.length() + " " + date.format( new Date( file.lastModified() ) ) + " " + file.getName() + "\r\n";
				}
			}
			
			try {
				
				PrintWriter outData = new PrintWriter( this.dataSocket.getOutputStream() );
				outData.print( list );
				outData.flush();
				outData.close();
				
				this.dataSocket.close();
				this.out.println( "226 Transferencia realizada com sucesso" );
				
			} catch( IOException e ){
				
				this.out.println( "451 A ultima acao terminou anormalmente" );
				System.out.println( "deu pau na hora de fechar o socket" );
			}
		}
	}
	
	private void cwd( String param ){

		String old = this.fileManager.getPath();
		
		if( !param.startsWith("/") )
			param = old + File.separator + param;
		
		this.fileManager = new File( param );
		
		if( !this.fileManager.isDirectory() )
			this.fileManager = new File( old );

		this.out.println( "200 cwd OK" );
//		this.pwd();
//		this.pasv();
//		this.list();
	}
	
	private void cdup(){
		
		String path = this.fileManager.getPath();

		if( this.fileManager.isDirectory() ){
			
			path = path.substring( 0, path.lastIndexOf( File.separator ) );
		
		} else {
			
			path = path.substring( 0, path.lastIndexOf( File.separator ) );
			path = path.substring( 0, path.lastIndexOf( File.separator ) );
		}
		
		this.fileManager = new File( path );
		this.out.println( "200 cdup OK" );
//		System.out.println(this.fileManager.getPath());
//		this.pwd();
//		this.pasv();
//		this.list();
	}
	
	private void stor( String filename ){
		
		File file = new File( this.fileManager.getPath() + File.separator + filename );
		System.out.println(file.getPath());
        try {
        	
        	if( file.exists() )
        		if( !file.delete() )
        			this.out.println( "550 erro: o arquivo existente não pode ser substituído" );
        
            if( file.createNewFile() ) {
            	
                this.out.println( "125 Abrindo conexão pra transferência" );

                InputStream input 		= this.dataSocket.getInputStream();
                RandomAccessFile writer = new RandomAccessFile( file, "rw" );
                byte[] bytes 			= new byte[ 256 ];
                
                int stream = input.read( bytes );
                
                while( stream > 0 ){
                	
                    writer.write( bytes, 0, stream );
                    stream = input.read( bytes );
                }
                

                writer.close();
                input.close();
                
                this.out.println( "226 Transferência completa" );
                this.dataSocket.close();
                
            } else {
            	
                this.out.println( "550 Não foi possível criar o arquivo " + filename );
            }
            
        } catch( IOException ex ){
        	
            System.out.println( "Erro em stor()" );
        }
	}
	
	private void retr( String filename ){
		
		OutputStream output = null;
		
		this.out.println( "150 Preparando-se para tranferir arquivo" );
		
		try {
			
			output 					= this.dataSocket.getOutputStream();
            File file 				= new File( this.fileManager.getPath() + File.separator + filename );
            RandomAccessFile reader = new RandomAccessFile( file, "rw" );
            byte[] fileBytes 		= new byte[ 256 ];
            
            while( reader.getFilePointer() < reader.length() )
                output.write( fileBytes, 0, reader.read( fileBytes ) );

            output.flush();
            output.close();
            
            this.out.println( "226 transferencia de " + filename + " concluída" );
			
		} catch( IOException e ){
			
			System.out.println( "DEU PAU!" );
		}
	}
	
	private String listenClient(){
		
		String str = null;

		try {
			str = this.in.readLine();
		} catch( IOException e ){
			str = null;
		}

		return str;
	}
	
	public void setNewSocket( Socket socket ){
		
		this.controlSocket2 = socket;
		this.canAcceptNewConnection = true;
		
		try {
			this.controlSocket.close();
		} catch (IOException e) {
			System.out.println( "Não foi possivel fechar Socket" );
		}
	}
	
	public ServerSocket getServerSocket(){
		
		return this.controlServerSocket;
	}
	
}