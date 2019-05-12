import requests
import urllib

from .common import get_base_url
from .common import get_blns


def test_search():
    url = get_base_url() + '?q=test'
    r = requests.get(url)

    assert r.status_code == 200


def test_search_invalid_encoding():
    '''
    This should be fixed, returning a 500 is not ideal as it crashes
    the application. Should catch it.
    '''
    url = get_base_url() + '''?q=<script src="/\%(jscript)s"></script>'''
    r = requests.get(url)

    assert r.status_code == 500


def test_search_blns():
    for naughty in get_blns():
        url = get_base_url() + '?q=' + urllib.parse.quote_plus(naughty)
        r = requests.get(url)

        assert r.status_code == 200, url