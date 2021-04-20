import matplotlib.pyplot as plt
import subprocess
import os
import math

CWD = os.path.dirname(os.path.realpath(__file__)).replace(" ", "\\ ")
REPORT_FOLDER = CWD + "/processed"
AVERAGE_MESSAGES_FILE = REPORT_FOLDER + "/average-messages.txt"
STATE_CHECKS_FILE = REPORT_FOLDER + "/state-checks.txt"
GRAPH_FOLDER = CWD + "/graphs"

if not os.path.exists(GRAPH_FOLDER):
    os.makedirs(GRAPH_FOLDER)


def filter_out_empty_strings(array):
    return list(filter(None, array))

def process_line(line):
    key, value = line.split(":")
    key = key.split("-")[1]
    return float(key), float(value)

def plot_graph(title, points, xlabel, ylabel):
    plt.clf()
    plt.plot([x[0] * 100 for x in points], [y[1] for y in points])
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.ylim(ymin=0)
    plt.savefig(GRAPH_FOLDER + "/" + title + ".png", pad_inches=0, bbox_inches = 'tight')

def plot_sub_graph(title, points1, label1, points2, label2, xlabel, ylabel):
    plt.clf()
    plt.plot([x[0] * 100 for x in points1], [y[1] for y in points1], "b-", label=label1)
    plt.plot([x[0] * 100 for x in points2], [y[1] for y in points2], "r-", label=label2)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.ylim(ymin=0)
    plt.legend(loc='best')
    plt.savefig(GRAPH_FOLDER + "/" + title + ".png", pad_inches=0, bbox_inches = 'tight')

def get_expected_message_failure(probability):
    message_failure_probability = (probability/4)/((1+probability)*0.5)
    return message_failure_probability * 90

def get_expected_errors(inactive_probabilities):
    res = []
    for probability in inactive_probabilities:
        res.append([probability, get_expected_message_failure(probability)])
    return res

def get_average_plots(results):
    points = []
    occurrences = 0
    total = 0
    last_key = ""
    for line in results:
        key, value = process_line(line)
        if key == last_key:
            occurrences += 1
            total += value
        else:
            if last_key != "":
                points.append([last_key, total / occurrences])
            last_key = key
            total = value
            occurrences = 1
    
    points.append([last_key, total / occurrences])
    return points

def get_max_plots(results):
    points = []
    max_value = 0
    last_key = ""
    for line in results:
        key, value = process_line(line)
        if key == last_key:
            max_value = max(max_value, value)
        else:
            if last_key != "":
                points.append([last_key, max_value])
            last_key = key
            max_value = value
    
    points.append([last_key, max_value])
    return points

def create_plots(average_results, state_check_results):
    average_messages = get_average_plots(average_results)
    average_errors = get_average_plots(state_check_results)
    expected_errors = get_expected_errors([x[0] for x in average_errors])
    plot_graph("Average Messages For Gossip Protocol", average_messages, "Inactive Probability", "Average messages")
    plot_sub_graph("Average Errors For Gossip Protocol", average_errors, "Experimental Results", expected_errors, "Expected Errors", "Inactive Probability", "Errors")


average_message_file = open(AVERAGE_MESSAGES_FILE, "r")
average_message_results = filter_out_empty_strings(average_message_file.read().split("\n"))
state_checks_file = open(STATE_CHECKS_FILE, "r")
state_check_results = filter_out_empty_strings(state_checks_file.read().split("\n"))


create_plots(average_message_results, state_check_results)

