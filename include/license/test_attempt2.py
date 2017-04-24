#!/usr/local/bin/python
# -*- coding: utf-8 -*-

import unittest
import attempt2


class TestIntegration(unittest.TestCase):

    def testBsdClear(self):
        license = attempt2.read_clean_file('./samples/bsd-clear')
        licenses = attempt2.load_database()
        result = attempt2.guess_license(license, licenses)
        self.assertEqual('BSD-3-Clause-Clear', result[0][1]['shortname'])

    def testFairSource(self):
        license = attempt2.read_clean_file('./samples/fair-source')
        licenses = attempt2.load_database()
        result = attempt2.guess_license(license, licenses)
        self.assertEqual('Fair-Source-0.9', result[0][1]['shortname'])

    def testGPLV2(self):
        license = attempt2.read_clean_file('./samples/gpl-v2')
        licenses = attempt2.load_database()
        result = attempt2.guess_license(license, licenses)
        self.assertEqual('GPL-2.0', result[0][1]['shortname'])

if __name__ == "__main__":
    unittest.main()
