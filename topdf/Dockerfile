FROM ubuntu:18.04

LABEL maintainer="mirsaes"

# make sure "stuff" is up to date
# install / update libre office
# ubuntu 18.04 has jre 11 installed by default
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

WORKDIR /topdf

# add the webapp
COPY \
	./target/topdf.jar /topdf/topdf.jar

# add the webapps configuration (by default just sample.properties)
ADD \
	./docker/*.properties /topdf/

# expose webapp listening on 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "topdf.jar"]
