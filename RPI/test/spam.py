#!/usr/bin
import os
from bluetooth import *
from config import *
import time

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


msgs = [
    "IMG|(1,3,3)\n",    "IMG|(2,3,7)\n",    "IMG|(3,3,11)\n",
    "IMG|(4,7,3)\n",    "IMG|(5,7,7)\n",    "IMG|(6,7,11)\n",
    "IMG|(7,11,3)\n",   "IMG|(8,11,7)\n",   "IMG|(9,11,11)\n",
    "IMG|(10,15,3)\n",  "IMG|(11,15,7)\n",  "IMG|(12,15,11)\n",
    "IMG|(13,19,3)\n",  "IMG|(14,19,7)\n",  "IMG|(15,19,11)\n",
]
    
try:
    while True:
        for msg in msgs:
            print("Sending ",msg) 
            client_sock.send(msg.encode())
        val = input("contine? ")
        if (val == "q"):
            break;
except IOError:
    pass
print("disconnected")
client_sock.close()
server_sock.close()
print("all done")
