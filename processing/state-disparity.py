
import os 
import sys

dir_path = os.path.dirname(os.path.realpath(__file__))
print("Arg 1: Name of entry")
entry = sys.argv[1]
if entry == "":
    print("Please pass in non-empty entry")
    exit(0)

report = open(dir_path + "/../generated/report/generated-report.txt", "r")
overviews = report.read().split("Overview Comparison")[1:]


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

processed_file = open(dir_path + "/processed/state-checks.txt", "a+")
processed_file.write(entry + ":" + str(errors) + "\n")
