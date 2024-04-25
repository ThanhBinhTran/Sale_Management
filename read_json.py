# Using readline()
file1 = open('customers.json', 'r')
count = 0
 
while True:
    count += 1
 
    # Get next line from file
    line = file1.readline()
 
    # if line is empty
    # end of file is reached
    if not line:
        break
    if "{" in line or "}" in line or ":" in line:
            continue
    print("Line{}: {}".format(count, line.strip()))
 
file1.close()