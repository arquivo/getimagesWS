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
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

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
	public ImageSearchResult getPropImage( ImageSearchResult img , int thumbWidth , int thumbHeight , List< String > sizes , int[] sizeInterval , int flagSafeImage, String hostSafeImage , BigDecimal safeValue , String safeImageType ) {
		BufferedImage bimg;
		ByteArrayOutputStream bao = new ByteArrayOutputStream( );
		String base64String, 
			base64StringOriginal;	
		String type = null;
		URLConnection uc = null;
		MessageDigest digest = null;
		try {
			uc = new URL( img.getUrl( ) ).openConnection( );
			InputStream inImg =  uc.getInputStream( );
			bimg = ImageIO.read( inImg );
			type = getMimeType( uc );
			digest = MessageDigest.getInstance( "SHA-256" );
			
			int width          	= bimg.getWidth( null );
			int height         	= bimg.getHeight( null );
			if( !checkSize( width , height , sizes , sizeInterval ) ) {
				log.info( "Size out of range [" + width + "*" + height + "]" );
				return null;
			}
			Iterator< ImageReader > imageReaders = ImageIO.getImageReaders( inImg );
			img.setMime( type );
			img.setHeight( Double.toString( height ) );
			img.setWidth( Double.toString( width ) );
			byte[ ] bytesImgOriginal = IOUtils.toByteArray( new URL( img.getUrl( ) ).openConnection( ).getInputStream( ) );
			base64StringOriginal = Base64.encode( bytesImgOriginal );
			//calculate digest
			digest.update( bytesImgOriginal );
			byte byteDigest[ ] = digest.digest();
			img.setDigest( convertByteArrayToHexString( byteDigest ) );
			
			if( type.equals( "image/gif" ) ) {
				img.setThumbnail( base64StringOriginal );
				if( flagSafeImage == 1  )
					img.setSafe( checkSafeImage( safeImageType , base64StringOriginal , hostSafeImage , log , img ) );
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
			
			log.debug( "Create thumbnail mime[" + img.getMime( ) + "] "
						+ "["+thumbWidth+"*"+thumbHeight+"]"
						+ " original size ["+width+"*"+height+"] img[" + img.getUrl( ) + "]");
			img.setThumbnail( base64String );
			
			if( flagSafeImage == 1  )
				img.setSafe( checkSafeImage(  safeImageType , base64StringOriginal , hostSafeImage , log , img ) );
			
		} catch ( NoSuchAlgorithmException e ) {
			log.error( "[ImageParse][getPropImage] Digest error, e = " );
			return null;
		} catch ( MalformedURLException e ) {
			log.error( "[ImageParse][getPropImage] get image from url[" + img.getUrl( ) + "] error = " , e );
			return null;
		} catch ( IOException e ) {
			log.error( "[ImageParse][getPropImage] [" + img.getUrl( ) + "] e = " , e );
			return null;
		} catch( IllegalArgumentException | ImagingOpException e ) {
			log.error( "[ImageParse][getPropImage] [" + img.getUrl( ) + "] e = " , e );
			return null;
		} finally {
			try {
				if( bao != null )
					bao.close( );
			} catch( IOException e2 ) {
				log.error( "[getPropImage] " );
			}
			
		}
		return img;
	}
	
	/**
	 * convert the byte to hex format method (digest imgge)
	 * @param arrayBytes
	 * @return
	 */
	private static String convertByteArrayToHexString( byte[ ] byteData ) {
        StringBuffer hexString = new StringBuffer( );
    	for ( int i = 0 ; i < byteData.length ; i++ ) {
    		String hex = Integer.toHexString( 0xff & byteData[ i ] );
   	     	if( hex.length( ) == 1 ) hexString.append( '0' );
   	     	hexString.append( hex );
    	}
    	
    	return hexString.toString( );
	}
	
	/**
	 * Get mimetype from url
	 * @param uc
	 * @return
	 * @throws java.io.IOException
	 * @throws MalformedURLException
	 */
	public static String getMimeType( URLConnection uc ) throws java.io.IOException, MalformedURLException {
		return uc.getContentType( );
    }

	
	/**
	 * Call safe image API
	 * @param safeImageType
	 * @param base64String
	 * @param hostSafeImage
	 * @param log
	 * @param img
	 * @return
	 */
	public BigDecimal checkSafeImage(  String safeImageType , String base64String , String hostSafeImage , Logger log , ImageSearchResult img ) {
		if( !safeImageType.toLowerCase( ).equals( "all" ) ) { //adult image filter
			//TODO 
			BigDecimal safe = SafeImageClient.getSafeImage( base64String , hostSafeImage , log , img.getUrl( ) );
			if( safe.compareTo( BigDecimal.ZERO ) < 0 ) {
				log.info( "Reject image!!!!! url["+img.getUrl( )+"]" );
				return null;
			} 
			return safe;
		} else 
			return new BigDecimal( -1 );

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
