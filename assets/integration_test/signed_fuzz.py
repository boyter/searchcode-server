from hashlib import sha1
from hmac import new as hmac
import urllib2
import json
import urllib
import pprint
import sqlite3

'''Simple usage of the signed key API endpoints.'''
publickey = "APIK-K1KBVOlbTDElXVMa86sn5zHnsyF"
privatekey = "RmpGihBgvJx9pT2cYk7Q0RbeFeSKlQ8f"

# Connect to mysql and add our keys
# http://pythoncentral.io/introduction-to-sqlite-in-python/
# db = sqlite3.connect('searchcode.sqlite')
# cursor = db.cursor()
# cursor.execute("DELETE FROM api WHERE publickey = '%s'" % (publickey))
# db.commit()
# cursor.execute('INSERT INTO api (publickey,privatekey,lastused,data) VALUES (?,?,?,?)', (publickey, privatekey, '', ''))
# db.commit()

blns = None
try:
    blns = open('./assets/blns/blns.txt')
except:
    blns = open('../blns/blns.txt')

for line in blns:
    reponame = line
    repourl = line
    repotype = "git"
    repousername = line
    repopassword = line
    reposource = line
    repobranch = line

    message = "pub=%s&reponame=%s&repourl=%s&repotype=%s&repousername=%s&repopassword=%s&reposource=%s&repobranch=%s" % (
            urllib.quote_plus(publickey),
            urllib.quote_plus(reponame),
            urllib.quote_plus(repourl),
            urllib.quote_plus(repotype),
            urllib.quote_plus(repousername),
            urllib.quote_plus(repopassword),
            urllib.quote_plus(reposource),
            urllib.quote_plus(repobranch)
        )

    sig = hmac(privatekey, message, sha1).hexdigest()

    url = "http://localhost:8080/api/repo/add/?sig=%s&%s" % (urllib.quote_plus(sig), message)

    if len(url) < 2000:
        data = urllib2.urlopen(url)
        assert 200 == data.getcode()

    message = "pub=%s&reponame=%s" % (
            urllib.quote_plus(publickey),
            urllib.quote_plus(reponame),
        )

    sig = hmac(privatekey, message, sha1).hexdigest()
    url = "http://localhost:8080/api/repo/delete/?sig=%s&%s" % (urllib.quote_plus(sig), message)

    if len(url) < 2000:
        data = urllib2.urlopen(url)
        assert 200 == data.getcode()

# cursor.execute("DELETE FROM api WHERE publickey = '%s'" % (publickey))
# cursor.execute("DELETE FROM repo")
# db.commit()
