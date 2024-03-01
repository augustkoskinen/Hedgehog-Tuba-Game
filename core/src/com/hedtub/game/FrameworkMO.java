package com.hedtub.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import java.util.ArrayList;

public class FrameworkMO {
    public static void DrawWithLayering(SpriteBatch spritebatch, ArrayList<TextureSet> list) {
        ArrayList<TextureSet> textlist = new ArrayList<TextureSet>();
        for (TextureSet textureSet : list) {
            float tempdepth = textureSet.depth;
            int ind = textlist.size();
            while (ind > 0 && tempdepth < textlist.get(ind - 1).depth) {
                ind--;
            }
            ind = Math.max(0, ind);
            textlist.add(ind, textureSet);
        }
        spritebatch.begin();
        for (int i = 0; i < list.size(); i++) {
            if(textlist.get(i).texture!=null)
                spritebatch.draw(textlist.get(i).texture, textlist.get(i).x, textlist.get(i).y, textlist.get(i).texture.getRegionWidth() / 2f, textlist.get(i).texture.getRegionHeight() / 2f, textlist.get(i).texture.getRegionWidth(), textlist.get(i).texture.getRegionHeight(), 1, 1, (float) Math.toDegrees(textlist.get(i).rotation));
        }
        spritebatch.end();
    }

    public static class SpriteObjectSqr {
        Texture texture;
        Rectangle collision = new Rectangle();
        float x = 0;
        float y = 0;
        float depth = 0;
        float coloffx = 0;
        float coloffy = 0;

        public SpriteObjectSqr(String texturepath, float xpos, float ypos, float colwidth, float colheight, float coladdx, float coladdy, boolean collides) {
            if (texturepath.isEmpty()) {
                texture = new Texture(Gdx.files.internal("empty.png"));
            } else {
                texture = new Texture(Gdx.files.internal(texturepath));
            }
            x = xpos;
            y = ypos;
            this.depth = ypos;
            collision.x = xpos;
            collision.y = ypos;
            if (collides) {
                collision.height = colheight;
                collision.width = colwidth;
                collision.x += coladdx;
                collision.y += coladdy;
                coloffx = coladdx;
                coloffy = coladdy;
            }
        }

        public void setPosition(float xpos, float ypos) {
            collision.setPosition(xpos + coloffx, ypos + coloffy);
            x = xpos;
            y = ypos;
        }

        public Vector3 getPosition() {
            return new Vector3(x, y, 0);
        }

        public void addPosition(Vector3 movevect) {
            collision.setPosition(x + movevect.x+coloffx, y + movevect.y+coloffy);
            x = x + movevect.x;
            y = y + movevect.y;
        }

        public void changeTexture(String texturepath){
            texture = new Texture(Gdx.files.internal(texturepath));
        }
    }

    public static class AnimationSet {
        Animation<TextureRegion> animation;
        Texture sheet;
        float time;
        boolean repeat = true;
        String textpath;
        int rows;
        int cols;
        float framereg;

        public AnimationSet(String textpath, int cols, int rows, float framereg) {
            sheet = new Texture(Gdx.files.internal(textpath));
            this.textpath = textpath;
            this.rows = rows;
            this.cols = cols;
            this.framereg = framereg;

            TextureRegion[][] tmp = TextureRegion.split(sheet,
                    sheet.getWidth() / cols,
                    sheet.getHeight() / rows
            );

            TextureRegion[] walkFrames = new TextureRegion[cols * rows];
            int index = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    walkFrames[index++] = tmp[i][j];
                }
            }

            animation = new Animation<TextureRegion>(framereg, walkFrames);

            time = 0f;
        }

        public AnimationSet(String textpath, int cols, int rows, float framereg, boolean irepeat) {
            sheet = new Texture(Gdx.files.internal(textpath));
            this.textpath = textpath;
            this.rows = rows;
            this.cols = cols;
            this.framereg = framereg;
            repeat = irepeat;

            TextureRegion[][] tmp = TextureRegion.split(sheet,
                    sheet.getWidth() / cols,
                    sheet.getHeight() / rows
            );

            TextureRegion[] walkFrames = new TextureRegion[cols * rows];
            int index = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    walkFrames[index++] = tmp[i][j];
                }
            }

            animation = new Animation<TextureRegion>(framereg, walkFrames);

            time = 0f;
        }

        public TextureRegion updateTime(float speed) {
            time += Gdx.graphics.getDeltaTime()*speed;
            return animation.getKeyFrame(time, repeat);
        }
    }

    public static class TextureSet {
        TextureRegion texture;
        AnimationSet animationtexture;
        boolean isanimated = false;
        float x;
        float y;
        float rotation;
        float depth;

        public TextureSet(TextureRegion text, float xpos, float ypos, float depth) {
            texture = text;
            x = xpos;
            y = ypos;
            rotation = 0;
            this.depth = depth;
        }

        public TextureSet(TextureRegion text, float xpos, float ypos, float depth, float rot) {
            texture = text;
            x = xpos;
            y = ypos;
            rotation = rot;
            this.depth = depth;
        }

        public TextureSet(String text, float xpos, float ypos, float depth, int cols, int rows, float framereg) {
            texture = new TextureRegion(new Texture(text));
            x = xpos;
            y = ypos;
            animationtexture = new AnimationSet(text, cols, rows, framereg);
            isanimated = true;
            rotation = 0;
            this.depth = depth;
        }

        public TextureSet(TextureSet text, float xpos, float ypos) {
            texture = text.texture;
            depth = text.depth;
            x = xpos;
            y = ypos;
            rotation = 0;
        }
    }

    public static class TransitionBox {
        Rectangle collision = new Rectangle();
        float rotation = 0;
        String room;
        float x;
        float y;
        float roomx = 0;
        float roomy = 0;

        public TransitionBox(float xpos, float ypos, float width, float height, float rot, String myroom, float iroomx, float iroomy) {
            collision.height = height;
            collision.width = width;
            collision.x = xpos;
            collision.y = ypos;
            x = xpos;
            y = ypos;
            rotation = rot;
            room = myroom;

            //add 7 to spawn in middle of tile
            roomx = iroomx;
            roomy = iroomy;
        }

        public void setPosition(float xpos, float ypos) {
            collision.setPosition(x + collision.width, y + collision.height);
            x = xpos;
            y = ypos;
        }

        public void addPosition(float addx, float addy) {
            collision.setPosition(collision.x + addx, collision.y + addy);
            x += addx;
            y += addy;
        }

        public void addX(float addx) {
            collision.x += addx;
            x += addx;
        }

        public void addY(float addy) {
            collision.y += addy;
            y += addy;
        }

        public void setX(float ix) {
            collision.x = ix + collision.width;
            x = ix;
        }

        public void setY(float iy) {
            collision.y = iy + collision.height;
            y = iy;
        }

        public Vector3 getPosition() {
            return new Vector3(x, y, 0);
        }
    }

    public static class Cutscene {
        public int type = 0;
        public float time = 0;
        public float maxtime = 0;
        public ArrayList<TextureSet> textures = new ArrayList<TextureSet>();
        public ArrayList<AnimationSet> animations = new ArrayList<AnimationSet>();
        public ArrayList<SpriteObjectSqr> sprites = new ArrayList<SpriteObjectSqr>();

        public Cutscene(int itype) {
            type = itype;
            switch (type) {
                case 0: {
                    maxtime = 0f;
                    break;
                }
            }
        }

        public void UpdateTime() {
            if (time <= maxtime) {
                switch (type) {
                    case 0: {
                        if (time < 0) {

                        }
                        break;
                    }
                }
            } else {
                //incutscene = false;
                //curcutscene = null;
            }
        }

        public void DrawSprites(SpriteBatch ibatch, ArrayList<TextureSet> textlist) {
            for (int i = 0; i < animations.size(); i++)
                //textlist.add(new TextureSet(animations.get(i).updateTime(),pastmonstpos.x-16,pastmonstpos.y-12,-pastmonstpos.y));

                DrawWithLayering(ibatch, textures);
        }
    }

    public static TextureRegion getSpriteTilemap(int[][] map, int x, int y, int maxx, int maxy){
        int type = map[x][y];
        switch(type){
            case 0: {
                return null;
            }
            case 1: {
                if(map[Math.max(x-1,0)][y]==1&&
                    map[Math.min(x+1,maxx-1)][y]!=1&&
                    map[x][Math.max(y-1,0)]==1) {
                    return new TextureRegion(new Texture("bricks/r.png"));
                }
                if(map[Math.max(x-1,0)][y]!=1&&
                    map[Math.min(x+1,maxx-1)][y]==1&&
                    map[x][Math.max(y-1,0)]==1) {
                    return new TextureRegion(new Texture("bricks/l.png"));
                }
                if(map[Math.max(x-1,0)][y]==1&&
                    map[Math.min(x+1,maxx-1)][y]==1&&
                    map[x][Math.max(y-1,0)]!=1) {
                    return new TextureRegion(new Texture("bricks/d.png"));
                }
                if(map[Math.max(x-1,0)][y]!=1&&
                    map[Math.min(x+1,maxx-1)][y]==1&&
                    map[x][Math.max(y-1,0)]!=1) {
                    return new TextureRegion(new Texture("bricks/dl.png"));
                }
                if(map[Math.max(x-1,0)][y]==1&&
                    map[Math.min(x+1,maxx-1)][y]!=1&&
                    map[x][Math.max(y-1,0)]!=1) {
                    return new TextureRegion(new Texture("bricks/dr.png"));
                }
                if(map[Math.max(x-1,0)][y]!=1&&
                    map[Math.min(x+1,maxx-1)][y]!=1&&
                    map[x][Math.max(y-1,0)]==1) {
                    return new TextureRegion(new Texture("bricks/lr.png"));
                }
                if(map[Math.max(x-1,0)][y]!=1&&
                    map[Math.min(x+1,maxx-1)][y]!=1&&
                    map[x][Math.max(y-1,0)]!=1) {
                    return new TextureRegion(new Texture("bricks/a.png"));
                }
                return new TextureRegion(new Texture("bricks/u.png"));
            }
            case 2: {
                return new TextureRegion(new Texture("bricks/back.png"));
            }
        }
        return null;
    }

    public static int[][] getMap() {
        int rand = (int)(Math.random()*2);

        switch(rand) {
            case 0 : {
                return new int[][] {
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 2, 2, 1, 1, 2, 2, 1, 1, 1, 1},
                    {1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 1},
                    {1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 1, 1, 2, 2, 2, 2, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 0, 0, 0, 2, 2, 1, 2, 2, 0, 0, 0, 1, 2, 2, 0, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 0, 0, 0, 0, 1, 2, 2, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 0, 0, 0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 0, 0, 2, 1, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 2, 1, 2, 0, 0, 0, 2, 2, 2, 2, 1, 2, 2, 2, 1},
                    {2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 2, 1, 2, 0, 0, 1, 1, 1, 2, 2, 1, 2, 2, 2, 2},
                    {2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 2, 1, 2, 0, 0, 0, 2, 0, 0, 2, 1, 2, 2, 2, 2},
                    {2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 2},
                    {1, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 1},
                    {1, 2, 2, 2, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 1, 1, 2, 0, 0, 0, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 1, 1, 1, 2, 2, 2, 2, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1},
                    {1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1},
                    {1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 1},
                    {1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 0, 2, 1, 2, 2, 2, 1, 1, 2, 2, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
                };
            }
            case 1 : {
                return new int[][] {
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
                };
            }
        }
        return new int[][]{{}};
    }
}