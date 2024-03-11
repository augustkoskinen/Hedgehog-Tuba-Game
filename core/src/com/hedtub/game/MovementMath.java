package com.hedtub.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.controllers.Controller;

import java.util.ArrayList;

import static com.badlogic.gdx.math.MathUtils.clamp;

public class MovementMath extends ApplicationAdapter {
    //trig

    //float cos functions
    public static float cosf(float f){
        return (float) Math.cos(f);
    }
    public static float sinf(float f){
        return (float) Math.sin(f);
    }

    //simple trig

    //gets position based on direction and magnitude
    static public Vector3 lengthDir(float direction, float length){
        return new Vector3(MovementMath.cosf(direction)*length,MovementMath.sinf(direction)*length,0);
    }

    //gets dir between points
    static public float pointDir(Vector3 pointa, Vector3 pointb){
        return (float) Math.atan2((pointb.y-pointa.y),(pointb.x-pointa.x));
    }

    //gets dist between points
    static public float pointDis(Vector3 pointa, Vector3 pointb){
        return (float) Math.sqrt(Math.pow(pointb.y-pointa.y,2)+Math.pow(pointb.x-pointa.x,2));
    }

    //gets mp of two points
    static public Vector3 midpoint(Vector3 pointa, Vector3 pointb){
        return new Vector3((pointa.x+pointb.x)/2,(pointa.y+pointb.y)/2,0);
    }

    static public int CheckCollisions(Rectangle colbox, ArrayList<Rectangle> collist){
        for(int i = 0; i< collist.size();i++){
            if(colbox!=collist.get(i)&&overlaps(colbox,collist.get(i))) {
                return i;
            }
        }
        return -1;
    }
    static public boolean CheckCollisions(int[][] map, Rectangle player, int extendamount){
         if (map[clamp((int) ((player.x + 16) / 32) + extendamount/2,0,map.length-1)][clamp((int) (player.y / 32) + extendamount/2,0,map[0].length-1)] !=0) {
             return true;
         }

         return false;
    }
    static public boolean CheckCollisions(int[][] map, Rectangle col, int extendamount, Vector3 offset, Vector3 bounds){
        if (map[clamp((int) ((col.x+offset.x+(16-bounds.x)) / 16) + extendamount/2,0,map.length-1)][clamp((int) ((col.y+offset.y+(16-bounds.y)) / 16) + extendamount/2,0,map[0].length-1)] == 1||
            map[clamp((int) ((col.x+offset.x+(16-bounds.x)) / 16) + extendamount/2,0,map.length-1)][clamp((int) ((col.y+offset.y+bounds.y) / 16) + extendamount/2,0,map[0].length-1)] == 1||
            map[clamp((int) ((col.x+offset.x+bounds.x) / 16) + extendamount/2,0,map.length-1)][clamp((int) ((col.y+offset.y+bounds.y) / 16) + extendamount/2,0,map[0].length-1)] == 1||
            map[clamp((int) ((col.x+offset.x+bounds.x) / 16) + extendamount/2,0,map.length-1)][clamp((int) ((col.y+offset.y+(16-bounds.y)) / 16) + extendamount/2,0,map[0].length-1)] == 1) {
            return true;
        }

        return false;
    }
    static public Rectangle DuplicateRect(Rectangle rect){
        return new Rectangle(rect.x,rect.y,rect.width,rect.height);
    }
    static public int toDegrees(Controller controller) {
        if(controller == null) {
            if (Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isKeyPressed(Input.Keys.D)) {
                return 315;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.W)) {
                return 45;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.A)) {
                return 135;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.S)) {
                return 225;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                return 0;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                return 90;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                return 180;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                return 270;
            }
        } else {
            boolean rightmove = ((double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d > .4);
            boolean leftmove = ((double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d < -.4);
            boolean upmove = ((double) Math.round((controller.getAxis(controller.getMapping().axisLeftY)) * 100d) / 100d < -.4);
            boolean downmove = ((double) Math.round((controller.getAxis(controller.getMapping().axisLeftY)) * 100d) / 100d > .4);

            if (upmove && rightmove) {
                return 315;
            }
            if (leftmove && upmove) {
                return 45;
            }
            if (downmove && leftmove) {
                return 135;
            }
            if (rightmove && downmove) {
                return 225;
            }
            if (upmove) {
                return 0;
            }
            if (leftmove) {
                return 90;
            }
            if (downmove) {
                return 180;
            }
            if (rightmove) {
                return 270;
            }
        }
        return -1;
    }

    //checks an overlap between a circle and a rectangle
    static public boolean overlaps(Circle circ, Rectangle rect){
        if(rect.width+rect.height<=0){
            return false;
        }
        float circledistx = Math.abs((circ.x+circ.radius) - (rect.x+rect.width/2));
        float circledisty = Math.abs((circ.y+circ.radius) - (rect.y+rect.width/2));

        if (circledistx > (rect.width/2 + circ.radius)||circledisty > (rect.height/2 + circ.radius)) { return false; }
        else if (circledistx <= (rect.width/2)||(circledisty <= (rect.height/2))) { return true; } 

        float cornerdist = (float) Math.pow(circledistx,2) + (float) Math.pow(circledisty,2);

        return (cornerdist <= Math.pow(circ.radius,2));
    }

    //checks an overlap between 2 circles
    static public boolean overlaps(Circle circ, Circle circ2){
        return (pointDis(new Vector3(circ.x+circ.radius, circ.y+circ.radius, 0),new Vector3(circ2.x+circ2.radius, circ2.y+circ2.radius, 0))<circ.radius+circ2.radius);
    }
    static public boolean overlaps (Rectangle r1, Rectangle r2) {
        return !(r1.x + r1.width < r2.x || r1.y + r1.height < r2.y || r1.x > r2.x + r2.width || r1.y > r2.y + r2.height);
    }

    //gets the slope of two points
    static public Vector3 getSlope(Vector3 pointa, Vector3 pointb){
        Vector3 slope = new Vector3(pointa.x-pointb.x, pointa.y-pointb.y, 0);
        return slope;
    }

    //checks if there's a circle between two points
    static public boolean lineCol(Vector3 pointa, Vector3 pointb, Circle circ){
        float rate = 10;
        float dist = pointDis(pointa, pointb);
        float repeat =(float)Math.ceil(dist/rate);
        Vector3 curpoint = pointa;
        Vector3 velocity = getSlope(pointa,pointb);
        velocity.x /= repeat;
        velocity.y /= repeat;
        for(int i = 0; i<repeat;i++){
            if(pointDis(curpoint, new Vector3(circ.x+circ.radius, circ.y+circ.radius, 0))<+circ.radius)
                return true;
            curpoint.x+=velocity.x;
            curpoint.y+=velocity.y;
        }

        return false;
    }

    static public Vector3 averagePos(ArrayList<OfflineManager.Player> playerlist) {
        float sumx = 0;
        float sumy = 0;
        for(int i  = 0; i<playerlist.size();i++) {
            sumx+=playerlist.get(i).sprite.x+8;
            sumy+=playerlist.get(i).sprite.y+8;
        }
        return new Vector3(sumx/playerlist.size(),sumy/playerlist.size(),0);
    }
    static public Vector3 averagePosOnline(ArrayList<OnlineManager.Player> playerlist) {
        float sumx = 0;
        float sumy = 0;
        for(int i  = 0; i<playerlist.size();i++) {
            sumx+=playerlist.get(i).sprite.x+8;
            sumy+=playerlist.get(i).sprite.y+8;
        }
        return new Vector3(sumx/playerlist.size(),sumy/playerlist.size(),0);
    }
    static public float furthestDist(ArrayList<OfflineManager.Player> playerlist) {
        float furthestdist = 0;
        for(int i  = 0; i<playerlist.size()-1;i++)
            for (int j = i+1; j < playerlist.size(); j++) {
                float dist = pointDis(playerlist.get(i).sprite.getPosition(),playerlist.get(j).sprite.getPosition());
                if (dist > furthestdist)
                    furthestdist = dist;
            }

        return furthestdist;
    }
    static public float furthestDistOnline(ArrayList<OnlineManager.Player> playerlist) {
        float furthestdist = 0;
        for(int i  = 0; i<playerlist.size()-1;i++)
            for (int j = i+1; j < playerlist.size(); j++) {
                float dist = pointDis(playerlist.get(i).sprite.getPosition(),playerlist.get(j).sprite.getPosition());
                if (dist > furthestdist)
                    furthestdist = dist;
            }

        return furthestdist;
    }
    /*
    static boolean polygonIntersection(Polygon a, Polygon b)
    {
        for (int x=0; x<2; x++)
        {
            Polygon polygon = (x==0) ? a : b;

            for (int i1=0; i1<polygon.npoints; i1++)
            {
                int   i2 = (i1 + 1) % polygon.npoints;
                Point p1 = new Point(polygon.xpoints[i1],polygon.ypoints[i1]);
                Point p2 = new Point(polygon.xpoints[i2],polygon.ypoints[i2]);

                Point normal = new Point(p2.y - p1.y, p1.x - p2.x);

                double minA = Double.POSITIVE_INFINITY;
                double maxA = Double.NEGATIVE_INFINITY;

                for (int i = 0; i < a.npoints; i++)
                {
                    double projected = normal.x * a.xpoints[i] + normal.y * a.ypoints[i];

                    if (projected < minA)
                        minA = projected;
                    if (projected > maxA)
                        maxA = projected;
                }

                double minB = Double.POSITIVE_INFINITY;
                double maxB = Double.NEGATIVE_INFINITY;

                for (int i = 0; i < b.npoints; i++) {
                    double projected = normal.x * b.xpoints[i] + normal.y * b.xpoints[i];

                    if (projected < minB)
                        minB = projected;
                    if (projected > maxB)
                        maxB = projected;
                }

                if (maxA < minB || maxB < minA)
                    return false;
            }
        }

        return true;
    }
    */
}