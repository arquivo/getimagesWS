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
http://localhost:8080/getimagesWS/?query=rtp 
```

## Configurations
* File blacklist url: /getimagesWS/blacklistUrl
* FIle blacklist domain: /getimagesWS/blacklistDomain
* File stop words: /getimagesWS/stopWords
	 
## Contacts
Developed by João Nobre (joaoanobre@gmail.com) 
Feel free to send emails with comments and questions.

##Input example
```
query = rtp 
```
**Input attributes**
* query: query is searching 
* start: start index to search in openSearch
* stamp: time interval to search, format: startDate-endDate (example:"19960101000000-20151022163016") 

##Output example (json)
[source,json]
----
{"totalResults":1,"content":[{"url":"http://arquivo.pt/noFrame/replay/20110520204656im_/http://www.jornaldenegocios.pt/images/2010_05/rtp_not_pe.jpg","width":"","height":"","alt":"","title":"","urlOriginal":"http://topicos.jornaldenegocios.pt/RTP","digest":"ab1af682c12ff47f365732bc1cdc5b99","score":{"score":3.0,"rank":0},"timestamp":"20110520204656","mime":"image/jpeg"}]}
----

Search has generated *389* results, as described by the *totalResults* field

**Output attributes**
* url: link to resource
* width: image width
* height: image height
* alt: image alt
* title: image title
* urlOriginal: original url where resource is available
* digest: resource hash
* score: number for the ranking (the higher the more relevant) 
* timestamp: image timestamp
* mime: image mimetype


##Advanced Search Image





