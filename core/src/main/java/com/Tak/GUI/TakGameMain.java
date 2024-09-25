package com.Tak.GUI;

import com.badlogic.gdx.Game;

public class TakGameMain extends Game {
    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }
}
