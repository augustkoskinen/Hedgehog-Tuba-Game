package com.hedtub.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class GameScreenOnline implements Screen {
    //320, 160 spawn
    private OrthographicCamera cam;
    private OnlineManager manager;
    private MainMenuScreen game;
    private OnlineManager.Player player;
    private SpriteBatch batch;
    private Texture background;
    private Texture blackpix;
    private ArrayList<FrameworkMO.TextureSet> mapTextures = new ArrayList<>();
    public GameScreenOnline (OnlineManager manager, MainMenuScreen game) {
        this.game = game;
        this.manager = manager;

        for (int i = 0; i<manager.PlayerList.size();i++) {
            manager.PlayerList.get(i).healthwheel = new FrameworkMO.AnimationSet("p"+manager.PlayerList.get(i).skintype+"h.png",9,1,0.1f);

            float addx = 160;
            float addy = 80;
            if(manager.seed.nextInt(2)*2==0) {
                addx = manager.WORLD_WIDTH*manager.TILE_WIDTH - 160;
            }
            if(manager.seed.nextInt(2)*2==0) {
                addy = manager.WORLD_HEIGHT*manager.TILE_WIDTH - 80;
            }
            manager.PlayerList.get(i).sprite.setPosition(addx,addy);
        }

        player = manager.PlayerList.get(0);

        batch = new SpriteBatch();

        // cam setup
        cam = new OrthographicCamera();
        cam.setToOrtho(false, (manager.WORLD_WIDTH*manager.TILE_WIDTH), (manager.WORLD_HEIGHT*manager.TILE_WIDTH));
        cam.position.set(new Vector3(player.sprite.x, player.sprite.y, 0));
        background = new Texture("background.png");
        blackpix = new Texture("blackpix.png");

        TextureRegion maptext;
        for(int i = 0; i<manager.WORLD_MAP.length;i++) {
            for (int j = 0; j < manager.WORLD_MAP[i].length; j++) {
                maptext = FrameworkMO.getSpriteTilemap(manager.WORLD_MAP, i, j, manager.WORLD_MAP.length, manager.WORLD_MAP[i].length);
                if (maptext != null) {
                    mapTextures.add(new FrameworkMO.TextureSet(maptext, i * manager.TILE_WIDTH, j * manager.TILE_WIDTH, j * manager.TILE_WIDTH));
                }
            }
        }


        manager.countopen = 0;
        manager.gamecam = cam;
        manager.gamestarted = true;
    }

    @Override
    public void render(float delta) {
        //clears screen
        ScreenUtils.clear(0, 0, 0, 1, true);

        Vector3 movecampos = manager.PlayerList.get(0).sprite.getPosition();
        float camdis = MovementMath.pointDis(cam.position, new Vector3(movecampos.x, movecampos.y, 0));
        float camdir = MovementMath.pointDir(cam.position, new Vector3(movecampos.x, movecampos.y, 0));
        Vector3 campos = MovementMath.lengthDir(camdir, camdis);
        cam.position.set(cam.position.x + campos.x * .05f + manager.loadjiggle.x, cam.position.y + campos.y * .05f + manager.loadjiggle.y, 0);
        cam.zoom = (0.9f);

        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();
        batch.draw(background,0,0,manager.WORLD_WIDTH*manager.TILE_WIDTH,manager.WORLD_HEIGHT*manager.TILE_WIDTH);
        for(int i = 0; i<mapTextures.size(); i++)
            batch.draw(mapTextures.get(i).texture,mapTextures.get(i).x,mapTextures.get(i).y);

        for(int i =0; i<manager.PlayerList.size();i++) {
            OnlineManager.Player curplayer = manager.PlayerList.get(i);
            TextureRegion playertext;
            if(i==0) {
                curplayer.updateControls();
                playertext = curplayer.updatePlayerPos();
            } else {
                playertext = curplayer.getPlayerText();
            }
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

        for(int i = 0; i<manager.PoofCloudList.size();i++) {
            FrameworkMO.TextureSet text = manager.PoofCloudList.get(i).updateTime();
            if(text!=null) {
                batch.draw(text.texture,text.x,text.y,text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,text.texture.getRegionWidth(),text.texture.getRegionHeight(),1,1,(float) Math.toDegrees(text.rotation));
            } else
                i--;
        }

        ArrayList<Rectangle> MonsterCollisionList = new ArrayList<>();
        for(int i = 0; i<manager.BulletList.size();i++) {
            FrameworkMO.TextureSet text = manager.BulletList.get(i).updateTime();
            if(text!=null) {
                batch.draw(text.texture,text.x,text.y,text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,text.texture.getRegionWidth(),text.texture.getRegionHeight(),1,1,(float) Math.toDegrees(text.rotation));
                MonsterCollisionList.add(manager.BulletList.get(i).collision);
            } else
                i--;
        }

        int colind;

        batch.draw(blackpix,-96,0,96,manager.WORLD_HEIGHT*manager.TILE_WIDTH);
        batch.draw(blackpix,manager.WORLD_WIDTH*manager.TILE_WIDTH,0,96,manager.WORLD_HEIGHT*manager.TILE_WIDTH);
        batch.draw(blackpix,0,-96,manager.WORLD_WIDTH*manager.TILE_WIDTH,96);
        batch.draw(blackpix,0,manager.WORLD_HEIGHT*manager.TILE_WIDTH,manager.WORLD_WIDTH*manager.TILE_WIDTH,96);

        for(int i = 0; i< manager.DeletedPlayerList.size();i++){
            manager.DeletedPlayerList.get(i).countDead();
        }

        batch.end();

        //ui
        manager.batch.begin();
        if(manager.gamestarted)
            for(int i =0; i<manager.PlayerList.size();i++) {
                OnlineManager.Player curplayer = manager.PlayerList.get(i);
                manager.batch.draw(manager.healthback,16+i*72*manager.CHANGE_RATIO,manager.WIND_HEIGHT-16-64*manager.CHANGE_RATIO,64*manager.CHANGE_RATIO,64*manager.CHANGE_RATIO);
                manager.batch.draw(curplayer.healthwheel.getAnim(),16+i*72*manager.CHANGE_RATIO,manager.WIND_HEIGHT-16-64*manager.CHANGE_RATIO,64*manager.CHANGE_RATIO,64*manager.CHANGE_RATIO);
            }

        if(manager.countopen < 0.875) {
            manager.countopen+= Gdx.graphics.getDeltaTime();
            manager.batch.draw(manager.openbattle.updateTime(1),0,0,manager.WIND_WIDTH,manager.WIND_HEIGHT);
        }

        if(manager.waitstart < 2&&manager.gamestarted) {
            manager.waitstart += Gdx.graphics.getDeltaTime();
        } else if(manager.gamestarted) {
            if(manager.startanim.time+Gdx.graphics.getDeltaTime()<manager.startanim.framereg*3)
                manager.batch.draw(manager.startanim.updateTime(1),manager.WIND_WIDTH/2-64*manager.CHANGE_RATIO,manager.WIND_HEIGHT/2-64*manager.CHANGE_RATIO,128*manager.CHANGE_RATIO,128*manager.CHANGE_RATIO);
            else
                manager.pause = false;
        }

        manager.updateShake();

        manager.batch.end();
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
