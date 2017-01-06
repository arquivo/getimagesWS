package pt.archive.utils;

import org.slf4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class SafeImageClient {

	
	public SafeImageClient( ) {
		
	}
	
	
	public static char getSafeImage( String imgBase64 , String host , Logger log ) {
		Client client = Client.create( );
		WebResource webResource = client.resource( host );
		ClientResponse response = webResource.type( "application/json" )
					.post( ClientResponse.class , imgBase64 );

		if ( response.getStatus() != 200 ) {
			throw new RuntimeException("Failed : HTTP error code : "
			     + response.getStatus());
		}

		log.info( "Output from Server .... \n" );
		String output = response.getEntity( String.class );
		log.info( output );
		
		return 45;
	}
	
}
