package app.entity;


/**
 * Entity to hold all configurable commands to prevent excessive loading of data
 */
public class Command {
    public String up;
    public String left;
    public String right;
    public String down;
    public String explore;
    public String fastest;
    public String imgRecognition;
    public String f1;
    public String f2;
    public String f3;
    public String stop;
    public String reqMap;

    @Override
    public String toString() {
        return "Command{" +
                "up='" + up + '\'' +
                ", left='" + left + '\'' +
                ", right='" + right + '\'' +
                ", down='" + down + '\'' +
                ", explore='" + explore + '\'' +
                ", fastest='" + fastest + '\'' +
                ", f1='" + f1 + '\'' +
                ", f2='" + f2 + '\'' +
                ", f3='" + f3 + '\'' +
                ", stop='" + stop + '\'' +
                ", reqMap='" + reqMap + '\'' +
                '}';
    }
}
