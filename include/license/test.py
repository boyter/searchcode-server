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
    lis = license['text'].lower()
    hea = license['header'].lower()
    license['concordance'] = v.concordance(lis),
    license['headerconcordance'] = v.concordance(hea)


project_directory = '/Users/boyter/Documents/Projects/goap/'

# Check the base for a LICENCE file or README which contains one
directory_list = os.listdir(project_directory)
possible_files = [project_directory + x for x in directory_list if 'license' in x.lower() or 'copying' in x.lower()]

if len(possible_files) == 0:
    possible_files = [project_directory + x for x in directory_list if 'readme' in x.lower()]

# This works pretty well for license files not so well for headers
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

print 'Project License'
for i in matches[:3]:
    print i[0], i[1]['fullname']

# Inspect files for license
for root, dirs, files in os.walk(project_directory):
    for file in [root + '/' + x for x in files if '.js' in x]:
        matches = []
        with open(file, 'r') as myfile:
            content = myfile.read()
        display = False
        for license in [x for x in licenses if len(x['header']) != 0]:
            length = int(len(license['header']) * 1)
            con = v.concordance(content[:length].lower())

            relation = v.relation(con, license['headerconcordance'])
            if relation != 0:
                display = True
            matches.append((relation, license))

        for license in [x for x in licenses if len(x['header']) == 0]:
            length = int(len(license['text']) * 1)
            con = v.concordance(content[:length].lower())

            relation = v.relation(con, license['concordance'][0])
            if relation != 0:
                display = True
            matches.append((relation, license))

        matches.sort(reverse=True)
        matches = [x for x in matches if x[0] > 0.9]

        if len(matches) != 0:
            for i in matches:
                print i[0], i[1]['fullname'], file
