import os 
import sys

dir_path = os.path.dirname(os.path.realpath(__file__))
entry = sys.argv[1]
if entry == "":
    print("Please pass in non-empty entry")
    exit(0)

report = open(dir_path + "/../generated/report/metrics-collector-report.txt", "r")
report_lines = report.read().split("\n")
total_messages_line = []
for line in report_lines:
    if line.startswith('Total Messages:'):
        total_messages_line.append(line)

total = 0
for line in total_messages_line:
    messages = int(line.split(": ")[1])
    total += int(messages)

average = total / len(total_messages_line)

processed_file = open(dir_path + "/processed/average-messages.txt", "a+")
processed_file.write(entry + ":" + str(average) + "\n")
