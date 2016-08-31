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


	cp target/topdf-0.0.1-SNAPSHOT.jar ../docker
	cd ../docker
    docker build -t mirsaes/cyao2pdf:beta ./
	

 3. launch the docker image


	docker run -p 8080:8080 mirsaes/cyao2pdf:beta ./


 4. use curl to convert a file to pdf


	curl -X POST -F "name=test.txt" -F "file=@/home/mirsaes/test.txt" http://localhost:8080/live/topdf
	

if using password, however ssl is not configured
	

	curl -X POST -u user:password -F "name=test.txt" -F "file=@/home/mirsaes/test.txt" http://localhost:8080/live/topdf

## Notes
* it might make sense to use a Dockerfile to build the java app
* disclaimer: written with one handed typing due to holding a newborn during vacation  : )
	* yes, that IS slow
