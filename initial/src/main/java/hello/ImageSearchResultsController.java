package hello;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
public class ImageSearchResultsController {

	private List<ImageSearchResult> imageResults = new ArrayList<ImageSearchResult>();
    

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ImageSearchResults getImages(@RequestParam(value="query", defaultValue="") String query) {

    	imageResults = getImageResults(query);
        return new ImageSearchResults(imageResults);
    }
    /* Method that calls the OpenSearchAPI to get the first 10 urls of the query
    * and Returns a list of Images
    */
    public List<ImageSearchResult> getImageResults(String query){
    	ImageSearchResult exampleTest1 = new ImageSearchResult("http://sapo.pt/img.jpg", (float) 300.0, (float)200.0, "sapo servico de apontadores", "SAPO titulo", "http://sapo.pt", 20040000000000L); 
 		ImageSearchResult exampleTest2 = new ImageSearchResult("http://dn.pt/img.png", (float) 450.0, (float)150.0, "DN servico de apontadores", "DN titulo", "http://dn.pt", 20080100000000L); 
 		imageResults.add(exampleTest1);
 		imageResults.add(exampleTest2);
 		return imageResults;
    }
}
