package com.hedtub.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.Sprite;
import java.util.ArrayList;

import com.badlogic.gdx.math.Vector3;
import com.hedtub.game.FrameworkMO;

public class HedTub extends Game {
	//world vars
	SpriteBatch batch;
	public static final int TILE_WIDTH = 16;
	public static final int WORLD_WIDTH = 46;
	public static final int WORLD_HEIGHT = 26;
	public static float SLOWSPEED = 1;
	public static int[][] WORLD_MAP = FrameworkMO.getMap();
	public String launcher;

	//player
	public int numplayers = 0;
	public static Player mainplayer;
	public static final float GRAVITY = 0.2f;
	public static final float RUN_SPEED = 3;
	public static final float WALK_SPEED = 2f;
	public static final float JUMP_SPEED = 5.2f;

	//ArrayLists
	public ArrayList<Rectangle> CollisionList;
	public ArrayList<FrameworkMO.TransitionBox> TransitionList;
	public static ArrayList<PoofCloud> PoofCloudList;
	public static ArrayList<Bullet> BulletList;
	public static ArrayList<HedTub.Monster> MonsterList = new ArrayList<>();
	public static ArrayList<HedTub.Player> PlayerList = new ArrayList<>();
	public static ArrayList<Controller> ControllerList = new ArrayList<>();
	public static boolean keyboardtaken = false;
	public boolean pause = false;
	//main
	public HedTub(String str) {
		launcher = str;
	}

	@Override
	public void create() {
		CollisionList = new ArrayList<>();
		TransitionList = new ArrayList<>();
		PoofCloudList = new ArrayList<>();
		BulletList = new ArrayList<>();

		batch = new SpriteBatch();

		//start
		this.setScreen(new ChooseScreen(this));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	//=============|
	//OBJECTS
	//=============|


	public static class PoofCloud {
		private final float life = 0.7f;
		private float time = 0;
		private float dir;
		private float speed = 1;
		private FrameworkMO.AnimationSet animation;
		private Vector3 pos = new Vector3();

		public PoofCloud(float dir, Vector3 pos){
			animation = new FrameworkMO.AnimationSet("poof.png",7,1,0.1f);
			this.pos = pos;
			this.dir = dir;
		}
		public FrameworkMO.TextureSet updateTime(){
			Vector3 addvect = MovementMath.lengthDir((float)Math.toRadians(dir-90),speed);
			pos = new Vector3(pos.x+addvect.x,pos.y+addvect.y,0);

			time+=Gdx.graphics.getDeltaTime();
			speed*=.9f;

			if(time>=life) {
				PoofCloudList.remove(this);
				return null;
			}

			return new FrameworkMO.TextureSet(animation.updateTime(SLOWSPEED),pos.x,pos.y,100000,(float)Math.toRadians(dir));
		}
	}

	public static class Bullet {
		private final float life = 0.7f;
		private float time = 0;
		private float dir;
		private float speed = 10;
		private Vector3 pos = new Vector3();
		private Texture text;
		public Rectangle collision;

		public Bullet(float dir, Vector3 pos){
			text = new Texture("bullet.png");
			this.pos = pos;
			this.dir = dir;

			collision = new Rectangle(pos.x,pos.y,6,2);
		}
		public FrameworkMO.TextureSet updateTime(){
			Vector3 addvect = MovementMath.lengthDir((float)Math.toRadians(dir-90),speed);
			pos = new Vector3(pos.x+addvect.x*SLOWSPEED,pos.y+addvect.y*SLOWSPEED,0);
			collision.x+=addvect.x*SLOWSPEED;
			collision.y+=addvect.y*SLOWSPEED;

			time+=Gdx.graphics.getDeltaTime()*SLOWSPEED;

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
				addvect = MovementMath.lengthDir((float)Math.toRadians(dir),speed).add(floatpos);
			else
				addvect = MovementMath.lengthDir((float)Math.toRadians(dir+85),speed).add(floatpos);

			pos = new Vector3(pos.x+addvect.x*SLOWSPEED,pos.y+addvect.y*SLOWSPEED,0);
			collision.x+=addvect.x*SLOWSPEED;
			collision.y+=addvect.y*SLOWSPEED;

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

			return dir<-90||dir>90 ? new FrameworkMO.TextureSet(animl.updateTime(SLOWSPEED),pos.x,pos.y,100000,(float)Math.toRadians(dir-90)) : new FrameworkMO.TextureSet(animr.updateTime(SLOWSPEED),pos.x,pos.y,100000,(float)Math.toRadians(dir-90));
		}

		public void takeDamage() {
			life--;
			if(life<=0) {
				MonsterList.remove(this);
			}
		}
	}

	public static class Player {
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
		public Controller controller;
		private boolean firebutton = false;
		private boolean jumpbutton = false;
		private boolean firebuttonrelease = false;
		private boolean jumpbuttonrelease = false;

		public Player(Vector3 pos, int type) {
			sprite = new FrameworkMO.SpriteObjectSqr("hedtubr.png",pos.x,pos.y,24,24,0,0,true);
			animrw = new FrameworkMO.AnimationSet("hedtubwr.png",4,1,0.2f);
			animlw = new FrameworkMO.AnimationSet("hedtubwl.png",4,1,0.2f);
			movevect = new Vector3();
			controltype = type;

			if(Controllers.getControllers().size==0) controltype = 0;
			if(controltype==1) {
				controller = Controllers.getControllers().get(0);
			}
		}
		public Player(Vector3 pos, int type, Controller controller) {
			sprite = new FrameworkMO.SpriteObjectSqr("hedtubr.png",pos.x,pos.y,24,24,0,0,true);
			animrw = new FrameworkMO.AnimationSet("hedtubwr.png",4,1,0.2f);
			animlw = new FrameworkMO.AnimationSet("hedtubwl.png",4,1,0.2f);
			movevect = new Vector3();
			controltype = type;

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

			SLOWSPEED = 1;
			if ((controltype==0 ? Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) : controller.getButton(controller.getMapping().buttonL1))) {
				SLOWSPEED = 0.25f;
			}

			if(netmove!=0) {
				float movespeed = (controltype==0 ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? RUN_SPEED : WALK_SPEED;
				horzmove = netmove*(movespeed)+movevect.x;
				horzmove = Math.max(Math.min(movevect.x+horzmove,movespeed),-movespeed)-movevect.x;
				movedir = rightmove-leftmove;
			}

			animlw.framereg = (controltype==0 ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? 0.1f : 0.2f;
			animrw.framereg = (controltype==0 ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? 0.1f : 0.2f;

			if (movedir==1) {
				playertext = new TextureRegion(new Texture("body.png"));
			} else if (movedir==-1) {
				playertext = new TextureRegion(new Texture("body.png"));
			}

			if (movevect.y < 10) movevect.y -= GRAVITY*SLOWSPEED;

			boolean grounded = MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(0,-1,0),new Vector3(15,15,0));
			boolean wallsliding = ((MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(1,0,0),new Vector3(15,15,0))&&rightmove==1)||(MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(-1,0,0),new Vector3(15,15,0))&&leftmove==1));
			if(grounded) jumpcount = 5;

			if(controltype==1&&jumpbuttonrelease&&controller.getButton(controller.getMapping().buttonB)&&!jumpbutton) jumpbutton = true;
			if(controltype==1&&!controller.getButton(controller.getMapping().buttonB)) jumpbuttonrelease = true; else jumpbuttonrelease = false;

			if ((controltype==0 ? Gdx.input.isKeyJustPressed(Input.Keys.F) : jumpbutton)&&jumpcount>0) {
				int degree = MovementMath.toDegrees(controller);
				if(degree!=-1) {
					float movex = 0;
					float movey = 0;
					int turnfactor = 0;
					switch (degree) {
						case 0 : {
							movey = JUMP_SPEED;
							turnfactor = -8;
							break;
						}
						case 45 : {
							movex = -JUMP_SPEED;
							movey = JUMP_SPEED;
							turnfactor = -8;
							break;
						}
						case 90 : {
							movex = -JUMP_SPEED;
							turnfactor = -36;
							break;
						}
						case 135 : {
							movex = -JUMP_SPEED;
							movey = -JUMP_SPEED;
							turnfactor = -36;
							break;
						}
						case 180 : {
							movey = -JUMP_SPEED;
							turnfactor = -36;
							break;
						}
						case 225 : {
							movex = JUMP_SPEED;
							movey = -JUMP_SPEED;
							turnfactor = -36;
							break;
						}
						case 270 : {
							movex = JUMP_SPEED;
							turnfactor = -36;
							break;
						}
						case 315 : {
							movex = JUMP_SPEED;
							movey = JUMP_SPEED;
							turnfactor = -8;
							break;
						}
					}
					PoofCloudList.add(new PoofCloud(degree,new Vector3(sprite.x+4,sprite.y+4,0)));
					jumpcount--;
					if(jumpdiradd==0) {
						jumpdiradd = movedir*turnfactor;
						if(movedir<0)
							jumpdir = -360;
						else
							jumpdir = 360;
					}
					if(movevect.y < 0) movevect.y = 0;

					if(!wallsliding) {
						movevect.x += movex;
						movevect.y += movey;
					} else {
						movevect.x = -movedir * 4.5f;
						movevect.y += 3;
					}
					movevect.y = Math.min(movevect.y,5);

					lastdir = degree;
				}
			} else {
				if(wallsliding) movevect.y = -1;
			}

			if(MovementMath.toDegrees(controller)!=-1) lastdir = MovementMath.toDegrees(controller);

			if(controltype==1&&firebuttonrelease&&controller.getButton(controller.getMapping().buttonA)&&!firebutton) firebutton = true;
			if(controltype==1&&!controller.getButton(controller.getMapping().buttonA)) firebuttonrelease = true; else firebuttonrelease = false;

			if ((controltype==0 ? Gdx.input.isKeyJustPressed(Input.Keys.G) : firebutton)) {
				BulletList.add(new Bullet(lastdir+180,new Vector3(sprite.x+4,sprite.y+4,0)));
			}

			playercol = MovementMath.DuplicateRect(sprite.collision);

			if (MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3((movevect.x+horzmove)*SLOWSPEED,0,0),new Vector3(15,15,0))){
				float sign = Math.abs(movevect.x+horzmove)/(movevect.x+horzmove);
				while(!MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(sign,0,0),new Vector3(15,15,0))){
					sprite.addPosition(new Vector3(sign,0,0));
					playercol = MovementMath.DuplicateRect(sprite.collision);
				}
				movevect.x = 0;
				horzmove = 0;
			}
			sprite.addPosition(new Vector3((movevect.x+horzmove)*SLOWSPEED,0,0));

			playercol = MovementMath.DuplicateRect(sprite.collision);
			if (MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(0,(movevect.y)*SLOWSPEED,0),new Vector3(15,15,0))){
				float sign = Math.abs(movevect.y)/movevect.y;
				while(!MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(0,sign,0),new Vector3(15,15,0))) {
					sprite.addPosition(new Vector3(0,sign,0));
					playercol = MovementMath.DuplicateRect(sprite.collision);
				}
				movevect.y = 0;
			}

			sprite.addPosition(new Vector3(0,(movevect.y)*SLOWSPEED,0));
			moverot -= ((movevect.x+horzmove)*SLOWSPEED)*5;
			movevect.x*=.88f;
			horzmove*=(grounded ? .5f : .88f);
			firebutton = false;
			jumpbutton = false;

			if(sprite.getPosition().y<-16) sprite.setPosition(sprite.x,WORLD_HEIGHT*TILE_WIDTH+16);
			if(sprite.getPosition().y>WORLD_HEIGHT*TILE_WIDTH+16) sprite.setPosition(sprite.x,-16);
			if(sprite.getPosition().x<-16) sprite.setPosition(WORLD_WIDTH*TILE_WIDTH+16,sprite.y);
			if(sprite.getPosition().x>WORLD_WIDTH*TILE_WIDTH+16) sprite.setPosition(-16,sprite.y);

			return playertext;
		}

		public FrameworkMO.TextureSet getEyeText() {
			Vector3 addpos = MovementMath.lengthDir((float)Math.toRadians(lastdir+90),1.8f);

			float xpos = sprite.collision.x +addpos.x;
			float ypos = sprite.collision.y-0.5f+addpos.y;

			return new FrameworkMO.TextureSet(new TextureRegion(new Texture("eyes.png")),xpos,ypos,10000,(float)Math.toRadians(lastdir+90));
		}
	}
}