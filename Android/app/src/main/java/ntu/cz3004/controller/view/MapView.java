package ntu.cz3004.controller.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.HashMap;

import app.entity.Map;
import app.entity.MapAnnotation;
import app.util.Utility;
import ntu.cz3004.controller.R;


/**
 * MapView display 2D presentation of map, which includes following
 * <ul>
 *     <li>display grid for map and each cell's status (unexplored/explore/obstacle)</li>
 *     <li>display of annotation like robot, way-point, detected images</li>
 *     <li>display label for row and column, highlight the label when selected</li>
 *     <li>allow cell selection and highlight
 *       <ul>
 *           <li>highlight only cell</li>
 *           <li>highlight cell and sounding</li>
 *           <li>highlight to the axis</li>
 *       </ul>
 *     </li>
 *     <li>allow drag and drop for annotation</li>
 * </ul>
 * @see Map
 * @see MapAnnotation
 */
public class MapView extends LinearLayout {
    private static String TAG = "mdp.view.map_view";
    public static final int FLAG_NONE = 0;
    public static final int FLAG_COLOR_VALID = 1;
    public static final int FLAG_COLOR_WARN = 1<<1;
    public static final int FLAG_HIGHLIGHT_CELL = 1<<2;
    public static final int FLAG_HIGHLIGHT_SURROUNDING = 1<<3;
    public static final int FLAG_HIGHLIGHT_TO_AXIS = 1<<4;
    private Point selection = null;
    private int flagSelection = FLAG_NONE;
    private Map map = null;
    private MapAnnotation endPoint;
    private HashMap<String, ImageView> imgLookup;
    private Paint paintLabel;


    public MapView(Context context) {
        super(context);
        init();
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        // initialise
        setOrientation(LinearLayout.VERTICAL);
        imgLookup = new HashMap<>();
        endPoint = new MapAnnotation( Map.MAX_COL-2, Map.MAX_ROW-2,"End point");

        // allow custom drawing for row and col labeling
        setWillNotDraw(false);
        paintLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLabel.setColor(Color.WHITE);
        paintLabel.setStrokeWidth(0.1f);
        paintLabel.setTextSize(20);

        createGrid();
    }

    /**
     * Preparation of grid that represent the map, bottom left is origin point
     * <ul>
     *     <li>position can be derived from id. eg.
     *          <p>{@code int row = cell.getId()/Map.MAX_COL}</p>
     *          <p>{@code int col = cell.getId()%Map.MAX_COL}</p>
     *     </li>
     *     <li>each cell has position added as tag</li>
     * </ul>
     */
    private void createGrid(){
        removeAllViews();
        for (int r = 0; r<Map.MAX_ROW; r++){
            LinearLayout row = new LinearLayout(getContext());
            row.setId(r);
            row.setWeightSum(1f);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0, 1f/Map.MAX_ROW));
            row.setOrientation(LinearLayout.HORIZONTAL);
            addView(row, 0);
            for (int c = 0; c<Map.MAX_COL; c++){
                FrameLayout cell = new FrameLayout(getContext());
                LinearLayout.LayoutParams layoutParams = new LayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f/Map.MAX_COL));
                layoutParams.setMargins(1,1,1,1);
                cell.setId(r*Map.MAX_COL+c);
                cell.setLayoutParams(layoutParams);
                cell.setBackgroundColor(Color.GRAY);
                cell.setTag(new Point(c, r));
                row.addView(cell);
            }
        }
    }

    public Map getMap(){
        return this.map;
    }

    public void setMap(Map map){
        clear();
        this.map = map;
        invalidate();
    }

    /**
     * {@inheritDoc}
     * <p>Setting OnClickListener to each cell</p>
     */
    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        for (int r = 0; r<Map.MAX_ROW; r++){
            for (int c = 0; c<Map.MAX_COL; c++){
                getCell(r,c).setOnClickListener(listener);
            }
        }
    }

    /**
     * getter for row on the map
     * @param row index of row, range = [0, {@link Map#MAX_ROW}-1]
     * @return {@link LinearLayout} that represent the row given row number
     */
    private LinearLayout getRow(int row){
        return (LinearLayout) findViewById(row);
    }

    /**
     * getter for cell on the map
     * @param row index of row, range = [0, {@link Map#MAX_ROW}-1]
     * @param col index of column, range = [0, {@link Map#MAX_COL}-1]
     * @return {@link FrameLayout} that represent the cell of given position.
     */
    private FrameLayout getCell(int row, int col){
        LinearLayout layoutRow = getRow(row);
        return (FrameLayout) layoutRow.getChildAt(col);
    }

    /**
     * getter to get selected cell
     * @return {@link Point} of cell selected
     */
    public Point getSelection(){
        return selection;
    }

    /**
     * select and highlight a cell with given position and highlight flags
     * @param position position to be highlighted
     * @param flags flags represent how selection being highlighted
     * @see #updateSelectionHighlight()
     * @see #FLAG_NONE
     * @see #FLAG_COLOR_VALID
     * @see #FLAG_COLOR_WARN
     * @see #FLAG_HIGHLIGHT_CELL
     * @see #FLAG_HIGHLIGHT_SURROUNDING
     * @see #FLAG_HIGHLIGHT_TO_AXIS
     */
    public void setSelection(Point position, int flags){
        this.selection = position;
        this.flagSelection = flags;
        invalidate();
    }

    /**
     * overriding method for {@link #setSelection(Point, int)}
     * @param position position to be highlighted, null to remove selection
     */
    public void setSelection(Point position) {
        if (position != null){
            setSelection(position, FLAG_HIGHLIGHT_CELL);
        }  else {
            setSelection(null, FLAG_NONE);
        }
    }


    public void clear(){
        // clearing image lookup
        for(String key:imgLookup.keySet()){
            ImageView imgView = imgLookup.get(key);
            if (imgView!=null){
                FrameLayout cell = (FrameLayout) imgView.getParent();
                if (cell != null){
                    cell.removeView(imgView);
                }
            }
        }
        imgLookup.clear();
    }


    @Override
    public void invalidate() {
        super.invalidate();
        update();
    }

    /**
     * update the map presentation based on {@link Map map} status
     * @see #updateGrid()
     * @see #updateAnnotations()
     * @see #updateSelectionHighlight()
     */
    private void update(){
        if (map == null){
            return;
        }
        updateGrid();
        updateAnnotations();
        updateSelectionHighlight();
    }

    /**
     * preparation of {@link ImageView} that represent an {@link MapAnnotation annotation}
     * @param annotation annotation like robot, way-point or recognised images
     * @return {@link ImageView} to represent annotation
     * @see #updateAnnotations()
     * @see MapAnnotation
     */
    private ImageView prepareAnnotationImage(MapAnnotation annotation){
        if (!annotation.hasIcon()){
            return null;
        }
        ImageView imgView = imgLookup.get(annotation.getName());
        // create image view
        if (imgView == null){
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            imgView = new ImageView(getContext());
            imgView.setLayoutParams(params);
            imgView.setImageResource(annotation.getIcon());
            imgView.setImageResource(annotation.getIcon());
//            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            imgLookup.put(annotation.getName(), imgView);
        }
        imgView.setTag(annotation);
        // update img view position on grid
        FrameLayout cell = getCell(annotation.getY(), annotation.getX());
        cell.addView(imgView);
        return imgView;
    }

    /**
     * based on {@link Map map} status update each cell's exploration and obstacle status
     * @see Map#STATE_UNEXPLORED
     * @see Map#STATE_EXPLORED
     * @see Map#STATE_OBSTACLE
     */
    private void updateGrid(){
        FrameLayout cell = null;
        for (int r = 0; r<Map.MAX_ROW; r++){
            for (int c = 0; c<Map.MAX_COL; c++){
                cell = getCell(r,c);
                cell.getBackground().clearColorFilter();
                cell.removeAllViews();
                cell.invalidate();
                switch (map.getStateAt(r,c)){
                    case Map.STATE_EXPLORED: cell.setBackgroundColor(Color.WHITE);break;
                    case Map.STATE_OBSTACLE: cell.setBackgroundColor(Color.BLACK);break;
                    default: cell.setBackgroundColor(Color.GRAY); break;
                }
            }
        }
    }

    /**
     * Based on {@link Map map}'s annotations, placing each annotation to respective cell
     * @see #prepareAnnotationImage(MapAnnotation)
     */
    private void updateAnnotations(){
        changeSurroundingColor(endPoint.getY(), endPoint.getX(), ContextCompat.getColor(getContext(), R.color.primaryColor));
        if (map!=null){
            ImageView imgView;
            // draw robot
            imgView = prepareAnnotationImage(map.getRobot());
            imgView.setRotation(map.getRobot().getDirection());
            changeSurroundingColor(map.getRobot().getY(), map.getRobot().getX(), ContextCompat.getColor(getContext(), R.color.yellowLight) );
            // draw way point
            if (map.getWayPoint()!=null){
                prepareAnnotationImage(map.getWayPoint());
                //changeSurroundingColor(map.getWayPoint().getY(), map.getWayPoint().getX(), ContextCompat.getColor(getContext(), R.color.yellowLight) );
            }

            if (map.getImages().size()>0){
                HashMap<String, MapAnnotation> images = map.getImages();
                for (String imgId: images.keySet()){
                    prepareAnnotationImage(images.get(imgId));
                }
            }
        }
    }

    /**
     * highlight to show selection of cell
     * @see #setSelection(Point, int)
     */
    private void updateSelectionHighlight(){
        if (selection == null ){
            return;
        } else {
            if (flagSelection == FLAG_NONE){
                return;
            }
            int color = ContextCompat.getColor(getContext(), R.color.transparentYellow);
            if (Utility.bitwiseCompare(flagSelection, FLAG_COLOR_WARN)){
                color = ContextCompat.getColor(getContext(), R.color.transparentRed);
            }
            if (Utility.bitwiseCompare(flagSelection, FLAG_HIGHLIGHT_CELL)){
                highlightCell(selection.y, selection.x, color);
            }
            if (Utility.bitwiseCompare(flagSelection, FLAG_HIGHLIGHT_SURROUNDING)){
                highlightSurrounding(selection.y, selection.x, color);
            }
            if (Utility.bitwiseCompare(flagSelection, FLAG_HIGHLIGHT_TO_AXIS)){
                highlightToAxis(selection.y, selection.x, color);
            }
        }
    }


    /**
     * given a position, change the background color of cell and it's surrounding
     * @param row row of center position
     * @param col column of center position
     * @param color new color for the cell
     */
    private void changeSurroundingColor(int row, int col, int color){
        for (int r=row-1; r<=row+1;r++){
            for (int c=col-1; c<=col+1;c++){
                try {
                    getCell(r, c).setBackgroundColor( color );
                }catch (Exception e){
                    // prevent from index out of bound
                }
            }
        }
    }

    /**
     * given a position, highlight the cell by apply color filter to cell's background
     * @param row row of cell to be highlight
     * @param col column of cell to be highlight
     * @param color color of filter to be applied
     */
    private void highlightCell(int row, int col, int color) {
        try {
            FrameLayout cell = getCell(row, col);
            cell.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            cell.invalidate();
        }catch (Exception e) { }
    }

    /**
     * given a position, highlight the cell and it's surrounding cells
     * @param row row of center position
     * @param col column of center position
     * @param color color of highlight
     * @see #highlightCell(int, int, int)
     */
    private void highlightSurrounding(int row, int col, int color){
        for (int r=row-1; r<=row+1;r++){
            for (int c=col-1; c<=col+1;c++){
                highlightCell(r, c, color);
            }
        }
    }

    /**
     * given a postion, highlight cell to both vertical and horizontal axis
     * @param row row of center position
     * @param col column of center position
     * @param color color of highlight
     * @see #highlightCell(int, int, int)
     */
    private void highlightToAxis(int row, int col, int color){
        for (int r=0; r<=row;r++){
            highlightCell(r, col, color);
        }
        for (int c=0; c<=col;c++){
            highlightCell(row, c, color);
        }
    }

    /**
     * {@inheritDoc}
     * label of row and column number around the map
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight() - getPaddingTop()   - getPaddingBottom();
        int width  = getWidth()  - getPaddingStart() - getPaddingEnd();
        int cellHeight = height / Map.MAX_ROW;
        int cellWidth  = width  / Map.MAX_COL;
        Rect bounds = new Rect();
        int x, y, x2, y2;
        String text;
        paintLabel.setColor(Color.WHITE);

        // draw row labels
        x = getPaddingStart();
        x2 = getWidth()-getPaddingEnd();
        y = getPaddingTop()+cellHeight;
        for (int r=0;r<Map.MAX_ROW;r++){
            if (selection != null){
                paintLabel.setColor((Map.MAX_ROW-r-1)==selection.y?Color.YELLOW:Color.WHITE );
            }
            text = ""+(Map.MAX_ROW-r-1);
            paintLabel.getTextBounds(text, 0, text.length(), bounds);
            canvas.drawText(text, x-bounds.width()-5, y-(cellHeight-bounds.height())/2, paintLabel );
            canvas.drawText(text, x2+5, y-(cellHeight-bounds.height())/2, paintLabel );
            y+=cellHeight;
        }

        // draw col labels
        x = getPaddingLeft();
        y = getHeight()-getPaddingBottom();
        y2 = getPaddingTop();
        for (int c=0;c<Map.MAX_COL;c++){
            if (selection != null){
                paintLabel.setColor(c==selection.x?Color.YELLOW:Color.WHITE );
            }
            text = ""+c;
            paintLabel.getTextBounds(text, 0, text.length(), bounds);
            canvas.drawText(text, x+(cellWidth-bounds.width())/2, y+bounds.height()+5, paintLabel );
            canvas.drawText(text, x+(cellWidth-bounds.width())/2, y2-5, paintLabel );
            x+=cellWidth;
        }
        // To be fix: calculation of space is wrong as margin of each cell is not considered
    }
}
