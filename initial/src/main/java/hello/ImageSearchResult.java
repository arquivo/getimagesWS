package hello;

public class ImageSearchResult {

	String url;
	Float width;
	Float height;
	String alt;
	String title;
	String urlOriginal;
    long timestamp;
	
	public ImageSearchResult(String url, Float width, Float height, String alt, String title, String urlOriginal, long timestamp){
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
	public Float getWidth() {
		return width;
	}
	public void setWidth(Float width) {
		this.width = width;
	}
	public Float getHeight() {
		return height;
	}
	public void setHeight(Float height) {
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
