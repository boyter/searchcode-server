#!/usr/local/bin/python
# -*- coding: utf-8 -*-

import os
import json
import math
import codecs


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


def load_database():
    with codecs.open('database_keywords.json', 'r', 'utf-8') as file:
        database = file.read()

    licenses = json.loads(database)
    vector_compare = VectorCompare()

    for license in licenses:
        license['clean'] = clean_text(license['text'])
        license['concordance'] = vector_compare.concordance(license['clean'])

    return licenses


def find_possible_licence_files(project_directory):
    # Check the base for a LICENCE file or README which contains one
    directory_list = os.listdir(project_directory)
    possible_files = [project_directory + x for x in directory_list if 'license' in x.lower() or 'copying' in x.lower()]

    if len(possible_files) == 0:
        possible_files = [project_directory + x for x in directory_list if 'readme' in x.lower()]

    return possible_files


def clean_text(text):
    text = text.lower()
    text = ' '.join(text.split())
    return text


def _keyword_guess(check_license, licenses):
    matching = []

    for license in licenses:
        keywordmatch = 0
        for keyword in license['keywords']:
            if keyword in check_license:
                keywordmatch = keywordmatch + 1

        if len(license['keywords']):
            if keywordmatch >= 1:
                matching.append({
                    'shortname': license['shortname'],
                    'percentage': (float(keywordmatch) / float(len(license['keywords'])) * 100)
                })

    return matching

def guess_license(check_license, licenses):
    matching = _keyword_guess(check_license, licenses)

    matches = []
    vector_compare = VectorCompare()
    for match in matching:
        for license in [x for x in licenses if x['shortname'] in [y['shortname'] for y in matching]]:
            licence_concordance = vector_compare.concordance(license['clean'])

            check_license_concordance = vector_compare.concordance(check_license[:len(license['clean'])])

            relation = vector_compare.relation(license['concordance'], check_license_concordance)

            if relation >= 0.85:
                matches.append((relation, license))

    matches.sort(reverse=True)

    return matches


def read_clean_file(filename):
    with codecs.open(filename, 'r', 'utf-8') as file:
        contents = clean_text(file.read())
    return contents

if __name__ == '__main__':
    licenses = load_database()

    project_directory = '/Users/boyter/Documents/Projects/searchcode-server/'
    possible_files = find_possible_licence_files(project_directory)

    for possible_file in possible_files:
        potential_license = read_clean_file(possible_files[0])
        guess = guess_license(potential_license, licenses)
        print guess[0][0], guess[0][1]['shortname'], possible_file

    for root, dirs, files in os.walk(project_directory):
        for file in [root + '/' + x for x in files if x.endswith('.js') or x.endswith('.java') or x.endswith('.py')]:
            content = None
            try:
                content = read_clean_file(file)
            except:
                # We have issues reading binary files, just swallow the error
                pass

            if content:
                matches = {}
                guess = guess_license(content, licenses), file

                if len(guess[0]) != 0:
                    print guess[0][0][0], guess[0][0][1]['shortname'], file
