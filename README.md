# [Arquivo.pt](http://arquivo.pt/): Prototype Search Image
Web service rest extracted from the [OpenSearch](https://github.com/arquivo/pywb-opensearch-cdx) webpage harvested. Then, it collects from the sites parametrizable number of images of each page, assigning each image a score for the final ranking. It also uses the [CDXServer](https://github.com/ikreymer/pywb/wiki/CDX-Server-API) to verify that the resource was well indexed.

## Requirements
* JDK 1.7
* Maven 3
* Tomcat7 or another web-container

## Build and usage
* Build the application with: mvn clean install
* Deploy the link:getimagesWS/target[getimagesWS.war] file in a web-container
* Example usage ( search for **rtp** )
```
http://localhost:8080/getimagesWS/?query=antonio%20costa&stamp=19960101000000-20151022163016&start=2
```

## Configurations
* File blacklist url: /getimagesWS/blacklistUrl
* File blacklist domain: /getimagesWS/blacklistDomain
* File stop words: /getimagesWS/stopWords

## Properties file: application.properties
| **Propertie** | **Description** | **Example** |
|:------|:----------|:----------|
| urlBase | Opensearch url used in web search | urlBase=http://arquivo.pt/opensearch?query=
| type | Parameter of expected results type in opensearch | type=text/html | 
| hitsPerSite |  Maximum number of requests for each site in opensearch. | hitsPerSite=2 |
| hitsPerPage |  Maximum number of results to display in opensearch, the default is 10. | hitsPerPage=100 |
| NThreads | Number of threads that will process image fetch on each site | NThreads=10 |
| NumImgsbyUrl | Number images per site, -1 if get all images per site | NumImgbyUrl=-1 |
| hostGetImage | Host to get the images | hostGetImage=http://arquivo.pt |
| urldirectoriesImage | Excerpt from the reproduction url of the original site of the image | urldirectoriesImage=/noFrame/replay/ |
| urlBaseCDX | Host to check images in CDXServer | urlBaseCDX=http://arquivo.pt/wayback/-cdx? |
| outputCDX | Type output response in the CDXServer | outputCDX=json |
| flCDX | Parameter to CDXServer url, define response attributes  | flCDX=url,timestamp,digest,mime|
| logging.level.pt.archive | Define level for log4j | logging.level.pt.archive = INFO  |
| logging.file | Logger file location | logging.file = (...)/tomcat/logs/application-getimagesWS.log|
| stopWords.file | stop word file location | (...)/getimagesWS/stopWords |
| blacklistUrl.file | Location of urls black list file | blacklistUrl.file = (...)/getimagesWS/blacklistUrl |
| blacklistDomain.file | Location of black domains list file | blacklistDomain.file = (...)/getimagesWS/blacklistDomain |
| imgParseflag | lag for the web service return the actual size of the images (0 no return / 1 return) | imgParseflag = 1 |


## Contacts
Developed by João Nobre (joaoanobre@gmail.com) 
Feel free to send emails with comments and questions.

##Input example
```
query = rtp 
```
**Input attributes**
* ``query``: query is searching 
* ``start``: start index to search in openSearch (default:0)
* ``stamp``: time interval to search, format: startDate-endDate (default:"19960101000000-20151022163016") 

##Output example (json)

     {"totalResults":1,"content":[{"url":"http://arquivo.pt/noFrame/replay/20110520204656im_/http://www.jornaldenegocios.pt/images/2010_05/rtp_not_pe.jpg","width":"","height":"","alt":"","title":"","urlOriginal":"http://topicos.jornaldenegocios.pt/RTP","digest":"ab1af682c12ff47f365732bc1cdc5b99","score":{"score":3.0,"rank":0},"timestamp":"20110520204656","mime":"image/jpeg"}]}

Search has generated *389* results, as described by the *totalResults* field

**Output attributes**
* ``url``: link to resource
* ``width``: image width
* ``height``: image height
* ``alt``: image alt
* ``title``: image title
* ``urlOriginal``: original url where resource is available
* ``digest``: resource hash
* ``score``: number for the ranking (the higher the more relevant) 
* ``timestamp``: image timestamp
* ``mime``: image mimetype


##Advanced Search Image

##Search Operators (query terms)
| **Search Service** | **Description** | **Example** |
|:------|:----------|:----------|
| site: | site-only search | site:sapo.pt 
| sort: | returns results ordered by  <ul><li>Ranking (default)</li><li>New</li><li>Old</li></ul>  | sort:new (sort images descending  timestamp ) | 
| type: | returns only images of the type  | type:png |
| ""     | search images with a expression | "Antonio Costa" (search images containing a expression "Antonio Costa") |
