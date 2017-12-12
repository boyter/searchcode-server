from locust import HttpLocust, TaskSet, task
import json
import random
from datetime import date
import os


class UserBehavior(TaskSet):
    ''' Test designed to replicate users searching and clicking on various parts of searchcode.'''

    def domain(self):
        return '''https://searchcode.com'''

    def coderesult(self):
        url = '/file/' + random.choice(range(1, 60000000)) + '/locusttest'
        self.client.get(self.domain() + url)

    def get_random_search(self):
        with open('./example_searches.txt', 'r') as myfile:
            contents = myfile.read()
            cache = contents.split(',')
        return random.choice(cache)

    def searchresult(self):
        url = '?q=' + get_random_search()
        self.client.get(self.domain() + url)

    @task(10)
    def code_result_click(self):
        self.coderesult()

    @task(1)
    def search_result_click(self):
        self.searchresult()
        self.coderesult()


class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    min_wait = 1000
    max_wait = 15000
