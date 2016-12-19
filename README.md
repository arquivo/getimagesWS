# Arquivo.pt: Protótipo da pesquisa de imagens
Web service rest extracted from the [OpenSearch](https://github.com/arquivo/pywb-opensearch-cdx) webpage harvested. Then, it collects from the sites parametrizable number of images of each page, assigning each image a score for the final ranking. It also uses the link: https://github.com/ikreymer/pywb/wiki/CDX-Server-API [CDXServer] to verify that the resource was well indexed.

## Requirements
* JDK 1.7
* Maven 3
* Tomcat7 or another web-container

## Build and usage
* Build the application with: mvn clean install
* Deploy the link:getimagesWS/target[getimagesWS.war] file in a web-container
* Example usage ( search for **rtp** )
	----
	http://localhost:8080/getimagesWS/?query=rtp 
	----

## Configurations
* File blacklist url: /getimagesWS/blacklistUrl
* FIle blacklist domain: /getimagesWS/blacklistDomain
* File stop words: /getimagesWS/stopWords
	 
## Contacts
Developed by João Nobre (joaoanobre@gmail.com) 
Feel free to send emails with comments and questions.

## How Image search works
Input example
```
{total:0 , }
```

    




