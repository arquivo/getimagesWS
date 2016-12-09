package pt.archive.utils;

import java.util.ArrayList;
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
	private List< String > terms;
	
	public HTMLParser( CountDownLatch doneSignal , ItemXML itemtoSearch , int numImgsbyUrl , String hostImage , String urldirct , List< String > terms ) { 
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
		Ranking rank = new Ranking( );
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
			else {
				log.info( "[HTMLParser] return url["+ link +"] statusCode == " + resp.statusCode( ) );
				return;
			}
			
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
					if( !src.startsWith( urldirect ) )
						src = hostImage.concat( urldirect ).concat( src );
					else
						src = hostImage.concat( src );
			}
			else 
				continue;
			
			String titleImg = getAttribute( imgItem , "title" );
			String width 	= getAttribute( imgItem , "width" );
			String height 	= getAttribute( imgItem , "height" );
			String alt 		= getAttribute( imgItem , "alt" );
			
			log.debug( "[Tag Images] title["+titleImg+"] width["+width+"] height["+height+"] alt["+alt+"]" );
			float scoreImg = checkTerms( src, titleImg , alt );
			if( scoreImg == 0 )
				continue;
			else
				rank.setScore( scoreImg );
			
			if( title == null || title.trim().equals( "" ) )
				title = "";
			
			resultsImg.add( new ImageSearchResult(  src , width , height , alt , titleImg , itemtoSearch.getUrl( ) , itemtoSearch.getTstamp( ) , rank ) );
			
			log.debug( "[Images] source = " + imgItem.attr( "src" ) + " alt = " + imgItem.attr( "alt" ) 
			          + " height = " + imgItem.attr( "height" ) + " width = " + imgItem.attr( "width" ) + " urlOriginal = " + itemtoSearch.getUrl( ) + " score = " + rank.getScore( ) );
			
			if( numImgsbyUrl != -1 ) countImg++;
			
		}
		countImg = 0;
		
		log.debug( "Number of results = [" + resultsImg.size( ) + "] to url[" + link + "]" );
		
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
	
	private float checkTerms( String src , String titleImg , String alt ) {
		int counterTermssrc 	= 0;
		int counterTermsTitle 	= 0;
		int counterTermsAlt 	= 0;
		log.debug( "***Analisar Terms***" );
		for( String term : terms ) { 
			log.debug( "Term["+term.toLowerCase()+"] src["+src.toLowerCase()+"] title["+titleImg.toLowerCase()+"] alt["+alt.toLowerCase()+"]" );
			if( src.toLowerCase( ).contains( term.toLowerCase( ) ) ) {
				
				counterTermssrc++;
			}
			if( titleImg.toLowerCase( ).contains( term.toLowerCase( ) ) ) {
				counterTermsTitle++;
			}
			if( alt.toLowerCase( ).contains( term.toLowerCase( ) ) ) {
				counterTermsAlt++;
			}	
		}
		log.debug( "checkTerms src["+ counterTermssrc +"] title["+ counterTermsTitle +"] alt["+ counterTermsAlt +"]" );
		if( counterTermsAlt == 0 && counterTermssrc == 0 && counterTermsTitle == 0 )
			return 0;
		
		if( counterTermssrc >= counterTermsTitle &&  counterTermssrc >= counterTermsAlt ) {
			if( counterTermssrc == terms.size( ) )
				return Constants.srcScore + Constants.incrementRank;
			else
				return Constants.srcScore;
		} else if( counterTermsTitle >= counterTermssrc &&  counterTermsTitle >= counterTermsAlt ) {
			if( counterTermsTitle == terms.size( ) )
				return Constants.titleScore + Constants.incrementRank;
			else
				return Constants.titleScore;
		} else if( counterTermsAlt >= counterTermssrc &&  counterTermsAlt >= counterTermsTitle ) {
			if( counterTermsAlt == terms.size( ) )
				return Constants.altScore + Constants.incrementRank;
			else
				return Constants.altScore;
		}
		
		return 0;
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
