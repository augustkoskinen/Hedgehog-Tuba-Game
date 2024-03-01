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

public class GameScreen implements Screen {
    private OrthographicCamera cam;
    private HedTub game;
    private HedTub.Player player;
    private SpriteBatch batch;
    private Texture background;
    private FrameworkMO.AnimationSet openbattle = new FrameworkMO.AnimationSet("openbattle.png",36,1,0.025f);
    private float countopen = 0;
    private ArrayList<FrameworkMO.TextureSet> mapTextures = new ArrayList<>();
    public GameScreen (HedTub game) {
        this.game = game;

        for (int i = 0; i<game.PlayerList.size();i++)
            game.PlayerList.get(i).sprite.setPosition(game.WORLD_WIDTH/2*game.TILE_WIDTH,game.WORLD_HEIGHT/2*game.TILE_WIDTH+game.TILE_WIDTH);

        player = game.PlayerList.get(0);

        batch = new SpriteBatch();

        // cam setup
        cam = new OrthographicCamera();
        cam.setToOrtho(false, (game.WORLD_WIDTH*game.TILE_WIDTH), (game.WORLD_HEIGHT*game.TILE_WIDTH));
        cam.position.set(new Vector3(player.sprite.x, player.sprite.y, 0));
        background = new Texture("background.png");

        TextureRegion maptext;
        for(int i = 0; i<game.WORLD_MAP.length;i++) {
            for (int j = 0; j < game.WORLD_MAP[i].length; j++) {
                maptext = FrameworkMO.getSpriteTilemap(game.WORLD_MAP, i, j, game.WORLD_MAP.length, game.WORLD_MAP[i].length);
                if (maptext != null) {
                    mapTextures.add(new FrameworkMO.TextureSet(maptext, i * game.TILE_WIDTH, j * game.TILE_WIDTH, j * game.TILE_WIDTH));
                }
            }
        }

        //for(int i = 0; i<10; i++)
        game.MonsterList.add(new HedTub.Monster(new Vector3((int)(Math.random()*game.WORLD_WIDTH*32),(int)(Math.random()*game.WORLD_HEIGHT*32),0),0));
    }

    @Override
    public void render(float delta) {

        //clears screen
        ScreenUtils.clear(0.415f, 0.764f, 0.851f, 1, true);

        cam.position.set((game.WORLD_WIDTH*game.TILE_WIDTH)/2, (game.WORLD_HEIGHT*game.TILE_WIDTH)/2, 0);
        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();
        batch.draw(background,0,0,game.WORLD_WIDTH*game.TILE_WIDTH,game.WORLD_HEIGHT*game.TILE_WIDTH);
        for(int i = 0; i<mapTextures.size(); i++)
            batch.draw(mapTextures.get(i).texture,mapTextures.get(i).x,mapTextures.get(i).y);
        batch.end();
        //draws sprites
        ArrayList<FrameworkMO.TextureSet> textures = new ArrayList<>();

        for(int i =0; i<game.numplayers;i++) {
            TextureRegion playertext = player.updatePlayerPos();
            player = game.PlayerList.get(i);
            player.sprite.depth = player.sprite.y;
            if (player.jumpdir != 0) {
                player.jumpdir += player.jumpdiradd;
                if (player.jumpdir == 0) {
                    player.jumpdiradd = 0;
                }
            }
            textures.add(new FrameworkMO.TextureSet(playertext, player.sprite.x, player.sprite.y, player.sprite.depth, (float) Math.toRadians(player.moverot)));
            textures.add(player.getEyeText());
        }

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

        if(countopen < 0.875) {
            countopen+= Gdx.graphics.getDeltaTime();

            batch.draw(openbattle.updateTime(1),0,0,cam.viewportWidth,cam.viewportHeight);
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
