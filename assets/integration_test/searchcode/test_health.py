import requests

from .common import get_base_url


def test_health_check():
    url = get_base_url() + 'health-check/'
    r = requests.get(url)

    assert r.status_code == 200


def test_health_check_legacy():
    url = get_base_url() + 'healthcheck/'
    r = requests.get(url)

    assert r.status_code == 200
