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
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;

@Configuration
@RestController
public class ImageSearchResultsController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass()); //Define the logger object for this class
	private List< ImageSearchResult > imageResults = new ArrayList< ImageSearchResult >( );
	private String[] terms;
	
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
	
	@Value( "${NThreads}" )
	private int NThreads;
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
    									 @RequestParam(value="stamp", defaultValue="19960101000000-20151022163016") String stamtp ) {
    	log.info( "New request query[" + query + "] stamp["+ stamtp +"]" );
    	printProperties( );
    	
    	return new ImageSearchResults( getImageResults( query , stamtp ) );
    }
    
    /* Method that calls the OpenSearchAPI to get the first N urls of the query
    * and Returns a list of Images
    */
    public List<ImageSearchResult> getImageResults( String query , String stamp ) {
    	String url;
    	ExecutorService pool = Executors.newFixedThreadPool( NThreads );
    	CountDownLatch latch;
    	Set< Future< List< ImageSearchResult > > > set = new HashSet< Future< List< ImageSearchResult > > >( );
    	List< Callable< Object > > todo;
    	
    	if( query == null || query.trim( ).equals( "" ) ) {
 			log.warn("[ImageSearchResultsController][getImageResults] Query empty!");
 			imageResults.add( getErrorCode( "-1: query empty" ) ); 
 			return Collections.emptyList( );
 		}
 		
 		try {
 			terms = query.split( " " );
 			url = buildURL( query , stamp );
 			log.debug( "Teste input == " + URLEncoder.encode(query, "UTF-8").replace("+", "%20") 
 					+ " url == " + url );
	 		// the SAX parser
 			UserHandler userhandler = new UserHandler( );
	 		XMLReader myReader = XMLReaderFactory.createXMLReader( );
	 		myReader.setContentHandler( userhandler );
	 		myReader.parse( new InputSource(new URL( url ).openStream( ) ) );
	 		resultOpenSearch = userhandler.getItems( );
	 		
	 		if( resultOpenSearch == null || resultOpenSearch.size( ) == 0 )  
	 			return  Collections.emptyList();
	 		
	 		log.debug( "[ImageSearchResultsController][getImageResults] OpenSearch result : " + resultOpenSearch.size( ) );
	 		latch = new CountDownLatch( resultOpenSearch.size( ) );
	 		todo  = new ArrayList< Callable < Object > >( resultOpenSearch.size( ) );
	 		List< Callable< List< ImageSearchResult > > > callables = new ArrayList<>();
	 		//Search information tag <img>
	 		for( ItemXML item : resultOpenSearch ) {
	 			//Callable< List< ImageSearchResult > > callable = new HTMLParser( latch , item,  numImgsbyUrl , hostGetImage , urldirectoriesImage ) );;
	 			callables.add( new HTMLParser( latch , item,  numImgsbyUrl , hostGetImage , urldirectoriesImage ) ); 
	 			
	 			//Future< List< ImageSearchResult > > future = pool.submit( callable );
	 			//set.add( future );
	 		}
	 		
	 		List< Future< List< ImageSearchResult > > > answer = pool.invokeAll( callables );
	 		
	 		//get images result to search
	 		for( Future< List< ImageSearchResult > >  future : answer ) {
	 			if( future.isDone( ) ) {
	 				List< ImageSearchResult > result = future.get( ); // wait for a processor to complete
		 			if( result != null && !result.isEmpty( ) )
		 				imageResults.addAll( result );
	 			}
	 		}
	 		
	        
	 		log.info( "Request query[" + query + "] stamp["+ stamp +"] Number of results["+ imageResults.size( ) +"]" );
	 		
		} catch( InterruptedException e ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e );
 			imageResults.add( getErrorCode( "[ERROR] Internal Error" ) );
 		} catch( ExecutionException e1 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e1 );
 			imageResults.add( getErrorCode( "[ERROR] Internal Error" ) );
 		} catch( UnsupportedEncodingException e2 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e2 );
 			imageResults.add( getErrorCode( "[ERROR] -5: URL Encoder Error" ) );
 		} catch( SAXException e3 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e3 );
 			imageResults.add( getErrorCode( "[ERROR] -2: Parser Error" ) ); 
 		} catch( MalformedURLException e4 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e4 );
 			imageResults.add( getErrorCode( "[ERROR] -3: URL OpenSearch Error" ) );
 		} catch( IOException e5 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e5 );
 			imageResults.add( getErrorCode( "[ERROR] -4: IOException Error" ) );
 		}catch( Exception e6 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e6 );
 			imageResults.add( getErrorCode( "[ERROR]: No images found" ) );
 		}finally{
	 		if( pool != null )
	 			pool.shutdown( ); //shut down the executor service now
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
    
    private void printProperties( ){
    	log.info( "********* Properties *********" );
    	log.info( "	urlBase=" +urlBase );
    	log.info( "	type=" +type );
    	log.info( "	hitsPerSite=" +hitsPerSite);
    	log.info( "	hitsPerPage=" +hitsPerPage );
    	log.info( "	NThreads=" +NThreads );
    	log.info( "	NumImgsbyUrl=" +numImgsbyUrl );
    	log.info( "	HostGetImage=" +hostGetImage );
    	log.info( "	urldirectoriesImage=" +urldirectoriesImage );
    	log.info( "	NThreads=" +NThreads );
    	log.info( "******************************" );
    }
    
}
