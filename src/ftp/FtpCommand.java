package ftp;

import java.util.ArrayList;

public class FtpCommand {

	private String type;
	
	private ArrayList<String> params;
	
	public FtpCommand(){
		
		this.params = new ArrayList<String>();
	}
	
	public FtpCommand( String command ){
		
		super();
		this.set( command );
	}
	
	private String extractType( String command ){
		
		if( command != null ){
			
			String[] array = command.split( " ", 2 );
			command = array[0];
			
			if( command.equals( FtpCommandType.USER ) )
				return FtpCommandType.USER;
			
			else if( command.equals( FtpCommandType.PASS ) )
				return FtpCommandType.PASS;
			
			else if( command.equals( FtpCommandType.SYST ) )
				return FtpCommandType.SYST;
			
			else if( command.equals( FtpCommandType.FEAT ) )
				return FtpCommandType.FEAT;
			
			else if( command.equals( FtpCommandType.PWD ) )
				return FtpCommandType.PWD;
			
			else if( command.equals( FtpCommandType.TYPE ) )
				return FtpCommandType.TYPE;
			
			else if( command.equals( FtpCommandType.PASV ) )
				return FtpCommandType.PASV;
			
			else if( command.equals( FtpCommandType.LIST ) )
				return FtpCommandType.LIST;
			
			else if( command.equals( FtpCommandType.CWD ) )
				return FtpCommandType.CWD;
			
			else if( command.equals( FtpCommandType.CDUP ) )
				return FtpCommandType.CDUP;
			
			else if( command.equals( FtpCommandType.EPSV ) )
				return FtpCommandType.EPSV;
			
			else if( command.equals( FtpCommandType.STOR ) )
				return FtpCommandType.STOR;
			
			else if( command.equals( FtpCommandType.RETR ) )
				return FtpCommandType.RETR;
			
			return FtpCommandType.UNKNOWN;
		}
		
		return FtpCommandType.NONE;
	}
	
	private ArrayList<String> extractParams( String command ){
		
		String[] array = command.split( " ", 2 );
		
		if( array.length > 1 )
			for( int i = 1; i < array.length; i++ )
				params.add( array[i] );
		
		return params;
	}

	public void set( String command ){
		
		this.type = this.extractType( command );
		this.params.clear();
		
		if( !this.type.equals( FtpCommandType.UNKNOWN ) && !this.type.equals( FtpCommandType.NONE ) )
			this.params = this.extractParams( command );
	}
	
	public String toString(){
		
		String str = "";
		str += this.type;
		
		for( String param : this.params )
			str += " " + param;
		
		return str;
	}
	
	public String getType(){
		
		return this.type;
	}

	public String getParam( int index ){
		
		if( index < this.params.size() )
			return this.params.get( index );
		
		return null;
	}

	public ArrayList<String> getParams(){
		
		return this.params;
	}
	
}
