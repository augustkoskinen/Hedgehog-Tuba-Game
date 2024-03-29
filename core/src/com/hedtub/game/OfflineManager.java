package com.hedtub.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

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

public class OfflineManager implements Screen {
	//world vars
	SpriteBatch batch;
	public MainMenuScreen game;
	public static final int TILE_WIDTH = 16;
	public static final int WORLD_WIDTH = 46;
	public static final int WORLD_HEIGHT = 26;
	public static final float MULT_AMOUNT = 55;
	public static float WIND_WIDTH = 1024;
	public static float WIND_HEIGHT = 576;
	public static float CHANGE_RATIO = 1;
	public static float SLOWSPEED = 1;
	public static int[][] WORLD_MAP = FrameworkMO.getMap();
	public String launcher;
	public FrameworkMO.AnimationSet openbattle;
	public static float countopen = 0.875f;
	public static OrthographicCamera gamecam  = null;
	public static boolean gamestarted = false;
	public Texture healthback;
	public float waitstart = 0;
	public FrameworkMO.AnimationSet startanim;

	//player
	public static int numplayers = 0;
	public static final float GRAVITY = 0.25f*MULT_AMOUNT;
	public static final float WALK_SPEED = 2*MULT_AMOUNT;
	public static final float JUMP_SPEED = 5.2f*MULT_AMOUNT;

	//ArrayLists
	public ArrayList<Rectangle> CollisionList;
	public static ArrayList<PoofCloud> PoofCloudList;
	public static ArrayList<Bullet> BulletList;
	public static ArrayList<Monster> MonsterList = new ArrayList<>();
	public static ArrayList<Player> PlayerList = new ArrayList<>();
	public static ArrayList<Player> DeletedPlayerList = new ArrayList<>();
	public static ArrayList<Controller> ControllerList = new ArrayList<>();
	public static boolean keyboardtaken = false;
	public static boolean pause = true;
	public static boolean shake = false;
	public static float shaketime = 0.7f;
	public static Vector3 loadjiggle = new Vector3();
	//main
	public OfflineManager(MainMenuScreen game, String str) {
		launcher = str;
		this.game = game;
		CollisionList = new ArrayList<>();
		PoofCloudList = new ArrayList<>();
		BulletList = new ArrayList<>();

		batch = new SpriteBatch();

		openbattle = new FrameworkMO.AnimationSet("openbattle.png",36,1,0.025f);
		startanim = new FrameworkMO.AnimationSet("countdown.png",3,1,1f);
		healthback = new Texture("healthback.png");

		if(launcher.equals("html")) {
			WIND_WIDTH = Gdx.graphics.getWidth();
			WIND_HEIGHT = Gdx.graphics.getHeight();
			CHANGE_RATIO = Gdx.graphics.getWidth()/1024f;
		}
	}

	@Override
	public void show() {
		ChooseScreen newscreen = new ChooseScreen(this, game);
		game.setScreen(newscreen);
	}

	@Override
	public void render(float delta) {
		batch.begin();

		if(gamestarted)
			for(int i =0; i<numplayers;i++) {
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
			loadjiggle = new Vector3((shaketime*2)*((int)(Math.random()*2)==0 ? 1f : -1f),(shaketime*2)*((int)(Math.random()*2)==0 ? 1f : -1f),0);

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

		if(shaketime>0) shaketime-=Gdx.graphics.getDeltaTime();
		else {shaketime = .7f; shake = false; loadjiggle = new Vector3();
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

	public static class Monster {
		private float life = 0;
		private float dir;
		private float speed = 1.5f;
		private Vector3 pos;
		private Vector3 floatpos = new Vector3(0,2,0);
		private float floatadd = 0.1f;
		private FrameworkMO.AnimationSet animr;
		private FrameworkMO.AnimationSet animl;
		public Rectangle collision;

		public Monster(Vector3 pos, int type){
			this.pos = pos;
			switch (type) {
				case 0 : {
					animl = new FrameworkMO.AnimationSet("ghostl.png",2,1,0.5f);
					animr = new FrameworkMO.AnimationSet("ghostr.png",2,1,0.5f);
					life = 5;
					collision = new Rectangle(pos.x,pos.y,64,64);

					break;
				}
			}
		}
		public FrameworkMO.TextureSet updateTime(Vector3 aimpos) {
			dir = (float) Math.toDegrees(MovementMath.pointDir(new Vector3(pos.x+32,pos.y+32,0), new Vector3(aimpos.x+8,aimpos.y+8, 0)));
			float dis = MovementMath.pointDis(new Vector3(pos.x+32,pos.y+32,0), new Vector3(aimpos.x+8,aimpos.y+8, 0));

			Vector3 addvect;
			if(dis>96)
				addvect = MovementMath.lengthDir((float)Math.toRadians(dir),speed*Gdx.graphics.getDeltaTime()*MULT_AMOUNT).add(floatpos);
			else
				addvect = MovementMath.lengthDir((float)Math.toRadians(dir+85),speed*Gdx.graphics.getDeltaTime()*MULT_AMOUNT).add(floatpos);

			if(!pause) {
				pos = new Vector3(pos.x + addvect.x * SLOWSPEED, pos.y + addvect.y * SLOWSPEED, 0);
				collision.x += addvect.x * SLOWSPEED;
				collision.y += addvect.y * SLOWSPEED;
			}

			if(life < 0) {
				MonsterList.remove(this);
				return null;
			}

			floatpos.y+=floatadd;
			if(floatpos.y>2&&floatadd == 0.1f) {
				floatadd = -0.1f;
			}
			if(floatpos.y<-2&&floatadd == -0.1f) {
				floatadd = 0.1f;
			}

			if(!pause)
				return dir<-90||dir>90 ? new FrameworkMO.TextureSet(animl.updateTime(SLOWSPEED),pos.x,pos.y,100000,(float)Math.toRadians(dir-90)) : new FrameworkMO.TextureSet(animr.updateTime(SLOWSPEED),pos.x,pos.y,100000,(float)Math.toRadians(dir-90));
			else
				return dir<-90||dir>90 ? new FrameworkMO.TextureSet(animl.updateTime(SLOWSPEED),pos.x,pos.y,100000,(float)Math.toRadians(dir-90)) : new FrameworkMO.TextureSet(animr.getAnim(),pos.x,pos.y,100000,(float)Math.toRadians(dir-90));
		}

		public void takeDamage() {
			life--;
			if(life<=0) {
				MonsterList.remove(this);
			}
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
		public int jumpcount = 5;
		public int jumpdir = 0;
		public double moverot = 0;
		public int jumpdiradd = 0;
		public int controltype;
		public int skintype = 1;
		public int prevskintype = 1;
		public Controller controller;
		public boolean chosechar = false;
		public boolean firebutton = false;
		public boolean jumpbutton = false;
		public boolean firebuttonrelease = false;
		public boolean jumpbuttonrelease = false;
		public boolean pressright = false;
		public boolean pressleft = false;
		public boolean releaseright = false;
		public boolean releaseleft = false;
		public float deadcount = 0;

		public Player(Vector3 pos, int type, ArrayList<Integer> colors) {
			sprite = new FrameworkMO.SpriteObjectSqr("hedtubr.png",pos.x,pos.y,24,24,0,0,true);
			animrw = new FrameworkMO.AnimationSet("hedtubwr.png",4,1,0.2f);
			animlw = new FrameworkMO.AnimationSet("hedtubwl.png",4,1,0.2f);
			movevect = new Vector3();
			controltype = type;

			if(colors.contains(this.skintype)) {
				int i = 1;
				while (colors.contains(i)) {
					i++;
				}
				this.skintype = i;
			}

			colors.add(this.skintype);

			if(Controllers.getControllers().size==0) controltype = 0;
			if(controltype==1) {
				controller = Controllers.getControllers().get(0);
			}
		}
		public Player(Vector3 pos, int type, Controller controller,int skintype, ArrayList<Integer> colors) {
			sprite = new FrameworkMO.SpriteObjectSqr("hedtubr.png",pos.x,pos.y,24,24,0,0,true);
			animrw = new FrameworkMO.AnimationSet("hedtubwr.png",4,1,0.2f);
			animlw = new FrameworkMO.AnimationSet("hedtubwl.png",4,1,0.2f);
			movevect = new Vector3();
			controltype = type;
			this.skintype = skintype;

			if(colors.contains(this.skintype)) {
				int i = 1;
				while (colors.contains(i)) {
					i++;
				}
				this.skintype = i;
			}

			colors.add(this.skintype);

			if(Controllers.getControllers().size==0) controltype = 0;
			if(controltype==1) {
				this.controller = controller;
			}
		}
		public TextureRegion updatePlayerPos(){
			TextureRegion playertext = null;
			int rightmove = (controltype==0 ? Gdx.input.isKeyPressed(Input.Keys.D) : (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d > .25) ? 1 : 0;
			int leftmove = (controltype==0 ? Gdx.input.isKeyPressed(Input.Keys.A) : (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d < -.25) ? 1 : 0;
			int netmove = (rightmove-leftmove);
			Rectangle playercol = MovementMath.DuplicateRect(sprite.collision);
			playertext = new TextureRegion(new Texture("p"+skintype+"body.png"));

			if(!pause) {
				SLOWSPEED = 1;
				if (numplayers == 1 && (controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) : controller.getButton(controller.getMapping().buttonL1))) {
					SLOWSPEED = 0.25f;
				}

				if (netmove != 0) {
					float movespeed = WALK_SPEED; //(controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? RUN_SPEED :
					horzmove = netmove * (movespeed) + movevect.x;
					horzmove = Math.max(Math.min(movevect.x + horzmove, movespeed), -movespeed) - movevect.x;
					movedir = rightmove - leftmove;
				}

				animlw.framereg = (controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? 0.1f : 0.2f;
				animrw.framereg = (controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? 0.1f : 0.2f;

				if (movevect.y < 10*MULT_AMOUNT) movevect.y -= GRAVITY*MULT_AMOUNT*Gdx.graphics.getDeltaTime() * SLOWSPEED;

				boolean grounded = MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(0, -1, 0), new Vector3(15, 15, 0));
				boolean wallsliding = ((MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(1, 0, 0), new Vector3(15, 15, 0)) && rightmove == 1) || (MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(-1, 0, 0), new Vector3(15, 15, 0)) && leftmove == 1));
				if (grounded) jumpcount = 5;

				if ((controltype == 0 ? Gdx.input.isKeyJustPressed(Input.Keys.F) : jumpbutton)&& jumpcount > 0) {
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

						lastdir = degree;
					}
				} else {
					if (wallsliding) movevect.y = -1*MULT_AMOUNT;
				}

				if (MovementMath.toDegrees(controller) != -1) lastdir = MovementMath.toDegrees(controller);

				if ((controltype == 0 ? Gdx.input.isKeyJustPressed(Input.Keys.G) : firebutton)) {
					BulletList.add(new Bullet(lastdir + 180, new Vector3(sprite.x + 4, sprite.y + 4, 0), "p" + skintype + "bullet.png", this));
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
			return playertext;
		}

		public FrameworkMO.TextureSet getEyeText() {
			Vector3 addpos = MovementMath.lengthDir((float)Math.toRadians(lastdir+90),1.8f);

			float xpos = sprite.collision.x +addpos.x;
			float ypos = sprite.collision.y-0.5f+addpos.y;

			return new FrameworkMO.TextureSet(new TextureRegion(new Texture("eyes.png")),xpos,ypos,10000,(float)Math.toRadians(lastdir+90));
		}
		public void updateControls() {
			if(controltype == 1) {
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
			addShake(0.3f);
			healthwheel.incrementTime();
			if(health<=0) {
				PlayerList.remove(this);
				DeletedPlayerList.add(this);
				numplayers--;
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
				numplayers++;
				healthwheel.time = 0;
				deadcount = 0;
				movevect = new Vector3();

				float addx = 160;
				float addy = 80;
				if((int)(Math.random()*2)==0) {
					addx = WORLD_WIDTH*TILE_WIDTH - 160;
				}
				if((int)(Math.random()*2)==0) {
					addy = WORLD_HEIGHT*TILE_WIDTH - 80;
				}
				sprite.setPosition(addx,addy);
			}
		}

	}
}