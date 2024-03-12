package com.hedtub.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;

public class ControllerManager implements ControllerListener, InputProcessor {
    public Controller controller;
    public ControllerManager() {
        Controllers.addListener(this);
        Gdx.input.setInputProcessor(this);
    }
    public boolean buttonPressed(int buttonCode) {
        if(controller.equals(null))
            return false;
        return buttonDown(controller,buttonCode);
    }
    public boolean joystickMoved(int axisCode, float value) {
        if(controller.equals(null))
            return false;
        return axisMoved(controller, axisCode, value);
    }

    public boolean isConnected(){
        return controller != null;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void connected(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void disconnected(Controller controller) {
        this.controller = null;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return controller.getButton(buttonCode);
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return !controller.getButton(buttonCode);
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return controller.getAxis(axisCode)>value;
    }
}