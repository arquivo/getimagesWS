package pt.archive.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
//import com.sun.org.apache.xml.internal.security.utils.Base64;
//import org.apache.commons.codec.binary.Base64;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import javax.imageio.ImageIO;

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
	public ImageSearchResult getPropImage( ImageSearchResult img , int widthThumbnail , int heightThumbnail ) {
		BufferedImage bimg;
		try {
			bimg = ImageIO.read( new URL( img.getUrl( ) ) );
			
			double width          	= bimg.getWidth( );
			double height         	= bimg.getHeight( );
			double wThumbnail		= width * 0.5 ;
			double hThumbnail		= height * 0.5;
			
			//Image thumbnail = bimg.getScaledInstance( widthThumbnail , heightThumbnail , BufferedImage.SCALE_SMOOTH );
			img.setHeight( Double.toString( height ) );
			img.setWidth( Double.toString( width ) );
			BufferedImage scaledImg = null;
			scaledImg = Scalr.resize( bimg, 
					Method.SPEED, 
					Scalr.Mode.AUTOMATIC, 
					IntegralPart( wThumbnail ), 
					IntegralPart( hThumbnail ), 
					Scalr.OP_ANTIALIAS ); //create thumbnail
		
			// Create a byte array output stream.
			ByteArrayOutputStream bao = new ByteArrayOutputStream( );
			// Write to output stream
	        ImageIO.write( scaledImg , img.getMime( ).substring( 6 ) , bao );
	        bao.flush( );
	        String base64String = Base64.encode( bao.toByteArray( ) );
			bao.close( );
			log.info( "create thumbnail mime[" + img.getMime( ).substring( 6 ) + "] "
					+ "["+wThumbnail+"*"+hThumbnail+"]"
					+ " original size ["+width+"*"+height+"]");
	        img.setThumbnail( base64String );
			
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
	
	public int IntegralPart( Double number ) {
		double fractionalPart 	= ( number - 0.5 ) % 1;
		return ( int ) ( number - fractionalPart );
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
