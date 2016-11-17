#!/usr/local/bin/python
# coding: utf-8

import urllib2
import urllib
import json
import sys
import re
import os
import random
import string
import time

'''Used to shake out any issues with indexing filepaths or git repositories'''

host = "localhost:8080"
gitdir = '/tmp/gittest/' # Needs to have git init run in it
filedir = '/tmp/filetest/'


def getData(url):
    data = urllib2.urlopen(url)
    data = data.read()
    return data

def dosearch(text):
    url = "http://%s/api/codesearch/?q=%s" % (host, text)
    data = getData(url)
    return json.loads(data)

run = True
while(run):
    for x in range(1, 101):
        with open('%sfile%s.php' % (gitdir, x), 'w') as f:
            f.write('indextest gittest gittest%s' % (x))

        with open('%sfile%s.py' % (filedir, x), 'w') as f:
            f.write('indextest filetest filetest%s' % (x))

    os.system('cd %s && git add . && git commit -m "add"' % (gitdir))

    result = dosearch('gittest')
    print 'gittest %s' % (result['totalHits'])
    result = dosearch('filetest')
    print 'filetest %s' % (result['totalHits'])
    
    time.sleep(180)

    for x in range(1, 101):
        os.system('cd %s && git rm file%s.php && git commit -m "remove"' % (gitdir, x))
        os.system('cd %s && rm file%s.py' % (filedir, x))

    time.sleep(180)

    result = dosearch('gittest')
    print 'gittest %s' % (result['totalHits'])
    result = dosearch('filetest')
    print 'filetest %s' % (result['totalHits'])
















