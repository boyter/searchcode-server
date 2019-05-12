from hashlib import sha1
from hmac import new as hmac
import urllib2
import urllib
import sqlite3
import json

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


reponame = ' test'
repourl = ' test'
repotype = "git"
repousername = ''
repopassword = ''
reposource = ''
repobranch = ''

# NB using quote not quote plus
message = "pub=%s&reponame=%s&repourl=%s&repotype=%s&repousername=%s&repopassword=%s&reposource=%s&repobranch=%s" % (
        urllib.quote(publickey),
        urllib.quote(reponame),
        urllib.quote(repourl),
        urllib.quote(repotype),
        urllib.quote(repousername),
        urllib.quote(repopassword),
        urllib.quote(reposource),
        urllib.quote(repobranch)
    )

sig = hmac(privatekey, message, sha1).hexdigest()

url = "http://localhost:8080/api/repo/add/?sig=%s&%s" % (urllib.quote_plus(sig), message)
data = urllib2.urlopen(url)

code = data.getcode()
data = data.read()

parsed = json.loads(data)

assert parsed['message'] == 'added repository successfully', parsed['message']


# cursor.execute("DELETE FROM api WHERE publickey = '%s'" % (publickey))
# cursor.execute("DELETE FROM repo")
# db.commit()
