import fabric

from fabric.api import env
from fabric.api import run
from fabric.api import local
from fabric.api import sudo
from fabric.api import prompt
from fabric.utils import warn
from fabric.contrib.files import sed
from fabric.context_managers import settings, hide, cd
from fabric.colors import yellow

def setup_site():
    local('python generate.py compress')
