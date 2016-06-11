#debian
#FROM ubuntu:14.04
FROM ubuntu:16.04

#red hat (rpm) via yum
#FROM centos:6
ENV topdf_version 0.0.1-SNAPSHOT

MAINTAINER mirsaes

# make sure "stuff" is up to date
RUN \
	apt-get update && \
	apt-get -y upgrade


#install / update libre office
RUN \
	apt -y install libreoffice

# above is "base image"

# now the "app/web service image"
# could use python flask or spring boot, whatever, express js
# other image can then install a flask app or a go app or tomcat app

# need java for open office
#https://www.atlantic.net/community/howto/install-java-jre-jdk-on-ubuntu-16-04/
RUN \
	apt -y install openjdk-8-jre
#	apt -y install openjdk-8-jdk

WORKDIR /topdf

ADD \
	./topdf/topdf-${topdf_version}.jar /topdf/topdf.jar

ADD \
	./topdf/*.properties /topdf/

# expose webapp listening on 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "topdf.jar"]
