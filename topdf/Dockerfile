#debian
#FROM ubuntu:14.04
#FROM ubuntu:16.04
FROM ubuntu:18.04

#red hat (rpm) via yum
#FROM centos:6

MAINTAINER mirsaes

# make sure "stuff" is up to date
# install / update libre office
# install jre for app
# ubuntu 18 has jre 10 installed by default
RUN \
	apt-get update && \
	apt-get -y upgrade && \
	apt-get -y install libreoffice && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
RUN java -version
# above is "base image"

# now the "app/web service image"
# could use python flask or spring boot, whatever, express js
# other image can then install a flask app or a go app or tomcat app

# need java for open office
#https://www.atlantic.net/community/howto/install-java-jre-jdk-on-ubuntu-16-04/
#	apt -y install openjdk-8-jdk

WORKDIR /topdf

# add the webapp
ADD \
	./target/topdf.jar /topdf/topdf.jar

# add the webapps configuration (by default just sample.properties)
ADD \
	./docker/*.properties /topdf/

# expose webapp listening on 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "topdf.jar"]
