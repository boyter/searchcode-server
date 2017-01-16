#!/usr/bin/env python

import sys
import os
import argparse
import random
import struct
import time

global VALID_TYPES
VALID_TYPES = ['file', 'dir', 'symlink', 'broken_symlink', 'self_symlink', 'circular_symlink', 'link', 'fifo']

def get_random_bytes(count):
    with open('/dev/urandom', "rb") as fd:
        return fd.read(count)

def get_random_utf8():
    gotutf8 = False
    while not gotutf8:
        try:
            codepoint = random.SystemRandom().randint(0, 4294967296)
            possible_utf8_bytes = struct.pack(">I", codepoint)
            possible_utf8_bytes.decode('UTF8')
            gotutf8 = True
        except UnicodeDecodeError:
            pass
    return possible_utf8_bytes

def write_file(name, data=b''):
    if not isinstance(name, bytes):
        print("write_file() requires the file name to be in bytes. Exiting.")
        os._exit(1)
    if not isinstance(data, bytes):
        print("write_file() requires all data to be in bytes. Exiting.")
        os._exit(1)
    with open(name, 'xb') as fh:
        fh.write(data)

def all_valid_filename_bytes(): # everything but NULL and / # interestingly, / is a valid symlink dest
    return set([bytes(chr(x), encoding='Latin-1') for x in range(0,256)]) - set([b'\x00',b'\x2F'])

def random_filename_length():
    return random.SystemRandom().randint(0, 256)    #should be 255

def create_object(name, file_type):
    if file_type not in VALID_TYPES:
        print("you must specify one of:", VALID_TYPES)
        os._exit(1)
    if file_type == 'file':
        write_file(name)
    elif file_type == 'dir':
        os.makedirs(name)
    elif file_type == 'symlink':
        os.symlink(b'.', name)
    elif file_type == 'broken_symlink':
        # setting the target to a random byte does not gurantee a broken symlink
        # in the case of a fixed target like b'a' _at least_ one symlink
        # (in the complete coverage of n bytes case, when n=1 the 'a' symlink will be circular) 
        # will be circular and broken.
        # methods:
        #   set target ../ OUTSIDE (because one inside could name clash) the current folder and
        # choose a path guranteed to not exist.
        # to gurantee the target does not exist, assume the "root" folder tree is deleted every run,
        # then assume a custom ../$dest_dir_$timestamp_for_broken_symlinks/name DOES NOT EXIST.
        # check the byte (int) value, if it's >0, add 1, if it's = max_value then return \x00,
        # this way every symlink has a unique existing (or soon exising) target.
        # might be a nice way to gen circular symlinks... must skip '.', '..', '/', '\x00'

        non_existing_target = b'../' + str(time.time()).encode('UTF8') + b'/' + name
        os.symlink(non_existing_target, name)

    elif file_type == 'self_symlink':
        os.symlink(name, name)
#   elif file_type == 'circular_symlink':
#       os.symlink(name_back_to_name, name)
    else:
        print("unsupported file_type:", file_type)
        os._exit(1)

def make_all_one_byte_objects(dest_dir, file_type, count):
    os.makedirs(dest_dir)
    os.chdir(dest_dir)
    for byte in all_valid_filename_bytes():
        if byte != b'\x2E': # '.' is not a valid single byte file name #note it is a valid symlink destination
            create_object(byte, file_type)
    os.chdir(b'../')
    check_file_count(dest_dir, count)

def make_all_two_byte_objects(dest_dir, file_type, count):
    os.makedirs(dest_dir)
    os.chdir(dest_dir)
    for first_byte in all_valid_filename_bytes():
        for second_byte in all_valid_filename_bytes():
            file_name = first_byte + second_byte
            if file_name != b'..':  # '..' is not a valid 2 byte file name, but it is a valid symlink destination
                create_object(file_name, file_type)
    os.chdir(b'../')
    check_file_count(dest_dir, count)

def make_all_length_objects(dest_dir, file_type, count):
    os.makedirs(dest_dir)
    os.chdir(dest_dir)
    byte_length = 1
    while byte_length < 256:
        file_name = b'a' * byte_length
        create_object(file_name, file_type)
        byte_length += 1
    os.chdir(b'../')
    check_file_count(dest_dir, count)

def check_file_count(dir, expected_count):
    if not os.path.isdir(dir):
        print("dir:", dir, "is not a dir")
        os._exit(1)
    count = len(os.listdir(dir))
    if count != expected_count:
        print("dir:", dir, "has", count, "files. Expected:", expected_count)
        os._exit(1)

class DefaultHelpParser(argparse.ArgumentParser):
    def error(self, message):
        sys.stderr.write('error: %s\n\n' % message)
        self.print_help()
        sys.exit(2)
    def _get_option_tuples(self, option_string):    #https://bugs.python.org/issue14910
        return []

class SmartFormatter(argparse.HelpFormatter):
    def _split_lines(self, text, width):
        # this is the RawTextHelpFormatter._split_lines
        if text.startswith('R|'):
            return text[2:].splitlines()
        return argparse.HelpFormatter._split_lines(self, text, width)

def main():
    # 1 byte names
    # expected file count = 255 - 2 = 253 (. and / note 0 is NULL)
    # /bin/ls -A 1/1_byte_file_names | wc -l returns 254 because one file is '\n'
    make_all_one_byte_objects(b'1_byte_file_names', 'file', 253)
    make_all_one_byte_objects(b'1_byte_dir_names', 'dir', 253)
    make_all_one_byte_objects(b'1_byte_symlink_names', 'symlink', 253)
    make_all_one_byte_objects(b'1_byte_broken_symlink_names', 'broken_symlink', 253)
    make_all_one_byte_objects(b'1_byte_self_symlink_names', 'self_symlink', 253)

    # 2 byte names
    # expected file count = (255 - 1) * (255 - 1) = 64516 - 1 = 64515
    # since only NULL and / are invalid, and there is no '..' file
    # /bin/ls -A -f --hide-control-chars 1/2_byte_file_names | wc -l returns 64515
    make_all_two_byte_objects(b'2_byte_file_names', 'file', 64515)

    if cmd_args.long_tests:
        make_all_two_byte_objects(b'2_byte_dir_names', 'dir', 64515) # takes forever to delete
        make_all_two_byte_objects(b'2_byte_symlink_names', 'symlink', 64515)
        make_all_two_byte_objects(b'2_byte_broken_symlink_names', 'broken_symlink', 64515)

    # all length objects
    # expected file count = 255
    make_all_length_objects(b'all_length_file_names', 'file', 255)
    make_all_length_objects(b'all_length_symlink_names', 'symlink', 255)
    make_all_length_objects(b'all_length_broken_symlink_names', 'broken_symlink', 255)
    make_all_length_objects(b'all_length_self_symlink_names', 'self_symlink', 255)
    make_all_length_objects(b'all_length_dir_names', 'dir', 255)


if __name__ == '__main__':
    parser = DefaultHelpParser(formatter_class=SmartFormatter, add_help=True)
    long_tests_help = 'R|Run tests that may take hours to complete and even longer to delete.\n'
    dest_dir_help = 'R|Directory to make files under. Should be empty.\n'

    parser.add_argument("dest_dir", help=dest_dir_help, type=str)
    parser.add_argument("--long-tests", help=long_tests_help, action="store_true", default=False)

    cmd_args = parser.parse_args()

    dest_dir = os.path.abspath(os.path.expanduser(cmd_args.dest_dir))
#    print("dest_dir:", dest_dir)

    os.makedirs(dest_dir)
#    print("chainging to", dest_dir)
    os.chdir(dest_dir)

    main()

