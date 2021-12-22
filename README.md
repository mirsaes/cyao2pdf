# cyao2pdf
Convert documents to PDF
"chow" "two" p.d.f. - Cloud, Yet Another Office 2 PDF

## Introduction
cyao2pdf is a POC to convert office documents to pdf.
The docker image exposes a REST'ish service that connects users to libreoffice "convert to" pdf functionality.

## Usage
Using curl to convert a file to pdf

 1. build the java app
	

	cd topdf

	mvn package

 2. build the docker image
	

 	docker build -t mirsaes/cyao2pdf:beta ./
	

 3. launch the docker image
	

	docker run --rm --init -p 8080:8080 mirsaes/cyao2pdf:beta

```
	# run with process reaper and local file used as application.properties to override app settings
	docker run --init --rm -d -p 8080:8080 --mount 'type=bind,src=/full/path/to/sample.properties,dst=/topdf/application.properties' mirsaes/cyao2pdf:beta
```

 4. use curl to convert a file to pdf
	

	curl -X POST -F "name=test.txt" -F "file=@/home/mirsaes/test.txt" http://localhost:8080/live/topdf


if configured to use a password use the below, however ssl is not configured on the server
		

	curl -X POST -u user:password -F "name=test.txt" -F "file=@/home/mirsaes/test.txt" http://localhost:8080/live/topdf
	

5. use curl to convert a remote file to a pdf

	curl -X POST -F "name=web.txt" -F "file=https://somesite.com/withatextfile" http://localhost:8080/live/urltopdf
	
	curl -X POST -F "name=test.docx" -F "file=https://interoperability.blob.core.windows.net/files/MS-DOCX/%5bMS-DOCX%5d-200219.docx" http://localhost:8080/live/urltopdf > docx.pdf

This might be useful when using Amazon S3 and [Temporary Credentials via Query String Request Authentication](http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html#RESTAuthenticationQueryStringAuth) - but that has not been tested.

## Notes
* noted

## Sample Properties
```
# whether to use a user pool to convert documents
convertusers.enabled: true

# number of users to use (users must exist, up to max of 8 users)
convertusers.count: 4

# username prefix used to form converting username, e.g. cyao2pdf1, cyao2pdf2, etc
convertusers.username.prefix: cyao2pdf

```

## Versions
* 0.0.8c
* update to include log4j 2.17

* 0.0.8b
  * log4helle fix vengeance
  * update to include log4j 2.16

* 0.0.8a
  * log4helle fix, dunkel
  * analysis showed default implementation in 0.0.8 and 0.0.7 should have been unaffected, however if users configured access log logging via property file overrides then it would have been affected

* 0.0.7a
  * includes updated log4j library to fix exploit

* 0.0.8
  * Reduced image size, ubuntu 20
  * Ubuntu 20
  * LibreOffice
  * jre 11

* 0.0.7c
  * update to include log4j 2.17

* 0.0.7b
  * update to include log4j 2.16

* 0.0.7a
  * update to include log4j 2.15

* 0.0.7
  * Basic parallel document conversion support
  * Ubuntu 18
  * LibreOffice
  * jre 11


