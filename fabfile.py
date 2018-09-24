'''
/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */
'''

import fabric

from fabric.api import env
from fabric.api import run
from fabric.api import local
from fabric.api import sudo
from fabric.api import prompt
from fabric.utils import warn
from fabric.contrib.files import sed
from fabric.context_managers import settings, hide, cd, lcd
from fabric.colors import yellow

import platform
import os
from os import path
import re
import hashlib
import sys
import datetime

VERSION = "1.3.15"


def setup_npm():
    # Required to run Qunit via command line
    # http://stackoverflow.com/questions/24704043/how-to-run-qunit-tests-from-command-line
    local('npm install -g node-qunit-phantomjs')


def run_proxy():
    local('python ./assets/javascript_proxy/webserver.py')


def package():
    local('rm -rf ./target/*')
    local("mvn package")


def test_full():
    test()
    test_integration()


def test():
    local('mvn test')
    js_test()


def test_docker():
    local('''docker build -t searchcode-server-test -f ./assets/docker/Dockerfile.test .''')
    local('''docker run --rm \
            -v ~/.m2/:/root/.m2/ \
            -v $(PWD):/opt/app/ \
            -w /opt/app/ \
            searchcode-server-test mvn test''')


def test_docker_e2e():
    local('mkdir -p e2e_test')
    local('mkdir -p ./e2e_test/git_example/')
    local('mkdir -p ./e2e_test/svn_example/')
    local('mkdir -p ./e2e_test/file_example/')

    with lcd('./e2e_test/git_example/'):
        local('echo "indextest git_example" > "git_example.py"')

    with lcd('./e2e_test/svn_example/'):
        local('echo "indextest svn_example" > "svn_example.py"')

    with lcd('./e2e_test/file_example/'):
        local('echo "indextest file_example" > "file_example.py"')

    local('''docker build -t searchcode-server-test -f ./assets/docker/Dockerfile.test.e2e .''')
    local('''docker run --rm \
            -v ~/.m2/:/root/.m2/ \
            -v $(PWD):/opt/app/ \
            -w /opt/app/ \
            searchcode-server-test mvn test''')


def js_test():
    local('node-qunit-phantomjs ./src/test/javascript/index.html')


def test_integration():
    print(yellow('Be sure to run: "fab compile_js configure_prod run" first'))
    # js_test()
    local("python ./assets/integration_test/test.py")
    local("python ./assets/integration_test/fuzztest.py")
    local("python ./assets/integration_test/signed_testing.py")
    local("python ./assets/integration_test/signed_testing_sha512.py")
    local("python ./assets/integration_test/signed_testing_optional.py")
    local("python ./assets/integration_test/signed_fuzz.py")
    local("python ./assets/integration_test/quote_plus.py")


def stress_test():
    local('python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/signed_testing.py')


def run():
    package()
    local("java -jar ./target/searchcode-%s.jar" % (VERSION))


def pushtagbranch():
    local('git push --tags')


def build_all_release():
    #js_test()
    build_release()
    build_community_release()


def build_release():
    _check_os
    replacements = {
        'public static final boolean ISCOMMUNITY = true;': 'public static final boolean ISCOMMUNITY = false;'
    }
    _python_sed(
        fileloc='./src/main/java/com/searchcode/app/App.java', replacements=replacements)
    local('rm -rf ./repo/')
    local('rm -rf ./index/')
    compile_js()
    configure_prod()
    package()
    _build_package()
    if not os.path.exists('./searchcode-server/'):
        os.makedirs('./searchcode-server/')
    local('rm -rf ./searchcode-server/*')
    local('mv ./release ./searchcode-server')
    local('cp -R include ./searchcode-server/release/')
    local('tar cvf searchcode-server.tar searchcode-server && gzip --no-name -f searchcode-server.tar')
    local('md5sum searchcode-server.tar.gz')
    local('sha1sum searchcode-server.tar.gz')


def build_community_release():
    _check_os
    # modify community flag in application
    replacements = {
        'public static final boolean ISCOMMUNITY = false;': 'public static final boolean ISCOMMUNITY = true;'
    }
    _python_sed(
        fileloc='./src/main/java/com/searchcode/app/App.java', replacements=replacements)
    compile_js()
    configure_prod()
    package()
    _build_package()
    if not os.path.exists('./searchcode-server-community/'):
        os.makedirs('./searchcode-server-community/')
    local('rm -rf ./searchcode-server-community/*')
    local('mv ./release ./searchcode-server-community')
    local('cp -R include ./searchcode-server-community/release/')
    local('tar cvf searchcode-server-community.tar searchcode-server-community && gzip --no-name -f searchcode-server-community.tar')
    local('md5sum searchcode-server-community.tar.gz')
    local('sha1sum searchcode-server-community.tar.gz')


def compile_js():
    files = [
        '',
        'underscore-min.js',
        'cache.js',
        'chart.js',
        'mithril.min.js',
        'script.js'
    ]

    tomin = ' ./src/main/resources/public/js/'.join(files)
    local(
        'java -jar ./assets/js_compiler/compiler.jar  --js_output_file=./src/main/resources/public/js/script.min.js --js ' + tomin)


def configure_local():
    replacements = {
        '<script src="/js/script.min.js"></script>':
            '<script src="/js/mithril.min.js"></script><script src="/js/underscore-min.js"></script><script src="/js/chart.js"></script><script src="/js/cache.js"></script><script src="/js/script.js"></script>',
        'public static final boolean ISCOMMUNITY = false;': 'public static final boolean ISCOMMUNITY = true;',
    }
    _python_sed(
        fileloc='./src/main/resources/spark/template/freemarker/search_ajax.ftl', replacements=replacements)


def configure_prod():
    replacements = {
        '<script src="/js/mithril.min.js"></script><script src="/js/underscore-min.js"></script><script src="/js/chart.js"></script><script src="/js/cache.js"></script><script src="/js/script.js"></script>':
            '<script src="/js/script.min.js"></script>'
    }
    _python_sed(
        fileloc='./src/main/resources/spark/template/freemarker/search_ajax.ftl', replacements=replacements)


def _check_os():
    if 'windows' in platform.system().lower():
        fabric.operations.prompt('Warning. Creating a build through Windows is not currently supported. Please any key to contine.', key=None, default='', validate=None)


def _build_package():
    if not os.path.exists('./release/'):
        os.makedirs('./release/')
    local('rm -rf ./release/*')

    local('cp -R ./target/dependency-jars ./release/')
    local('cp ./target/*.jar ./release/')
    local('cp ./searchcode.properties.example ./release/searchcode.properties')
    local('cp ./quartz.properties ./release/quartz.properties')
    local('cp ./searchcode.sqlite.empty ./release/searchcode.sqlite')
    local('cp ./README.md ./release/')
    local('chmod +x ./searchcode-server.sh')
    local('cp ./searchcode-server.sh ./release/')
    local('cp ./searchcode-server.bat ./release/')
    local('mkdir ./release/logs')


def _python_sed(fileloc, replacements):
    import fileinput
    for search, replace in replacements.iteritems():
        for line in fileinput.input(fileloc, inplace=True):
            print line.replace(search, replace).rstrip()
