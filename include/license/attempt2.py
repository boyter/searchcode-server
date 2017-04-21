#!/usr/local/bin/python
# -*- coding: utf-8 -*-

import os
import json
import math
import codecs


def load_database():
    with open('database_keywords.json', 'r') as file:
        database = file.read()

    licenses = json.loads(database)

    for license in licenses:
        license['clean'] = clean_text(license['text'])

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


if __name__ == '__main__':
    licenses = load_database()

    project_directory = '/Users/boyter/Documents/Projects/armory-react/'
    possible_files = find_possible_licence_files(project_directory)

    for possible_file in possible_files:
        with codecs.open(possible_files[0], 'r', 'utf-8') as file:
            potential_license = clean_text(file.read())

        print potential_license

        for license in licenses:
            keywordmatch = 0
            for keyword in license['keywords']:
                if keyword in potential_license:
                    keywordmatch = keywordmatch + 1

            if len(license['keywords']):
                if (float(keywordmatch) / float(len(license['keywords'])) * 100) >= 70:
                    print possible_file, license['shortname']


    # for root, dirs, files in os.walk(project_directory):
    #     for file in [root + '/' + x for x in files if '.js' in x]:
    #         matches = {}
    #         with codecs.open(file, 'r', 'utf-8') as myfile:
    #             content = myfile.read()

    #         content = clean_text(content)

    #         for license in licenses:
    #             keywordmatch = 0
    #             for keyword in license['keywords']:
    #                 if keyword in content:
    #                     keywordmatch = keywordmatch + 1

    #             if (float(keywordmatch) / float(len(license['keywords'])) * 100) >= 70:
    #                 matches[license['shortname']] = license

    #         if len(matches) != 0:
    #             for i in matches.keys():
    #                 print i, file
