package pt.archive.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pt.archive.model.ItemOpenSearch;

public class UserHandler extends DefaultHandler{
   private final Logger log = LoggerFactory.getLogger( this.getClass( ) );
   private List<ItemOpenSearch> items = null;
   private ItemOpenSearch item	= null;
   private boolean bId	 	= false;
   private boolean bTStamp 	= false;
   private boolean bSource  = false;
   private boolean bLink    = false;
   
   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	   log.debug( "Start Element = " + qName );
	   if ( qName.equalsIgnoreCase( "item" ) ) {
         item = new ItemOpenSearch( ); // create a new item and put it in Map
         if( items == null ) { //initialize list 
        	 log.debug( "---> Initialize list!!" );
        	 items = new ArrayList<>();
         }
	   } else if ( qName.equalsIgnoreCase( "source" )  ) {
		   bSource = true;
		   item.setUrl( attributes.getValue( "url" ) );
	   } else if( qName.equalsIgnoreCase( "pwa:id" ) ) {
		   bId = true;
	   } else if( qName.equalsIgnoreCase( "pwa:tstamp" ) ) {
		   bTStamp = true;
	   } else if( qName.equalsIgnoreCase( "pwa:tstamp" ) ) {
		   bLink = true;
	   }
	   
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
       log.debug( "[End Element] Name = " + qName );
	   if (qName.equalsIgnoreCase( "item" )) {
         items.add( item );
      }
   }

   @Override
   public void characters(char ch[], int start, int length) throws SAXException {
	   
	   if( bId ) {
		   log.debug( "[ID Element] contents = " + new String( ch , start , length ) );
		   item.setId( Long.parseLong( new String( ch , start , length ) ) ); 
		   bId = false;
	   } else if( bTStamp ) {
		   log.debug( "[bTStamp Element] contents = " + new String( ch , start , length ) );
		   item.setTstamp( Long.parseLong( new String( ch , start , length ) ) );
		   bTStamp = false;
	   } else if( bSource ) {
		   log.debug( "[ID Element] contents = " + new String( ch , start , length ) );
		   bSource = false;
	   } else if( bLink ) {
		   log.debug( "[ID Element] contents = " + new String( ch , start , length ) );
		   item.setLink( new String( ch , start , length ) );
		   bLink = false;
	   }
	   
   }
   
   /*
    * getter method for images list
    */
   public List< ItemOpenSearch > getItems( ) {
	   return items;
   }


	
}
