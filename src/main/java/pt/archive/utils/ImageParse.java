package pt.archive.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.archive.model.ImageSearchResult;

public class ImageParse {
	private final Logger log = LoggerFactory.getLogger( this.getClass( ) );
	
	private ImageSearchResult getPropImage( ImageSearchResult img ){
		BufferedImage bimg;
		try {
			bimg = ImageIO.read( new URL( img.getUrl() ) );
		} catch (MalformedURLException e) {
			log.error( "[ImageParse][getPropImage] get image from url[" + img.getUrl( ) + "] error = " , e );
			return null;
		} catch (IOException e) {
			log.error( "[ImageParse][getPropImage] e = " , e );
			return null;
		}
		float width          = bimg.getWidth();
		float height         = bimg.getHeight();
		
		img.setHeight( Float.toString( height ) );
		img.setWidth( Float.toString( width ) );
		
		return img;
	}
	
	
	class Image {
		private String width;
		private String heigth;
		private String src;
		
		public Image( String width , String heigth , String src ) {
			this.width  = width;
			this.heigth = heigth;
			this.src    = src;
		}
		public String getWidth() {
			return width;
		}
		public void setWidth(String width) {
			this.width = width;
		}
		public String getHeigth() {
			return heigth;
		}
		public void setHeigth(String heigth) {
			this.heigth = heigth;
		}
		public String getSrc() {
			return src;
		}
		public void setSrc(String src) {
			this.src = src;
		}
		
		
	}
	

}
