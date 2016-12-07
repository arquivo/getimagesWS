package pt.archive.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.archive.model.ImageSearchResult;
import pt.archive.model.ItemXML;

public class HTMLParser implements Callable< List< ImageSearchResult > > {
	private final Logger log = LoggerFactory.getLogger( this.getClass( ) );
	private Document doc;
	private ItemXML itemtoSearch;
	private int numImgsbyUrl;
	private String hostImage;
	private String urldirect;
	private List< ImageSearchResult > resultsImg;
	private CountDownLatch doneSignal;
	private String[ ] terms;
	
	public HTMLParser( CountDownLatch doneSignal , ItemXML itemtoSearch , int numImgsbyUrl , String hostImage , String urldirct , String[] terms ) { 
		this.itemtoSearch 	= itemtoSearch;
		this.numImgsbyUrl 	= numImgsbyUrl;
		this.hostImage		= hostImage;
		this.urldirect		= urldirct;
		this.resultsImg 	= new ArrayList< >( ); 
		this.doneSignal 	= doneSignal;
		this.terms			= terms;
	}
	
	@Override
	public List< ImageSearchResult > call( ) throws Exception {
		try {
			buildResponse( ); //build response to search images per link
		} catch( Exception e ) {
			log.error( "[HTMLParser][call] item-digest[" + itemtoSearch.getDigest( ) + "]" , e );
		} finally {
			doneSignal.countDown( );
		}
		return resultsImg;
	}

	public void buildResponse( ) {
		int countImg = 0;
		String link = getLink( itemtoSearch.getUrl( ) , itemtoSearch.getTstamp( ) , hostImage.concat( urldirect ) );
		
		log.debug( "[HTMLParser][buildResponse] URL search = " + link );
		try {
			Connection.Response resp = Jsoup.connect( link )
					.userAgent( "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21" )
					.timeout( 5000 )
					.ignoreHttpErrors( true )
					.execute( );
			doc = null;
			if( resp.statusCode( ) == 200 ) 
				doc = resp.parse( );
			
		} catch( Exception e ) {
			log.error( "[Jsoup] get response link["+ link +"] orignalURL["+ itemtoSearch.getUrl( ) +"] exception = ", e );
			return;
		}
		
		String title = doc.title( ); //get page title
		Elements links = doc.select( "img" ); //get all tag <img />
		for( Element imgItem : links ) {
				
			if( numImgsbyUrl != -1 ) 
				if( countImg == numImgsbyUrl ) break; 
			
			String src;
			if( imgItem.attr( "src" ) != null && !imgItem.attr( "src" ).trim().equals( "" ) ) {
				src  = imgItem.attr( "src" );
				if( !src.startsWith( hostImage ) ) 
					src = hostImage.concat( src );
			}
			else 
				continue;
			
			String titleImg = getAttribute( imgItem , "title" );
			String width 	= getAttribute( imgItem , "width" );
			String height 	= getAttribute( imgItem , "height" );
			String alt 		= getAttribute( imgItem , "alt" );
			log.info( "[Tag Images] title["+titleImg+"] width["+width+"] height["+height+"] alt["+alt+"]" );
			if( !checkTerms( src, titleImg , width , height , alt ) )
				continue;
			
			if( title == null || title.trim().equals( "" ) )
				title = "";
			
			resultsImg.add( new ImageSearchResult(  src , width , height , alt , titleImg , itemtoSearch.getUrl( ) , itemtoSearch.getTstamp( ) ) );
			
			log.debug( "[Images] source = " + imgItem.attr( "src" ) + " alt = " + imgItem.attr( "alt" ) 
			          + " height = " + imgItem.attr( "height" ) + " width = " + imgItem.attr( "width" ) + " urlOriginal = " + itemtoSearch.getUrl( ) );
			
			if( numImgsbyUrl != -1 ) countImg++;
			
		}
		countImg = 0;			
		log.info( "Number of results = [" + resultsImg.size( ) + "] to url[" + link + "]" );
		
	}
	
	private String getAttribute( Element tag , String attrName ) {
		if( tag.attr( attrName ) != null && !tag.attr( attrName ).trim().equals( "" ) )
			return tag.attr( attrName );
		else 
			return "";
	}
	
	private String getLink( String url , long tstamp , String linkGetImage ) {
		log.debug( "[getLink] linkGetImage["+ linkGetImage +"] url["+ url +"] tstamp["+ tstamp +"] " );
		return linkGetImage
				.concat( String.valueOf( tstamp ) )
				.concat( Constants.urlBarOP )
				.concat( url );
	}
	
	private boolean checkTerms( String src, String titleImg , String width , String height , String alt ) {
		for( String term : terms ) { 
			if( src.toLowerCase( ).contains( term.toLowerCase( ) ) 
					|| titleImg.toLowerCase( ).contains( term.toLowerCase( ) ) 
					|| width.toLowerCase( ).contains( term.toLowerCase( ) ) 
					|| height.toLowerCase( ).contains( term.toLowerCase( ) ) 
					|| alt.toLowerCase( ).contains( term.toLowerCase( ) ) )
				return true;
		}
		return false;
	}
	
	public List< ImageSearchResult > getResultsImg( ) {
		return resultsImg;
	}

	public void setResultsImg( List< ImageSearchResult > resultsImg ) {
		this.resultsImg = resultsImg;
	}

	@Override
	public String toString( ) {
		return "HTMLParser [doc=" + doc.getElementsByAttribute( "title" ) + ", numImgsbyUrl=" + numImgsbyUrl + ", hostImage=" + hostImage + ", urldirect="
				+ urldirect + "]";
	}

}
