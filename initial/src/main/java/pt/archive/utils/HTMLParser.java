package pt.archive.utils;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.archive.model.ImageSearchResult;
import pt.archive.model.ItemXML;

public class HTMLParser {
	private final Logger log = LoggerFactory.getLogger( this.getClass( ) );
	private Document doc;
	
	public HTMLParser(  ) { }
	
	public List< ImageSearchResult > buildResponse( List< ItemXML > urlOpenSearch , int numImgsbyUrl , String hostImage , String urldirct ) {
		List< ImageSearchResult > result = new ArrayList< >( );
		int countImg = 0;
		
		log.debug( "[buildResponse] size url = " + urlOpenSearch.size( ) );
		for( ItemXML item : urlOpenSearch ) {
			String link = getLink( item.getUrl( ) , item.getTstamp( ) , hostImage.concat( urldirct ) );
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
				log.error( "[Jsoup] get response ["+ item.getUrl( ) +"] exception = ", e );
				continue;
			}
			
			String title = doc.title( ); //get page title
			Elements links = doc.select( "img" ); //get all tag <img />
			for( Element imgItem : links) {
					
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
				
				result.add( new ImageSearchResult(  src , width , height , alt , title , item.getUrl( ) , item.getTstamp( ) ) );
				
				log.debug( "[Images] source = " + imgItem.attr( "src" ) + " alt = " + imgItem.attr( "alt" ) 
				          + " height = " + imgItem.attr( "height" ) + " width = " + imgItem.attr( "width" ) + " urlOriginal = " + item.getUrl( ) );
				
				if( numImgsbyUrl != -1) countImg++;
				
			}
			countImg = 0;			
		}
		
		log.info( "Total de results = " + result.size( ) );
		
		return result;
	}
	
	private String getLink( String url , long tstamp , String linkGetImage ) {
		log.debug( "[getLink] linkGetImage["+ linkGetImage +"] url["+ url +"] tstamp["+ tstamp +"] " );
		return linkGetImage
				.concat( String.valueOf( tstamp ) )
				.concat( Constants.urlBarOP )
				.concat( url );
	}
	
	
}
