import requests
import urllib

from .common import get_base_url
from .common import get_blns


def test_search_blns():
    for naughty in get_blns():
        url = get_base_url() + 'v1/' + naughty
        r = requests.get(url)

        assert r.status_code == 404 or r.status_code == 400, url
