package ntu.cz3004.controller.entity;

import android.graphics.Point;


/**
 * Entity to hold annotation information, which is used to denote any object to be shown on the map
 * <ul>
 *     <li>x represent col, and y represent row</li>
 *     <li>icon takes only -1 or drawable resource id</li>
 * </ul>
 * @see Map
 * @see Point
 */
public class MapAnnotation {
    public static final int ICON_NONE = -1;
    private Point position;
    private int icon;
    private String name;

    public MapAnnotation(int x, int y,  int icon, String name) {
        this.position = new Point(x, y);
        this.icon = icon;
        this.name = name;
    }

    public MapAnnotation(int x, int y, String name) {
        this(x, y, ICON_NONE, name);
    }

    /**
     * position of annotation
     * @return {@link Point} that represent the coordinate of annotation
     */
    public Point getPosition(){
        return position;
    }

    public void setPosition(Point position){
        this.position = position;
    }

    public int getX() {
        return this.position.x;
    }
    public int getY() {
        return this.position.y;
    }
    public boolean hasIcon(){
        return this.icon!=ICON_NONE;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return String.format("(%s,%d,%d)",name,position.x,position.y);
    }
}
