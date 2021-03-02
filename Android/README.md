# Android MDP Controller
## Protocol format
### Sending
default value can be set in `res/values/strings_pref.xml` or setting page
* `W1`: to manually move robot forward by 1 possition
* `S1`: to manually move robot backforward by 1 possition
* `A`: to manually turn robot to it's left
* `D`: to manually turn robot to it's right
* `ES`: to start exploration
* `FS`: to start fastest path
* `IR`: to start image recognition
* `MAP`: to receive map update
* `CONFIG|x,y,dir|wx,wy|<MDF.P1>|<MDF.P2>`: send configuration to algorithm team for setting up fastest path  
  
### Receiving
* `MOV|F`: move robot foward on 2D map view
* `MOV|B`: move robot backword on 2D map view
* `MOV|L`: turn robot to its left on 2D map view
* `MOV|R`: turn robot to its right on 2D map view
* `IMG|(id,x,y)`: add a new image on 2D map view
* `IMGS|[<list of image strings>]`: replace current collection images to received images, the list must be comma separated
* `MAP|x,y,dir|<MDF.P1>|<MDF.P2>`: to update robot postition and 2D map view


## Remarks
1. Bluetooth service is adopted from [android-BluetoothChat][1]
2. Icons are adopted from [Material Design Icons][2]


[1]: https://github.com/googlearchive/android-BluetoothChat
[2]: https://materialdesignicons.com/