package com.hedtub.game.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.hedtub.game.MainMenuScreen;
import com.github.czyzby.websocket.GwtWebSockets;
import com.badlogic.gdx.Gdx;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                //Resizable application, uses available space in browser
                return new GwtApplicationConfiguration(true);
        }

        @Override
        public ApplicationListener createApplicationListener () {
                //instantiating game for html
                GwtWebSockets.initiate();
                return new MainMenuScreen("html");
        }
}