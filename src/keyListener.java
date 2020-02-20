import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

//Basic key listener only looking for space press. Run in Main method and accessible from any class
public class keyListener extends KeyAdapter {

    private static volatile boolean spacePressed = false;
    static boolean isSpace() {
        synchronized (keyListener.class) {
            return spacePressed;
        }
    }

    public static void main(String[] args) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(key -> {
            synchronized (keyListener.class) {
                switch (key.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        if (key.getKeyCode() == KeyEvent.VK_SPACE) {
                            spacePressed = true;
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                        if (key.getKeyCode() == KeyEvent.VK_SPACE) {
                            spacePressed = false;
                        }
                        break;
                }
                return false;
            }
        });
    }
}