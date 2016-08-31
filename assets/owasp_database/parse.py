from xml.dom import minidom
import re
import json

# XML is taken from OWASP Code Crawler which is taken from OWASP so must be under the https://creativecommons.org/licenses/by-sa/3.0/
# Creative Commons 3.0 License. To avoid any issues lets convert it to JSON and then ship with that
xmldoc = minidom.parse('owasp_database.xml')
itemlist = xmldoc.getElementsByTagName('KeyPointer')

results = []

for s in itemlist:
    name = s.getElementsByTagName('k_name')[0].firstChild.nodeValue
    desc = s.getElementsByTagName('k_description')[0].firstChild.nodeValue.strip()
    # Ugly hacks ahoy!
    vunl = ','.join(set([x.strip() for x in re.sub('<.*?>', '', s.getElementsByTagName('Stride')[0].toprettyxml()).split('\n') if x.strip() != '']))

    results.append({ 'name': name, 'desc': desc, 'type': vunl})
print json.dumps(results)