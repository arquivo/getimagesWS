package pt.archive.utils;

import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
import pt.archive.model.ItemCDXServer;
import pt.archive.model.ItemOpenSearch;
import pt.archive.model.Ranking;

public class HTMLParser implements Callable< List< ImageSearchResult > > {
	private final Logger log = LoggerFactory.getLogger( this.getClass( ) );
	private Document doc;
	private ItemOpenSearch itemtoSearch;
	private int numImgsbyUrl;
	private String hostImage;
	private String urldirect;
	private List< ImageSearchResult > resultsImg;
	private CountDownLatch doneSignal;
	private List< String > terms;
	private String urlBaseCDX;
	private String outputCDX;
	private String flParam;
	private List< String > blacklistUrls;
	
	public HTMLParser( CountDownLatch doneSignal , ItemOpenSearch itemtoSearch , int numImgsbyUrl , String hostImage , String urldirct , List< String > terms , String urlBaseCDX, String outputCDX, String flParam, List< String > blacklistUrls) { 
		this.itemtoSearch 	= itemtoSearch;
		this.numImgsbyUrl 	= numImgsbyUrl;
		this.hostImage		= hostImage;
		this.urldirect		= urldirct;
		this.resultsImg 	= new ArrayList< >( ); 
		this.doneSignal 	= doneSignal;
		this.terms			= terms;
		this.urlBaseCDX		= urlBaseCDX;
		this.outputCDX		= outputCDX;
		this.flParam		= flParam;
		this.blacklistUrls  = blacklistUrls;
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
		ItemCDXServer resultCDXServer;
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
			ImageSearchResult resultImg;
			if( numImgsbyUrl != -1 ) 
				if( countImg == numImgsbyUrl ) break; 
			
			String src, srcOriginal;
			if( imgItem.attr( "src" ) != null && !imgItem.attr( "src" ).trim().equals( "" ) && !presentBlackList( imgItem.attr( "src" ) ) ) {
				src = imgItem.attr( "src" ); //absolute URL on src
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
			
			//log.info( "Parent : " + imgItem.parent( ).tagName( ) );
			
			log.debug( "[Tag Images] title["+titleImg+"] width["+width+"] height["+height+"] alt["+alt+"]" );
			float scoreImg = checkTerms( src, titleImg , alt );
			if( scoreImg == 0 )
				continue;
			else
				rank.setScore( scoreImg );
			
			if( title == null || title.trim( ).equals( "" ) )
				title = "";
			
			try {
				log.info( " [CDXParser] URL = " + itemtoSearch.getUrl( ) + " src = " + src );
				CDXParser itemCDX = new CDXParser( urlBaseCDX , outputCDX , flParam , new ImageSearchResult(  src , width , height , alt , titleImg , itemtoSearch.getUrl( ) , itemtoSearch.getTstamp( ) , rank , null , null ) );
				resultCDXServer = itemCDX.getImgCDX( );
				if( resultCDXServer == null )
					continue;
				
				resultsImg.add( new ImageSearchResult(  src , width , height , alt , titleImg , itemtoSearch.getUrl( ) , resultCDXServer.getTimestamp( ) , rank , resultCDXServer.getDigest() , resultCDXServer.getMime() ) );
				
				log.debug( "[Images] source = " + imgItem.attr( "src" ) + " alt = " + imgItem.attr( "alt" ) 
				          + " height = " + imgItem.attr( "height" ) + " width = " + imgItem.attr( "width" ) + " urlOriginal = " + itemtoSearch.getUrl( ) + " score = " + rank.getScore( ) );
				
				if( numImgsbyUrl != -1 ) countImg++;
				
			} catch( Exception e ) {
				log.warn( "[Image] Error get resource["+src+"] " );
				continue;
			}
		}
		countImg = 0;
		
		log.debug( "Number of results = [" + resultsImg.size( ) + "] to url[" + link + "]" );
		
	}
	
	private boolean presentBlackList( String src ) {
		for( String blacksrc : blacklistUrls ) 
			if( blacksrc.equals( src ) ) 
				return true;
		
		return false;
	}
	
	private String getAttribute( Element tag , String attrName ) {
		if( tag.attr( attrName ) != null && !tag.attr( attrName ).trim().equals( "" ) )
			return tag.attr( attrName );
		else 
			return "";
	}
	
	private String getLink( String url , String tstamp , String linkGetImage ) {
		log.debug( "[getLink] linkGetImage["+ linkGetImage +"] url["+ url +"] tstamp["+ tstamp +"] " );
		return linkGetImage
				.concat( tstamp )
				.concat( Constants.urlBarOP )
				.concat( url );
	}
	
	private float checkTerms( String src , String titleImg , String alt ) {
		int counterTermssrc 	= 0;
		int counterTermsTitle 	= 0;
		int counterTermsAlt 	= 0;
		URL urlSrc = null;
		List< String > urlTerms;
		float resultScore = 0;
		try {
			urlSrc = new URL( src );
			urlTerms = new LinkedList< String >( Arrays.asList( urlSrc.getPath( ).split( "/" ) ) );
		} catch( Exception e ) {
			return 0.0f;
		}
		
		log.debug( "src["+src+"]" );
		log.debug("protocol = " + urlSrc.getProtocol());
        log.debug("authority = " + urlSrc.getAuthority());
        log.debug("host = " + urlSrc.getHost());
        log.debug("port = " + urlSrc.getPort());
        log.debug("path = " + urlSrc.getPath());
        log.debug("query = " + urlSrc.getQuery());
        log.debug("filename = " + urlSrc.getFile());
        log.debug("ref = " + urlSrc.getRef());
        
		log.debug( "***Analisar Terms***" );
		for( String term : terms ) { //counter number of term occurrence (title,src,alt)
			log.debug( "Term["+term.toLowerCase()+"] src["+src.toLowerCase()+"] title["+titleImg.toLowerCase()+"] alt["+alt.toLowerCase()+"]" );
			int index = src.toLowerCase( ).indexOf( term.toLowerCase( ) );
			if( index != -1 ) 
				counterTermssrc += checkWhereis( urlTerms , term ) + 1;
			
			if( titleImg.toLowerCase( ).contains( term.toLowerCase( ) ) ) 
				counterTermsTitle++;
			
			if( alt.toLowerCase( ).contains( term.toLowerCase( ) ) ) 
				counterTermsAlt++;
		}
		log.debug( "checkTerms src["+ counterTermssrc +"] title["+ counterTermsTitle +"] alt["+ counterTermsAlt +"]" );
		if( counterTermsAlt == 0 && counterTermssrc == 0 && counterTermsTitle == 0 )
			return 0;
		
		if( counterTermssrc >= counterTermsTitle &&  counterTermssrc >= counterTermsAlt ) {
			resultScore += counterTermssrc;
			if( counterTermssrc == terms.size( ) )
				resultScore += Constants.srcScore + Constants.incrementRank;
			else
				resultScore += Constants.srcScore;
		} else if( counterTermsTitle >= counterTermssrc &&  counterTermsTitle >= counterTermsAlt ) {
			resultScore += counterTermsTitle;
			if( counterTermsTitle == terms.size( ) )
				resultScore += Constants.titleScore + Constants.incrementRank;
			else
				resultScore += Constants.titleScore;
		} else if( counterTermsAlt >= counterTermssrc &&  counterTermsAlt >= counterTermsTitle ) {
			resultScore += counterTermsAlt;
			if( counterTermsAlt == terms.size( ) )
				resultScore += Constants.altScore + Constants.incrementRank;
			else
				resultScore += Constants.altScore;
		}
		
		return resultScore;
	}
	
	
	private int checkWhereis( List< String > urlTerms , String queryTerm ) {
		int counterIndex = 1,
			counterResult = 0;
		for( String term : urlTerms ) {
			int index = term.indexOf( queryTerm );
			if( index != -1 ) {
				if( counterIndex == urlTerms.size( ) ) 
					counterResult += Constants.incrScorePath * 2; 
				else
					counterResult += Constants.incrScorePath;
			}
			counterIndex++;
		}
		
		return counterResult;
	}
	
	public List< ImageSearchResult > getResultsImg( ) {
		return resultsImg;
	}

	public void setResultsImg( List< ImageSearchResult > resultsImg ) {
		this.resultsImg = resultsImg;
	}
	
	private String convertByteYoHex( String str ) {
		try {
			MessageDigest md = MessageDigest.getInstance( "SHA-256" );
			md.update( str.getBytes( ) );
			
			byte byteData[ ] = md.digest( );
			
			//convert the byte to hex format method 1
			StringBuffer sb = new StringBuffer( );
			
			for ( int i = 0 ; i < byteData.length ; i++ ) {
			    sb.append( Integer.toString( ( byteData[ i ] & 0xff ) + 0x100, 16 ).substring( 1 ) );
			}	
			log.debug( "src["+str+"] digest["+sb.toString( )+"]" );
			return sb.toString( );

		} catch( Exception e ) {
			return null;
		}
		
	}
	
	@Override
	public String toString( ) {
		return "HTMLParser [doc=" + doc.getElementsByAttribute( "title" ) + ", numImgsbyUrl=" + numImgsbyUrl + ", hostImage=" + hostImage + ", urldirect="
				+ urldirect + "]";
	}

}
