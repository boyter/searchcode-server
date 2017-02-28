FROM openjdk:8-jre

RUN apt-get update && apt-get install -y \
    curl \
    tar

RUN curl -o searchcode-server-community.tar.gz https://searchcode.com/static/searchcode-server-community.tar.gz \
    && tar zxvf searchcode-server-community.tar.gz

WORKDIR /searchcode-server-community/release

EXPOSE 8080

CMD ["./searchcode-server.sh"]