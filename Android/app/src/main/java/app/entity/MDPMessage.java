package app.entity;

import java.util.Objects;

public class MDPMessage {
    public enum Type {
        SYSTEM, INFO, INCOMING, OUTGOING;
    }

    private Type type;
    private String tag;
    private String content;
    private int count;

    public MDPMessage(Type type, String tag, String content) {
        this.type = type;
        this.tag = tag;
        this.content = content;
        this.count = 1;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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
        MDPMessage btMessage = (MDPMessage) o;
        return type == btMessage.type &&
                tag.equals(btMessage.tag) &&
                content.equals(btMessage.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, tag, content);
    }
}
