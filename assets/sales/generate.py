# A dead simple static site generator because I didn't like any others

import os
import sys
from shutil import copyfile, copytree, rmtree
from subprocess import call

# Target output
target = './output/'

# Generic header/footer to be applied to every template
header = './generic/header.html'
footer = './generic/footer.html'

config = [{
        'file': 'index.html',
        'title': 'searchcode server | Development Productivity through Powerful Code Search'
    }, {
        'file': 'features.html',
        'title': 'Productivity features of searchcode server'
    }, {
        'file': 'pricing.html',
        'title': 'Community Download and Pricing for searchcode server'
    }, {
        'file': 'contact.html',
        'title': 'Sales contact for searchcode server'
    }, {
        'file': 'developer-productivity-tools.html',
        'title': 'Tools that Drive Developer Productivity'
    }, {
        'file': 'version.json',
        'title': ''
    }, {
        'directory': './knowledge-base/',
        'footer': './generic/kb_footer.html',
        'title': 'Knowledge Base Article'
    }, {
        'file': './knowledge-base/index.html',
        'title': 'The searchcode server knowledge base'
    },
]

assets = [
    'css',
    'fonts',
    'images',
    'js',
    'favicon.ico'
]

#####################################
# Don't configure things below here #
#####################################

if not os.path.exists(target):
    os.makedirs(target)

for x in os.listdir(target):
    if os.path.isfile(target + x):
        os.remove(target + x)
    else:
        rmtree(target + x)

header_template = ''
footer_template = ''

with open(header, 'r') as file:
    header_template = file.read()
with open(footer, 'r') as file:
    footer_template = file.read()

for conf in config:

    merged_header_template = header_template
    if conf['title']:
        merged_header_template = header_template.replace('{{TITLE}}', conf['title'])

    pre_header = ''
    pre_footer = ''

    if 'header' in conf:
        with open(conf['header'], 'r') as myfile:
            pre_header = myfile.read()
    if 'footer' in conf:
        with open(conf['footer'], 'r') as myfile:
            pre_footer = myfile.read()

    if 'file' in conf:
        with open(conf['file'], 'r') as myfile:
            data = myfile.read()

        if 'target' in conf:
            pass
        else:
            with open(target + conf['file'].replace('./', ''), 'w') as myfile:
                if '.json' not in conf['file']:
                    myfile.write(merged_header_template + pre_header + data + pre_footer + footer_template)
                else:
                    myfile.write(data)

    if 'directory' in conf:
        if not os.path.exists(target + conf['directory']):
            os.makedirs(target + conf['directory'])

        pre_header = ''
        pre_footer = ''

        if 'header' in conf:
            with open(conf['header'], 'r') as myfile:
                pre_header = myfile.read()
        if 'footer' in conf:
            with open(conf['footer'], 'r') as myfile:
                pre_footer = myfile.read()

        for file in os.listdir(conf['directory']):

            title = file.split('.')[0].replace('-', ' ').title()
            merged_header_template = header_template.replace('{{TITLE}}', title)

            with open(conf['directory'] + file, 'r') as myfile:
                data = myfile.read()

            with open(target + conf['directory'].replace('./', '') + file, 'w') as myfile:
                myfile.write(merged_header_template + pre_header + data + pre_footer + footer_template)

for asset in assets:
    if os.path.isdir(asset):
        copytree(asset, target + asset)
    else:
        copyfile(asset, target + asset)

# Compress images etc...
if len(sys.argv[1:]) != 0:
    for root, dirs, files in os.walk(target):
        call(['./png_crush.sh', root])
        call(['./gif_crush.sh', root])
