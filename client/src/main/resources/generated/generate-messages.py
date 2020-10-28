import random
import time
import json

lastNames = ["Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Goh", "Carver", "Downey", "Kwong", "Tang"]
firstNames = ["Julian", "Tom", "Beth", "Sophia", "Adam", "Will", "Ken", "Alex", "Collin", "Anthony", "Lebron", "James", "Caris", "Joey", "Fiona", "Yui", "Megan", "Farissa", "Bruce", "Clark", "Robert"]
occupations = ["Software Developer", "Writter", "Restaurant Owner", "Musician", "Artist", "Data Scientist", "Researcher", "Advertising", "Entrepreneur", "Influencer", "Baker", "Physiotherapist", "Chiropractor"]
clothes = ['T-shirt', 'Trouser', 'Pullover', 'Dress', 'Coat', 'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle boot', 'Balaclava', 'Boxers', 'Dress Shirt', 'Flak Jacket']

def createFile():
    firstName = random.choice(firstNames)
    lastName = random.choice(lastNames)
    data = {}
    general_info(data, firstName, lastName)
    order_info(data)
    with open('messages/' + firstName + '-' + lastName + '.json', 'w') as outfile:
        json.dump(data, outfile, indent=2)


def general_info(data, firstName, lastName):
    data['info'] = {
        'firstName': firstName,
        'lastName': lastName,
        'time': random_date("1/1/2019 1:30 PM", "31/12/2020 4:50 AM", random.random())
    }

def order_info(data):
    data['order'] = []
    orders = random.randint(1, 4)
    for _ in range(orders):
        data['order'].append(generate_order())

def generate_order():
    return {
                'clothing': random.choice(clothes),
                'quantity': random.randint(1, 10),
               'cost': random.randint(10, 100)
           }

def str_time_prop(start, end, format, prop):
    stime = time.mktime(time.strptime(start, format))
    etime = time.mktime(time.strptime(end, format))

    ptime = stime + prop * (etime - stime)

    return time.strftime(format, time.localtime(ptime))


def random_date(start, end, prop):
    return str_time_prop(start, end, '%d/%m/%Y %I:%M %p', prop)

num_files = 25
for i in range(num_files):
    createFile()