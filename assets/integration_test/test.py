#!/usr/local/bin/python
# coding: utf-8

import unittest
import urllib2
import json
import sys
import re
import os
import random
import string

'''Variety of simple tests to check that nothing is obviously broken'''


class TestIntegration(unittest.TestCase):

    def getData(self, url):
        data = urllib2.urlopen(url)
        self.assertEqual(200, data.getcode())
        data = data.read()
        return data

    def getRandomLetters(self, count):
        return ''.join(random.choice(string.letters) for i in xrange(count))

    def testMainPage(self):
        data = self.getData("http://%s/" % (host))
        result = 'Searching across' in data or 'You have no repositories indexed' in data
        self.assertTrue(result)
        self.assertTrue('Repositories' in data)
        self.assertTrue('Documentation' in data)
        self.assertTrue('Admin' in data)

    def testDocumentationPage(self):
        data = self.getData("http://%s/documentation/" % (host))
        self.assertTrue('<h2>Documentation</h2>' in data)

    def testLoginPage(self):
        data = self.getData("http://%s/login/" % (host))
        self.assertTrue('Enter Password' in data)

    def testAdminRedirect(self):
        data = self.getData("http://%s/admin/" % (host))
        self.assertTrue('Enter Password' in data)

    def testAdminBulkRedirect(self):
        data = self.getData("http://%s/admin/bulk/" % (host))
        self.assertTrue('Enter Password' in data)

    def testAdminSettingsRedirect(self):
        data = self.getData("http://%s/admin/settings/" % (host))
        self.assertTrue('Enter Password' in data)

    def testJsonLoads(self):
        data = self.getData("http://%s/api/codesearch/?q=test&p=0" % (host))
        data = json.loads(data)
        self.assertTrue('totalHits' in data)

    def testSearchJsPreload(self):
        data = self.getData("http://%s/?q=test" % (host))
        self.assertTrue('var preload = {' in data)

    def testSearch(self):
        data = self.getData("http://%s/html/?q=test" % (host))
        self.assertTrue('Filter Results' in data)

    def testCodeResults(self):
        url = "http://%s/file/zeroclickinfo-fathead/lib/fathead/java/test_parse.py" % (
            host)
        data = self.getData(url)
        #self.assertTrue('MD5 Hash' in data)

    def testRepositoryList(self):
        url = "http://%s/repository/list/" % (host)
        data = self.getData(url)
        self.assertTrue(
            '<script src="/js/intercooler-1.1.2.min.js"></script>' in data)

    def testNoSearch(self):
        url = "http://%s/?q=&p=0" % (host)
        data = self.getData(url)
        result = 'Searching across' in data or 'You have no repositories indexed' in data
        self.assertTrue(result)

    def testNoSearchHtml(self):
        url = "http://%s/html/?q=&p=0" % (host)
        self.getData(url)

    def testNoSearchJson(self):
        url = "http://%s/api/codesearch/?q=&p=0" % (host)
        self.getData(url)

    def testSearchLoad(self):
        for x in xrange(1000):
            url = "http://%s/html/?q=%s" % (host, self.getRandomLetters(10))
            data = self.getData(url)
            self.assertTrue('No results found' in data)

    def test_index_suggest(self):
        for x in xrange(1000):
            url = "http://%s/api/repo/index/?repoUrl=http://test.com/" % (host)
            data = self.getData(url)
            self.assertTrue('Was unable to find repository' in data)

    def test_rss_search(self):
        url = "http://%s/api/codesearch/rss/?q=test&p=0" % (host)
        data = self.getData(url)
        self.assertTrue('title>Search for "test"</title>' in data)
        self.assertTrue(
            '<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">' in data)

    def testCheckResponseHeadersApi(self):
        urls = [
            'api/codesearch/?q=test',
            'api/timecodesearch/?q=test',
            'api/repo/list/',
            'api/repo/add/',
            'api/repo/delete/',
            'api/repo/reindex/',
            'api/repo/index/',
            'api/repo/repo/?reponame=searchcode',
        ]

        for url in urls:
            url = 'http://%s/%s' % (host, url)
            data = urllib2.urlopen(url)
            header = data.info().getheader('Content-Type')
            self.assertEqual(header, 'application/json', url)

    def testCheckResponse200(self):
        urls = [
            'api/repo/indextime/?reponame=searchcode',
            'api/repo/filecount/?reponame=searchcode',
            'api/repo/indextimeseconds/?reponame=searchcode',
        ]

        for url in urls:
            url = 'http://%s/%s' % (host, url)
            urllib2.urlopen(url)

    def testFuzzyBadData(self):
        self.getData("http://%s/html/?q=test&p=100" % (host))
        self.getData("http://%s/html/?q=test&p=a" % (host))
        self.getData("http://%s/html/?&p=a" % (host))
        self.getData("http://%s/html/?q=test&p=1asds" % (host))
        self.getData("http://%s/html/?q=test&p=1&repo=test&lan=test" % (host))

        for x in xrange(1000):
            url = "http://%s/html/?%s=%s&%s=%s" % (
                host,
                self.getRandomLetters(1),
                self.getRandomLetters(10),
                self.getRandomLetters(1),
                self.getRandomLetters(10))
            self.getData(url)

        for x in xrange(1000):
            self.getData("http://%s/html/?q=%s&repo=%s&lan=%s" % (
                host,
                self.getRandomLetters(10),
                self.getRandomLetters(10),
                self.getRandomLetters(10)))


if __name__ == "__main__":
    host = "localhost:8080"
    unittest.main()
