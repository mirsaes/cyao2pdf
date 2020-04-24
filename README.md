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
	

	docker run -p 8080:8080 mirsaes/cyao2pdf:beta


 4. use curl to convert a file to pdf
	

	curl -X POST -F "name=test.txt" -F "file=@/home/mirsaes/test.txt" http://localhost:8080/live/topdf


if configured to use a password use the below, however ssl is not configured on the server
		

	curl -X POST -u user:password -F "name=test.txt" -F "file=@/home/mirsaes/test.txt" http://localhost:8080/live/topdf
	

5. use curl to convert a remote file to a pdf

	curl -X POST -F "name=web.txt" -F "file=https://somesite.com/withatextfile" http://localhost:8080/live/urltopdf
	
	curl -X POST -F "name=test.docx" -F "file=https://interoperability.blob.core.windows.net/files/MS-DOCX/%5bMS-DOCX%5d-200219.docx" http://localhost:8080/live/urltopdf > docx.pdf

This might be useful when using Amazon S3 and [Temporary Credentials via Query String Request Authentication](http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html#RESTAuthenticationQueryStringAuth) - but that has not been tested.

## Notes
* wanted to use spotify's dockerfile-maven-plugin to automate the docker build, but it has a few issues still
