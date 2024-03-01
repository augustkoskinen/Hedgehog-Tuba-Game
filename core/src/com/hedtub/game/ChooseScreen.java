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
    private SpriteBatch batch;
    private Texture background;
    private Texture sign;
    private Texture p1;
    private Texture p2;
    private Texture p3;
    private Texture p4;
    private Texture p5;
    private Texture controller;
    private Texture chose;
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
    private ArrayList<ChooseSkin> ChooseList = new ArrayList<>();
    public ChooseScreen (HedTub game) {
        this.game = game;

        batch = new SpriteBatch();

        // cam setup
        cam = new OrthographicCamera();
        cam.setToOrtho(false, 135,92);
        cam.position.set(135, 92, 0);
        background = new Texture("chooseback.png");
        sign = new Texture("choosesign.png");
        p1 = new Texture("p1.png");
        p2 = new Texture("p2.png");
        p3 = new Texture("p3.png");
        p4 = new Texture("p4.png");
        p5 = new Texture("p5.png");
        chose = new Texture("chose.png");
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
                game.PlayerList.add(new HedTub.Player(new Vector3(),1,controller,1));
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
            boolean allchose = true;
            boolean pressedstart = false;
            for(int i = 0; i < game.numplayers;i++) {
                switch(game.PlayerList.get(i).skintype) {
                    case 1: {
                        batch.draw(p1, cam.viewportWidth / 2 + loadjiggle.x + 33 * i, cam.viewportHeight / 2 + loadjiggle.y);
                        break;
                    }
                    case 2: {
                        batch.draw(p2, cam.viewportWidth / 2 + loadjiggle.x + 33 * i, cam.viewportHeight / 2 + loadjiggle.y);
                        break;
                    }
                    case 3: {
                        batch.draw(p3, cam.viewportWidth / 2 + loadjiggle.x + 33 * i, cam.viewportHeight / 2 + loadjiggle.y);
                        break;
                    }
                    case 4: {
                        batch.draw(p4, cam.viewportWidth / 2 + loadjiggle.x + 33 * i, cam.viewportHeight / 2 + loadjiggle.y);
                        break;
                    }
                    case 5: {
                        batch.draw(p5, cam.viewportWidth / 2 + loadjiggle.x + 33 * i, cam.viewportHeight / 2 + loadjiggle.y);
                        break;
                    }
                }

                if(game.PlayerList.get(i).controltype==0) {
                    batch.draw(keyboard,cam.viewportWidth/2+6+loadjiggle.x+33*i,cam.viewportHeight/2+loadjiggle.y,24,24);

                    if(Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                        if(game.PlayerList.get(i).chosechar) pressedstart = true;
                        else {
                            game.PlayerList.get(i).chosechar = true;
                            shake = true;
                        }
                    }
                    if(Gdx.input.isKeyJustPressed(Input.Keys.G)) game.PlayerList.get(i).chosechar = false;
                    if(!game.PlayerList.get(i).chosechar) allchose = false;

                    if(!game.PlayerList.get(i).chosechar) {
                        if (Gdx.input.isKeyJustPressed(Input.Keys.D) && !ChooseList.contains(game.PlayerList.get(i)))
                            ChooseList.add(new ChooseSkin(game.PlayerList.get(i).skintype == 5 ? 1 : game.PlayerList.get(i).skintype + 1, game.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                        if (Gdx.input.isKeyJustPressed(Input.Keys.A) && !ChooseList.contains(game.PlayerList.get(i)))
                            ChooseList.add(new ChooseSkin(game.PlayerList.get(i).skintype == 1 ? 5 : game.PlayerList.get(i).skintype - 1, game.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                    }
                } else if(game.PlayerList.get(i).controltype==1) {
                    game.PlayerList.get(i).updateControls();
                    batch.draw(this.controller,cam.viewportWidth/2+6+loadjiggle.x+33*i,cam.viewportHeight/2+loadjiggle.y,24,24);

                    if(game.PlayerList.get(i).firebutton) {
                        if(game.PlayerList.get(i).chosechar) pressedstart = true;
                        else {
                            game.PlayerList.get(i).chosechar = true;
                            shake = true;
                        }
                    }
                    if(game.PlayerList.get(i).jumpbutton) game.PlayerList.get(i).chosechar = false;
                    if(!game.PlayerList.get(i).chosechar) allchose = false;

                    if(!game.PlayerList.get(i).chosechar) {
                        if (game.PlayerList.get(i).pressright && !ChooseList.contains(game.PlayerList.get(i)))
                            ChooseList.add(new ChooseSkin(game.PlayerList.get(i).skintype == 5 ? 1 : game.PlayerList.get(i).skintype + 1, game.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                        if (game.PlayerList.get(i).pressleft && !ChooseList.contains(game.PlayerList.get(i)))
                            ChooseList.add(new ChooseSkin(game.PlayerList.get(i).skintype == 1 ? 5 : game.PlayerList.get(i).skintype - 1, game.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                    }
                }
                if(game.PlayerList.get(i).chosechar) {
                    batch.draw(chose, cam.viewportWidth / 2 + loadjiggle.x + 33 * i, cam.viewportHeight / 2 + loadjiggle.y);
                }
            }

            if(pressedstart&&allchose) {
                shake = true;
                if(game.numplayers>0)
                    loading = true;
            }

            for(int i = 0; i < ChooseList.size();i++) {
                ChooseSkin curskin = ChooseList.get(i);
                batch.draw(curskin.anim.updateTime(1),curskin.pos.x+loadjiggle.x,curskin.pos.y+loadjiggle.y);
                if(curskin.anim.time>=0.21f) {
                    curskin.player.skintype = curskin.skin;
                }
                if(curskin.anim.time>=0.42f) {
                    ChooseList.remove(curskin);
                    i--;
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
    public class ChooseSkin {
        public FrameworkMO.AnimationSet anim;
        public int skin;
        public HedTub.Player player;
        public Vector3 pos;
        public ChooseSkin(int num, HedTub.Player player, Vector3 pos){
            skin = num;
            this.player = player;
            anim = new FrameworkMO.AnimationSet("chooseplayer.png",42,1,0.01f);
            this.pos = pos;
        }
    }
}
