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
	public final int TILE_WIDTH = 32;
	public final int WORLD_WIDTH = 33;
	public final int WORLD_HEIGHT = 33;
	public final float GRAVITY = 0.2f;
	public final float FALL_SPEED = 0.4f;
	public final float RUN_SPEED = 3;
	public final float WALK_SPEED = 2f;
	public final float JUMP_SPEED = 5.2f;
	public final int[][] WORLD_MAP = new int[WORLD_WIDTH][WORLD_HEIGHT];

	//transition vars
	public Sprite transspr;
	public float transtime = -1f;
	public float fadeintime = -1f;
	public String transroom = "transroom";
	public String launcher;
	public boolean transchange = false;
	public  float transroomx = -1f;
	public  float transroomy = -1f;

	//player
	public FrameworkMO.SpriteObjectSqr player;
	public FrameworkMO.AnimationSet playerrw;
	public FrameworkMO.AnimationSet playerlw;
	public Vector3 movevect;
	public int movedir = 1;
	public int jumpcount = 5;
	public int jumpdir = 0;
	public double moverot = 0;
	public int jumpdiradd = 0;


	//ArrayLists
	public ArrayList<String> textboxArrayList = new ArrayList<>();
	public ArrayList<Rectangle> CollisionList;
	public ArrayList<FrameworkMO.TransitionBox> TransitionList;
	public ArrayList<PoofCloud> PoofCloudList;
	public ArrayList<Bullet> BulletList;

	//booleans for movement
	public  boolean transitioning = false;
	public  boolean incutscene = false;
	public  FrameworkMO.Cutscene curcutscene = null;

	//controller
	Controller controller = null;

	//main
	public HedTub(String str) {
		launcher = str;
	}

	@Override
	public void create() {
		for(int i = 0; i< WORLD_MAP.length; i++){
			for(int j = 0; j < WORLD_MAP[i].length; j++) {
				WORLD_MAP[j][i] = (int)(Math.random()*3) == 0 ? 1 : 0;
			}
		}
		WORLD_MAP[WORLD_WIDTH/2][WORLD_HEIGHT/2] = 0;
		WORLD_MAP[WORLD_WIDTH/2][WORLD_HEIGHT/2-1] = 0;
		WORLD_MAP[WORLD_WIDTH/2][WORLD_HEIGHT/2+1] = 0;
		/*
		if (Controllers.getControllers().size != 0) {
			controller = Controllers.getControllers().first();
		} else {
			controller = null;
		}
		*/

		CollisionList = new ArrayList<>();
		TransitionList = new ArrayList<>();
		PoofCloudList = new ArrayList<>();
		BulletList = new ArrayList<>();

		batch = new SpriteBatch();

		//textures
		//transspr = new Sprite(new Texture("transtext.png"));

		player = new FrameworkMO.SpriteObjectSqr("hedtubr.png",WORLD_HEIGHT/2*32,WORLD_HEIGHT/2*32,24,24,0,0,true);
		playerrw = new FrameworkMO.AnimationSet("hedtubwr.png",4,1,0.2f);
		playerlw = new FrameworkMO.AnimationSet("hedtubwl.png",4,1,0.2f);
		movevect = new Vector3();

		//start
		this.setScreen(new Overworld(this));
	}

	@Override
	public void render() {
		/*
		if (Controllers.getControllers().size != 0) {
			controller = Controllers.getControllers().first();
		} else {
			controller = null;
		}
		*/
		super.render();
		batch.begin();

		//ui stuff here


		//transitions
		if (fadeintime >= 10) {
			fadeintime = -1;
			transtime = -1;
		}

		if (transtime != -1) {
			transspr.draw(batch, (transtime / 10));
		} else if (fadeintime != -1) {
			transspr.draw(batch, Math.max((10 - fadeintime) / 10, 0));
			fadeintime += 10 * Gdx.graphics.getDeltaTime();
		}

		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	public TextureRegion updatePlayerPos(){
		TextureRegion playertext = null;

		int rightmove = Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0;
		int leftmove = Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0;
		int netmove = (rightmove-leftmove);
		Rectangle playercol = MovementMath.DuplicateRect(player.collision);
		Vector3 walkvect = new Vector3();

		if(netmove!=0) {
			float movespeed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ? RUN_SPEED : WALK_SPEED;
			walkvect.x += netmove*(movespeed)+movevect.x;
			walkvect.x = Math.max(Math.min(movevect.x+walkvect.x,movespeed),-movespeed)-movevect.x;
			movedir = rightmove-leftmove;
		}

		playerlw.framereg = (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ? 0.1f : 0.2f);
		playerrw.framereg = (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ? 0.1f : 0.2f);

		if (movedir==1) {
			playertext = new TextureRegion(new Texture("playerr.png"));
		} else if (movedir==-1) {
			playertext = new TextureRegion(new Texture("playerl.png"));
		}

		/*
		if ((movevect.x>0 ? Math.floor(movevect.x) : Math.ceil(movevect.x)) == 0&&movedir==1) {
			playertext = new TextureRegion(new Texture("playerr.png"));
		} else if ((movevect.x>0 ? Math.floor(movevect.x) : Math.ceil(movevect.x)) == 0&&movedir==-1) {
			playertext = new TextureRegion(new Texture("playerl.png"));
		} else if(movevect.x > 0) {
			playertext = playerrw.updateTime();
		} else if(movevect.x < 0) {
			playertext = playerlw.updateTime();
		}
		*/


		if (movevect.y < 10) movevect.y -= (Gdx.input.isKeyPressed(Input.Keys.S) ? FALL_SPEED : GRAVITY);

		boolean grounded = MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(0,-1,0));
		boolean wallsliding = ((MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(1,0,0))&&rightmove==1)||(MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(-1,0,0))&&leftmove==1));
		if(grounded) jumpcount = 5;

		if (Gdx.input.isKeyJustPressed(Input.Keys.F)&&jumpcount>0) {
			int degree = MovementMath.toDegrees();
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
				PoofCloudList.add(new PoofCloud(degree,new Vector3(player.x+4,player.y+4,0)));
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
					movevect.x = -movedir * 4;
					movevect.y += 3;
				}
				movevect.y = Math.min(movevect.y,5);
			}
		} else {
			if(wallsliding) movevect.y = -1;
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
			BulletList.add(new Bullet(MovementMath.toDegrees()+180,new Vector3(player.x+4,player.y+4,0)));
		}

		playercol = MovementMath.DuplicateRect(player.collision);

		if (MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(movevect.x+walkvect.x,0,0))){
			float sign = Math.abs(movevect.x+walkvect.x)/(movevect.x+walkvect.x);
			while(!MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(sign,0,0))){
				player.addPosition(new Vector3(sign,0,0));
				playercol = MovementMath.DuplicateRect(player.collision);
			}
			movevect.x = 0;
			walkvect.x = 0;
		}
		player.addPosition(new Vector3(movevect.x+walkvect.x,0,0));

		playercol = MovementMath.DuplicateRect(player.collision);
		if (MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(0,movevect.y,0))){
			float sign = Math.abs(movevect.y)/movevect.y;
			while(!MovementMath.CheckCollisions(WORLD_MAP,playercol,0,new Vector3(0,sign,0))) {
				player.addPosition(new Vector3(0,sign,0));
				playercol = MovementMath.DuplicateRect(player.collision);
			}
			movevect.y = 0;
		}

		player.addPosition(new Vector3(0,movevect.y,0));
		moverot -= (movevect.x+walkvect.x)*5;
		movevect.x*=.88f;

		if(player.getPosition().y<-64||player.getPosition().y>WORLD_HEIGHT*32+64||player.getPosition().x<-64||player.getPosition().x>WORLD_WIDTH*32+64) {
			player.setPosition(WORLD_HEIGHT/2*32,WORLD_HEIGHT/2*32);
			movevect = new Vector3(0,0,0);
		}
		return playertext;
	}

	public class PoofCloud {
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

			return new FrameworkMO.TextureSet(animation.updateTime(),pos.x,pos.y,100000,(float)Math.toRadians(dir));
		}
	}

	public class Bullet {
		private final float life = 0.7f;
		private float time = 0;
		private float dir;
		private float speed = 10;
		private Vector3 pos = new Vector3();
		private Texture text;

		public Bullet(float dir, Vector3 pos){
			text = new Texture("bullet.png");
			this.pos = pos;
			this.dir = dir;
		}
		public FrameworkMO.TextureSet updateTime(){
			Vector3 addvect = MovementMath.lengthDir((float)Math.toRadians(dir-90),speed);
			pos = new Vector3(pos.x+addvect.x,pos.y+addvect.y,0);

			time+=Gdx.graphics.getDeltaTime();

			if(time>=life) {
				BulletList.remove(this);
				return null;
			}

			return new FrameworkMO.TextureSet(new TextureRegion(text),pos.x,pos.y,100000,(float)Math.toRadians(dir-90));
		}
	}
}