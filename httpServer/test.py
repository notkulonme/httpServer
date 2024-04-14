import socket

try:
    s = socket.socket()
    command = input("command: ")
    command = command + "\n"
    s.connect(('127.0.0.1', 28852))
    s.send(command.encode())
    line = ""
    while True:
        part = s.recv(1).decode()
        if part != "\"":
            line += str(part)
        else:
            break
    print(line)
    s.close()
except:
    print("the server is down")
