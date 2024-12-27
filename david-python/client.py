import socket
import threading
import sys
from question import Question
'''
(SERVER, HEADER, PORT, FORMAT, DISCONNECT_MESSAGE)
(socket.gethostbyname(socket.gethostname()), 64, 5050, "utf-8", "!DISCONNECT")
'''

class Client:
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
    self.questionIndex = 0

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
      if msg == "start quiz" and not self.quiz_mode: # jika host memulai quiz
        print("host telah memulai quiz!\n")
        self.questions.clear()
        while True:            # terima semua pertanyaan
          new_msg = self.receive_message()
          msg = self.get_message(new_msg)
          if msg == "all-questions-sent":
            break
          self.questions.append(Question.unwrap(msg))

        self.quiz_mode = True 
        self.questionIndex = 0
        self.questions[self.questionIndex].print_question()        # tampilkan pertanyaan pertama
        self.questionIndex += 1

      elif msg == "timer-ended":
        print("waktu habis, anda tidak dapat mengirim jawaban lagi\n")
        self.quiz_mode = False

      elif msg == "send-leaderboard":
        print("Berikut adalah leaderboard dari quiz:")
        # terima leaderboard
        while True:
          new_msg = self.receive_message()
          msg = self.get_message(new_msg)
          if msg == "leaderboard-sent": break
          print(msg)
        print("\n")
      else : # pesan biasa  
        print(new_msg)

  def handshake(self):
    client.send_message(f"set-username:{username}") # set username
    client.send_message(f"set-host:0") # set host status

username = input("enter username: ")
client = Client(username, socket.gethostbyname(socket.gethostname()), 64, 5050, "utf-8", "!DISCONNECT")
client.handshake()

# keep listening for message
keep_listening_thread = threading.Thread(target=client.keep_listening_message)
keep_listening_thread.daemon = True
keep_listening_thread.start()

# keep reading input from user
while client.connected:
  new_msg = input()
  if new_msg == "exit":
    client.connected = False
    client.send_message(client.DISCONNECT_MESSAGE)
  else : # pesan biasa
    client.send_message(new_msg)
    if client.quiz_mode: # pesan biasa adalah jawaban utk quiz
      # tampilkan pertanyaan berikutnya
      if client.questionIndex < (len(client.questions)):
        client.questions[client.questionIndex].print_question()
        client.questionIndex += 1

client.send_message(client.DISCONNECT_MESSAGE)
client.conn.close()
sys.exit()