import requests
import urllib
from jsonschema import validate

from .common import get_base_url
from .common import get_blns


def test_search_api():
    url = get_base_url() + 'api/codesearch_I/?q=test'
    r = requests.get(url)
    j = r.json()

    assert r.status_code == 200


def test_search_api_blns():
    for naughty in get_blns():
        url = get_base_url() + 'api/codesearch_I/?q=' + urllib.parse.quote_plus(naughty)
        r = requests.get(url)
        try:
            j = r.json()
        except:
            raise Exception("Error decoding JSON for ", url)

        assert r.status_code == 200, url


def test_search_values():
    url = get_base_url() + 'api/codesearch_I/?q=test'
    r = requests.get(url)
    j = r.json()

    # TODO investigate jsonschema
    assert r.status_code == 200
    assert 'matchterm' in j
    assert 'previouspage' in j
    assert 'searchterm' in j
    assert 'query' in j
    assert 'language_filters' in j

    for x in j['language_filters']:
        assert 'count' in x
        assert 'id' in x
        assert 'language' in x

    assert 'total' in j
    assert 'results' in j

    for x in j['results']:
        assert 'repo' in x
        assert 'language' in x
        assert 'linescount' in x
        assert 'name' in x
        assert 'url' in x
        assert 'md5hash' in x
        assert 'lines' in x
        assert 'id' in x
        assert 'filename' in x

    assert 'page' in j
    assert 'nextpage' in j
    assert 'source_filters' in j

    for x in j['source_filters']:
        assert 'count' in x
        assert 'id' in x
        assert 'source' in x