'''
/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.8
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
from fabric.context_managers import settings, hide, cd
from fabric.colors import yellow


import os
from os import path
import re
import hashlib
import sys
import datetime

VERSION = "1.3.8"


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


def js_test():
    local('node-qunit-phantomjs ./src/test/javascript/index.html')


def test_integration():
    print(yellow('Be sure to run: "fab compile_js configure_prod run" first'))
    local('mvn integration-test')
    js_test()
    local("python ./assets/integration_test/test.py")
    local("python ./assets/integration_test/fuzztest.py")
    local("python ./assets/integration_test/signed_testing.py")
    local("python ./assets/integration_test/signed_testing_sha512.py")
    local("python ./assets/integration_test/signed_fuzz.py")

def stress_test():
    local('python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/test.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/fuzztest.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/signed_testing.py & python ./assets/integration_test/signed_testing.py')


def run():
    package()
    local("java -jar ./target/searchcode-%s.jar" % (VERSION))


def pushtagbranch():
    local('git push --tags')


def build_all_release():
    js_test()
    build_release()
    build_community_release()


def build_release():
    replacements = {
        'public static final boolean ISCOMMUNITY = true;': 'public static final boolean ISCOMMUNITY = false;'
    }
    _python_sed(
        fileloc='./src/main/java/com/searchcode/app/App.java', replacements=replacements)
    compile_js()
    configure_prod()
    package()
    _build_package()
    if not os.path.exists('./searchcode-server/'):
        os.makedirs('./searchcode-server/')
    local('rm -rf ./searchcode-server/*')
    local('mv ./release ./searchcode-server')
    local('cp -R include ./searchcode-server/release/')
    local('tar cvzf searchcode-server.tar.gz searchcode-server')


def build_community_release():
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
    local(
        'tar cvzf searchcode-server-community.tar.gz searchcode-server-community')


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
            '<script src="/js/mithril.min.js"></script><script src="/js/underscore-min.js"></script><script src="/js/chart.js"></script><script src="/js/cache.js"></script><script src="/js/script.js"></script>'

    }
    _python_sed(
        fileloc='./src/main/resources/spark/template/freemarker/search_test.ftl', replacements=replacements)


def configure_prod():
    replacements = {
        '<script src="/js/mithril.min.js"></script><script src="/js/underscore-min.js"></script><script src="/js/chart.js"></script><script src="/js/cache.js"></script><script src="/js/script.js"></script>':
            '<script src="/js/script.min.js"></script>'
    }
    _python_sed(
        fileloc='./src/main/resources/spark/template/freemarker/search_test.ftl', replacements=replacements)


def _build_package():
    if not os.path.exists('./release/'):
        os.makedirs('./release/')
    local('rm -rf ./release/*')

    local('cp -R ./target/dependency-jars ./release/')
    local('cp ./target/*.jar ./release/')
    local('cp ./searchcode.properties.example ./release/searchcode.properties')
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
