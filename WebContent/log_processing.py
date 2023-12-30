import sys

def log_processing(f):
    tsTotal, tjTotal = 0, 0
    cnt = 0

    with open(f, 'r') as file:
        for line in file:
            _, TS, _, TJ = line.strip().split(',')
            tsTotal += float(TS)
            tjTotal += float(TJ)
            cnt += 1

    average_ts = tsTotal / cnt
    average_tj = tjTotal / cnt
    print(f"Average TS: {average_ts} ms")
    print(f"Average TJ: {average_tj} ms")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        f = sys.argv[1]
        log_processing(f)
    else:
        print("No file name provided.")
