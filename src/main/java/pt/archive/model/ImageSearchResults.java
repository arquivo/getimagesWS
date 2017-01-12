package pt.archive.model;
import java.util.*;

public class ImageSearchResults {

    private final long totalResults;
    private final List< ImageSearchResult > imageResults;
    private final String query;
    
    public ImageSearchResults( List< ImageSearchResult > imageResults , int numResults, String query ) {
        this.totalResults = numResults;
        this.imageResults = imageResults;
        this.query 		  = query;
    }

    public long getTotalResults( ) {
        return totalResults;
    }

    public List< ImageSearchResult > getContent( ) {
        return imageResults;
    }

	public String getQuery() {
		return query;
	}
}
