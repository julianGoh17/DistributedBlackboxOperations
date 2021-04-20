import os
import sys

CWD = os.path.dirname(os.path.realpath(__file__))
GENERATED_FOLDER = CWD + "/../generated/report"
METRICS_REPORT = GENERATED_FOLDER + "/metrics-collector-report.txt"
GENERATED_REPORT = GENERATED_FOLDER + "/generated-report.txt"
processed_file = open(CWD + "/processed/comparison.txt", "a+")


def readFile(path):
    return open(path, "r").read()

def filter_out_empty_strings(array):
    return list(filter(None, array))

def process_average_message_size(line):
    return float(line.split(': ')[1].split(" ")[0])

def process_messages(line):
    return float(line.split(':')[1].strip())

def process_time(line):
    _, minutes_string, seconds_string = line.split(":")
    minutes = int(minutes_string.strip().split(" ")[0])
    seconds = float(seconds_string.split(" ")[0])
    return minutes * 60 + seconds


def process_metrics_report(metrics_report):
    metrics_components = filter_out_empty_strings(metrics_report.split("\n\n"))
    total_bytes = 0
    total_messages = 0
    total_time = 0
    
    for component in metrics_components:
        lines = component.split("\n")
        total_bytes += process_average_message_size(lines[2])
        total_messages += process_messages(lines[3])
        total_time += process_time(lines[6])
    
    total_bytes /= len(metrics_components)
    total_messages /= len(metrics_components)
    total_time /= len(metrics_components)
    
    processed_file.write("Average Message Size: " + str(total_bytes) + "  Bytes\n")
    processed_file.write("Average Messages: " + str(total_messages) + "\n")
    processed_file.write("Average Time: " + str(total_time) + " Seconds\n")

def process_generated_report_for_errors(generated_report):
    overviews = filter_out_empty_strings(generated_report.split("Overview Comparison")[1:])
    errors = 0
    for overview in overviews:
        lines = overview.split("\n")
        index = 0
        missing_ids = set()
        isProcessing = True
        while isProcessing and index < len(lines):
            if lines[index].startswith("Missing Expected IDs"):
                index += 1
                while lines[index].startswith("- "):
                    missing_ids.add(lines[index][2:])
                    index += 1
                if not lines[index].startswith("- "):
                    isProcessing = False
            else:
                index += 1

        isProcessing = True
        while isProcessing and index < len(lines):
            if lines[index].startswith("Unexpected IDs In Server"):
                index += 1
                if not lines[index].startswith("- "):
                    isProcessing = False
                while lines[index].startswith("- "):
                    missing_ids.add(lines[index][2:])
                    index += 1
            else:
                index += 1
        errors = max(len(missing_ids), errors)
    return errors

metrics_report = readFile(METRICS_REPORT)
process_metrics_report(metrics_report)
generated_report = readFile(GENERATED_REPORT)
errors = process_generated_report_for_errors(generated_report)
processed_file.write("Errors: " + str(errors) + "\n") 