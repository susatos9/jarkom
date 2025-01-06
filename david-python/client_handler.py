class Client_handler:
  clients = []
  def __init__(self, conn, addr, HEADER, PORT, FORMAT, DISCONNECT_MESSAGE):
    self.conn = conn
    self.addr = addr
    self.HEADER = HEADER
    self.PORT = PORT
    self.FORMAT = FORMAT
    self.DISCONNECT_MESSAGE = DISCONNECT_MESSAGE
    self.username = "no username"
    self.is_a_host = False
    self.quiz_mode = False
    Client_handler.clients.append(self)

  def set_username(self, username):
    self.username = username

  def receive_message(self):
    msg_length = self.conn.recv(self.HEADER).decode(self.FORMAT)
    if msg_length:
      msg_length = int(msg_length)
      msg = self.conn.recv(msg_length).decode(self.FORMAT)
      return msg
    else :
      return "message not found"
    
  def send_message(self, msg):
    message = msg.encode(self.FORMAT)
    msg_length = len(message)
    send_length = str(msg_length).encode(self.FORMAT)
    send_length += b" " * (self.HEADER - len(send_length))

    #send the length
    self.conn.send(send_length)
    #send the actual message
    self.conn.send(message)

  def send_message_to_clients(self, msg):
    for client in Client_handler.clients:
      if not client.is_a_host:
        client.send_message(f"[HOST]{self.username}: {msg}")
  
  def send_message_to_host(self, msg):
    for client in Client_handler.clients:
      if client.is_a_host:
        client.send_message(f"{self.username}: {msg}")

  def handle_client(self):
    print(f"new connection: {self.addr} has connected")

    # handshake messages
    new_msg = self.receive_message()      # username
    self.set_username(new_msg[13:])
    print("username: ", self.username)

    new_msg = self.receive_message()      # host status
    self.is_a_host = new_msg[9] == '1'
    print("host status: ", self.is_a_host)

    connected = True
    while connected: # actively listening messages
      new_msg = self.receive_message()

      print(f"{self.username}@{self.addr}: {new_msg}")

      # exit message
      if new_msg == self.DISCONNECT_MESSAGE:
        connected = False
        new_msg = "has left the server"
        # send this to all clients
        for client in Client_handler.clients:
          if(client.username != self.username):
            if(self.is_a_host):
              # client.send_message(f"[HOST]{self.username}@{self.addr}: {new_msg}")
              client.send_message(f"[HOST]{self.username}: {new_msg}")
            else :
              # client.send_message(f"{self.username}@{self.addr}: {new_msg}")
              client.send_message(f"{self.username}: {new_msg}")
        continue

      if(self.is_a_host): # pesan dari host
        if not self.quiz_mode and new_msg == "start quiz": # jika host memulai quiz
          self.quiz_mode = True
          self.send_message_to_clients(new_msg) # start quiz

          while True: # terima semua pertanyaan
            new_msg = self.receive_message()
            self.send_message_to_clients(new_msg)
            if new_msg == "all-questions-sent":
              break
        else :# pesan biasa
          if new_msg == "timer-ended": self.quiz_mode = False # timer selesai, quiz selesai
          self.send_message_to_clients(new_msg)

      else : # pesan dari client
        self.send_message_to_host(new_msg)

    self.conn.close()
    Client_handler.clients.remove(self)
