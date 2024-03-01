package com.hedtub.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class ChooseScreen implements Screen {
    private OrthographicCamera cam;
    private HedTub game;
    private HedTub.Player player;
    private SpriteBatch batch;
    private Texture background;
    private Texture sign;
    private Texture p1;
    private Texture controller;
    private Texture keyboard;
    private FrameworkMO.AnimationSet loadbattle = new FrameworkMO.AnimationSet("loadbattle.png",30,1,0.025f);
    private ArrayList<FrameworkMO.TextureSet> mapTextures = new ArrayList<>();
    private Vector3 floatpos = new Vector3();
    private float floatadd = 0.01f;
    private Vector3 loadjiggle = new Vector3();
    private boolean loading = false;
    private boolean loaded = false;
    private float loadtime = 0;
    private boolean shake = false;
    private float shaketime = 0;
    public ChooseScreen (HedTub game) {
        this.game = game;
        player = game.mainplayer;

        batch = new SpriteBatch();

        // cam setup
        cam = new OrthographicCamera();
        cam.setToOrtho(false, 135,92);
        cam.position.set(135, 92, 0);
        background = new Texture("chooseback.png");
        sign = new Texture("choosesign.png");
        p1 = new Texture("playerchoose.png");
        controller = new Texture("controller.png");
        keyboard = new Texture("keyboard.png");
    }

    @Override
    public void render(float delta) {
        //finding players
        for(int i = 0; i< Controllers.getControllers().size;i++){
            Controller controller = Controllers.getControllers().get(i);
            if(controller.getButton(controller.getMapping().buttonR1)&&controller.getButton(controller.getMapping().buttonL1)&&!game.ControllerList.contains(controller)){
                game.ControllerList.add(controller);
                game.PlayerList.add(new HedTub.Player(new Vector3(),1,controller));
                game.numplayers++;
                shake = true;
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.F)&&Gdx.input.isKeyPressed(Input.Keys.G)&&!game.keyboardtaken){
            game.keyboardtaken = true;
            game.PlayerList.add(new HedTub.Player(new Vector3(),0));
            game.numplayers++;
            shake = true;
        }

        //clears screen
        ScreenUtils.clear(0, 0, 0, 1, true);

        if(shake)
            loadjiggle = new Vector3(((float)(0.7-shaketime)*2)*((int)(Math.random()*2)==0 ? 1f : -1f),((float)(0.7-shaketime)*2)*((int)(Math.random()*2)==0 ? 1f : -1f),0);

        if(shaketime<.7) shaketime+=Gdx.graphics.getDeltaTime();
        else {shaketime = 0; shake = false; loadjiggle = new Vector3();}

        cam.position.set(135, 92, 0);
        cam.update();
        batch.setProjectionMatrix(cam.combined);

        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            shake = true;
            if(game.numplayers>0)
                loading = true;
        }

        System.out.println(floatadd);
        floatpos.y+=floatadd;
        if(floatpos.y>.5&&floatadd == 0.01f) {
            floatadd = -0.01f;
        }
        if(floatpos.y<-.5&&floatadd == -0.01f) {
            floatadd = 0.01f;
        }

        batch.begin();
        if(!loaded) {
            batch.draw(background,cam.viewportWidth/2+loadjiggle.x,cam.viewportHeight/2+loadjiggle.y);
            for(int i = 0; i < game.numplayers;i++) {
                batch.draw(p1,cam.viewportWidth/2+loadjiggle.x+33*i,cam.viewportHeight/2+loadjiggle.y);
                if(game.PlayerList.get(i).controltype==0) {
                    batch.draw(keyboard,cam.viewportWidth/2+6+loadjiggle.x+32*i,cam.viewportHeight/2+loadjiggle.y,24,24);
                } else if(game.PlayerList.get(i).controltype==1) {
                    batch.draw(controller,cam.viewportWidth/2+6+loadjiggle.x+32*i,cam.viewportHeight/2+loadjiggle.y,24,24);
                }
            }
            batch.draw(sign,cam.viewportWidth/2+loadjiggle.x+floatpos.x,cam.viewportHeight/2+loadjiggle.y+floatpos.y);
            if(loading) batch.draw(loadbattle.updateTime(1),cam.viewportWidth/2,cam.viewportHeight/2);
        } else {
            loading = false;
            if (loadtime < 2) loadtime += Gdx.graphics.getDeltaTime();
            else game.setScreen(new GameScreen(game));
        }
        if(loading) {
            if (loadtime < 0.7f) loadtime += Gdx.graphics.getDeltaTime();
            else { loaded = true; loadtime = 0; }
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
