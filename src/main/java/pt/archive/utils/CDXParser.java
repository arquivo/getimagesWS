package pt.archive.utils;

import pt.archive.model.ImageSearchResult;
import pt.archive.model.ItemCDXServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
	private ImageSearchResult img;
	private final String keyUrl 		= "url";
	private final String keyDigest 		= "digest";
	private final String keyTimestamp 	= "timestamp"; 
	private final String keyMimeType 	= "mime";
	
	public CDXParser( String hostBaseCDX, String outputCDX, String flParam, ImageSearchResult img ) {
		super( );
		this.hostBaseCDX 	= hostBaseCDX;
		this.outputCDX 		= outputCDX;
		this.flParam 		= flParam;
		this.img 			= img;
	}
	
	/**
	 * get image from CDXServer with url,timstamp,digest,mime attributes
	 * @return ItemCDXServer
	 */
	public ItemCDXServer getImgCDX( ) {
		
		ItemCDXServer  imgCDX = null;

		String urlCDX = getLink( img.getUrl( ) , img.getTimestamp( ) );
		try{
			List< JSONObject > jsonValues = readJsonFromUrl( urlCDX );
			
			if( jsonValues == null )
				return null;
			
			imgCDX = new ItemCDXServer( jsonValues.get( 0 ).get( keyUrl ).toString( ) ,
										jsonValues.get( 0 ).get( keyTimestamp ).toString( ),
										jsonValues.get( 0 ).get( keyDigest ).toString( ),
										jsonValues.get( 0 ).get( keyMimeType ).toString( ) );
			//printDebug( jsonValues );
		} catch( JSONException e ) {
			log.debug( "[CDXParser][GetuniqueResults] URL["+urlCDX+"] JSONParser e " , e );
			return null;
		} catch( Exception e1 ) {
			log.debug( "[CDXParser][GetuniqueResults] URL["+urlCDX+"] e " , e1 );
			return null;
		}
	
		return imgCDX;
	}
	
	/**
	 * build CDXServer url
	 * @param url
	 * @param timestamp
	 * @return
	 */
	private String getLink( String url , String timestamp ) {
		String urlaux = url.substring( url.indexOf( timestamp ) +  18 );
		log.debug( "[CDXParser][getLink] url["+url+"] timestamp["+timestamp+"] urlaux["+urlaux+"]" );
		return hostBaseCDX
					.concat( "url" )
					.concat( Constants.equalOP )
					.concat( urlaux )
					.concat( Constants.andOP )
					.concat( "output" )
					.concat( Constants.equalOP )
					.concat( outputCDX )
					//.concat( Constants.andOP )
					//.concat( "from" )
					//.concat( Constants.equalOP )
					//.concat( timestamp )
					//.concat( Constants.andOP )
					//.concat( "to" )
					//.concat( Constants.equalOP )
					//.concat( timestamp )
					.concat( Constants.andOP )
					.concat( "fl" )
					.concat( Constants.equalOP )
					.concat( flParam )
					.concat( Constants.andOP )
					.concat( "closest" )
					.concat( Constants.equalOP )
					.concat( timestamp );
	}
	
	/**
	 * build json struture with CDXServer response
	 * @param rd
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private  ArrayList< JSONObject > readAll( BufferedReader rd ) throws IOException, ParseException {
		ArrayList< JSONObject > json = new ArrayList< JSONObject >( );
		String line;
		while ( ( line = rd.readLine( ) ) != null ) {
			JSONObject obj= ( JSONObject ) new JSONParser( ).parse( line );
			json.add( obj );
		}
		return json;
	}
 
	/**
	 * Connect and get response to the CDXServer
	 * @param strurl
	 * @return
	 */
	private ArrayList< JSONObject > readJsonFromUrl( String strurl ) {
		InputStream is = null;
		ArrayList< JSONObject >  jsonResponse = new ArrayList< >( );
		try {
			URL url = new URL( strurl );
			URLConnection con = url.openConnection( );
			con.setConnectTimeout( Constants.timeoutConn ); // 3 sec
			con.setReadTimeout( Constants.timeoutreadConn ); //5 sec
			is = con.getInputStream( );
			BufferedReader rd = new BufferedReader( new InputStreamReader( is , Charset.forName( "UTF-8" ) ) );
			jsonResponse = readAll( rd );
		    return jsonResponse;
		} catch( Exception e ) {
			log.error( "[readJsonFromUrl]" + e );
			return null;
		} finally {
			if( is != null ) {
				try { is.close( ); } catch( IOException e1 ) {  log.error( "[readJsonFromUrl] Close Stream: " + e1 ); }
			}
		}
		
	 }
	
}
