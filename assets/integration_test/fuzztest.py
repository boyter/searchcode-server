#!/usr/local/bin/python
# coding: utf-8

import unittest
import urllib2
import urllib
import json
import sys
import re
import os
import random
import string

'''Throws all sorts of queries at the HTML endpoint to see if we get any non 200
responses as well as a few other checks. Useful for shaking out performance issues as
well as identifying anything that has suddenly broken.'''
class TestIntegration(unittest.TestCase):
    def getData(self, url):
        data = urllib2.urlopen(url)
        self.assertEqual(200, data.getcode())
        data = data.read()
        return data

    def getRandomCharacters(self, count):
        return ''.join(random.choice(string.printable) for i in xrange(count))

    def getRandomLetters(self, count):
        return ''.join(random.choice(string.letters) for i in xrange(count))


    def testFuzzSearchLetters(self):
        for x in xrange(1000):
            one = urllib.urlencode({'q': self.getRandomLetters(10)})
            two = urllib.urlencode({'repo': self.getRandomLetters(10)})
            
            url = "http://%s/html/?%s&%s" % (host, one, two)
            data = self.getData(url)
            self.assertTrue('No results found for' in data)

    def testFuzzSearchLettersTilde(self):
        for x in xrange(1000):
            one = urllib.urlencode({'q': self.getRandomLetters(10)})
            two = urllib.urlencode({'repo': '~' + self.getRandomLetters(10)})
            
            url = "http://%s/html/?%s&%s" % (host, one, two)
            data = self.getData(url)
            self.assertTrue('No results found for' in data)

    def testFuzzSearchLettersTildeSlash(self):
        for x in xrange(1000):
            one = urllib.urlencode({'q': self.getRandomLetters(10)})
            two = urllib.urlencode({'repo': '~' + self.getRandomLetters(10) + '/' + self.getRandomLetters(10) })
            
            url = "http://%s/html/?%s&%s" % (host, one, two)
            data = self.getData(url)
            self.assertTrue('No results found for' in data)

    def testFuzzSearchCharacters(self):
        for x in xrange(1000):
            one = urllib.urlencode({'q': self.getRandomCharacters(10)})
            two = urllib.urlencode({'repo': self.getRandomCharacters(10)})
            
            url = "http://%s/html/?%s&%s" % (host, one, two)
            data = self.getData(url)
            self.assertTrue('No results found for' in data)

    def testFuzzSearchCharactersTilde(self):
        for x in xrange(1000):
            one = urllib.urlencode({'q': self.getRandomCharacters(10)})
            two = urllib.urlencode({'repo': '~' + self.getRandomCharacters(10)})
            
            url = "http://%s/html/?%s&%s" % (host, one, two)
            data = self.getData(url)
            self.assertTrue('No results found for' in data)

    def testFuzzSearchCharactersTildeSlash(self):
        for x in xrange(1000):
            one = urllib.urlencode({'q': self.getRandomCharacters(10)})
            two = urllib.urlencode({'repo': '~' + self.getRandomCharacters(10)  + '/' + self.getRandomLetters(10) })
            
            url = "http://%s/html/?%s&%s" % (host, one, two)
            data = self.getData(url)
            self.assertTrue('No results found for' in data)

    def testBigListNaughtyStrings(self):
        blns = None
        try:
            blns = open('./assets/blns/blns.txt')
        except:
            blns = open('../blns/blns.txt')

        for line in blns:
            one = urllib.urlencode({'q': line})
            two = urllib.urlencode({'repo': line})
            
            url = "http://%s/html/?%s&%s" % (host, one, two)
            data = self.getData(url)
            self.assertTrue('No results found for' in data)


if __name__ == "__main__":
    host = "localhost:8080"
    unittest.main()