package app.entity;

import java.util.Objects;

public class BTMessage {
    public enum Type {
        SYSTEM, INCOMING, OUTGOING;
    }

    private Type type;
//    private Date time;
    private String sender;
    private String content;
    private int count;

    public BTMessage(Type type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
//        this.time = new Date();
        this.count = 1;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

//    public Date getTime() {
//        return time;
//    }
//
//    public void setTime(Date time) {
//        this.time = time;
//    }

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increaseCount(){
        count++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTMessage btMessage = (BTMessage) o;
        return type == btMessage.type &&
                sender.equals(btMessage.sender) &&
                content.equals(btMessage.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sender, content);
    }
}
