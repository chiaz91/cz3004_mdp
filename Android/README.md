# Android MDP Controller
## Features
* Support both phone and tablet
* Comprehensive setting page
* Comprehensive map editing modes
* Allow clearing of checklist with another android phone installing the same app

## Protocol format
### Sending
default value can be set in `res/values/strings_pref.xml` or setting page
* `0`: to manually move robot forward by 1 possition
* `Q`: to manually move robot backforward by 1 possition
* `A`: to manually turn robot to it's left
* `D`: to manually turn robot to it's right
* `ES`: to start exploration
* `FS`: to start fastest path
* `IR`: to start image recognition
* `MAP`: to receive map update
* `CONFIG|row,col,dir|wRow,wCol|<MDF.P1>|<MDF.P2>`: send configuration to algorithm team for setting up fastest path  
  
### Receiving
* `MOV|<0-9>`: move robot to forward by **n+1** moves on 2D map view
* `MOV|A`: turn robot to its left on 2D map view
* `MOV|D`: turn robot to its right on 2D map view
* `MOV|Q`: turn robot to its back on 2D map view
* `IMG|(id,row,col)`: add a new image on 2D map view
* `IMGS|[<list of image strings>]`: replace current collection images to received images, the list must be comma separated
* `MAP|row,col,dir|<MDF.P1>|<MDF.P2>`: to update robot postition and 2D map view


## Remarks
1. Bluetooth service is adopted from [android-BluetoothChat][1]
2. Icons are adopted from [Material Design Icons][2]


[1]: https://github.com/googlearchive/android-BluetoothChat
[2]: https://materialdesignicons.com/