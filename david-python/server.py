import socket
import threading
from client_handler import Client_handler

HEADER = 64
PORT = 5050
# SERVER = "192.168.2.244"
SERVER = socket.gethostbyname(socket.gethostname())
ADDR = (SERVER, PORT)
FORMAT = "utf-8"
DISCONNECT_MESSAGE = "!DISCONNECT"

# make server socket
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(ADDR)

def start():
  server.listen()
  print(f"server is listening on {SERVER}")
  while True:
    conn, addr = server.accept() # wait for a new connection to server
    new_client = Client_handler(conn, addr, HEADER, PORT, FORMAT, DISCONNECT_MESSAGE)
    thread = threading.Thread(target = new_client.handle_client)
    thread.start()
    print(f"active connections: {threading.active_count() - 1}")

print("server is starting")
start()
