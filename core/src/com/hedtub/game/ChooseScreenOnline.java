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
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import java.util.ArrayList;

public class ChooseScreenOnline implements Screen {
    private final OnlineManager manager;
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
    private Vector3 floatpos = new Vector3();
    private float floatadd = 0.01f;
    private boolean loading = false;
    private boolean loaded = false;
    private float loadtime = 0;
    private ArrayList<Integer> chosencolors = new ArrayList<>();
    private ArrayList<ChooseSkin> ChooseList = new ArrayList<>();
    public ChooseScreenOnline(OnlineManager manager, MainMenuScreen game) {
        this.game = game;
        this.manager = manager;

        if(Controllers.getControllers().size>0){
            manager.mainplayer = new OnlineManager.Player(new Vector3(),1,Controllers.getControllers().get(0),1,chosencolors,manager.socket.toString(),true);
            manager.PlayerList.add(manager.mainplayer);
            manager.addShake(.4f);
        } else {
            manager.mainplayer = new OnlineManager.Player(new Vector3(),0,manager.socket.toString(),true);
            manager.PlayerList.add(manager.mainplayer);
            manager.addShake(.4f);
        }

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
        if(manager.serverready)
            loading = true;
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
        /*
        JSONObject senddata = new JSONObject();
        senddata.put("event", new JSONString("sendPlayerName"));
        manager.socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));
         */

        batch.begin();
        if(!loaded) {
            batch.draw(background,cam.viewportWidth/2+manager.loadjiggle.x,cam.viewportHeight/2+manager.loadjiggle.y);
            boolean pressedstart = false;
            for(int i = 0; i < manager.PlayerList.size();i++) {
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

                    if(Gdx.input.isKeyJustPressed(Input.Keys.F)&&i==0) {
                        if(manager.PlayerList.get(i).ready) pressedstart = true;
                        else {
                            manager.PlayerList.get(i).ready = true;
                            manager.addShake(.4f);

                            JSONObject senddata = new JSONObject();
                            senddata.put("event", new JSONString("readyPlayer"));
                            manager.socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));
                        }
                    }
                    if(Gdx.input.isKeyJustPressed(Input.Keys.G)&&i==0) {
                        manager.PlayerList.get(i).ready = false;

                        JSONObject senddata = new JSONObject();
                        senddata.put("event", new JSONString("unReadyPlayer"));
                        manager.socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));
                    }

                    if(!manager.PlayerList.get(i).ready&&i==0) {
                        if (Gdx.input.isKeyJustPressed(Input.Keys.D) && !ChooseList.contains(manager.PlayerList.get(i))) {
                            int skin = (manager.PlayerList.get(i).skintype == 5 ? 1 : manager.PlayerList.get(i).skintype + 1);
                            ChooseList.add(new ChooseSkin(skin, manager.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                            if(chosencolors.contains(manager.PlayerList.get(i).prevskintype)) {
                                chosencolors.remove(chosencolors.indexOf(manager.PlayerList.get(i).prevskintype));
                                manager.PlayerList.get(i).prevskintype = skin;
                            }
                            JSONObject senddata = new JSONObject();
                            senddata.put("event", new JSONString("updateSkin"));
                            senddata.put("skin", new JSONString(skin + ""));
                            manager.socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));
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
                            JSONObject senddata = new JSONObject();
                            senddata.put("event", new JSONString("updateSkin"));
                            senddata.put("skin", new JSONString(skin + ""));
                            manager.socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));
                        }
                    }
                } else if(manager.PlayerList.get(i).controltype==1) {
                    manager.PlayerList.get(i).updateControls();
                    batch.draw(this.controller,cam.viewportWidth/2+6+manager.loadjiggle.x+33*i,cam.viewportHeight/2+manager.loadjiggle.y,24,24);

                    if(manager.PlayerList.get(i).firebutton&&i==0) {
                        if(manager.PlayerList.get(i).ready) pressedstart = true;
                        else {
                            manager.PlayerList.get(i).ready = true;
                            manager.addShake(.4f);

                            JSONObject senddata = new JSONObject();
                            senddata.put("event", new JSONString("readyPlayer"));
                            manager.socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));
                        }
                    }
                    if(manager.PlayerList.get(i).jumpbutton&&i==0){
                        manager.PlayerList.get(i).ready = false;

                        JSONObject senddata = new JSONObject();
                        senddata.put("event", new JSONString("unReadyPlayer"));
                        manager.socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));
                    }

                    if(!manager.PlayerList.get(i).ready&&i==0) {
                        if (manager.PlayerList.get(i).pressright && !ChooseList.contains(manager.PlayerList.get(i))) {
                            int skin = (manager.PlayerList.get(i).skintype == 5 ? 1 : manager.PlayerList.get(i).skintype + 1);
                            ChooseList.add(new ChooseSkin(skin, manager.PlayerList.get(i), new Vector3(cam.viewportWidth / 2 + 33 * i, cam.viewportHeight / 2, 0)));
                            if(chosencolors.contains(manager.PlayerList.get(i).prevskintype)) {
                                chosencolors.remove(chosencolors.indexOf(manager.PlayerList.get(i).prevskintype));
                                manager.PlayerList.get(i).prevskintype = skin;
                            }
                            JSONObject senddata = new JSONObject();
                            senddata.put("event", new JSONString("updateSkin"));
                            senddata.put("skin", new JSONString(skin + ""));
                            manager.socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));
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
                            if (!chosencolors.contains(skin)) {
                                chosencolors.add(skin);
                                JSONObject senddata = new JSONObject();
                                senddata.put("event", new JSONString("updateSkin"));
                                senddata.put("skin", new JSONString(skin + ""));
                                manager.socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));
                            }
                        }
                    }
                }
                if(manager.PlayerList.get(i).ready) {
                    batch.draw(chose, cam.viewportWidth / 2 + manager.loadjiggle.x + 33 * i, cam.viewportHeight / 2 + manager.loadjiggle.y);
                }
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
            else game.setScreen(new GameScreenOnline(manager, game));
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
    public void show() {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
    }
    public class ChooseSkin {
        public FrameworkMO.AnimationSet anim;
        public int skin;
        public OnlineManager.Player player;
        public Vector3 pos;
        public ChooseSkin(int num, OnlineManager.Player player, Vector3 pos){
            skin = num;
            this.player = player;
            anim = new FrameworkMO.AnimationSet("chooseplayer.png",42,1,0.01f);
            this.pos = pos;
        }
    }
}
