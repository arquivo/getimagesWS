package pt.archive.utils;

import pt.archive.model.ImageSearchResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CDXParser {
	
	private final Logger log = LoggerFactory.getLogger( this.getClass( ) ); //Define the logger object for this class
	private String hostBaseCDX;
	private String outputCDX;
	private String flParam;
	private List< ImageSearchResult > input;

	public CDXParser( String hostBaseCDX, String outputCDX, String flParam, List<ImageSearchResult> input ) {
		super();
		this.hostBaseCDX 	= hostBaseCDX;
		this.outputCDX 		= outputCDX;
		this.flParam 		= flParam;
		this.input 			= input;
	}

	public List< ImageSearchResult > getuniqueResults( ) {
		String responseCDXServer;
		String keyDigest = "digest";
		List< ImageSearchResult > resultsUnique = new ArrayList< >( );
		int counterImg = 0;
		for( ImageSearchResult img : input ) {
			String urlCDX = getLink( img.getUrl( ) , img.getTimestamp( ) );
			//log.info( "[urlCDXServer] = " + urlCDX );
			try{
				List< JSONObject > jsonValues = readJsonFromUrl( urlCDX );
				printDebug( jsonValues );
				Collections.sort( jsonValues , new Comparator<JSONObject>() {
			        //You can change "Name" with "ID" if you want to sort by ID
			        private static final String KEY_NAME = "timestamp";

			        @Override
			        public int compare( JSONObject a , JSONObject b ) {
			            long valA = 0;
			            long valB = 0;

			            try {
			                valA = Long.valueOf( ( String ) a.get( KEY_NAME ) );
			                valB = Long.valueOf( ( String ) b.get( KEY_NAME ) );
			            } catch ( JSONException e ) {
			                log.error( "[getuniqueResults][compare] e = " , e );
			            }

			            return Long.compare( valB , valA );
			            //if you want to change the sort order, simply use the following:
			            //return -valA.compareTo(valB);
			        }
			    } );
				
				if( !isExists( jsonValues.get( 0 ).get( keyDigest ).toString( ) , counterImg ) ) {
					img.setDigest( jsonValues.get( 0 ).get( "digest" ).toString( ) );
					img.setTimestamp( jsonValues.get( 0 ).get( "timestamp" ).toString( ) );
					resultsUnique.add( img );
				}
				
				log.info( "Depois de ordenado => " );
				printDebug( jsonValues );
			} catch( JSONException e ) {
				log.error( "[CDXParser][GetuniqueResults] JSONParser e " , e );
			} catch( Exception e1 ) {
				resultsUnique.add( img );
				log.error( "[CDXParser][GetuniqueResults] e " , e1 );
			}
			counterImg++;
		}
		return resultsUnique;
	}
	
	private void printDebug( List< JSONObject > jsonValues ) {
		log.info( "**** Values JSON ****" );
		for( JSONObject obj : jsonValues ) {
			log.info( "  obj = " + obj.toString( ) );
		}
		log.info( "**********************" );
	}
	
	private boolean isExists( String digest , int indexImg ) {
		
		for( int i = 0 ; i <= indexImg ; i++ ) 
			if( input.get( i ).getDigest( ).equals( digest ) )
				return true;
		
		return false;
	}
	
	private String getLink( String url , String timestamp ) {
		
		String urlaux = url.substring( url.indexOf( timestamp.substring( 0 , ( timestamp.length( ) - 3 ) - 1 ) ) + timestamp.length( ) + 1 );
		return hostBaseCDX
					.concat( "url" )
					.concat( Constants.equalOP )
					.concat( urlaux )
					.concat( Constants.andOP )
					.concat( "output" )
					.concat( Constants.equalOP )
					.concat( outputCDX )
					.concat( Constants.andOP )
					.concat( "fl" )
					.concat( Constants.equalOP )
					.concat( flParam );
	}
	
	private  ArrayList< JSONObject > readAll( BufferedReader rd ) throws IOException, ParseException {
		StringBuilder sb = new StringBuilder();
		ArrayList<JSONObject> json=new ArrayList<JSONObject>();
		String line;
		while ( ( line = rd.readLine( ) ) != null ) {
			JSONObject obj= (JSONObject) new JSONParser().parse( line );
			json.add( obj );
		}
		return json;
	}
 
	private ArrayList< JSONObject > readJsonFromUrl(String url) throws IOException, JSONException, ParseException {
		 InputStream is = new URL( url ).openStream( );
		 ArrayList< JSONObject >  jsonResponse = new ArrayList< >( );
		 try {
			 BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			 jsonResponse = readAll( rd );
		     return jsonResponse;
		 } finally {
			 is.close( );
		 }
	 }
	
	
}
