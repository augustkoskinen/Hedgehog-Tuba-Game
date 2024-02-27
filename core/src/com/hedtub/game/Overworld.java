package com.hedtub.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class Overworld implements Screen {
    private OrthographicCamera cam;
    private HedTub game;
    private FrameworkMO.SpriteObjectSqr player;
    private SpriteBatch batch;
    private ArrayList<FrameworkMO.TextureSet> mapTextures = new ArrayList<>();
    public Overworld (HedTub game) {
        this.game = game;
        player = game.player;

        batch = new SpriteBatch();

        // cam setup
        cam = new OrthographicCamera();
        cam.setToOrtho(false, 192, 132);
        cam.position.set(new Vector3(player.x, player.y, 0));


        TextureRegion maptext;
        for(int i = 0; i<game.WORLD_MAP.length;i++) {
            for (int j = 0; j < game.WORLD_MAP[i].length; j++) {
                maptext = FrameworkMO.getSpriteTilemap(game.WORLD_MAP, i, j, game.WORLD_MAP.length, game.WORLD_MAP[i].length);
                if (maptext != null) {
                    mapTextures.add(new FrameworkMO.TextureSet(maptext, i * 32, j * 32, j * 32));
                }
            }
        }
    }

    @Override
    public void render(float delta) {

        //clears screen
        ScreenUtils.clear(0.415f, 0.764f, 0.851f, 1, true);
        TextureRegion playertext = game.updatePlayerPos();

        //updates cam position/visuals
        float camdis = MovementMath.pointDis(cam.position, new Vector3(game.player.x + 16, game.player.y + 16, 0));
        float camdir = MovementMath.pointDir(cam.position, new Vector3(game.player.x + 16, game.player.y + 16, 0));
        Vector3 campos = MovementMath.lengthDir(camdir, camdis);
        cam.position.set(cam.position.x + campos.x * .05f, cam.position.y + campos.y * .05f, 0);
        cam.update();
        batch.setProjectionMatrix(cam.combined);


        //draws sprites
        ArrayList<FrameworkMO.TextureSet> textures = new ArrayList<>();

        player.depth= player.y;
        if(game.jumpdir!=0) {
            game.jumpdir+=game.jumpdiradd;
            if(game.jumpdir==0) {
                game.jumpdiradd = 0;
            }
        }
        textures.add(new FrameworkMO.TextureSet(playertext,player.x, player.y, player.depth,(float) Math.toRadians(game.moverot)));

        textures.addAll(mapTextures);

        for(int i = 0; i<game.PoofCloudList.size();i++) {
            FrameworkMO.TextureSet text = game.PoofCloudList.get(i).updateTime();
            if(text!=null) {
                textures.add(text);
            } else
                i--;
        }
        for(int i = 0; i<game.BulletList.size();i++) {
            FrameworkMO.TextureSet text = game.BulletList.get(i).updateTime();
            if(text!=null) {
                textures.add(text);
            } else
                i--;
        }

        FrameworkMO.DrawWithLayering(batch,textures);
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
