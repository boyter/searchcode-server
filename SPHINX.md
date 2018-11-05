http://sphinxsearch.com/docs/current.html#sphinxql-reference
http://sphinxsearch.com/downloads/sphinx-2.2.11-release.tar.gz/thankyou.html
http://sphinxsearch.com/files/sphinx-2.2.11-release.tar.gz

mysql -P9306 -h 127.0.0.1
mysql -P9306 -h 127.0.0.1 < assets/sql/truncate_sphinx.sql

SELECT id FROM codesearchrealtime WHERE MATCH('test');