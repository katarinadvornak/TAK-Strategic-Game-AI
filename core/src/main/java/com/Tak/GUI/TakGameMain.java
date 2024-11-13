package com.Tak.GUI;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ApplicationAdapter;
import com.Tak.utils.Logger;

public class TakGameMain extends Game {
    @Override
    public void create() {
        Logger.initialize();
        setScreen(new MainMenuScreen(this));
    }
}
