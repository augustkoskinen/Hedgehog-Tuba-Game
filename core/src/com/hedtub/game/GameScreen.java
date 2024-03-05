package com.hedtub.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class GameScreen implements Screen {
    //320, 160 spawn
    private OrthographicCamera cam;
    private HedTub game;
    private HedTub.Player player;
    private SpriteBatch batch;
    private Texture background;
    private Texture blackpix;
    private ArrayList<FrameworkMO.TextureSet> mapTextures = new ArrayList<>();
    public GameScreen (HedTub game) {
        this.game = game;

        for (int i = 0; i<game.PlayerList.size();i++) {
            game.PlayerList.get(i).healthwheel = new FrameworkMO.AnimationSet("p"+game.PlayerList.get(i).skintype+"h.png",9,1,0.1f);

            float addx = 160;
            float addy = 80;
            if((int)(Math.random()*2)==0) {
                addx = game.WORLD_WIDTH*game.TILE_WIDTH - 160;
            }
            if((int)(Math.random()*2)==0) {
                addy = game.WORLD_HEIGHT*game.TILE_WIDTH - 80;
            }
            game.PlayerList.get(i).sprite.setPosition(addx,addy);
        }

        player = game.PlayerList.get(0);

        batch = new SpriteBatch();

        // cam setup
        cam = new OrthographicCamera();
        cam.setToOrtho(false, (game.WORLD_WIDTH*game.TILE_WIDTH), (game.WORLD_HEIGHT*game.TILE_WIDTH));
        cam.position.set(new Vector3(player.sprite.x, player.sprite.y, 0));
        background = new Texture("background.png");
        blackpix = new Texture("blackpix.png");

        TextureRegion maptext;
        for(int i = 0; i<game.WORLD_MAP.length;i++) {
            for (int j = 0; j < game.WORLD_MAP[i].length; j++) {
                maptext = FrameworkMO.getSpriteTilemap(game.WORLD_MAP, i, j, game.WORLD_MAP.length, game.WORLD_MAP[i].length);
                if (maptext != null) {
                    mapTextures.add(new FrameworkMO.TextureSet(maptext, i * game.TILE_WIDTH, j * game.TILE_WIDTH, j * game.TILE_WIDTH));
                }
            }
        }


        game.countopen = 0;
        game.gamecam = cam;
        game.gamestarted = true;

        //for(int i = 0; i<10; i++)
        //game.MonsterList.add(new HedTub.Monster(new Vector3((int)(Math.random()*game.WORLD_WIDTH*32),(int)(Math.random()*game.WORLD_HEIGHT*32),0),0));
    }

    @Override
    public void render(float delta) {
        //clears screen
        ScreenUtils.clear(0, 0, 0, 1, true);

        Vector3 movecampos = MovementMath.averagePos(game.PlayerList);
        float camdis = MovementMath.pointDis(cam.position, new Vector3(movecampos.x, movecampos.y, 0));
        float camdir = MovementMath.pointDir(cam.position, new Vector3(movecampos.x, movecampos.y, 0));
        Vector3 campos = MovementMath.lengthDir(camdir, camdis);
        cam.position.set(cam.position.x + campos.x * .05f + game.loadjiggle.x, cam.position.y + campos.y * .05f + game.loadjiggle.y, 0);
        cam.zoom = (cam.zoom+Math.max(0.9f, MovementMath.furthestDist(game.PlayerList) / 550))/2;
        //cam.position.set((game.WORLD_WIDTH*game.TILE_WIDTH)/2, (game.WORLD_HEIGHT*game.TILE_WIDTH)/2, 0);

        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();
        batch.draw(background,0,0,game.WORLD_WIDTH*game.TILE_WIDTH,game.WORLD_HEIGHT*game.TILE_WIDTH);
        for(int i = 0; i<mapTextures.size(); i++)
            batch.draw(mapTextures.get(i).texture,mapTextures.get(i).x,mapTextures.get(i).y);

        for(int i =0; i<game.numplayers;i++) {
            HedTub.Player curplayer = game.PlayerList.get(i);
            curplayer.updateControls();
            TextureRegion playertext = curplayer.updatePlayerPos();
            curplayer.sprite.depth = curplayer.sprite.y;
            if (curplayer.jumpdir != 0) {
                curplayer.jumpdir += curplayer.jumpdiradd;
                if (curplayer.jumpdir == 0) {
                    curplayer.jumpdiradd = 0;
                }
            }
            batch.draw(playertext,curplayer.sprite.x, curplayer.sprite.y,playertext.getRegionWidth()/2,playertext.getRegionHeight()/2,playertext.getRegionWidth(),playertext.getRegionHeight(),1,1,(float)curplayer.moverot);
            FrameworkMO.TextureSet eyetext = curplayer.getEyeText();
            batch.draw(eyetext.texture,eyetext.x,eyetext.y,eyetext.texture.getRegionWidth()/2,eyetext.texture.getRegionHeight()/2,eyetext.texture.getRegionWidth(),eyetext.texture.getRegionHeight(),1,1,(float) Math.toDegrees(eyetext.rotation));
        }

        for(int i = 0; i<game.PoofCloudList.size();i++) {
            FrameworkMO.TextureSet text = game.PoofCloudList.get(i).updateTime();
            if(text!=null) {
                batch.draw(text.texture,text.x,text.y,text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,text.texture.getRegionWidth(),text.texture.getRegionHeight(),1,1,(float) Math.toDegrees(text.rotation));
            } else
                i--;
        }

        ArrayList<Rectangle> MonsterCollisionList = new ArrayList<>();
        for(int i = 0; i<game.BulletList.size();i++) {
            FrameworkMO.TextureSet text = game.BulletList.get(i).updateTime();
            if(text!=null) {
                batch.draw(text.texture,text.x,text.y,text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,text.texture.getRegionWidth(),text.texture.getRegionHeight(),1,1,(float) Math.toDegrees(text.rotation));
                MonsterCollisionList.add(game.BulletList.get(i).collision);
            } else
                i--;
        }

        int colind;
        for(int i = 0; i<game.MonsterList.size();i++) {
            colind = MovementMath.CheckCollisions(game.MonsterList.get(i).collision,MonsterCollisionList);
            FrameworkMO.TextureSet text = game.MonsterList.get(i).updateTime(player.sprite.getPosition());

            if(text!=null) {
                if(colind!=-1) {
                    int bulletind = -1;
                    for(int j = 0; j < game.BulletList.size(); j++)
                        if(game.BulletList.get(j).collision.equals(MonsterCollisionList.get(colind)))
                            bulletind = j;
                    if (bulletind != -1) game.BulletList.get(bulletind).Remove();
                    game.MonsterList.get(i).takeDamage();
                }

                batch.draw(text.texture,text.x,text.y);
            } else
                i--;
        }

        batch.draw(blackpix,-96,0,96,game.WORLD_HEIGHT*game.TILE_WIDTH);
        batch.draw(blackpix,game.WORLD_WIDTH*game.TILE_WIDTH,0,96,game.WORLD_HEIGHT*game.TILE_WIDTH);
        batch.draw(blackpix,0,-96,game.WORLD_WIDTH*game.TILE_WIDTH,96);
        batch.draw(blackpix,0,game.WORLD_HEIGHT*game.TILE_WIDTH,game.WORLD_WIDTH*game.TILE_WIDTH,96);

        for(int i = 0; i< game.DeletedPlayerList.size();i++){
            game.DeletedPlayerList.get(i).countDead();
        }

        batch.end();
    }

    //necessary overrides
    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
