package pt.archive.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
	public ImageSearchResult getPropImage( ImageSearchResult img , int thumbWidth , int thumbHeight , String mimetype ) {
		BufferedImage bimg;
		ByteArrayOutputStream bao = new ByteArrayOutputStream( );
		String base64String;	
		
		try {
			InputStream inImg =  new URL( img.getUrl( ) ).openConnection( ).getInputStream( );
			bimg = ImageIO.read( inImg );
			int width          	= bimg.getWidth( null );
			int height         	= bimg.getHeight( null );
			img.setHeight( Double.toString( height ) );
			img.setWidth( Double.toString( width ) );

			if( mimetype.equals( "image/gif" ) ) {
				byte[] bytesImgOriginal = IOUtils.toByteArray( new URL( img.getUrl( ) ).openConnection( ).getInputStream( ) );
				base64String = Base64.encode( bytesImgOriginal );
				img.setThumbnail( base64String );
				return img;
			}
			double wThumbnail		= width * 0.5 ;
			double hThumbnail		= height * 0.5;
			
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
			log.info( "create thumbnail mime[" + img.getMime( ).substring( 6 ) + "] "
					+ "["+thumbWidth+"*"+thumbHeight+"]"
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
