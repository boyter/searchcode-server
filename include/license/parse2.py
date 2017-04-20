import os
import json
import re


def clean_text(text):
    text = text.lower()
    text = ' '.join(text.split())
    return text


def find_ngrams(input_list, n):
    return zip(*[input_list[i:] for i in range(n)])


def load_database():
    with open('database.json', 'r') as file:
        database = file.read()

    licenses = json.loads(database)

    for license in licenses:
        license['clean'] = clean_text(license['text'])
        ngrams = []

        start = 7
        end = 8

        if license['shortname'] in ['Artistic-1.0', 'BSD-3-Clause']:
            start = 2
            end = 35

        for x in range(start, end):
            ngrams = ngrams + find_ngrams(license['clean'].split(), x)
        license['ngrams'] = ngrams

    return licenses

if __name__ == '__main__':
    licenses = load_database()

    for license in licenses:
        matches = []

        for ngram in license['ngrams']:
            find = ' '.join(ngram)
            ismatch = True

            filtered = [x for x in licenses if x['shortname'] != license['shortname'] and x['shortname'] == 'NPL-1.1']
            for lic in filtered:
                if find in lic['clean']:
                    ismatch = False
                    break

            if ismatch:
                matches.append(find)

        if len(matches) == 0:
            print '>>>>', license['shortname'], len(matches)
        else:
            print license['shortname'], len(matches)

        license['keywords'] = matches

    licenses = [{
        'text': x['text'],
        'fullname': x['fullname'],
        'shortname': x['shortname'],
        'header': x['header'],
        'keywords': x['keywords'][:50]
    } for x in licenses]

    with open('database_keywords.json', 'w') as myfile:
        myfile.write(json.dumps(licenses))
