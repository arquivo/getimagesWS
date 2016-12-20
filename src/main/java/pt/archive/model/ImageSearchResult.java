package pt.archive.model;

import pt.archive.utils.Constants;
import pt.archive.utils.Constants.criteriaRank;

public class ImageSearchResult implements Comparable< ImageSearchResult > {
	
	String url;
	String width;
	String height;
	String alt;
	String title;
	String urlOriginal;
	String digest;
	Ranking score;
    String timestamp;
	String mime;
	
    public ImageSearchResult( ) { }
    
	public ImageSearchResult( String url, String width, String height, String alt, String title, String urlOriginal, String timestamp, Ranking score, String digest , String mime){
        this.url 			= url;
        this.width 			= width;
        this.height 		= height;
        this.alt 			= alt;
        this.title 			= title;
        this.urlOriginal 	= urlOriginal;
        this.timestamp 		= timestamp;
        this.score 			= score;
        this.digest			= digest;
        this.mime 			= mime;
    }

	public Ranking getScore() {
		return score;
	}
	public void setScore(Ranking score) {
		this.score = score;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getAlt() {
		return alt;
	}
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
    public String getTimestamp(){
        return timestamp;
    }
    public void setDigest(String digest) {
		this.digest = digest;
	}
	public String getDigest() {
		return digest;
	}
	public String getUrlOriginal() {
		return urlOriginal;
	}
	public void setUrlOriginal(String urlOriginal) {
		this.urlOriginal = urlOriginal;
	}
	public String getMime( ) {
		return mime;
	}
	public void setMime( String mime ) {
		this.mime = mime;
	}
	
	@Override
	public boolean equals( Object o ) {
		
		if ( o == this ) {
            return true;
        }
        if ( !( o instanceof ImageSearchResult ) ) {
            return false;
        }
        ImageSearchResult other = ( ImageSearchResult ) o;
        return this.digest.equals( other.digest );
        
	}
	
	private long ConvertTimstampToLong(  ) {
		return Long.parseLong( this.getTimestamp( ) );
	}
	
	@Override
	public int hashCode() {
	     return this.digest.hashCode( );
	}
	
	@Override
	public int compareTo( ImageSearchResult another ) {
		int result = 0;
		
		if( this.score.getRank( ).equals( Constants.criteriaRank.NEW.toString( ) ) ) {
			if( this.ConvertTimstampToLong( ) > another.ConvertTimstampToLong( ) )
				result = -1; 
			else if( this.ConvertTimstampToLong( ) < another.ConvertTimstampToLong( ) )
				result = 1; 
			else
				result = 0;
		} else if( this.score.getRank( ).equals( Constants.criteriaRank.OLD.toString( ) ) ) {
			if( this.ConvertTimstampToLong( ) < another.ConvertTimstampToLong( ) )
				result = -1; 
			else if( this.ConvertTimstampToLong( ) > another.ConvertTimstampToLong( ) )
				result = 1; 
			else
				result = 0;
		} else if( this.score.getRank( ).equals( Constants.criteriaRank.SCORE.toString( ) ) ) {
			if( this.getScore( ).getScore( ) > another.getScore( ).getScore( ) )
				result = -1; 
			else if( this.getScore( ).getScore( ) < another.getScore( ).getScore( ) )
				result = 1; 
			else
				result = 0;
		}
		
		
		return result;
	}
		
}
