FROM ubuntu:16.04
ENV http_proxy ${http_proxy}
ENV https_proxy ${https_proxy}

RUN apt-get update && apt-get install -y \
    openjdk-8* \
    maven \
    maven* \
    python-pip \
    python-dev \
    build-essential \
    libffi-dev \
    libssl-dev

RUN pip install fabric

COPY ./ /opt/app/
WORKDIR /opt/app/
RUN fab test