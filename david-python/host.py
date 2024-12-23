
import socket
import threading
import sys
from question import Question
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
    self.answers = {}

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
      if host.quiz_mode: # terima jawaban dari peserta ketika quiz mode
        if usr in self.answers:
          self.answers[usr] += msg
        else :
          self.answers[usr] = msg

  def handshake(self):
    host.send_message(f"set-username:{username}") # set username
    host.send_message(f"set-host:1") # set host status
  
  def read_question(self):
    q = Question("Di dalam sebuah router, HOL (head-of-the-line) blocking disebabkan oleh?")
    q.add_option("Queueing delay dan kehilangan paket di sebuah input buffer")
    q.add_option("queueing delay dan kehilangan paket di sebuah output buffer")
    q.add_option("saling menghalangi paket-paket yang berada di input buffer yang berbeda")
    q.add_option("saling menghalangi paket-paket yang berada di output buffer yang berbeda")
    q.answer = 'c'
    self.questions.append(q)

    q = Question("Andaikan bit error adalah independed secara statistik ketika sebuah frame ditransmisikan di suatu link; andaikan p adalah probabilitas terjadinya sebuah bit error. Berapakah probabilitas bahwa paling sedikit dua bit error terjadi ketika k buah bit ditransmisikan?")
    q.add_option("1 - (1 - p)^k")
    q.add_option("1 - (1 - p)^k - kp(1 - p)^(k - 1)")
    q.add_option("(1 - p)^(k+1) - p(1 - p)^k")
    q.add_option("p")
    q.answer = "b"
    self.questions.append(q)

    print("all question has been read successfully")

  def send_leaderboard(self):
    # for name in self.answers:
    #   self.send_message(name + ": " + self.answers[name])

    leaderboard = []
    for name in self.answers:
      score = 0
      # jawaban ke-i sama dengan kunci jawaban dari soal ke-i
      for i in range(min( len(self.answers[name]), len(self.questions)) ):
        score += self.answers[name][i] == self.questions[i].answer
      leaderboard.append((name, score))
    
    self.send_message("send-leaderboard")
    leaderboard.sort(key = lambda x: x[1], reverse = True)
    for item in leaderboard:
      self.send_message(item[0] + ", score: " + str(item[1]))
    self.send_message("leaderboard-sent")
    

username = input("enter username: ")
host = Host(username, socket.gethostbyname(socket.gethostname()), 64, 5050, "utf-8", "!DISCONNECT")
host.handshake()
host.read_question()

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
    host.send_message(new_msg)
    host.quiz_mode = True
    for q in host.questions:
      host.send_message(q.wrap())
    host.send_message("all-questions-sent")
  elif new_msg == "send leaderboard":
    host.send_leaderboard()
  else :
    host.send_message(new_msg)

host.send_message(host.DISCONNECT_MESSAGE)
host.conn.close()
sys.exit()