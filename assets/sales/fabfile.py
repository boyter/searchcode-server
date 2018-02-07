import fabric

from fabric.api import env
from fabric.api import run
from fabric.api import local
from fabric.api import sudo
from fabric.api import prompt
from fabric.utils import warn
from fabric.operations import put
from fabric.contrib.files import sed
from fabric.context_managers import settings, hide, cd
from fabric.colors import yellow

env.user = 'root'
env.SERVERS = [
    {'name': 'server1', 'ip': '45.63.29.175'},
]


def all(server_filter=None):
    if server_filter is None:
        env.hosts = [x['ip'] for x in env.SERVERS]
    else:
        env.hosts = [x['ip'] for x in env.SERVERS if server_filter in x['name']]


def setup_site():
    local('python generate.py compress')
    put('output', '/var/www/html')
    sudo('cp -R /var/www/html/output/* /var/www/html/')
    sudo('rm -rf /var/www/html/output')
    sudo('cp ~/searchcode-server-community.tar.gz /var/www/html/')


# TODO should be a cron job
def renew_cert():
    sudo('service nginx stop')
    sudo('certbot renew')
    sudo('service nginx start')


def setup_installs():
    _setup_swapfile()
    packages = [
        'nginx',
        'htop',
        'iotop',
        'iftop',
        'screen',
        'nano',
        'openjdk-8-jre',
        'certbot',
    ]

    # https://certbot.eff.org/#ubuntuxenial-nginx
    sudo('add-apt-repository -y ppa:certbot/certbot')
    sudo('apt-get -y update')
    sudo('apt-get -y --force-yes install %s' % ' '.join(packages))
    sudo('certbot certonly --standalone -d searchcodeserver.com')

    put('config/nginx/nginx.frontend.conf', '/etc/nginx/sites-available/default')
    sudo('service nginx restart')


def _setup_swapfile(size=1):
    if fabric.contrib.files.exists('/swapfile') == False:
        sudo('''fallocate -l %sG /swapfile''' % (size))
        sudo('''chmod 600 /swapfile''')
        sudo('''mkswap /swapfile''')
        sudo('''swapon /swapfile''')
        sudo('''echo "/swapfile   none    swap    sw    0   0" >> /etc/fstab''')


def run_server():
    local('python generate.py && cd output && python -m SimpleHTTPServer')