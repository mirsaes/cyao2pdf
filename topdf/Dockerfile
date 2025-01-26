FROM ubuntu:24.04

LABEL maintainer="mirsaes"

# avoid tzone prompts, etc
ARG DEBIAN_FRONTEND=noninteractive

# make sure "stuff" is up to date
# install / update libre office
# install jre 21
RUN \
	apt-get update && \
	apt-get -y upgrade && \
	apt-get -y --no-install-recommends install libreoffice && \
	apt-get -y install openjdk-21-jre-headless && \
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

COPY \
	./docker/convert.sh /topdf/upload-dir/convert.sh

# add a few available users
RUN addgroup cyao2pdf && \
	adduser --disabled-login --quiet --gecos "cyao2pdf1 user" cyao2pdf1 && \
	adduser --disabled-login --quiet --gecos "cyao2pdf2 user" cyao2pdf2 && \
	adduser --disabled-login --quiet --gecos "cyao2pdf3 user" cyao2pdf3 && \
	adduser --disabled-login --quiet --gecos "cyao2pdf4 user" cyao2pdf4 && \
	adduser --disabled-login --quiet --gecos "cyao2pdf5 user" cyao2pdf5 && \
	adduser --disabled-login --quiet --gecos "cyao2pdf6 user" cyao2pdf6 && \
	adduser --disabled-login --quiet --gecos "cyao2pdf7 user" cyao2pdf7 && \
	adduser --disabled-login --quiet --gecos "cyao2pdf8 user" cyao2pdf8 && \
	usermod -a -G cyao2pdf cyao2pdf1 && \
	usermod -a -G cyao2pdf cyao2pdf2 && \
	usermod -a -G cyao2pdf cyao2pdf3 && \
	usermod -a -G cyao2pdf cyao2pdf4 && \
	usermod -a -G cyao2pdf cyao2pdf5 && \
	usermod -a -G cyao2pdf cyao2pdf6 && \
	usermod -a -G cyao2pdf cyao2pdf7 && \
	usermod -a -G cyao2pdf cyao2pdf8 && \
	chown -R root:cyao2pdf /topdf/upload-dir  && \
	chmod g+rw /topdf/upload-dir

# expose webapp listening on 8080
EXPOSE 8080

CMD ["java", "-jar", "topdf.jar"]
