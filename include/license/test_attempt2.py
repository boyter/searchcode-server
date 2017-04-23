#!/usr/local/bin/python
# -*- coding: utf-8 -*-

import unittest
import attempt2


class TestIntegration(unittest.TestCase):

    def testBsdClear(self):
        license = attempt2.read_clean_file('./samples/bsd-clear')
        licenses = attempt2.load_database()
        result = attempt2.guess_license(license, licenses)
        self.assertEqual('BSD-3-Clause-Clear', result)

    def testFairSource(self):
        license = attempt2.read_clean_file('./samples/fair-source')
        licenses = attempt2.load_database()
        result = attempt2.guess_license(license, licenses)
        self.assertEqual('Fair-Source-0.9', result)

if __name__ == "__main__":
    unittest.main()
