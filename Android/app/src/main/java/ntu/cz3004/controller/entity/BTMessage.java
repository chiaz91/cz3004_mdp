package ntu.cz3004.controller.entity;

import java.util.Date;

public class BTMessage {
    public enum Type {
        SYSTEM, INCOMING, OUTGOING;
    }

    private Type type;
    private Date time;
    private String sender;
    private String content;

    public BTMessage(Type type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.time = new Date();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
