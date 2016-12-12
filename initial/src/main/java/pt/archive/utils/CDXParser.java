package pt.archive.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.archive.model.ImageSearchResult;
import pt.archive.model.ItemOpenSearch;

public class CDXParser {
	
	private final Logger log = LoggerFactory.getLogger( this.getClass( ) ); //Define the logger object for this class
	
	public List< ImageSearchResult > GetuniqueResults( List< ImageSearchResult > input ) {
		String urlCDXServer;
		for( ImageSearchResult img : input ) {
			urlCDXServer = callURL( img.getUrl() );
			
			try{
				JSONArray cdxResponse = new JSONArray( urlCDXServer );
				log.info( cdxResponse.toString() );
			} catch( JSONException e ) {
				log.error( "[CDXParser][GetuniqueResults] e " , e );
			}
		}
		return new ArrayList<>();
	}
	
	public static String callURL(String myURL) {
		System.out.println("Requested URL:" + myURL);
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(myURL);
			urlConn = url.openConnection();
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(),
						Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
		in.close();
		} catch (Exception e) {
			throw new RuntimeException( "Exception while calling URL:"+ myURL , e );
		} 
 
		return sb.toString();
	}
	
	
}
