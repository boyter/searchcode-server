FROM manticoresearch/manticore:2.8.2

COPY ./sphinx.conf /opt/app/
COPY ./sphinx.conf /usr/bin/

CMD ["/usr/bin/searchd" "--nodetach"]
