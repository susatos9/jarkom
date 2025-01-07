from datetime import datetime
import time

time_a = 0
time_b = 0

def get_time_taken(time_a, time_b): # returns in second
    minute_delta = time_b.minute - time_a.minute
    second_delta = time_b.second - time_a.second
    return minute_delta * 60 + second_delta

time_a = datetime.now()

print(time_a)
print(time_a.second)
print(time_a.minute)
print(time_a.hour)

time.sleep(3)

time_b = datetime.now()
print(time_b)
print(time_b.second)
print(time_b.minute)
print(time_b.hour)

print("time taken: ", get_time_taken(time_a, time_b))



