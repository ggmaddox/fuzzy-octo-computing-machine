# JMeter
The html file jmeter_report.html is located in the `jmeter-report` folder  
Logs are located in `jmeter-report/logs/`  
At the root, there is a python3 script called average.py  
Usage:  
python3 average.py jmeter-report/logs/log-file-to-test    

Log files in the logs folder:    
http-scaled-1-thread  
http-scaled-10-threads  
http-scaled-10-threads-no-cp  
http-scaled-10-threads-no-ps  
http-single-1-thread  
http-single-10-threads  
http-single-10-threads-no-cp  
http-single-10-threads-no-ps  
https-single-10-threads  

There are also copies of the slave/master log files from which the scaled logs were derived.  

Each log file contains two columns in the format: TJ TQ\n  

# IP addresses (no longer applicable)
aws: 3.17.64.3  
aws master: 18.217.148.81  
aws slave: 18.222.208.184  
google cloud: 35.235.73.254  



