
QUIZ_DURATION = 15

import socket
import threading
import sys
from question import Question
import time
from datetime import datetime
'''
(SERVER, HEADER, PORT, FORMAT, DISCONNECT_MESSAGE)
(socket.gethostbyname(socket.gethostname()), 64, 5050, "utf-8", "!DISCONNECT")
'''

class Host:
  def __init__(self, username, SERVER, HEADER, PORT, FORMAT, DISCONNECT_MESSAGE):
    self.username = username
    self.HEADER = HEADER
    self.PORT = PORT
    self.FORMAT = FORMAT
    self.DISCONNECT_MESSAGE = DISCONNECT_MESSAGE
    self.SERVER = SERVER
    self.connected = True
    
    self.ADDR = (SERVER, PORT)
    self.conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    self.conn.connect(self.ADDR)

    self.quiz_mode = False
    self.questions = []
    self.answers = {} # map -> "username" : "kunci jawaban"
    self.last_answer_time = {} # map -> "username" : kapan terakhir kali user mengirim jawaban quiz

    self.quiz_started_time = 0

  def receive_message(self):
    if self.connected:
      msg_length = self.conn.recv(self.HEADER).decode(self.FORMAT)
      if msg_length:
        msg_length = int(msg_length)
        msg = self.conn.recv(msg_length).decode(self.FORMAT)
        return msg
      return "message not found"
    return "message not found"
  
  def get_username(self, msg):
    for i in range(len(msg)):
      if msg[i] == ':':
        return msg[0:i]
    return "username not found" 

  def get_message(self, msg):
    for i in range(len(msg)):
      if msg[i] == ':':
        return msg[i + 2:]
    return "message not found"
    
  def send_message(self, msg):
    message = msg.encode(self.FORMAT)
    msg_length = len(message)
    send_length = str(msg_length).encode(self.FORMAT)
    send_length += b" " * (self.HEADER - len(send_length))

    #send the length
    self.conn.send(send_length)
    self.conn.send(message)
  
  def keep_listening_message(self):
    while self.connected:
      new_msg = self.receive_message()
      msg = self.get_message(new_msg)
      usr = self.get_username(new_msg)
      print(new_msg)
      if self.quiz_mode: # terima jawaban dari peserta ketika quiz mode
        if usr in self.answers:
          self.answers[usr] += msg
        else :
          self.answers[usr] = msg

        if len(self.answers[usr]) <= len(self.questions):
          self.last_answer_time[usr] = datetime.now() # kapan terakhir kali seorang user mengirim sebuah jawaban

  def handshake(self):
    host.send_message(f"set-username:{username}") # set username
    host.send_message(f"set-host:1") # set host status
  
  def read_question(self):
    self.questions.clear()
    self.answers.clear()
    with open("question.txt", "r") as file:
      q = Question("")
      for line in file:
        now = line.strip()
        if now[0] == 'Q': # question part
          q = Question(now[2:])
        elif now[0:3] == 'OPT': # option part
          q.add_option(now[4:])
        elif now[0:3] == 'KEY':
          q.answer = now[4]
        else : # end question
          self.questions.append(q)

    print("all question has been read successfully")
  
  def quiz_timer(self, second):
    print("timer started: " + str(second) + " seconds...")
    time.sleep(second)
    print("timer stopped")
    self.send_message("timer-ended")

    # means, quiz also ended
    self.quiz_mode = False

  def get_time_taken(self, time_a, time_b): # returns in second
    minute_delta = time_b.minute - time_a.minute
    second_delta = time_b.second - time_a.second
    return minute_delta * 60 + second_delta

  def send_leaderboard(self):
    # for name in self.answers:
    #   self.send_message(name + ": " + self.answers[name])

    leaderboard = []
    for name in self.answers:
      score = 0
      # jawaban ke-i sama dengan kunci jawaban dari soal ke-i
      for i in range(min( len(self.answers[name]), len(self.questions)) ):
        score += self.answers[name][i] == self.questions[i].answer

      # tuple = (nama, skor, durasi_penyelesaian_test)
      durasi_penyelesaian_test = self.get_time_taken(self.quiz_started_time, self.last_answer_time[name])
      leaderboard.append(  (name, score, durasi_penyelesaian_test)  )
    

    self.send_message("send-leaderboard")
    # urutkan berdasarkan skor terbesar terlebih dahulu, kalau skor sama, urutkan berdasarkan durasi_penyelesaian_test terkecil
    leaderboard.sort(key = lambda x: (-x[1], x[2]))
    rank = 1
    for item in leaderboard:
      self.send_message(f"[{rank}]: " + item[0] + ", SCORE: " + str(item[1]) + ", TIME TAKEN: " + str(QUIZ_DURATION + item[2]))
      rank += 1
    self.send_message("leaderboard-sent")
    

username = input("enter username: ")
host = Host(username, "192.168.92.244", 64, 5050, "utf-8", "!DISCONNECT")
host.handshake()

# keep listening for message
keep_listening_thread = threading.Thread(target=host.keep_listening_message)
keep_listening_thread.daemon = True
keep_listening_thread.start()

# keep reading input from user
while host.connected:
  new_msg = input()
  if new_msg == "exit":
    host.connected = False
    host.send_message(host.DISCONNECT_MESSAGE)

  elif new_msg == "start quiz" and not host.quiz_mode:
    host.read_question()
    host.send_message(new_msg)
    host.quiz_mode = True
    for q in host.questions:   # kirim semua pertanyaan
      host.send_message(q.wrap())
    host.send_message("all-questions-sent")

    # mulai juga timer nya
    # thread for timer
    quiz_timer_thread = threading.Thread(target=host.quiz_timer, args=(QUIZ_DURATION,))
    quiz_timer_thread.start()
    quiz_timer_thread.join()
    host.quiz_started_time = datetime.now()

  elif new_msg == "send leaderboard":
    host.send_leaderboard()
  else : # pesan biasa
    host.send_message(new_msg)

host.send_message(host.DISCONNECT_MESSAGE)
host.conn.close()
sys.exit()