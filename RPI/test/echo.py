#!/usr/bin
import os
from bluetooth import *
from config import *

# bluetooth discoverable
os.system("sudo hciconfig hci0 piscan")

server_sock = BluetoothSocket(RFCOMM)
server_sock.bind(("",RFCOMM_CHANNEL))
server_sock.listen(1)
port = server_sock.getsockname()[1]
advertise_service( server_sock, "MDP-Server",
                    service_id = UUID,
                    service_classes = [ UUID, SERIAL_PORT_CLASS ],
                    profiles = [ SERIAL_PORT_PROFILE ],
                    # protocols = [ OBEX_UUID ]
)
print("Waiting for connection on RFCOMM channel %d" % port)
client_sock, client_info = server_sock.accept()
print("Accepted connection from ", client_info)
try:
    while True:
        print ("In while loop...")
        data = client_sock.recv(1024)
        if len(data) == 0: 
            break
        print("Received [%s]" % data)
        client_sock.send(data.decode() + " i am pi!")
except IOError:
    pass
print("disconnected")
client_sock.close()
server_sock.close()
print("all done")