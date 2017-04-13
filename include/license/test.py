import os
import json
import math


class VectorCompare:
    def magnitude(self, concordance):
        if type(concordance) != dict:
            raise ValueError('Supplied Argument should be of type dict')
        total = 0
        for word, count in concordance.iteritems():
            total += count ** 2
        return math.sqrt(total)

    def relation(self, concordance1, concordance2):
        if type(concordance1) != dict:
            raise ValueError('Supplied Argument 1 should be of type dict')
        if type(concordance2) != dict:
            raise ValueError('Supplied Argument 2 should be of type dict')
        relevance = 0
        topvalue = 0
        for word, count in concordance1.iteritems():
            if word in concordance2:
                topvalue += count * concordance2[word]

        if (self.magnitude(concordance1) * self.magnitude(concordance2)) != 0:
            return topvalue / (self.magnitude(concordance1) * self.magnitude(concordance2))
        else:
            return 0

    def concordance(self, document):
        con = {}
        for word in document.split(' '):
            if word in con:
                con[word] = con[word] + 1
            else:
                con[word] = 1
        return con

with open('database.json', 'r') as file:
    database = file.read()

licenses = json.loads(database)

v = VectorCompare()

for license in licenses:
    value = license['text'].lower()
    license['concordance'] = v.concordance(value),

# Check the base for a LICENCE file or README which contains one
directory_list = os.listdir('../../')
possible_files = ['../../' + x for x in directory_list if 'license' in x.lower()]

for possible_file in possible_files:
    with open(possible_files[0]) as file:
        potential_license = file.read()

        con = v.concordance(potential_license.lower())

        matches = []

        for license in licenses:
            relation = v.relation(con, license['concordance'][0])
            if relation != 0:
                matches.append((relation, license))

matches.sort(reverse=True)

for i in matches:
    print i[0], i[1]['fullname']


# for root, dirs, files in os.walk('../../'):
#     for file in [root + '/' + x for x in files]:
#         print file
