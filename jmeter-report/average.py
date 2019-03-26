import sys

f = open(sys.argv[1])
lines = list(f)
f.close()

length = len(lines)
jdbc = 0
search = 0

for line in lines:
	temp = line.split()
	ms_jdbc_temp = int(temp[0]) / 1000000
	ms_search_temp = int(temp[1]) / 1000000
	jdbc += ms_jdbc_temp
	search += ms_search_temp
	
jdbc_avg = jdbc / length
search_avg = search / length
print(jdbc_avg)
print(search_avg)