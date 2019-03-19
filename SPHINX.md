http://sphinxsearch.com/docs/current.html#sphinxql-reference
http://sphinxsearch.com/downloads/sphinx-2.2.11-release.tar.gz/thankyou.html
http://sphinxsearch.com/files/sphinx-2.2.11-release.tar.gz

# Currently using this one
http://sphinxsearch.com/downloads/sphinx-2.3.2-beta.tar.gz/thankyou.html
http://sphinxsearch.com/files/sphinx-2.3.2-beta.tar.gz

mysql -P9306 -h 127.0.0.1
mysql -P9306 -h 127.0.0.1 < assets/sql/truncate_sphinx.sql

SELECT id FROM codesearchrealtime WHERE MATCH('test');

Have flipped over to Manticore Search 2.8.0 when built from source

Example queries

SELECT id FROM codesearchrealtime WHERE MATCH('test public') AND linescount >= 100 AND linescount <= 300;