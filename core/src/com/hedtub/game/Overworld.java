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

public class Overworld implements Screen {
    private OrthographicCamera cam;
    private HedTub game;
    private HedTub.Player player;
    private SpriteBatch batch;
    private ArrayList<FrameworkMO.TextureSet> mapTextures = new ArrayList<>();
    public Overworld (HedTub game) {
        this.game = game;
        player = game.mainplayer;

        batch = new SpriteBatch();

        // cam setup
        cam = new OrthographicCamera();
        cam.setToOrtho(false, 1056, 1056);
        cam.position.set(new Vector3(player.sprite.x, player.sprite.y, 0));


        TextureRegion maptext;
        for(int i = 0; i<game.WORLD_MAP.length;i++) {
            for (int j = 0; j < game.WORLD_MAP[i].length; j++) {
                maptext = FrameworkMO.getSpriteTilemap(game.WORLD_MAP, i, j, game.WORLD_MAP.length, game.WORLD_MAP[i].length);
                if (maptext != null) {
                    mapTextures.add(new FrameworkMO.TextureSet(maptext, i * 32, j * 32, j * 32));
                }
            }
        }

        game.MonsterList.add(new HedTub.Monster(new Vector3(player.sprite.x,player.sprite.y,0),0));
    }

    @Override
    public void render(float delta) {

        //clears screen
        ScreenUtils.clear(0.415f, 0.764f, 0.851f, 1, true);
        TextureRegion playertext = player.updatePlayerPos();

        //updates cam position/visuals
        //float camdis = MovementMath.pointDis(cam.position, new Vector3(game.mainplayer.x + 16, game.mainplayer.y + 16, 0));
        //float camdir = MovementMath.pointDir(cam.position, new Vector3(game.mainplayer.x + 16, game.mainplayer.y + 16, 0));
        //Vector3 campos = MovementMath.lengthDir(camdir, camdis);
        //cam.position.set(cam.position.x + campos.x * .05f, cam.position.y + campos.y * .05f, 0);
        cam.position.set(528, 528, 0);
        cam.update();
        batch.setProjectionMatrix(cam.combined);


        //draws sprites
        ArrayList<FrameworkMO.TextureSet> textures = new ArrayList<>();

        player.sprite.depth= player.sprite.y;
        if(player.jumpdir!=0) {
            player.jumpdir+=player.jumpdiradd;
            if(player.jumpdir==0) {
                player.jumpdiradd = 0;
            }
        }
        textures.add(new FrameworkMO.TextureSet(playertext,player.sprite.x, player.sprite.y, player.sprite.depth,(float) Math.toRadians(player.moverot)));

        textures.addAll(mapTextures);

        for(int i = 0; i<game.PoofCloudList.size();i++) {
            FrameworkMO.TextureSet text = game.PoofCloudList.get(i).updateTime();
            if(text!=null) {
                textures.add(text);
            } else
                i--;
        }

        ArrayList<Rectangle> MonsterCollisionList = new ArrayList<>();
        for(int i = 0; i<game.BulletList.size();i++) {
            FrameworkMO.TextureSet text = game.BulletList.get(i).updateTime();
            if(text!=null) {

                textures.add(text);
                MonsterCollisionList.add(game.BulletList.get(i).collision);
            } else
                i--;
        }

        FrameworkMO.DrawWithLayering(batch,textures);

        batch.begin();

        int colind;
        for(int i = 0; i<game.MonsterList.size();i++) {
            colind = MovementMath.CheckCollisions(game.MonsterList.get(i).collision,MonsterCollisionList);
            FrameworkMO.TextureSet text = game.MonsterList.get(i).updateTime(game.mainplayer.sprite.getPosition());

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

        batch.end();

        textures = null;
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
