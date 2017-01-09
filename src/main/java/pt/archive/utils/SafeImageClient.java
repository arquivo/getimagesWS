package pt.archive.utils;

import org.slf4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class SafeImageClient {

	
	public static char getSafeImage( String imgBase64 , String host , float safeValue , Logger log ) {
		try{
			log.info( "Call safeImage API host["+host+"]" );
			
			Client client = Client.create( );
		    client.setConnectTimeout( Constants.timeoutConn );
		    client.setReadTimeout( Constants.timeoutreadConn );
			WebResource webResource = client.resource( host );
			ClientResponse response = webResource.type( "application/json" )
						.post( ClientResponse.class , imgBase64 );

			if ( response.getStatus( ) != 200 ) {
				throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus( ) );
			}

			log.info( "Output from Server response-status["+response.getStatus()+"].... \n" );
			AdultFilter output = response.getEntity( AdultFilter.class );
			log.info( "SafeImage api return safe[" + output.getSafe( ) + "] notSafe[" + output.getNotSafe( ) + "]" );
			
			if( output.getSafe( ) > safeValue )
				return 45;
			else
				return 46;
			
		} catch( Exception e ) {
			log.error( "[getSafeImage] error " , e );
			return 46;
		}
		
	}
	
}
