package pt.archive.model;
import java.util.*;

public class ImageSearchResults {

    private final long totalResults;
    private final List<ImageSearchResult> imageResults;

    public ImageSearchResults(List<ImageSearchResult> imageResults , int numResults) {
        this.totalResults = numResults;
        this.imageResults = imageResults;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public List<ImageSearchResult> getContent() {
        return imageResults;
    }
}
