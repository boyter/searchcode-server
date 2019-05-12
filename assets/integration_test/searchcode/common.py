import os

BASE_URL = 'BASE_URL'


def get_base_url():
    base_url = 'http://localhost:8080/'
    if BASE_URL in os.environ:
        base_url = os.environ['BASE_URL']

    return base_url


def get_blns():
    dir_path = os.path.dirname(os.path.realpath(__file__))
    with open(os.path.join(dir_path, 'blns.txt'), encoding='utf-8') as f:
        content = f.readlines()
        naughty = [x for x in content if len(x.strip()) != 0 and x.strip()[0] != "#"]

    return naughty


if __name__ == "__main__":
    print(get_blns())
