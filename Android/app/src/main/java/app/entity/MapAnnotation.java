package app.entity;

import android.graphics.Point;

import ntu.cz3004.controller.R;


/**
 * Entity to hold annotation information, which is used to denote any object to be shown on the map
 * <ul>
 *     <li>x represent col, and y represent row</li>
 *     <li>icon takes only -1 or drawable resource id</li>
 * </ul>
 * @see Map
 * @see Point
 */
public class MapAnnotation implements Comparable<MapAnnotation> {
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

    public void setPosition(int x, int y){
        setPosition(new Point(x,y));
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

    public String toPrettyString(){
        return String.format("(<font color='#92ffc0'>%s</font>,%d,%d)",name,position.x,position.y );
    }

    /**
     * factory create image annotation based on string given
     * @param imgStr assume format is (id,x,y) with no spaces.
     * @return created annotation object
     */
    public static MapAnnotation createImageFromString(String imgStr){
        MapAnnotation annotation = null;
        try{
            // TODO: (id,row,col) ==> (id,x,y)
            // assume format is (id,row,col) with no spaces.
            imgStr = imgStr.substring(1, imgStr.length()-1);
            String[] data = imgStr.split(",");
            int id = Integer.parseInt(data[0]);
            int x  = Integer.parseInt(data[2]);
            int y  = Integer.parseInt(data[1]);
            int icon = MapAnnotation.ICON_NONE;
            switch (id){
                case 1: icon = R.drawable.ic_map_1; break;
                case 2: icon = R.drawable.ic_map_2; break;
                case 3: icon = R.drawable.ic_map_3; break;
                case 4: icon = R.drawable.ic_map_4; break;
                case 5: icon = R.drawable.ic_map_5; break;
                case 6: icon = R.drawable.ic_map_6; break;
                case 7: icon = R.drawable.ic_map_7; break;
                case 8: icon = R.drawable.ic_map_8; break;
                case 9: icon = R.drawable.ic_map_9; break;
                case 10: icon = R.drawable.ic_map_10; break;
                case 11: icon = R.drawable.ic_map_11; break;
                case 12: icon = R.drawable.ic_map_12; break;
                case 13: icon = R.drawable.ic_map_13; break;
                case 14: icon = R.drawable.ic_map_14; break;
                case 15: icon = R.drawable.ic_map_15; break;
            }
            annotation = new MapAnnotation(x,y,icon,data[0]);
        } catch (Exception e){
            // encounter issue when parsing
            e.printStackTrace();
            return null;
        }

        return annotation;
    }

    @Override
    public int compareTo(MapAnnotation o) {
        try{
            Integer id1 = Integer.valueOf(this.getName());
            Integer id2 = Integer.valueOf(o.getName());
            return id1.compareTo(id2);
        } catch (Exception e){
            return this.getName().compareTo(o.getName());
        }
    }
}
