package pt.archive.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.archive.model.ImageSearchResult;

public class ImageParse {
	private final Logger log = LoggerFactory.getLogger( this.getClass( ) );
	
	public ImageParse( ) { }

	/**
	 * 
	 * @param img
	 * @param widthThumbnail 
	 * @param heightThumbnail
	 * @return
	 */
	public ImageSearchResult getPropImage( ImageSearchResult img , int thumbWidth , int thumbHeight , String mimetype , List< String > sizes , int[] sizeInterval , int flagSafeImage, String hostSafeImage , BigDecimal safeValue , String safeImageType ) {
		BufferedImage bimg;
		ByteArrayOutputStream bao = new ByteArrayOutputStream( );
		String base64String;	
		
		try {
			InputStream inImg =  new URL( img.getUrl( ) ).openConnection( ).getInputStream( );
			bimg = ImageIO.read( inImg );
			int width          	= bimg.getWidth( null );
			int height         	= bimg.getHeight( null );
			
			if( !checkSize( width , height , sizes , sizeInterval ) ){
				log.info( "Size out of range [" + width + "*" + height + "]" );
				return null;
			}
			
			img.setHeight( Double.toString( height ) );
			img.setWidth( Double.toString( width ) );
			
			if( mimetype.equals( "image/gif" ) ) {
				byte[ ] bytesImgOriginal = IOUtils.toByteArray( new URL( img.getUrl( ) ).openConnection( ).getInputStream( ) );
				
				base64String = Base64.encode( bytesImgOriginal );
				img.setThumbnail( base64String );
				return img;
			}
			
			double thumbRatio = (double) thumbWidth / (double) thumbHeight;
			double imageRatio = (double) width / (double) height;
			if ( thumbRatio < imageRatio ) 
				thumbHeight = (int)( thumbWidth / imageRatio );
			else 
				thumbWidth = (int)( thumbHeight * imageRatio );
			
			if( width < thumbWidth && height < thumbHeight ) {
				thumbWidth  = width;
				thumbHeight = height;
			} else if( width < thumbWidth )
				thumbWidth = width;
			else if( height < thumbHeight )
				thumbHeight = height;

			//Image thumbnail = bimg.getScaledInstance( widthThumbnail , heightThumbnail , BufferedImage.SCALE_SMOOTH );
			BufferedImage scaledImg = null;
			if( width < thumbWidth || height < thumbHeight )
				scaledImg = bimg;
			else
				scaledImg = Scalr.resize( bimg, 
						Method.SPEED, 
						Scalr.Mode.AUTOMATIC, 
						thumbWidth, 
						thumbHeight, 
						Scalr.OP_ANTIALIAS ); //create thumbnail
			
			// Write to output stream
	        ImageIO.write( scaledImg , img.getMime( ).substring( 6 ) , bao );
	        bao.flush( );
	        
	        // Create a byte array output stream.
	        base64String = Base64.encode( bao.toByteArray( ) );
			bao.close( );
			log.debug( "create thumbnail mime[" + img.getMime( ).substring( 6 ) + "] "
					+ "["+thumbWidth+"*"+thumbHeight+"]"
					+ " original size ["+width+"*"+height+"]");
			img.setThumbnail( base64String );
			
			if( flagSafeImage == 1  && !safeImageType.toLowerCase( ).equals( "all" ) ) { //adult image filter
				//TODO 
				BigDecimal safe = SafeImageClient.getSafeImage( base64String , hostSafeImage , log , img.getUrl( ) );
				if( safe.compareTo( BigDecimal.ZERO ) < 0 ){
					log.info( "Reject image!!!!! url["+img.getUrl( )+"]" );
					return null;
				} 
				img.setSafe( safe );
			} else 
				img.setSafe( new BigDecimal( -1 ) );
			
			
			
		} catch ( MalformedURLException e ) {
			log.error( "[ImageParse][getPropImage] get image from url[" + img.getUrl( ) + "] error = " , e );
			return null;
		} catch ( IOException e ) {
			log.error( "[ImageParse][getPropImage] e = " , e );
			return null;
		} catch( IllegalArgumentException | ImagingOpException e ) {
			log.error( "[ImageParse][getPropImage] e = " , e );
			return null;
		}
		return img;
	}
	
	
	/**
	 * return integral part of the number
	 * @param number
	 * @return
	 */
	public int IntegralPart( Double number ) {
		double fractionalPart 	= ( number - 0.5 ) % 1;
		return ( int ) ( number - fractionalPart );
	}
	
	
	
	/**
	 * Check if image size is within the desired range
	 * @param width
	 * @param heigth
	 * @param sizes
	 * @return
	 */
	public boolean checkSize( int width , int heigth , List< String > sizes , int[] sizeInterval ) {
		int indSize = 0;
		if( sizes == null || sizes.isEmpty( ) ){
			return true;
		}
		
		for( String size : sizes ) {
			if( size.toLowerCase( ).equals( Constants.sizeAll ) )
				return true;
			if( size.toLowerCase( ).equals( Constants.sizeIcon ) ) {
				if( betweenInclusive( width , sizeInterval[indSize] , sizeInterval[++indSize] ) 
					&& betweenInclusive( heigth , sizeInterval[++indSize] , sizeInterval[++indSize] ) ) 
					return true;
			}
			indSize = 3;
			if( size.toLowerCase( ).equals( Constants.sizeSmall ) ) {
				log.debug( "small width["+width+"] to ["+sizeInterval[indSize+1]+"*"+sizeInterval[indSize+2]+"]" );
				if( betweenInclusive( width , sizeInterval[indSize] , sizeInterval[++indSize] ) ) 
					return true;
				log.debug( "small width["+width+"] to ["+sizeInterval[indSize+1]+"*"+sizeInterval[indSize+2]+"]" );
				if( betweenInclusive( heigth , sizeInterval[++indSize] , sizeInterval[++indSize] ) ) 
					return true;
			}
			indSize = 7;
			if( size.toLowerCase( ).equals( Constants.sizeMedium ) ) {
				log.debug( "medium width["+width+"] to ["+sizeInterval[indSize+1]+"*"+sizeInterval[indSize+2]+"]" );
				if( betweenInclusive( width , sizeInterval[++indSize] , sizeInterval[++indSize] ) )
					return true;
				log.debug( "medium heigth["+heigth+"] to ["+sizeInterval[indSize+1]+"*"+sizeInterval[indSize+2]+"]" );
				if( betweenInclusive( heigth , sizeInterval[++indSize] , sizeInterval[++indSize] ) )
					return true;
			}
			indSize = 11;
			if( size.toLowerCase( ).equals( Constants.sizeLarge ) ) {
				log.debug( "large heigth["+heigth+"] to ["+sizeInterval[indSize+1]+"*"+sizeInterval[indSize+2]+"]" );
				if( betweenInclusive( width , sizeInterval[++indSize] , sizeInterval[++indSize] ) )
					return true;
				log.debug( "large heigth["+heigth+"] to ["+sizeInterval[indSize+1]+"*"+sizeInterval[indSize+2]+"]" );
				if( betweenInclusive( heigth , sizeInterval[++indSize] , sizeInterval[++indSize] ) )
					return true;
			}
			indSize = 0;
		}
		
		return false;
	}
	
	/**
	 * check x in range [lowerBound,upperBound]
	 * @param x
	 * @param lowerBound
	 * @param upperBoound
	 * @return
	 */
	private boolean betweenInclusive( int x , int lowerBound , int upperBoound ) {
	       return x >= lowerBound && x <= upperBoound;    
	}
	
	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage( Image img ) {
	    if ( img instanceof BufferedImage ) {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage( img.getWidth( null ), img.getHeight(null ), BufferedImage.TYPE_INT_ARGB );

	    // Return the buffered image
	    return bimage;
	}
	

}
