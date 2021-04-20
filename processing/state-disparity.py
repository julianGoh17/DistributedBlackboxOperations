
import os 
import sys
from general_statistics import process_generated_report_for_errors
dir_path = os.path.dirname(os.path.realpath(__file__))
entry = sys.argv[1]
if entry == "":
    print("Please pass in non-empty entry")
    exit(0)

report = open(dir_path + "/../generated/report/generated-report.txt", "r")
processed_file = open(dir_path + "/processed/state-checks.txt", "a+")
processed_file.write(entry + ":" + str(process_generated_report_for_errors(report.read())) + "\n")
