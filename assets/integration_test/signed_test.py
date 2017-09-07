from hashlib import sha1
from hmac import new as hmac
import urllib2
import json
import urllib

'''Simple usage of the signed key API endpoints.'''


publickey = "APIK-htEIV1L1GrOQhYRovctl2fkxdPf"
privatekey = "IBrB9m9bbxBaoK86D8XcS7jtaA52rcok"

for x in range(0, 10):
    reponame = "myrepo%s" % (x)
    repourl = "/Users/boyter/test2"
    repotype = "git"
    repousername = ""
    repopassword = ""
    reposource = ""
    repobranch = "master"

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

    message = "pub=%s&reponame=%s" % (
            urllib.quote_plus(publickey),
            urllib.quote_plus(reponame),
        )

    sig = hmac(privatekey, message, sha1).hexdigest()

    url = "http://localhost:8080/api/repo/add/?sig=%s&%s" % (urllib.quote_plus(sig), message)
    url = "http://localhost:8080/api/repo/delete/?sig=%s&%s" % (urllib.quote_plus(sig), message)

    data = urllib2.urlopen(url)
    data = data.read()

    data = json.loads(data)
    print x, data['sucessful'], data['message']
