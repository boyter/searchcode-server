FROM manticoresearch/manticore:2.8.2

COPY ./sphinx.conf /opt/app/
COPY ./sphinx.conf /usr/bin/
COPY ./sphinx.conf /etc/sphinxsearch/

RUN mkdir -p /tmp/sphinx-search/data/
RUN mkdir -p /var/data/

ENTRYPOINT ["/usr/bin/searchd", "--nodetach"]
