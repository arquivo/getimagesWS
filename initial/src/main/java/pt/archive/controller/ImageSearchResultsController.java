package pt.archive.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import pt.archive.model.ImageSearchResult;
import pt.archive.model.ImageSearchResults;
import pt.archive.model.ItemXML;
import pt.archive.utils.Constants;
import pt.archive.utils.HTMLParser;
import pt.archive.utils.UserHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

@Configuration
@RestController
public class ImageSearchResultsController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass()); //Define the logger object for this class
	private List<ImageSearchResult> imageResults = new ArrayList<ImageSearchResult>();
	
	/** Properties file application.properties**/
	@Value( "${urlBase}" )
	private String urlBase;
	
	@Value( "${type}" )
	private String type;
	
	@Value( "${hitsPerSite}" )
	private String hitsPerSite;
	
	@Value( "${NumImgsbyUrl}" )
	private int numImgsbyUrl;
	
	@Value( "${hostGetImage}" )
	private String hostGetImage;
	
	@Value( "${urldirectoriesImage}" )
	private String urldirectoriesImage;
	
	@Value( "${hitsPerPage}" )
	private String hitsPerPage;
	/***************************/
	
	private List< ItemXML > resultOpenSearch;
	
	/**
	 * 
	 * @param query: full-text element
	 * @param startData: 
	 * @param endData
	 * @return 
	 */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ImageSearchResults getImages( @RequestParam(value="query", defaultValue="") String query,
    									 @RequestParam(value="stamp", defaultValue="") String stamtp ) {
    	log.info( "New request query[" + query + "] stamp["+ stamtp +"]" );
    	
    	if( stamtp != null && stamtp.trim( ).equals( "" ) ){
    		stamtp = "19960101000000-20151022163016";
    	}
    	
    	return new ImageSearchResults( getImageResults( query , stamtp ) );
    }
    
    /* Method that calls the OpenSearchAPI to get the first 10 urls of the query
    * and Returns a list of Images
    */
    public List<ImageSearchResult> getImageResults( String query , String stamp ) {
    	String url;
    	HTMLParser resultImg;
    	
 		if( query == null || query.trim().equals( "" ) ) {
 			log.warn("[ImageSearchResultsController][getImageResults] Query empty!");
 			imageResults.add( getErrorCode( "-1: query empty" ) ); 
 			return Collections.emptyList();
 		}
 		
 		try {
 			url = buildURL( query , stamp );
 			log.info( "Teste input == " + URLEncoder.encode(query, "UTF-8").replace("+", "%20") 
 					+ " url == " + url );
	 		// the SAX parser
 			UserHandler userhandler = new UserHandler( );
	 		XMLReader myReader = XMLReaderFactory.createXMLReader( );
	 		myReader.setContentHandler( userhandler );
	 		myReader.parse( new InputSource(new URL( url ).openStream( ) ) );
	 		resultOpenSearch = userhandler.getItems( );
	 		
	 		//Search information tag <img>
	 		resultImg = new HTMLParser(  );
	 		imageResults = resultImg.buildResponse( resultOpenSearch , numImgsbyUrl , hostGetImage , urldirectoriesImage );
	 		
 		} catch( UnsupportedEncodingException e ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e);
 			imageResults.add( getErrorCode( "[ERROR] -5: URL Encoder Error" ) );
 		} catch( SAXException e1 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e1);
 			imageResults.add( getErrorCode( "[ERROR] -2: Parser Error" ) ); 
 		} catch( MalformedURLException e2 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e2);
 			imageResults.add( getErrorCode( "[ERROR] -3: URL OpenSearch Error" ) );
 		} catch( IOException e4 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e4);
 			imageResults.add( getErrorCode( "[ERROR] -4: IOException Error" ) );
 		}
 		
 		return imageResults;
    }
    
    
    private String buildURL( String input , String stamp ) throws UnsupportedEncodingException {
    	return urlBase
    			.concat(  URLEncoder.encode( input , "UTF-8").replace("+", "%20") )
    			.concat( Constants.inOP )
    			.concat( "type" )
    			.concat( Constants.colonOP )
    			.concat( type )
    			.concat( Constants.inOP )
    			.concat( "date" )
    			.concat( Constants.colonOP )
    			.concat( stamp )
    			.concat( Constants.andOP )
    			.concat( "hitsPerSite" )
    			.concat( Constants.equalOP )
    			.concat( hitsPerSite )
    			.concat( Constants.andOP )
    			.concat( "hitsPerPage" )
    			.concat( Constants.equalOP )
    			.concat( hitsPerPage );
    }
    
    private ImageSearchResult getErrorCode( String errorCode ) {
    	ImageSearchResult result = new ImageSearchResult( );
    	result.setUrl( errorCode );
    	return result;
    }
    
}
