import threading
import time


def quiz_timer(second):
  print("aa")
  time.sleep(second)
  print("bb")

quiz_timer_thread = threading.Thread(target=quiz_timer, args=(3, ))
quiz_timer_thread.start()
quiz_timer_thread.join()


print("main thread ends here")