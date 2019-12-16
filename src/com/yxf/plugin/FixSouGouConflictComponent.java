package com.yxf.plugin;

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class FixSouGouConflictComponent implements Component {

    private static final boolean sDebug = false;

    static {
        if (sDebug) {
            Logger.getInstance(FixSouGouConflictComponent.class).setLevel(Level.DEBUG);
        }
    }

    private Robot mRobot;

    private Set<Integer> mKeyDownSet = new HashSet<Integer>();
    private boolean mTriggered = false;

    private IdeEventQueue.EventDispatcher mDispatcher = awtEvent -> {
        if (awtEvent instanceof KeyEvent) {
            return onKeyEvent((KeyEvent) awtEvent);
        }
        return false;
    };

    private static void log(String message) {
        if (sDebug) {
            Logger.getInstance(FixSouGouConflictComponent.class).debug(message);
        }
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "FixSgC.FixSouGouConflictComponent";
    }

    @Override
    public void initComponent() {
        IdeEventQueue queue = IdeEventQueue.getInstance();
        try {
            mRobot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        queue.addPostprocessor(mDispatcher, null);
    }

    private boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (event.getID()) {
            case KeyEvent.KEY_PRESSED:
                if (event.isControlDown() && event.isAltDown()) {
                    if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_ALT) {
                        return false;
                    }
                    mKeyDownSet.add(keyCode);
                    mTriggered = true;
                }
                break;
            case KeyEvent.KEY_RELEASED:
                if (keyCode != KeyEvent.VK_CONTROL && keyCode != KeyEvent.VK_ALT) {
                    mKeyDownSet.remove(keyCode);
                }
                if (mTriggered && mKeyDownSet.size() == 0 && !event.isControlDown() && !event.isAltDown()) {
                    mTriggered = false;
                    if (mRobot != null) {
                        mRobot.keyPress(KeyEvent.VK_CONTROL);
                        mRobot.delay(50);
                        mRobot.keyRelease(KeyEvent.VK_CONTROL);
                    }
                }
                break;
        }

        return false;
    }

    @Override
    public void disposeComponent() {
        IdeEventQueue.getInstance().removeDispatcher(mDispatcher);
        mRobot = null;
        mKeyDownSet.clear();
    }
}
