package pt.archive.utils;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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
			
			float width          = bimg.getWidth( );
			float height         = bimg.getHeight( );
			//Image thumbnail = bimg.getScaledInstance( widthThumbnail , heightThumbnail , BufferedImage.SCALE_SMOOTH );
			img.setHeight( Float.toString( height ) );
			img.setWidth( Float.toString( width ) );
			
			BufferedImage scaledImg = Scalr.resize( bimg, 
									Method.SPEED, 
									Scalr.Mode.AUTOMATIC, 
									widthThumbnail, 
									heightThumbnail, 
									Scalr.OP_ANTIALIAS);

			// Create a byte array output stream.
	        ByteArrayOutputStream bao = new ByteArrayOutputStream( );
	        log.info( "create thumbnail mime["+img.getMime( ).substring( 6 )+"]" );
	        // Write to output stream
	        ImageIO.write( scaledImg , img.getMime().substring( 6 ) , bao );
	        bao.flush( );
	        String base64String = Base64.encode( bao.toByteArray( ) );
			bao.close( );
	 
	        img.setThumbnail( base64String );
			log.info( "ImageParse = " + img.getUrl( ) );
			
		} catch ( MalformedURLException e ) {
			log.error( "[ImageParse][getPropImage] get image from url[" + img.getUrl( ) + "] error = " , e );
			return null;
		} catch ( IOException e ) {
			log.error( "[ImageParse][getPropImage] e = " , e );
			return null;
		}
		return img;
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
