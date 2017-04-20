import os
import json
import re


def clean_text(text):
    text = text.lower()
    text = ' '.join(text.split())
    return text


def find_ngrams(input_list, n):
    return zip(*[input_list[i:] for i in range(n)])

with open('database.json', 'r') as file:
    database = file.read()

licenses = json.loads(database)

for license in licenses:
    license['clean'] = clean_text(license['text'])

for license in licenses:

    matches = []

    for x in range(5, 11):
        ngrams = find_ngrams(license['clean'].split(), x)

        for ngram in ngrams:
            find = ' '.join(ngram)
            ismatch = True

            for lic in licenses:
                if license['shortname'] != lic['shortname']:
                    if find in lic['clean']:
                        ismatch = False

            if ismatch:
                matches.append(find)

    print license['shortname'], len(matches)
    license['keywords'] = matches

print json.dumps(licenses)
