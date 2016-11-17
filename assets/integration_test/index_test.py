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
gitdir = '/tmp/gittest/'
filedir = '/tmp/filetest/'


def getData(url):
    data = urllib2.urlopen(url)
    data = data.read()
    return data

def dosearch(text):
    url = "http://%s/api/codesearch/?q=%s" % (host, text)
    data = getData(url)
    return json.loads(data)




# if not os.path.exists(directory):
#     os.makedirs(directory)

# git

# create 100 files
# add to git
# git add .
# git commit -m "add"

# # files

# create 100 files
run = True
while(run):
    for x in range(1, 101):
        with open('%sfile%s.php' % (gitdir, x), 'w') as f:
            f.write('indextest gittest gittest%s' % (x))

        os.system('git add . && git commit -m "add"')

        with open('%sfile%s.php' % (filedir, x), 'w') as f:
            f.write('indextest filetest filetest%s' % (x))

    result = dosearch('gittest')
    print 'gittest %s' % (result['totalHits'])

    result = dosearch('filetest')
    print 'filetest %s' % (result['totalHits'])
    
    #time.sleep(180)

    for x in range(1, 101):

        os.system('git rm %sfile%s.php && git commit -m "add"' % (gitdir, x))

        with open('%sfile%s.php' % (filedir, x), 'w') as f:
            f.write('indextest filetest filetest%s' % (x))

    run = False
















