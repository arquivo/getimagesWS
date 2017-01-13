package pt.archive.utils;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.geom.AffineTransform;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	 * @throws InterruptedException 
	 */
	public ImageSearchResult getPropImage( ImageSearchResult img , int thumbWidth , int thumbHeight , List< String > sizes , int[] sizeInterval , int flagSafeImage, String hostSafeImage , BigDecimal safeValue , String safeImageType ) throws InterruptedException {
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
			
			img.setMime( type );
			img.setHeight( Double.toString( height ) );
			img.setWidth( Double.toString( width ) );
			byte[ ] bytesImgOriginal = IOUtils.toByteArray( new URL( img.getUrl( ) ).openConnection( ).getInputStream( ) );
			base64StringOriginal = Base64.encode( bytesImgOriginal );
			//calculate digest
			digest.update( bytesImgOriginal );
			byte byteDigest[ ] = digest.digest();
			img.setDigest( convertByteArrayToHexString( byteDigest ) );
			
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
			else {
				if( type.equals( "image/gif" ) ) {
					
					//byte[] output = getThumbnailGif( inImg , thumbWidth , thumbHeight );
					// Create a byte array output stream.
			        base64String = Base64.encode( bytesImgOriginal );
					bao.close( );
					img.setThumbnail( base64String );
					if( flagSafeImage == 1  ) {
						SafeImage safeImage = checkSafeImage(  safeImageType , base64StringOriginal , hostSafeImage , log , img );
						if( safeImage != null ) {
							img.setSafe( safeImage.getSafe( ) );
							img.setNotSafe( safeImage.getNotSafe( ) );
						}
					}	
					return img;
				} else
					scaledImg = Scalr.resize( bimg, 
						Method.QUALITY, 
						Scalr.Mode.AUTOMATIC, 
						thumbWidth, 
						thumbHeight, 
						Scalr.OP_ANTIALIAS ); //create thumbnail
			}
				
			
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
			
			if( flagSafeImage == 1  ){
				SafeImage safeImage = checkSafeImage(  safeImageType , base64StringOriginal , hostSafeImage , log , img );
				if( safeImage != null ) {
					img.setSafe( safeImage.getSafe( ) );
					img.setNotSafe( safeImage.getNotSafe( ) );
				}
				
			}
			
			
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
	 * 
	 * @param gif
	 * @return
	 * @throws IOException
	 */
	public byte[] getThumbnailGif( InputStream gif , int thumbWidth , int thumbHeight ) throws IOException{
		//BufferedImage output;
		log.info( "gif = " + gif.toString( ) );
		List< BufferedImage > frames = getFrameGif( gif );
		log.info( "number of frames => " + frames.size() );
		/*GifDecoder d = new GifDecoder();
		d.read( gif );
		
		int n = d.getFrameCount();
		
		for (int i = 0; i < n; i++) {
			BufferedImage frame = d.getFrame( i );  // frame i
			//int t = d.getDelay(i);  // display duration of frame in milliseconds
			BufferedImage image = Scalr.resize( frame, 
					Method.QUALITY, 
					Scalr.Mode.AUTOMATIC, 
					thumbWidth, 
					thumbHeight, 
					Scalr.OP_ANTIALIAS ); //create thumbnail
			images.add( image );
		}*/
		//encode gif
		final ByteArrayOutputStream out = new ByteArrayOutputStream( );
		ImageOutputStream output = ImageIO.createImageOutputStream( out );
		byte[ ] imageBytes = null;
		if( frames == null || frames.size( ) == 0 )
			return null;
		
	    GifSequenceWriter writer = 
	    	      new GifSequenceWriter(output, frames.get( 0 ).getType(), 1, false);

	    // write out the first image to our sequence...
	    writer.writeToSequence( frames.get( 0 ) );
	    for(int i=1; i < frames.size( ) - 1 ; i++) {
	      writer.writeToSequence( frames.get( i ) );
	    }
	    writer.close( );
	    output.close( );
	    imageBytes=out.toByteArray();
		return imageBytes;
	}
	
	private List< BufferedImage > getFrameGif( InputStream gif ) {
		try {
		    String[] imageatt = new String[]{
		            "imageLeftPosition",
		            "imageTopPosition",
		            "imageWidth",
		            "imageHeight"
		    };    
		    List< BufferedImage > frames = new ArrayList< >( );
		    ImageReader reader = ( ImageReader ) ImageIO.getImageReadersByFormatName( "gif" ).next( );
		    ImageInputStream ciis = ImageIO.createImageInputStream( gif );
		    reader.setInput( ciis , false );

		    int noi = reader.getNumImages( true );
		    BufferedImage master = null;
		    log.info( "noi == " + noi );
		    for ( int i = 0 ; i < noi ; i++ ) { 
		        BufferedImage image = reader.read( i );
		        IIOMetadata metadata = reader.getImageMetadata( i );

		        Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
		        NodeList children = tree.getChildNodes();
		        if( image == null )
		        	log.info( "reader.read is null" );
		        if( metadata == null )
		        	log.info( "metadata is null" );
		        
		        for ( int j = 0 ; j < children.getLength( ) ; j++ ) {
		            Node nodeItem = children.item( j );

		            if(nodeItem.getNodeName().equals("ImageDescriptor")){
		                Map<String, Integer> imageAttr = new HashMap<String, Integer>();

		                for (int k = 0; k < imageatt.length; k++) {
		                    NamedNodeMap attr = nodeItem.getAttributes();
		                    Node attnode = attr.getNamedItem(imageatt[k]);
		                    imageAttr.put(imageatt[k], Integer.valueOf(attnode.getNodeValue()));
		                }
		                if(i==0){
		                    master = new BufferedImage(imageAttr.get("imageWidth"), imageAttr.get("imageHeight"), BufferedImage.TYPE_INT_ARGB);
		                }
		                master.getGraphics().drawImage(image, imageAttr.get("imageLeftPosition"), imageAttr.get("imageTopPosition"), null);
		            }
		        }
		        //ImageIO.write(master, "GIF", new File( i + ".gif"));
		        frames.add( master );
		    }
		    return frames;
		} catch (IOException e) {
		    e.printStackTrace();
		    return null;
		}
	}
	
	/**
	 * convert the byte to hex format method (digest imgge)
	 * @param arrayBytes
	 * @return
	 */
	private String convertByteArrayToHexString( byte[ ] byteData ) {
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
	public SafeImage checkSafeImage(  String safeImageType , String base64String , String hostSafeImage , Logger log , ImageSearchResult img ) {
		if( !safeImageType.toLowerCase( ).equals( "all" ) ) { //adult image filter
			//TODO 
			SafeImage safeImage = SafeImageClient.getSafeImage( base64String , hostSafeImage , log , img.getUrl( ) );
			if( safeImage == null ) {
				log.info( "Reject image!!!!! url["+img.getUrl( )+"]" );
				return null;
			} 
			return safeImage;
		} else 
			return null;

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
	
	private BufferedImage scale(BufferedImage source,double ratio) {
		  int w = (int) (source.getWidth() * ratio);
		  int h = (int) (source.getHeight() * ratio);
		  BufferedImage bi = getCompatibleImage(w, h);
		  Graphics2D g2d = bi.createGraphics();
		  double xScale = (double) w / source.getWidth();
		  double yScale = (double) h / source.getHeight();
		  AffineTransform at = AffineTransform.getScaleInstance(xScale,yScale);
		  g2d.drawRenderedImage(source, at);
		  g2d.dispose();
		  return bi;
	}
	
	private BufferedImage getCompatibleImage(int w, int h) {
		  GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		  GraphicsDevice gd = ge.getDefaultScreenDevice();
		  GraphicsConfiguration gc = gd.getDefaultConfiguration();
		  BufferedImage image = gc.createCompatibleImage(w, h);
		  return image;
		}
	

}
