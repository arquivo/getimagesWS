package pt.archive.utils;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.slf4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class SafeImageClient {

	
	public static SafeImage getSafeImage( String imgBase64 , String host , Logger log , String urlDebug) {
		try{
			log.debug( "Call safeImage API host[" + host + "]" );
			
			Client client = Client.create( );
		    client.setConnectTimeout( Constants.timeoutConn );
		    client.setReadTimeout( Constants.timeoutreadConn );
			WebResource webResource = client.resource( host );
			JSONObject input = new JSONObject( );
			input.put( "image", imgBase64 );
			ClientResponse response = webResource.type( "application/json" )
						.post( ClientResponse.class , input.toString( ) );
			
			if ( response.getStatus( ) != 200 ) {
				throw new RuntimeException( "Failed : HTTP error code : "+ response.getStatus( ) );
			}
			
			log.debug( "Output from Server .... \n" );
			String outputJS = response.getEntity( String.class );
			JSONObject output = new JSONObject(  outputJS.substring( 1, outputJS.length( ) - 2 ).replace( "\\" , "" ) ); 
			BigDecimal safe =  output.getBigDecimal( "Safe" );
			BigDecimal notSafe = output.getBigDecimal( "NotSafe" );
			SafeImage safeImage = new SafeImage( safe, notSafe );
			
		    
			log.debug( "SafeImage api  return safe[" + safe.floatValue() + "] notSafe[" 
						+ notSafe.floatValue() + "]  to url["+urlDebug+"]" );
			return safeImage;
			
		} catch( Exception e ) {
			log.error( "[getSafeImage] error " , e );
			return null;
		}
	}
	
}
