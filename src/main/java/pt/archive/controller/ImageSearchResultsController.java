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
import pt.archive.model.ItemOpenSearch;
import pt.archive.utils.Constants;
import pt.archive.utils.HTMLParser;
import pt.archive.utils.UserHandler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;

@Configuration
@RestController
public class ImageSearchResultsController {
	
	private final Logger log = LoggerFactory.getLogger( this.getClass( ) ); //Define the logger object for this class
	private List< String > terms;
	private List< String > allterms;
	private String startIndex;
	private List< String > blacklListUrls;
	private List< String > blackListDomain;
	private List< String > stopwords;
	private String criteriaRank;
	
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
	
	@Value( "${TimeoutThreads}" )
	private long timeout;
	
	@Value( "${blacklistUrl.file}" )
	private String blackListUrlFileLocation;
	
	@Value( "${blacklistDomain.file}" )
	private String blacklistDomainFileLocation;
	
	@Value( "${stopWords.file}" )
	private String stopWordsFileLocation;
	
	@Value( "${urlBaseCDX}" )
	private String urlBaseCDX;
	
	@Value( "${outputCDX}" )
	private String outputCDX;
	
	@Value( "${flCDX}" )
	private String flParam;
	
	@Value( "${imgParseflag}" )
	private int imgParseflag;
	
	@Value( "${widthThumbnail}" )
	private int widthThumbnail;
	
	@Value( "${heightThumbnail}" )
	private int heightThumbnail;
	
	@Value( "${adultfilter}" )
	private int adultfilter;
	
	@Value( "${safeValue}" )
	private BigDecimal safeValue;
	
	@Value( "#{'${interval.size.image}'.split(',')}" ) 
	private int[ ] sizeInterval;
	 
	@Value( "${safeImage}" )
	private String safeImageHost;
	/***************************/
	
	private List< ItemOpenSearch > resultOpenSearch;
	
	/**
	 * Initialize init 
	 * @throws Exception
	 */
	@PostConstruct
	public void initIt( ) throws Exception {
	  log.info("Init method after properties are set : blacklistUrlFile[" + blackListUrlFileLocation +"] & blacklistDomainFile[" + blacklistDomainFileLocation + "]");
	  loadBlackListFiles( );
	  loadStopWords( );
	  printProperties( );
	  printStopWords( );
	  printBlackList( );
	  printIntervalSize( );
	}
	
	/**
	 * print size interval (debug) 
	 */
	public void printIntervalSize( ) {
		log.info( "**** Interval Sizes ****" );
		for( int size: sizeInterval  )
			log.info( "   size: " + size );
		log.info( "******************" );
	}
	
	/**
	 * @param query: full-text element
	 * @param startData: 
	 * @param endData
	 * @return 
	 */
    @RequestMapping( value = "/" , method = RequestMethod.GET )
    public ImageSearchResults getImages( @RequestParam(value="query", defaultValue="") String query,
    									 @RequestParam(value="stamp", defaultValue="19960101000000-20151022163016") String stamtp,
    									 @RequestParam(value="start", defaultValue="0") String _startIndex,
    									 @RequestParam(value="safeImage", defaultValue="all") String _safeImage ) {
    	log.info( "New request query[" + query + "] stamp["+ stamtp +"] start["+ _startIndex +"] safeImage["+ _safeImage +"]" );
    	long start = System.currentTimeMillis( );
    	startIndex = _startIndex;
    	List< ImageSearchResult > imageResults = getImageResults( query , stamtp , _safeImage ); 
    	long elapsedTime = System.currentTimeMillis( ) - start;
    	log.info( "Search ["+query+"] Results = [" + imageResults.size( ) +"] time = [" + elapsedTime + "] milliseconds.");
    	return new ImageSearchResults( imageResults , imageResults.size( ) , query );
    }
    
   
    /**
     * Method that calls the OpenSearchAPI to get the first N urls of the query
     * and Returns a list of Images
     * @param query
     * @param stamp
     * @param safeImage
     * @return
     */
    public List< ImageSearchResult > getImageResults( String query , String stamp , String safeImage ) {
    	String url;
    	ExecutorService pool = Executors.newFixedThreadPool( NThreads );
    	CountDownLatch doneSignal;
    	List< ImageSearchResult > imageResults 	= new ArrayList< >( );
    	List< ImageSearchResult > resultImages 	= new ArrayList< >( );
    	List< String > types = new ArrayList< >( );
    	List< String > sizes = new ArrayList< >( );
    	int counter = 0;
    	boolean isAllDone = false;
    	String queryWithoutTerm;
    	
    	if( query == null ) 
    		return Collections.emptyList( );
    	
    	if( query == null || query.trim( ).equals( "" ) ) {
 			log.warn("[ImageSearchResultsController][getImageResults] Query empty!");
 			imageResults.add( getErrorCode( "-1: query empty" ) ); 
 			return Collections.emptyList( );
 		}
 		
 		try {
 			cleanUpMemory( );
 			getTerms( query );
 			queryWithoutTerm = prepareTerms( query );
 			log.info( "query final => " + queryWithoutTerm );
 			printTerms( );
 			types = getQueryTerms( query , Constants.typeSearch );
 			sizes = getQueryTerms( query , Constants.sizeSearch );
 			log.info( "****** Types *****" );
 			log.info( "  " + types );
 			log.info( "******************" );
 			
 			url = buildURL( queryWithoutTerm , stamp );
 			log.info( "Request to OpenSearch["+ url +"]" );
 			// the SAX parser
 			UserHandler userhandler = new UserHandler( );
	 		XMLReader myReader = XMLReaderFactory.createXMLReader( );
	 		myReader.setContentHandler( userhandler );
	 		myReader.parse( new InputSource(new URL( url ).openStream( ) ) );
	 		resultOpenSearch = userhandler.getItems( );
	 		
	 		if( resultOpenSearch == null || resultOpenSearch.size( ) == 0 )  //No results in OpenSearch
	 			return  Collections.emptyList( );
	 		
	 		log.info( "[ImageSearchResultsController][getImageResults] OpenSearch result : " + resultOpenSearch.size( ) );
	 		doneSignal = new CountDownLatch( resultOpenSearch.size( ) );
	 		
	 		List< Future< List< ImageSearchResult > > > submittedJobs = new ArrayList< >( );
	 		for( ItemOpenSearch item : resultOpenSearch ) { //Search information tag <img>
	 			Future< List< ImageSearchResult > > job = null;
	 			if( counter < 10 ) 
 					job = pool.submit( new HTMLParser( doneSignal , item,  numImgsbyUrl , hostGetImage , urldirectoriesImage , terms , blacklListUrls , blackListDomain , criteriaRank , types , imgParseflag , widthThumbnail , heightThumbnail , adultfilter , sizes , sizeInterval , safeImageHost , safeValue , safeImage , Constants.incrTopResults ) );
 				else
 					job = pool.submit( new HTMLParser( doneSignal , item,  numImgsbyUrl , hostGetImage , urldirectoriesImage , terms , blacklListUrls , blackListDomain , criteriaRank , types , imgParseflag , widthThumbnail , heightThumbnail , adultfilter , sizes , sizeInterval , safeImageHost , safeValue , safeImage , 0 ) );
 				submittedJobs.add( job );
 				counter++;
	 		}
	 		try {
	 			isAllDone = doneSignal.await( timeout , TimeUnit.SECONDS );
	 		    if ( !isAllDone ) 
	            	cleanUpThreads( submittedJobs );
	        } catch ( InterruptedException e1 ) {
	        	cleanUpThreads( submittedJobs ); // take care, or cleanup
	        }
	 		
	 		//get images result to search
	 		for( Future< List< ImageSearchResult > >  job : submittedJobs ) {
	 			try {
	                if ( !isAllDone && !job.isDone( ) ) {  // before doing a get you may check if it is done
	                    job.cancel( true ); // cancel job and continue with others
	                    continue;
	                }
	    			List< ImageSearchResult > result = job.get( ); // wait for a processor to complete
		 			if( result != null && !result.isEmpty( ) ) {
		 				log.debug( "Resultados do future = " + result.size( ) );
		 				imageResults.addAll( result );
		 			}
	            } catch ( ExecutionException cause ) {
	            	log.error( "[ImageSearchResultsController][getImageResults]", cause ); // exceptions occurred during execution, in any
	            } catch ( InterruptedException e ) {
	            	log.error( "[ImageSearchResultsController][getImageResults]", e ); // take care
	            }
	 		}
	 		
	 		Collections.sort( imageResults ); //sort 
	 		log.info( "Numero de resposta com duplicados: " + imageResults.size( ) );
	 		resultImages = uniqueResult( imageResults ); //remove duplicates
	 		log.info( "Numero de resposta sem duplicados: " + resultImages.size( ) );
	 		//CDXParser parseCDX = new CDXParser( urlBaseCDX, outputCDX, flParam, imageResults ); //SORT
	 		//resultImages = parseCDX.getuniqueResults( );
	 		
	 		log.debug( "Request query[" + query + "] stamp["+ stamp +"] Number of results["+ resultImages.size( ) +"]" );
	 		
		} catch( UnsupportedEncodingException e2 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e2 );
 		} catch( SAXException e3 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e3 );
 		} catch( MalformedURLException e4 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e4 );
 		} catch( IOException e5 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e5 );
 		}catch( Exception e6 ) {
 			log.error( "[ImageSearchResultsController][getImageResults]", e6 );
 		}finally{
	 		if( pool != null )
	 			pool.shutdown( ); //shut down the executor service now
 		}
 		
 		return resultImages;
    }
    
    
    /**
     * Build openSearch url
     * @param input
     * @param stamp
     * @return
     * @throws UnsupportedEncodingException
     */
    private String buildURL( String input , String stamp ) throws UnsupportedEncodingException {
    	log.info( "[buildURL] input => " + input );
    	return urlBase
    			.concat(  URLEncoder.encode( input , "UTF-8" ).replace( "+" , "%20" ) )
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
    			.concat( hitsPerPage )
    			.concat( Constants.andOP )
    			.concat( "start" )
    			.concat( Constants.equalOP )
    			.concat( startIndex );
    }
    
    /**
     * remove duplicates images
     * @param imageResults
     * @return
     */
    private List< ImageSearchResult > uniqueResult( List< ImageSearchResult > imageResults ) {
    	List< ImageSearchResult > uniqueList = new ArrayList< >( );
    	Set< ImageSearchResult > uniqueSet = new HashSet< >( );
    	for( ImageSearchResult obj : imageResults ) {
    		if( uniqueSet.add( obj ) ){
    			uniqueList.add( obj );
    		} 
    	}
    	return uniqueList;
    }
    
    /**
     * Error code define
     * @param errorCode
     * @return
     */
    private ImageSearchResult getErrorCode( String errorCode ) {
    	ImageSearchResult result = new ImageSearchResult( );
    	result.setUrl( errorCode );
    	return result;
    }
    
    /**
     * Clean memory threads
     * @param submittedJobs
     */
    private void cleanUpThreads( List< Future< List< ImageSearchResult > > > submittedJobs ) {
    	for ( Future< List< ImageSearchResult > > job : submittedJobs ) 
            job.cancel( true );
    }
    
    /**
     * get parameter of the query (Advanced search)
     * @param query
     */
    private void getTerms( String query ) {
    	terms = new LinkedList< >( );
    	allterms = new LinkedList< >( );
    	char sort = 45;
    	String sortTerm = "";
    	Matcher m = Pattern.compile( "([^\"]\\S*|\".+?\")\\s*" ).matcher( query );
    	while( m.find( ) ) {
    		if( m.group( 1 ).startsWith( Constants.sortCriteria ) ) {
    			String auxSort = m.group( 1 ).substring( m.group( 1 ).indexOf( Constants.sortCriteria ) + Constants.sortCriteria.length( ) );
    			sortTerm = m.group( 1 );
    			log.info( "  auxSort => " + auxSort + " remove = " + sortTerm );
    			sort = 46;
    			if( auxSort.equals( Constants.criteriaRank.NEW.toString( ) ) )
    				criteriaRank = "new";
    			else if( auxSort.equals( Constants.criteriaRank.OLD.toString( ) ) )
    				criteriaRank = "old";
    			else {
    				criteriaRank = "score";
    				sortTerm = "";
    			}
    		} else if( !m.group( 1 ).startsWith( Constants.typeSearch ) && !m.group( 1 ).startsWith( Constants.sizeSearch ) && !m.group( 1 ).startsWith( Constants.siteSearch ) && !m.group( 1 ).startsWith( Constants.negSearch ) ) {
    			terms.add( m.group( 1 ).replace( "\"" ,  "" ) );
    		}
    		allterms.add( m.group( 1 ).replace( "\"" ,  "" ) );
    	}
    	
    	if( sort == 45 )
    		criteriaRank = "score";
    	
    	log.info( "criteriaRank["+criteriaRank+"]" );
    }
    
    /**
     * Prepare terms of the ranking
     * @param query
     * @return
     */
    private String prepareTerms( String query ) {
    	removeStopWords( );
    	return removeCharactersAdvancedSearch( query );
    }
    
    /**
     * return advanced terms of the images (Advanced search)
     * @param query
     * @return
     */
    private List< String > getQueryTerms( String query , String queryTerm ) {
    	List< String > resultTerms = new ArrayList< >( );
    	for( String term : allterms ) 
    		if( term.startsWith( queryTerm ) ) {
    			if( queryTerm.equals( Constants.typeSearch ) )
    				resultTerms.add( Constants.mimeTypestr.concat( term.substring( term.indexOf( Constants.typeSearch ) + Constants.typeSearch.length( ) ) ) );
    			else
    				resultTerms.add( term.substring( term.indexOf( Constants.sizeSearch ) + Constants.sizeSearch.length( ) ) );
    		}
    	return resultTerms;
    }
    
    /**
     * Remove terms in the advanced search 
     * @param query
     * @return
     */
    private String removeCharactersAdvancedSearch( String query ) {
    	StringBuffer queryResult = new StringBuffer( );
    	for( String term : allterms ) {
    		log.info( "TERM => " + term );
    		if( !term.startsWith( Constants.sizeSearch ) &&
    			!term.startsWith( Constants.sortCriteria ) )  {
    			if( queryResult.length( ) > 0 )
    				queryResult.append( " ".concat( term ) );
    			else
    				queryResult.append( term );
    		}
    	}
    	log.info( "query no final do remove => " + queryResult.toString( ) );
    	return queryResult.toString( );
    }
    
    /**
     * Remove stop words of the terms
     */
    private void removeStopWords( ) {
    	for( Iterator< String > iterator = terms.iterator( ) ; iterator.hasNext( ); ) {
    		String term = iterator.next( );
    		for( String stopWord : stopwords ) {
    			if( term.toLowerCase( ).equals( stopWord ) ) {
    				log.info( "[StopWords] Remove term["+term+"] to ranking" );
    				iterator.remove( );
    			}
    		}
    	}
    }
    
    private void cleanUpMemory( ) {
		if( terms != null ) {
			log.info( "[DEBUGGG] imageResults["+ terms.size( ) +"] ");
			terms.clear( );
		}
		
		if( resultOpenSearch != null ) {
			log.info( "[DEBUGGG] resultOpenSearch["+ resultOpenSearch.size( ) +"] ");
			resultOpenSearch.clear( );
		}
    }
    
    /**
     * load blacklist files
     */
    private void loadBlackListFiles( ) {
    	loadBlackListUrls( );
    	loadBlackListDomain( );
    }
    
    
    private void loadBlackListUrls( ) {
    	Scanner s = null;
    	try{
    		s = new Scanner( new File( blackListUrlFileLocation ) );
    		blacklListUrls = new ArrayList< String >( );
    		while( s.hasNext( ) ) {
    			blacklListUrls.add( s.next( ) );
    		}
    	} catch( IOException e ) {
    		log.error( "Load blacklist file error: " , e );
    	} finally {
    		if( s != null )
    			s.close( );
    	}
    }
    
    private void loadBlackListDomain( ) {
    	Scanner s = null;
    	try{
    		s = new Scanner( new File( blacklistDomainFileLocation ) );
    		blackListDomain = new ArrayList< String >( );
    		while( s.hasNext( ) ) {
    			blackListDomain.add( s.next( ) );
    		}
    	} catch( IOException e ) {
    		log.error( "Load blacklist file error: " , e );
    	} finally {
    		if( s != null )
    			s.close( );
    	}
    }
    
    private void loadStopWords( ) {
    	Scanner s = null;
    	try{
    		
    		s = new Scanner( new File( stopWordsFileLocation ) );
    		stopwords = new ArrayList< String >( );
    		while( s.hasNext( ) ) {
    			stopwords.add( s.next( ) );
    		}
    	} catch( IOException e ) {
    		log.error( "Load stopWords file error: " , e );
    	} finally {
    		if( s != null )
    			s.close( );
    	}
    }
    
    private void printTerms(  ) {
    	log.info( "****** Terms List ******" );
    	for( String term : terms ) {
    		log.info( " " + term );
    	}
    	log.info( "***********************" );
    }
    
    private void printBlackList( ){
    	log.info( "******* BlackList Urls *******" );
    	for( String url : blacklListUrls ) 
    		log.info( "  " + url );
    	log.info("***************************");
    	
    }
    
    private void printStopWords( ){
    	log.info( "******* StopWords Urls *******" );
    	for( String word : stopwords ) 
    		log.info( "  " + word );
    	log.info("***************************");
    	
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
