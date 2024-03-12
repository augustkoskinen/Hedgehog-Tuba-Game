package com.hedtub.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSockets;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Float.parseFloat;

public class OnlineManager implements Screen {
	//world vars
	SpriteBatch batch;
	public MainMenuScreen game;
	public static WebSocket socket;
	public String id;
	public static final int TILE_WIDTH = 16;
	public static final int WORLD_WIDTH = 46;
	public static final int WORLD_HEIGHT = 26;
	public static final float MULT_AMOUNT = 55;
	public static float WIND_WIDTH = 1024;
	public static float WIND_HEIGHT = 576;
	public static float CHANGE_RATIO = 1;
	public static float SLOWSPEED = 1;
	public static int[][] WORLD_MAP;
	public String launcher;
	public FrameworkMO.AnimationSet openbattle;
	public static float countopen = 0.875f;
	public static OrthographicCamera gamecam  = null;
	public static boolean gamestarted = false;
	public Texture healthback;
	public float waitstart = 0;
	public FrameworkMO.AnimationSet startanim;

	//player
	ControllerManager connectedController;
	public static float dmgcount = 0;
	public Sprite dmgscreen;
	public Player mainplayer;
	public static final float GRAVITY = 0.25f*MULT_AMOUNT;
	public static final float WALK_SPEED = 2*MULT_AMOUNT;
	public static final float JUMP_SPEED = 5.2f*MULT_AMOUNT;
	//ArrayLists
	public static ArrayList<PoofCloud> PoofCloudList;
	public static ArrayList<Bullet> BulletList;
	public static ArrayList<Player> PlayerList = new ArrayList<>();
	public static ArrayList<Player> DeletedPlayerList = new ArrayList<>();
	public static Random seed;
	public static boolean pause = true;
	public static boolean shake = false;
	public static boolean serverready = false;
	public static float shaketime = 0;
	public static Vector3 loadjiggle = new Vector3();
	//main
	public OnlineManager(MainMenuScreen game, String str) {
		launcher = str;
		this.game = game;
		PoofCloudList = new ArrayList<>();
		BulletList = new ArrayList<>();

		connectedController = new ControllerManager();

		batch = new SpriteBatch();

		dmgscreen = new Sprite(new Texture("dmgscreen.png"));
		openbattle = new FrameworkMO.AnimationSet("openbattle.png",36,1,0.025f);
		startanim = new FrameworkMO.AnimationSet("countdown.png",3,1,1f);
		healthback = new Texture("healthback.png");

		if(launcher.equals("html")) {
			WIND_WIDTH = Gdx.graphics.getWidth();
			WIND_HEIGHT = Gdx.graphics.getHeight();
			CHANGE_RATIO = Gdx.graphics.getWidth()/1024f;
		}

		socket = configSocket();
	}

	@Override
	public void show() {
		ChooseScreenOnline newscreen = new ChooseScreenOnline(this, game);
		game.setScreen(newscreen);
	}

	@Override
	public void render(float delta) {
		batch.begin();

		if(gamestarted)
			for(int i =0; i<PlayerList.size();i++) {
				Player curplayer = PlayerList.get(i);
				batch.draw(healthback,16+i*72,WIND_HEIGHT-16-64*CHANGE_RATIO,64*CHANGE_RATIO,64*CHANGE_RATIO);
				batch.draw(curplayer.healthwheel.getAnim(),16+i*72,WIND_HEIGHT-16-64*CHANGE_RATIO,64*CHANGE_RATIO,64*CHANGE_RATIO);
			}

		if(countopen < 0.875) {
			countopen+= Gdx.graphics.getDeltaTime();
			batch.draw(openbattle.updateTime(1),0,0,WIND_WIDTH,WIND_HEIGHT);
		}

		if(waitstart < 2&&gamestarted) {
			waitstart += Gdx.graphics.getDeltaTime();
		} else if(gamestarted) {
			if(startanim.time<startanim.framereg*3)
				batch.draw(startanim.updateTime(1),WIND_WIDTH/2-64*CHANGE_RATIO,WIND_HEIGHT/2-64*CHANGE_RATIO,128*CHANGE_RATIO,128*CHANGE_RATIO);
			else
				pause = false;
		}

		if(shake)
			loadjiggle = new Vector3((shaketime*2)*(seed.nextInt(2)==0 ? 1f : -1f),(shaketime*2)*(seed.nextInt(2)==0 ? 1f : -1f),0);

		if(shaketime>0) shaketime-=Gdx.graphics.getDeltaTime();
		else {shaketime = .7f; shake = false; loadjiggle = new Vector3();}

		batch.end();
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

	public static void addShake(float add) {
		shake = true;
		shaketime = add;
	}

	public void updateShake() {
		if(shake)
			loadjiggle = new Vector3((shaketime*2)*((int)(Math.random()*2)==0 ? 1f : -1f),(shaketime*2)*((int)(Math.random()*2)==0 ? 1f : -1f),0);

		if(shaketime>0)
			shaketime-=Gdx.graphics.getDeltaTime();
		else {
			shaketime = .7f;
			shake = false;
			loadjiggle = new Vector3();
		}
	}

	//=============|
	//OBJECTS
	//=============|


	public static class PoofCloud {
		private float life = 0.7f;
		private int type = 0;
		private float time = 0;
		private float dir;
		private float speed = 1;
		private FrameworkMO.AnimationSet animation;
		private Vector3 pos;

		public PoofCloud(float dir, Vector3 pos){
			animation = new FrameworkMO.AnimationSet("poof.png",7,1,0.1f);
			this.pos = pos;
			this.dir = dir;
		}
		public PoofCloud(float dir, Vector3 pos,int type){
			this.type = type;
			if(type==1) {
				animation = new FrameworkMO.AnimationSet("poofcloud.png",8,1,0.1f,false);
				this.pos = pos;
				this.dir = 0;
				this.life = 0.8f;
			} else {
				animation = new FrameworkMO.AnimationSet("poof.png",7,1,0.1f);
				this.pos = pos;
				this.dir = dir;
			}
		}
		public FrameworkMO.TextureSet updateTime(){
			if(type==0) {
				Vector3 addvect = MovementMath.lengthDir((float) Math.toRadians(dir - 90), speed*Gdx.graphics.getDeltaTime()*MULT_AMOUNT);
				if(!pause)
					pos = new Vector3(pos.x + addvect.x, pos.y + addvect.y, 0);
			}

			if(!pause) {
				time += Gdx.graphics.getDeltaTime();
				speed *= .9f;
			}

			if(time>=life) {
				PoofCloudList.remove(this);
				return null;
			}

			if(!pause)
				return new FrameworkMO.TextureSet(animation.updateTime(SLOWSPEED),pos.x,pos.y,100000,(float)Math.toRadians(dir));
			else
				return new FrameworkMO.TextureSet(animation.getAnim(),pos.x,pos.y,100000,(float)Math.toRadians(dir));
		}
	}

	public static class Bullet {
		private final float life = 0.7f;
		private float time = 0;
		private float dir;
		private float speed = 10;
		private Vector3 pos;
		private Texture text;
		public Rectangle collision;
		public Player homeplayer;

		public Bullet(float dir, Vector3 pos,String path, Player homeplayer){
			text = new Texture(path);
			this.pos = pos;
			this.dir = dir;
			this.homeplayer = homeplayer;

			collision = new Rectangle(pos.x,pos.y,6,2);
		}
		public FrameworkMO.TextureSet updateTime(){
			Vector3 addvect = MovementMath.lengthDir((float)Math.toRadians(dir-90),speed*Gdx.graphics.getDeltaTime()*MULT_AMOUNT);
			if(!pause) {
				pos = new Vector3(pos.x + addvect.x * SLOWSPEED, pos.y + addvect.y * SLOWSPEED, 0);
				collision.x += addvect.x * SLOWSPEED;
				collision.y += addvect.y * SLOWSPEED;
				time += Gdx.graphics.getDeltaTime() * SLOWSPEED;
			}

			for(int i = 0; i<PlayerList.size();i++) {
				if (PlayerList.get(i) != homeplayer && MovementMath.overlaps(collision, PlayerList.get(i).sprite.collision)) {
					PlayerList.get(i).takeDamage();
					Remove();
					return null;
				}
			}

			if(time>=life||MovementMath.CheckCollisions(WORLD_MAP,collision,0,new Vector3(0,0,0),new Vector3(8,2,0))) {
				Remove();
				return null;
			}

			if(pos.y<-16) {
				pos.y = (WORLD_HEIGHT*TILE_WIDTH+16);
				collision.y = (WORLD_HEIGHT*TILE_WIDTH+16);
			}
			if(pos.y>WORLD_HEIGHT*TILE_WIDTH+16) {
				pos.y = (-16);
				collision.y = (-16);
			}
			if(pos.x<-16) {
				pos.x = (WORLD_WIDTH*TILE_WIDTH+16);
				collision.x = (WORLD_WIDTH*TILE_WIDTH+16);
			}
			if(pos.x>WORLD_WIDTH*TILE_WIDTH+16) {
				pos.x = (-16);
				collision.x = (-16);
			}

			return new FrameworkMO.TextureSet(new TextureRegion(text),pos.x,pos.y,100000,(float)Math.toRadians(dir-90));
		}
		public void Remove() {
			BulletList.remove(this);
		}
	}

	public static class Player {
		public int health = 8;
		public FrameworkMO.AnimationSet healthwheel;
		public FrameworkMO.SpriteObjectSqr sprite;
		public FrameworkMO.AnimationSet animrw;
		public FrameworkMO.AnimationSet animlw;
		public Vector3 movevect;
		public float horzmove;
		public int movedir = 1;
		public float lastdir = 1;
		public boolean ismainplayer = false;
		public boolean joined = false;
		public int jumpcount = 5;
		public int jumpdir = 0;
		public double moverot = 0;
		public int jumpdiradd = 0;
		public int controltype;
		public int skintype = 1;
		public int prevskintype = 1;
		public Controller controller;
		public boolean ready = false;
		public boolean firebutton = false;
		public boolean jumpbutton = false;
		public boolean firebuttonrelease = false;
		public boolean jumpbuttonrelease = false;
		public boolean pressright = false;
		public boolean pressleft = false;
		public boolean releaseright = false;
		public boolean releaseleft = false;
		public float deadcount = 0;
		public String socketid;

		public Player(Vector3 pos, int type, String socketid, boolean ismainplayer) {
			sprite = new FrameworkMO.SpriteObjectSqr("hedtubr.png",pos.x,pos.y,24,24,0,0,true);
			animrw = new FrameworkMO.AnimationSet("hedtubwr.png",4,1,0.2f);
			animlw = new FrameworkMO.AnimationSet("hedtubwl.png",4,1,0.2f);
			movevect = new Vector3();
			this.socketid = socketid;
			controltype = type;
			this.ismainplayer = ismainplayer;
			this.joined = ismainplayer;

			if(Controllers.getControllers().size==0&&type!=-1) controltype = 0;
			if(controltype==1) {
				controller = Controllers.getControllers().get(0);
			}
		}
		public Player(Vector3 pos, int type, Controller controller, int skintype, ArrayList<Integer> colors, String socketid, boolean ismainplayer) {
			sprite = new FrameworkMO.SpriteObjectSqr("hedtubr.png",pos.x,pos.y,24,24,0,0,true);
			animrw = new FrameworkMO.AnimationSet("hedtubwr.png",4,1,0.2f);
			animlw = new FrameworkMO.AnimationSet("hedtubwl.png",4,1,0.2f);
			movevect = new Vector3();
			controltype = type;
			this.socketid = socketid;
			this.skintype = skintype;
			if(colors.contains(this.skintype)) {
				int i = 1;
				while (colors.contains(i)) {
					i++;
				}
				this.skintype = i;
			}

			this.ismainplayer = ismainplayer;
			this.joined = ismainplayer;
			colors.add(this.skintype);

			if(Controllers.getControllers().size==0) controltype = 0;
			if(controltype==1) {
				this.controller = controller;
			}
		}
		public TextureRegion updatePlayerPos(){
			TextureRegion playertext = null;
			int rightmove = (controltype==0 ||controller==null? Gdx.input.isKeyPressed(Input.Keys.D) : (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d > .25) ? 1 : 0;
			int leftmove = (controltype==0 ||controller==null? Gdx.input.isKeyPressed(Input.Keys.A) : (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d < -.25) ? 1 : 0;
			int netmove = (rightmove-leftmove);
			Rectangle playercol = MovementMath.DuplicateRect(sprite.collision);
			playertext = new TextureRegion(new Texture("p"+skintype+"body.png"));
			boolean jumped = false;

			boolean justshot = false;

			if(!pause) {
				SLOWSPEED = 1;
				if (PlayerList.size() == 1 && (controltype == 0||controller==null ? Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) : controller.getButton(controller.getMapping().buttonL1))) {
					SLOWSPEED = 0.25f;
				}

				if (netmove != 0) {
					float movespeed = WALK_SPEED; //(controltype == 0||controller==null ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? RUN_SPEED :
					horzmove = netmove * (movespeed) + movevect.x;
					horzmove = Math.max(Math.min(movevect.x + horzmove, movespeed), -movespeed) - movevect.x;
					movedir = rightmove - leftmove;
				}

				animlw.framereg = (controltype == 0||controller==null ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? 0.1f : 0.2f;
				animrw.framereg = (controltype == 0||controller==null ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? 0.1f : 0.2f;

				if (movevect.y < 10*MULT_AMOUNT) movevect.y -= GRAVITY*MULT_AMOUNT*Gdx.graphics.getDeltaTime() * SLOWSPEED;

				boolean grounded = MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(0, -1, 0), new Vector3(15, 15, 0));
				boolean wallsliding = ((MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(1, 0, 0), new Vector3(15, 15, 0)) && rightmove == 1) || (MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(-1, 0, 0), new Vector3(15, 15, 0)) && leftmove == 1));
				if (grounded) jumpcount = 5;

				if ((controltype == 0||controller==null ? Gdx.input.isKeyJustPressed(Input.Keys.F) : jumpbutton)&& jumpcount > 0) {
					int degree = MovementMath.toDegrees(controller);
					if (degree != -1) {
						float movex = 0;
						float movey = 0;
						int turnfactor = 0;
						switch (degree) {
							case 0: {
								movey = JUMP_SPEED;
								turnfactor = -8;
								break;
							}
							case 45: {
								movex = -JUMP_SPEED;
								movey = JUMP_SPEED;
								turnfactor = -8;
								break;
							}
							case 90: {
								movex = -JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 135: {
								movex = -JUMP_SPEED;
								movey = -JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 180: {
								movey = -JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 225: {
								movex = JUMP_SPEED;
								movey = -JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 270: {
								movex = JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 315: {
								movex = JUMP_SPEED;
								movey = JUMP_SPEED;
								turnfactor = -8;
								break;
							}
						}
						PoofCloudList.add(new PoofCloud(degree, new Vector3(sprite.x, sprite.y, 0)));
						jumped = true;
						jumpcount--;
						if (jumpdiradd == 0) {
							jumpdiradd = movedir * turnfactor;
							if (movedir < 0)
								jumpdir = -360;
							else
								jumpdir = 360;
						}
						if (movevect.y < 0) movevect.y = 0;

						if (!wallsliding) {
							movevect.x += movex;
							movevect.y += movey;
						} else {
							movevect.x = -movedir * 4.5f*MULT_AMOUNT;
							movevect.y += 3*MULT_AMOUNT;
						}
						movevect.y = Math.min(movevect.y, 5*MULT_AMOUNT);

						if(ismainplayer)
							lastdir = degree;
					}
				} else {
					if (wallsliding) movevect.y = -1*MULT_AMOUNT;
				}

				if (MovementMath.toDegrees(controller) != -1 && ismainplayer) lastdir = MovementMath.toDegrees(controller);

				if ((controltype == 0||controller==null ? Gdx.input.isKeyJustPressed(Input.Keys.G) : firebutton)) {
					BulletList.add(new Bullet(lastdir + 180, new Vector3(sprite.x + 4, sprite.y + 4, 0), "p" + skintype + "bullet.png", this));
					justshot = true;
				}

				playercol = MovementMath.DuplicateRect(sprite.collision);

				if (MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3((movevect.x + horzmove) * Gdx.graphics.getDeltaTime() * SLOWSPEED, 0, 0), new Vector3(15, 15, 0))) {
					float sign = Math.abs(movevect.x + horzmove) / (movevect.x + horzmove);
					while (!MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(sign, 0, 0), new Vector3(15, 15, 0))) {
						sprite.addPosition(new Vector3(sign, 0, 0));
						playercol = MovementMath.DuplicateRect(sprite.collision);
					}
					movevect.x = 0;
					horzmove = 0;
				}
				sprite.addPosition(new Vector3((movevect.x + horzmove) * Gdx.graphics.getDeltaTime() * SLOWSPEED, 0, 0));

				playercol = MovementMath.DuplicateRect(sprite.collision);
				if (MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(0, (movevect.y) * Gdx.graphics.getDeltaTime() * SLOWSPEED, 0), new Vector3(15, 15, 0))) {
					float sign = Math.abs(movevect.y) / movevect.y;
					while (!MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(0, sign, 0), new Vector3(15, 15, 0))) {
						sprite.addPosition(new Vector3(0, sign, 0));
						playercol = MovementMath.DuplicateRect(sprite.collision);
					}
					movevect.y = 0;
				}
				sprite.addPosition(new Vector3(0, (movevect.y) * Gdx.graphics.getDeltaTime() * SLOWSPEED, 0));

				moverot -= ((movevect.x + horzmove) * Gdx.graphics.getDeltaTime() * SLOWSPEED) * 5;
				movevect.x *= .88f;
				horzmove *= (grounded ? .5f : .88f);

				if (sprite.getPosition().y < -16) sprite.setPosition(sprite.x, WORLD_HEIGHT * TILE_WIDTH + 16);
				if (sprite.getPosition().y > WORLD_HEIGHT * TILE_WIDTH + 16) sprite.setPosition(sprite.x, -16);
				if (sprite.getPosition().x < -16) sprite.setPosition(WORLD_WIDTH * TILE_WIDTH + 16, sprite.y);
				if (sprite.getPosition().x > WORLD_WIDTH * TILE_WIDTH + 16) sprite.setPosition(-16, sprite.y);
			}

			JSONObject senddata = new JSONObject();
			senddata.put("event", new JSONString("updatePos"));
			senddata.put("x",new JSONString(sprite.x+""));
			senddata.put("y",new JSONString(sprite.y+""));
			senddata.put("bodyrot",new JSONString(moverot+""));
			senddata.put("eyerot",new JSONString(lastdir+90+""));
			senddata.put("dead",new JSONString((deadcount>0)+""));
			senddata.put("jumped",new JSONString(jumped+""));
			senddata.put("justshot",new JSONString(justshot+""));
			socket.send(JsonUtils.stringify(senddata.getJavaScriptObject()));

			return playertext;
		}
		public TextureRegion getPlayerText(){
			return new TextureRegion(new Texture("p"+skintype+"body.png"));
		}

		public FrameworkMO.TextureSet getEyeText() {
			Vector3 addpos;
			if(ismainplayer)
				addpos = MovementMath.lengthDir((float)Math.toRadians(lastdir+90),1.8f);
			else
				addpos = MovementMath.lengthDir((float)Math.toRadians((float)Math.toDegrees(lastdir)),1.8f);

			float xpos = sprite.collision.x +addpos.x;
			float ypos = sprite.collision.y-0.5f+addpos.y;

			if(ismainplayer)
				return new FrameworkMO.TextureSet(new TextureRegion(new Texture("eyes.png")),xpos,ypos,10000,(float)Math.toRadians(lastdir+90));
			else
				return new FrameworkMO.TextureSet(new TextureRegion(new Texture("eyes.png")),xpos,ypos,10000,(float)Math.toRadians((float)Math.toDegrees(lastdir)));
		}
		public void updateControls() {
			if(controltype == 1&&controller!=null) {
				firebutton = false;
				jumpbutton = false;

				if(firebuttonrelease&&controller.getButton(controller.getMapping().buttonA)&&!firebutton) firebutton = true;
                firebuttonrelease = !controller.getButton(controller.getMapping().buttonA);

				if(jumpbuttonrelease&&controller.getButton(controller.getMapping().buttonB)&&!jumpbutton) jumpbutton = true;
                jumpbuttonrelease = !controller.getButton(controller.getMapping().buttonB);


				pressright = false;
				boolean rdown = (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d > .45;

				if (rdown&&releaseright) {
					pressright = true;
				}

				if(!rdown) releaseright = true;
				else releaseright = false;

				pressleft = false;
				boolean ldown = (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d < -.45;

				if (ldown&&releaseleft) {
					pressleft = true;
				}

				if(!ldown) releaseleft = true;
				else releaseleft = false;
			}
		}
		public void takeDamage(){
			health--;
			dmgcount = .5f;
			if(ismainplayer)
				addShake(0.6f);
			healthwheel.incrementTime();
			if(health<=0) {
				PlayerList.remove(this);
				DeletedPlayerList.add(this);
				addShake(0.6f);
				PoofCloudList.add(new PoofCloud(0,new Vector3(sprite.x-16,sprite.y-16,0),1));
			}
		}
		public void countDead(){
			deadcount+=Gdx.graphics.getDeltaTime();
			if(deadcount>=2) {
				DeletedPlayerList.remove(this);
				PlayerList.add(this);
				health = 8;
				healthwheel.time = 0;
				deadcount = 0;
				movevect = new Vector3();

				float addx = 160;
				float addy = 80;
				if(seed.nextInt(2)==0) {
					addx = WORLD_WIDTH*TILE_WIDTH - 160;
				}
				if(seed.nextInt(2)==0) {
					addy = WORLD_HEIGHT*TILE_WIDTH - 80;
				}
				sprite.setPosition(addx,addy);
			}
		}
	}

	public WebSocket configSocket() {
		//localhost: ws://localhost:8090
		//graham server: wss://hegog.frc.autos

		WebSocket holdsocket = WebSockets.newSocket("ws://localhost:8090");
		holdsocket.setSendGracefully(true);
		holdsocket.addListener(new WebSocketListener() {
			@Override
			public boolean onOpen(WebSocket webSocket) {
				//Gdx.app.log("Log", "Open");
				return false;
			}

			@Override
			public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
				//Gdx.app.log("Log", "Close");
				return false;
			}

			@Override
			public boolean onMessage(WebSocket webSocket, String packet) {
				JSONObject data = JSONParser.parse(packet).isObject();
				String event = data.get("event").isString().stringValue();
				//Gdx.app.log("Log", "Packet Message: " + data);

				if (event.equals("setWS")) {
					seed = new Random((long)data.get("seed").isNumber().doubleValue());
					WORLD_MAP = FrameworkMO.getMap(seed);
					id = data.get("id").isString().stringValue();
					JSONArray playerarray = data.get("playerlist").isArray();
					for(int i = 0; i<playerarray.size();i++) {
						if(!playerarray.get(i).isObject().get("ingame").isBoolean().booleanValue()) {
							Player newplayer = new OnlineManager.Player(
								new Vector3((float) (playerarray.get(i).isObject().get("x").isNumber().doubleValue()), (float) (playerarray.get(i).isObject().get("y").isNumber().doubleValue()), 0),
								-1,
								playerarray.get(i).isObject().get("id").isString().stringValue(),
								false
							);
							PlayerList.add(newplayer);
							newplayer.ready = (playerarray.get(i).isObject().get("ready").isBoolean().booleanValue());
							PlayerList.get(PlayerList.size() - 1).skintype = (int) (playerarray.get(i).isObject().get("skin").isNumber().doubleValue());
							PlayerList.get(PlayerList.size() - 1).ready = (playerarray.get(i).isObject().get("ready").isBoolean().booleanValue());
							PlayerList.get(PlayerList.size() - 1).joined = (playerarray.get(i).isObject().get("joined").isBoolean().booleanValue());
						}
					}
				}

				if (event.equals("joinPlayer")) {
					Player newplayer = new OnlineManager.Player(
						new Vector3((float)(data.get("player").isObject().get("x").isNumber().doubleValue()),(float)(data.get("player").isObject().get("y").isNumber().doubleValue()),0),
						-1,
						data.get("player").isObject().get("id").isString().stringValue(),
						false
					);
					PlayerList.add(newplayer);
					newplayer.ready = (data.get("player").isObject().get("ready").isBoolean().booleanValue());
					newplayer.joined = (data.get("player").isObject().get("joined").isBoolean().booleanValue());
				}

				if (event.equals("startGame")) {
					addShake(.4f);
					if(PlayerList.size()>0)
						serverready = true;
				}

				if (event.equals("updateOtherSkin")) {
					Player targetplayer = PlayerList.get(findPlayer(data.get("id").isString().stringValue()));
					targetplayer.skintype = (int)(data.get("skin").isNumber().doubleValue());
				}

				if (event.equals("updateOtherReady")) {
					Player targetplayer = PlayerList.get(findPlayer(data.get("id").isString().stringValue()));
					if(!targetplayer.ready&&data.get("ready").isBoolean().booleanValue())
						addShake(.4f);
					targetplayer.ready = data.get("ready").isBoolean().booleanValue();
				}

				if (event.equals("updateOtherJoined")) {
					Player targetplayer = PlayerList.get(findPlayer(data.get("id").isString().stringValue()));
					if(!targetplayer.joined&&data.get("joined").isBoolean().booleanValue())
						addShake(.4f);
					targetplayer.joined = data.get("joined").isBoolean().booleanValue();
				}

				if (event.equals("updateOtherPos")) {
					JSONObject playerobj = data.get("player").isObject();
					int targetpi = findPlayer(playerobj.get("id").isString().stringValue());
					if(targetpi!=-1) {
						Player targetplayer = PlayerList.get(targetpi);
						targetplayer.sprite.setPosition((float) (playerobj.get("x").isNumber().doubleValue()), (float) (playerobj.get("y").isNumber().doubleValue()));
						targetplayer.moverot = (float) (playerobj.get("bodyrot").isNumber().doubleValue());
						targetplayer.lastdir = (float) Math.toRadians(playerobj.get("eyerot").isNumber().doubleValue());

						if (data.get("deadcloud").isBoolean().booleanValue()&&!pause) {
							Vector3 pos = targetplayer.sprite.getPosition();
							PoofCloudList.add(new PoofCloud((float) Math.toDegrees(targetplayer.lastdir) - 90, new Vector3(pos.x - 16, pos.y - 16, 0), 1));
						}

						if (data.get("jumpcloud").isBoolean().booleanValue()&&!pause) {
							Vector3 pos = targetplayer.sprite.getPosition();
							PoofCloudList.add(new PoofCloud((float) Math.toDegrees(targetplayer.lastdir) - 90, new Vector3(pos.x, pos.y, 0), 0));
						}

						if (playerobj.get("justshot").isBoolean().booleanValue()&&!pause) {
							Vector3 pos = targetplayer.sprite.getPosition();
							BulletList.add(new Bullet((float) Math.toDegrees(targetplayer.lastdir) + 90, new Vector3(pos.x + 4, pos.y + 4, 0), "p" + targetplayer.skintype + "bullet.png", targetplayer));
						}
					}
				}

				if (event.equals("leavePlayer")) {
					PlayerList.remove(PlayerList.get(findPlayer(data.get("player").isObject().get("id").isString().stringValue())));
				}
				return false;
			}

			@Override
			public boolean onMessage(WebSocket webSocket, byte[] packet) {
				Gdx.app.log("Log", "Byte Message: ");
				return false;
			}

			@Override
			public boolean onError(WebSocket webSocket, Throwable error) {
				//Gdx.app.log("Log", "Error");
				return false;
			}
		});

		holdsocket.connect();
		return holdsocket;
	}

	public int findPlayer(String wsstring){
		for(int i = 0; i< PlayerList.size();i++)
			if(PlayerList.get(i).socketid.equals(wsstring))
				return i;
		return -1;
	}
}