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
	
	public HTMLParser( CountDownLatch doneSignal , ItemXML itemtoSearch , int numImgsbyUrl , String hostImage , String urldirct  ) { 
		this.itemtoSearch 	= itemtoSearch;
		this.numImgsbyUrl 	= numImgsbyUrl;
		this.hostImage		= hostImage;
		this.urldirect		= urldirct;
		this.resultsImg 	= new ArrayList< >( ); 
		this.doneSignal 	= doneSignal;
	}
	
	@Override
	public List< ImageSearchResult > call( ) throws Exception {
		buildResponse( ); //build response to search images per link
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
			
			String width;
			if( imgItem.attr( "width" ) != null && !imgItem.attr( "width" ).trim().equals( "" ) )
				width  = imgItem.attr( "width" );
			else 
				width = "";
			
			String height;
			if( imgItem.attr( "height" ) != null && !imgItem.attr( "height" ).trim().equals( "" ) )
				height  = imgItem.attr( "height" ); 
			else
				height = "";
			
			String alt;
			if( imgItem.attr( "alt" ) != null && !imgItem.attr( "height" ).trim().equals( "" ) )
				alt  = imgItem.attr( "alt" ); 
			else 
				alt = "";
			
			if( title == null || title.trim().equals( "" ) )
				title = "";
			
			resultsImg.add( new ImageSearchResult(  src , width , height , alt , title , itemtoSearch.getUrl( ) , itemtoSearch.getTstamp( ) ) );
			
			log.debug( "[Images] source = " + imgItem.attr( "src" ) + " alt = " + imgItem.attr( "alt" ) 
			          + " height = " + imgItem.attr( "height" ) + " width = " + imgItem.attr( "width" ) + " urlOriginal = " + itemtoSearch.getUrl( ) );
			
			if( numImgsbyUrl != -1) countImg++;
			
		}
		countImg = 0;			
		
		log.info( "Total de results = [" + resultsImg.size( ) + "] to url[" + link + "]" );
		
	}
	
	private String getLink( String url , long tstamp , String linkGetImage ) {
		log.debug( "[getLink] linkGetImage["+ linkGetImage +"] url["+ url +"] tstamp["+ tstamp +"] " );
		return linkGetImage
				.concat( String.valueOf( tstamp ) )
				.concat( Constants.urlBarOP )
				.concat( url );
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
