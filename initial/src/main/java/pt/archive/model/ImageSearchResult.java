package pt.archive.model;

public class ImageSearchResult {
	
	String url;
	String width;
	String height;
	String alt;
	String title;
	String urlOriginal;
    long timestamp;
	
    public ImageSearchResult() { }
    
	public ImageSearchResult(String url, String width, String height, String alt, String title, String urlOriginal, long timestamp){
        this.url = url;
        this.width = width;
        this.height = height;
        this.alt = alt;
        this.title = title;
        this.urlOriginal = urlOriginal;
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
	public String getUrlOriginal() {
		return urlOriginal;
	}
	public void setUrlOriginal(String urlOriginal) {
		this.urlOriginal = urlOriginal;
	}
    public long getTimestamp(){
        return timestamp;
    }
		
}
