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
	

	docker run --rm -d --init -p 8080:8080 mirsaes/cyao2pdf:beta

```
	# run with process reaper and local file used as application.properties to override app settings
	docker run --init --rm -d -p 8080:8080 --mount 'type=bind,src=/full/path/to/sample.properties,dst=/topdf/application.properties' mirsaes/cyao2pdf:beta
```
 4. check health or run basic tests

```
  curl http://localhost:8080/live/health

  curl http://localhost:8080/live/health?testConvert=true

  curl http://localhost:8080/live/test
```

 5. use curl to convert a file to pdf
	

	curl -X POST -F "name=test.txt" -F "file=@/home/mirsaes/test.txt" http://localhost:8080/live/topdf


if configured to use a password use the below, however ssl is not configured on the server
		

	curl -X POST -u user:password -F "name=test.txt" -F "file=@/home/mirsaes/test.txt" http://localhost:8080/live/topdf
	

6. use curl to convert a remote file to a pdf

	curl -X POST -F "name=web.txt" -F "file=https://somesite.com/withatextfile" http://localhost:8080/live/urltopdf

odd, this blob share apparently had its permissions locked down, oops

https://interoperability.blob.core.windows.net/files/MS-DOCX/%5bMS-DOCX%5d-200219.docx

so an alternative test file, has been specified in example below

	curl -X POST -F "name=test.docx" -F "file=https://msopenspecs.azureedge.net/files/MS-DOCX/%5bMS-DOCX%5d-230815.docx" http://localhost:8080/live/urltopdf > docx.pdf

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
* 0.0.12
  * update spring boot to [3.2.2](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes)[Support LifeCycle](https://spring.io/projects/spring-boot#support)
  * Ubuntu 22.04
  * LibreOffice 7.3
  * jre 17
  * spring 3.2

* 0.0.11
  * update spring boot to [3.0.6](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes) [Support LifeCycle](https://spring.io/projects/spring-boot#support)
  * Ubuntu 22.04
  * LibreOffice 7.3
  * jre 17
  * spring 3.0

* 0.0.10
  * update spring boot to [2.7.4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes) [LTS](https://spring.io/projects/spring-boot#support)
  * Ubuntu 22.04
  * LibreOffice 7.3
  * jre 17
  * spring 2.7

* 0.0.9
  * update spring boot to [2.6.6](https://spring.io/blog/2022/03/31/spring-boot-2-6-6-available-now) [LTS](https://spring.io/projects/spring-boot#support)
  * includes security fix for [CVE-2022-22965](https://tanzu.vmware.com/security/cve-2022-22965)
  * however, was not vulnerable as build uses default for generating executable jar rather than a war
  * Ubuntu 20.04
  * LibreOffice 6.4.7.2
  * jre 11

* 0.0.8d
  * security update to include log4j 2.17.1

* 0.0.8c
  * security update to include log4j 2.17.0

* 0.0.8b
  * log4helle fix vengeance
  * update to include log4j 2.16

* 0.0.8a
  * log4helle fix, dunkel
  * analysis showed default implementation in 0.0.8 and 0.0.7 should have been unaffected, however if users configured access log logging via property file overrides then it would have been affected

* 0.0.8
  * Reduced image size, ubuntu 20
  * Ubuntu 20
  * LibreOffice
  * jre 11

* 0.0.7d
  * security update to include log4j 2.17.1

* 0.0.7c
  * update to include log4j 2.17

* 0.0.7b
  * update to include log4j 2.16

* 0.0.7a
  * includes updated log4j library to fix exploit
  * update to include log4j 2.15

* 0.0.7
  * Basic parallel document conversion support
  * Ubuntu 18
  * LibreOffice
  * jre 11


