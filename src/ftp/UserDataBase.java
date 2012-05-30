package ftp;

import java.util.Hashtable;

public class UserDataBase {

	private Hashtable<String, String> table;
	
	public UserDataBase(){
		
		this.table = new Hashtable<String, String>();
		this.table.put( "almir", "1234" );
	}

	public boolean userPass( String username, String password ){

		if( this.table.containsKey( username ) )
			if( password.equals( this.table.get( username ) ) )
				return true;
		
		return false;
	}
	
}