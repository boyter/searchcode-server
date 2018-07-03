import json

with open('1.json') as myfile:
    one = json.loads(myfile.read())

with open('2.json') as myfile:
    two = json.loads(myfile.read())


inone = []
intwo = []

for key, value in one.iteritems():
    inone.append(key.lower())

for x in two:
    intwo.append(x['language'].lower())


for x in intwo:
    if x not in inone:
        print x, 'missing'
