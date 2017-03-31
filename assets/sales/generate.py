# A dead simple static site generater becaues I didn't like any others

import os
from shutil import copyfile, copytree

# Target output
target = './output/'

# Genetic header/footer to be applied to every template
header = './generic/header.html'
footer = './generic/footer.html'

config = [{
        'file': 'index.html'
    }, {
        'directory': './knowledge-base/'
    }
]

assets = [
    'css',
    'fonts',
    'images',
    'js',
    'favicon.ico',
    'style.css'
]

#####################################
# Don't configure things below here #
#####################################

if not os.path.exists(target):
    os.makedirs(target)

header_template = ''
footer_template = ''

with open(header, 'r') as file:
    header_template = file.read()
with open(footer, 'r') as file:
    footer_template = file.read()

for conf in config:
    if 'file' in conf:
        with open(conf['file'], 'r') as myfile:
            data = myfile.read()

        if 'target' in conf:
            pass
        else:
            with open(target + conf['file'], 'w') as myfile:
                myfile.write(header_template + data + footer_template)
    if 'directory' in conf:
        if not os.path.exists(target):
            os.makedirs(target + conf['directory'])

        for file in os.listdir(conf['directory']):
            with open(conf['directory'] + file, 'r') as myfile:
                data = myfile.read()

            with open(target + conf['directory'] + file, 'w') as myfile:
                myfile.write(header_template + data + footer_template)


for asset in assets:
    if os.path.isdir(asset):
        copytree(asset, target + asset)
    else:
        copyfile(asset, target + asset)
