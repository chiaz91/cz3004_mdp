package app.control;


import app.util.MdpLog;

public class MdpParser {
    private static final String TAG = "mdp.parser";
    private OnParseResultListener listener;

    public enum MoveType {
        FORWARD, TURN_LEFT, TURN_RIGHT, TURN_BACK;
    }
    public enum NavigationType {
        FASTEST_PATH, IMAGE_RECOGNITION, EXPLORATION;
    }
    public interface OnParseResultListener{
        void onReceivedStatus(String status);
        void onReceivedMove(MoveType type, int numMove);
        void onReceivedNavigation(NavigationType type);
        void onReceivedMapRequest();
        void onReceivedMapUpdate(int x, int y, int direction, String p1, String p2);
        void onReceivedNewImage(String strImage);
        void onReceivedNewImageSet(String strImages);
    }

    public MdpParser(OnParseResultListener listener) {
        this.listener = listener;
    }

    public void parseMessage(String received){
        // split message to prevent concatenation of message
        String[] cmds = received.split("\n");
        for(String command : cmds){
            MdpLog.d(TAG, "parsing ["+command+"]");
            String[] parts = command.split("\\|");
            try{
                switch (parts[0]){
                    case "MAP": parseMap(parts); break;
                    case "MOV": parseMove(parts); break;
                    case "IMG": parseImage(parts); break;
                    case "IMGS": parseImages(parts); break;
                    case "STATUS": parseStatus(parts); break;
                    default:
                        MdpLog.d(TAG, "Unknown Message: "+received);
                }
            } catch (Exception e){
                MdpLog.d(TAG, "Error Parsing: "+received);
                e.printStackTrace();
            }
        }
    }

    private void parseStatus(String... params){
        listener.onReceivedStatus(params[1]);
    }

    private void parseMap(String... params){
        if (params.length==1){
            listener.onReceivedMapRequest();
        } else {
            // TODO: row,col,dir ==> x,y,dir
            String[] botCoord = params[1].split(",");
            int x = Integer.parseInt(botCoord[1]);
            int y = Integer.parseInt(botCoord[0]);
            int dir = Integer.parseInt(botCoord[2]);
            String p1 = params[2];
            String p2 = "";
            if (params.length>=4){
                p2 = params[3];
            }

            listener.onReceivedMapUpdate(x, y,dir*90, p1, p2);
        }
    }

    private void parseMove(String... params){
        try{
            switch (params[1]){
                case "A": listener.onReceivedMove(MoveType.TURN_LEFT, 0); break;
                case "D": listener.onReceivedMove(MoveType.TURN_RIGHT, 0); break;
                case "Q": listener.onReceivedMove(MoveType.TURN_BACK, 0); break;
                default:
                    // move by n+1
                    int moves = Integer.parseInt(params[1]);
                    listener.onReceivedMove(MoveType.FORWARD, moves+1);
            }
        } catch (Exception e){
            MdpLog.a(TAG, "Error on parsingMove");
            e.printStackTrace();
        }
    }

    private void parseImage(String... param){
        listener.onReceivedNewImage(param[1]);
    }

    private void parseImages(String... params){
        try{
            String strImages = params[1].substring(1, params[1].length()-1);
            listener.onReceivedNewImageSet(strImages);
        } catch (Exception e){
            MdpLog.w(TAG, "Error on parseImages");
            e.printStackTrace();
        }
    }

    public void parseSpeechCommands(String... commands){
        boolean handled = true;
        // as audio recognition return list of strings, for loop is used to find first recognisable command
        for (String text:commands) {
            switch (text.toLowerCase()){
                case "turn left":
                case "left":
                    listener.onReceivedMove(MoveType.TURN_LEFT, 0);
                    break;
                case "turn right":
                case "right":
                    listener.onReceivedMove(MoveType.TURN_RIGHT, 0);
                    break;
                case "move forward":
                case "forward":
                    listener.onReceivedMove(MoveType.FORWARD, 1);
                    break;
                case "turn back":
                case "back":
                    listener.onReceivedMove(MoveType.TURN_BACK, 0);
                    break;
                case "fastest":
                    listener.onReceivedNavigation(NavigationType.FASTEST_PATH);
                    break;
                case "exploration":
                    listener.onReceivedNavigation(NavigationType.EXPLORATION);
                    break;
                case "image recognition":
                    listener.onReceivedNavigation(NavigationType.IMAGE_RECOGNITION);
                    break;
                default:
                    handled = false;
            }
            if (handled){
                break;
            }
        }
        if (!handled){
            MdpLog.d(TAG, "Unable to recognize any command ");
        }
    }

}
