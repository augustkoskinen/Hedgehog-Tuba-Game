package com.hedtub.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import com.badlogic.gdx.utils.ScreenUtils;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSockets;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Float.parseFloat;

public class MainMenuScreen extends Game {
	//world vars
	SpriteBatch batch;
	private String launcher;
	private int chosegame = 0;
	private Texture background;
	private Texture offbutton;
	private Texture onbutton;
	private Texture offbuttonhover;
	private Texture onbuttonhover;
	private boolean setupgame = false;
	//main
	public MainMenuScreen(String str) {
		launcher = str;
	}

	@Override
	public void create() {
		batch = new SpriteBatch();

		background = new Texture("menuback.png");
		offbutton = new Texture("offbutton.png");
		onbutton = new Texture("onbutton.png");
		offbuttonhover = new Texture("offbuttonhover.png");
		onbuttonhover = new Texture("onbuttonhover.png");
	}

	@Override
	public void render() {
		super.render();

		if(!setupgame) {
			batch.begin();
			batch.draw(background, 0, 0, 1024, 576);
			if (MovementMath.pointDis(new Vector3(384 * (Gdx.graphics.getWidth() / 1024f), 288 * (Gdx.graphics.getHeight() / 576f), 0f), new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f)) < 64f * (Gdx.graphics.getWidth() / 1024f)) {
				batch.draw(offbuttonhover, 448 - 128, 224, 128, 128);
				if (Gdx.input.isTouched())
					chosegame = 1;
			} else {
				batch.draw(offbutton, 448 - 128, 224, 128, 128);
			}
			if (MovementMath.pointDis(new Vector3(640 * (Gdx.graphics.getWidth() / 1024f), 288 * (Gdx.graphics.getHeight() / 576f), 0f), new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f)) < 64f * (Gdx.graphics.getWidth() / 1024f)) {
				batch.draw(onbuttonhover, 448 + 128, 224, 128, 128);
				if (Gdx.input.isTouched())
					chosegame = 2;
			} else {
				batch.draw(onbutton, 448 + 128, 224, 128, 128);
			}
			batch.end();
		}

		if(chosegame==1&&!setupgame) {
			this.setScreen(new OfflineManager(this, launcher));
			setupgame = true;
		} else if(chosegame==2&&!setupgame) {
			this.setScreen(new OnlineManager(this, launcher));
			setupgame = true;
		}
	}

	@Override
	public void dispose() {
		batch.dispose();
	}
}