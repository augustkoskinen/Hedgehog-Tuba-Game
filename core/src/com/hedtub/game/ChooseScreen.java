package com.hedtub.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class ChooseScreen implements Screen {
    private final OfflineManager manager;
    private OrthographicCamera cam;
    private MainMenuScreen game;
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
    private boolean loading = false;
    private boolean loaded = false;
    private float loadtime = 0;
    private ArrayList<Integer> chosencolors = new ArrayList<>();
    private ArrayList<ChooseSkin> ChooseList = new ArrayList<>();
    public ChooseScreen(OfflineManager manager, MainMenuScreen game) {
        this.game = game;
        this.manager = manager;

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
            if(controller.getButton(controller.getMapping().buttonR1)&&controller.getButton(controller.getMapping().buttonL1)&&!manager.ControllerList.contains(controller)){
                manager.ControllerList.add(controller);
                manager.PlayerList.add(new OfflineManager.Player(new Vector3(),1,controller,1,chosencolors));
                manager.numplayers++;
                manager.addShake(.4f);
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.F)&&Gdx.input.isKeyPressed(Input.Keys.G)&&!manager.keyboardtaken){
            manager.keyboardtaken = true;
            manager.PlayerList.add(new OfflineManager.Player(new Vector3(),0));
            chosencolors.add(1);
            manager.numplayers++;
            manager.addShake(.4f);
        }

        //clears screen
        ScreenUtils.clear(0, 0, 0, 1, true);

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
            batch.draw(background,cam.viewportWidth/2+manager.loadjiggle.x,cam.viewportHeight/2+manager.loadjiggle.y);
            boolean allchose = true;
            boolean pressedstart = false;
            for(int i = 0; i < manager.numplayers;i++) {
                switch(manager.PlayerList.get(i).skintype) {
                    case 1: {
                        batch.draw(p1, cam.viewportWidth / 2 + manager.loadjiggle.x + 33 * i, cam.viewportHeight / 2 + manager.loadjiggle.y);
                        break;
                    }
                    case 2: {
                        batch.draw(p2, cam.viewportWidth / 2 + manager.loadjiggle.x + 33 * i, cam.viewportHeight / 2 + manager.loadjiggle.y);
                        break;
                    }
                    case 3: {
                        batch.draw(p3, cam.viewportWidth / 2 + manager.loadjiggle.x + 33 * i, cam.viewportHeight / 2 + manager.loadjiggle.y);
                        break;
                    }
                    case 4: {
                        batch.draw(p4, cam.viewportWidth / 2 + manager.loadjiggle.x + 33 * i, cam.viewportHeight / 2 + manager.loadjiggle.y);
                        break;
                    }
                    case 5: {
                        batch.draw(p5, cam.viewportWidth / 2 + manager.loadjiggle.x + 33 * i, cam.viewportHeight / 2 + manager.loadjiggle.y);
                        break;
                    }
                }

                if(manager.PlayerList.get(i).controltype==0) {
                    batch.draw(keyboard,cam.viewportWidth/2+6+manager.loadjiggle.x+33*i,cam.viewportHeight/2+manager.loadjiggle.y,24,24);

                    if(Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                        if(manager.PlayerList.get(i).chosechar) pressedstart = true;
                        else {
                            manager.PlayerList.get(i).chosechar = true;
                            manager.addShake(.4f);
                        }
                    }
                    if(Gdx.input.isKeyJustPressed(Input.Keys.G)) manager.PlayerList.get(i).chosechar = false;
                    if(!manager.PlayerList.get(i).chosechar) allchose = false;

                    if(!manager.PlayerList.get(i).chosechar) {
                        if (Gdx.input.isKeyJustPressed(Input.Keys.D) && !ChooseList.contains(manager.PlayerList.get(i))) {
                            int skin = (manager.PlayerList.get(i).skintype == 5 ? 1 : manager.PlayerList.get(i).skintype + 1);
                            while(chosencolors.contains(skin))
                                skin = (skin == 5 ? 1 : skin + 1);
                            ChooseList.add(new ChooseSkin(skin, manager.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                            if(chosencolors.contains(manager.PlayerList.get(i).prevskintype)) {
                                chosencolors.remove(chosencolors.indexOf(manager.PlayerList.get(i).prevskintype));
                                manager.PlayerList.get(i).prevskintype = skin;
                            }
                            if (!chosencolors.contains(skin))
                                chosencolors.add(skin);
                        }
                        if (Gdx.input.isKeyJustPressed(Input.Keys.A) && !ChooseList.contains(manager.PlayerList.get(i))) {
                            int skin = manager.PlayerList.get(i).skintype == 1 ? 5 : manager.PlayerList.get(i).skintype - 1;
                            while(chosencolors.contains(skin))
                                skin = (skin == 1 ? 5 : skin - 1);
                            ChooseList.add(new ChooseSkin(skin, manager.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                            if(chosencolors.contains(manager.PlayerList.get(i).prevskintype)) {
                                chosencolors.remove(chosencolors.indexOf(manager.PlayerList.get(i).prevskintype));
                                manager.PlayerList.get(i).prevskintype = skin;
                            }
                            if (!chosencolors.contains(skin))
                                chosencolors.add(skin);
                        }
                    }
                } else if(manager.PlayerList.get(i).controltype==1) {
                    manager.PlayerList.get(i).updateControls();
                    batch.draw(this.controller,cam.viewportWidth/2+6+manager.loadjiggle.x+33*i,cam.viewportHeight/2+manager.loadjiggle.y,24,24);

                    if(manager.PlayerList.get(i).firebutton) {
                        if(manager.PlayerList.get(i).chosechar) pressedstart = true;
                        else {
                            manager.PlayerList.get(i).chosechar = true;
                            manager.addShake(.4f);
                        }
                    }
                    if(manager.PlayerList.get(i).jumpbutton) manager.PlayerList.get(i).chosechar = false;
                    if(!manager.PlayerList.get(i).chosechar) allchose = false;

                    if(!manager.PlayerList.get(i).chosechar) {
                        if (manager.PlayerList.get(i).pressright && !ChooseList.contains(manager.PlayerList.get(i))) {
                            int skin = (manager.PlayerList.get(i).skintype == 5 ? 1 : manager.PlayerList.get(i).skintype + 1);
                            while(chosencolors.contains(skin))
                                skin = (skin == 5 ? 1 : skin + 1);
                            ChooseList.add(new ChooseSkin(skin, manager.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                            if(chosencolors.contains(manager.PlayerList.get(i).prevskintype)) {
                                chosencolors.remove(chosencolors.indexOf(manager.PlayerList.get(i).prevskintype));
                                manager.PlayerList.get(i).prevskintype = skin;
                            }
                            if (!chosencolors.contains(skin))
                                chosencolors.add(skin);
                        }
                        if (manager.PlayerList.get(i).pressleft && !ChooseList.contains(manager.PlayerList.get(i))) {
                            int skin = manager.PlayerList.get(i).skintype == 1 ? 5 : manager.PlayerList.get(i).skintype - 1;
                            while(chosencolors.contains(skin))
                                skin = (skin == 1 ? 5 : skin - 1);
                            ChooseList.add(new ChooseSkin(skin, manager.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                            if(chosencolors.contains(manager.PlayerList.get(i).prevskintype)) {
                                chosencolors.remove(chosencolors.indexOf(manager.PlayerList.get(i).prevskintype));
                                manager.PlayerList.get(i).prevskintype = skin;
                            }
                            if (!chosencolors.contains(skin))
                                chosencolors.add(skin);
                        }
                    }
                }
                if(manager.PlayerList.get(i).chosechar) {
                    batch.draw(chose, cam.viewportWidth / 2 + manager.loadjiggle.x + 33 * i, cam.viewportHeight / 2 + manager.loadjiggle.y);
                }
            }

            if(pressedstart&&allchose) {
                manager.addShake(.4f);
                if(manager.numplayers>0)
                    loading = true;
            }

            for(int i = 0; i < ChooseList.size();i++) {
                ChooseSkin curskin = ChooseList.get(i);
                batch.draw(curskin.anim.updateTime(1),curskin.pos.x+manager.loadjiggle.x,curskin.pos.y+manager.loadjiggle.y);
                if(curskin.anim.time>=0.21f) {
                    curskin.player.skintype = curskin.skin;
                }
                if(curskin.anim.time>=0.42f) {
                    ChooseList.remove(curskin);
                    i--;
                }
            }

            batch.draw(sign,cam.viewportWidth/2+manager.loadjiggle.x+floatpos.x,cam.viewportHeight/2+manager.loadjiggle.y+floatpos.y);
            if(loading) batch.draw(loadbattle.updateTime(1),cam.viewportWidth/2,cam.viewportHeight/2);
        } else {
            loading = false;
            if (loadtime < 2) loadtime += Gdx.graphics.getDeltaTime();
            else game.setScreen(new GameScreen(manager, game));
        }
        if(loading) {
            if (loadtime < 0.7f) loadtime += Gdx.graphics.getDeltaTime();
            else { loaded = true; loadtime = 0; }
        }
        batch.end();

        manager.updateShake();
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
        public OfflineManager.Player player;
        public Vector3 pos;
        public ChooseSkin(int num, OfflineManager.Player player, Vector3 pos){
            skin = num;
            this.player = player;
            anim = new FrameworkMO.AnimationSet("chooseplayer.png",42,1,0.01f);
            this.pos = pos;
        }
    }
}
