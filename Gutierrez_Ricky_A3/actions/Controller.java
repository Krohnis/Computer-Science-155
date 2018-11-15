package actions;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import a3.Code;

public class Controller 
{
	Code code = null;
	
	public Controller(Code c)
	{
		this.code = c;
		init(code);
	}
	
	public void init(Code code)
	{
		//Create a JComponent to get focus, from there Input can be read
		JComponent contentPane = (JComponent) code.getContentPane();
        //Gets the focus InputMap from the content pane, allowing for inptus to be read 
        int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap imap = contentPane.getInputMap(mapName);
        //Creates KeyStrokes that can be listened to by the ContentPane
        KeyStroke wKey = KeyStroke.getKeyStroke('w');
        KeyStroke sKey = KeyStroke.getKeyStroke('s');
        KeyStroke aKey = KeyStroke.getKeyStroke('a');
        KeyStroke dKey = KeyStroke.getKeyStroke('d');
        KeyStroke qKey = KeyStroke.getKeyStroke('q');
        KeyStroke eKey = KeyStroke.getKeyStroke('e');
        KeyStroke ruKey = KeyStroke.getKeyStroke("UP");
        KeyStroke rdKey = KeyStroke.getKeyStroke("DOWN");
        KeyStroke rlKey = KeyStroke.getKeyStroke("LEFT");
        KeyStroke rrKey = KeyStroke.getKeyStroke("RIGHT");
        KeyStroke cvKey = KeyStroke.getKeyStroke("SPACE");
        KeyStroke laKey = KeyStroke.getKeyStroke('t');
        KeyStroke oKey = KeyStroke.getKeyStroke('o');
        KeyStroke kKey = KeyStroke.getKeyStroke('k');
        KeyStroke jKey = KeyStroke.getKeyStroke('j');
        KeyStroke lKey = KeyStroke.getKeyStroke('l');
        KeyStroke pKey = KeyStroke.getKeyStroke('p');
        KeyStroke iKey = KeyStroke.getKeyStroke('i');
        //Inputs placed into the InputMap, when input is noted, returns specific String that will trigger an Action
        imap.put(wKey, "forward");
        imap.put(sKey, "backward");
        imap.put(aKey, "left");
        imap.put(dKey, "right");
        imap.put(qKey, "up");
        imap.put(eKey, "down");
        imap.put(ruKey, "rotateUp");
        imap.put(rdKey, "rotateDown");
        imap.put(rlKey, "rotateLeft");
        imap.put(rrKey, "rotateRight");
        imap.put(cvKey, "changeVisible");
        imap.put(laKey, "lightAction");
        imap.put(oKey, "lFoward");
        imap.put(kKey, "lBackward");
        imap.put(jKey, "lLeft");
        imap.put(lKey, "lRight");
        imap.put(pKey, "lUp");
        imap.put(iKey, "lDown");
        // Generates the ActionMap from the content pane
        ActionMap amap = contentPane.getActionMap();
        // Puts the related Command into its related command in the content pane
        amap.put("forward", new ForwardAction(code));
        amap.put("backward", new BackAction(code));
        amap.put("left", new LeftAction(code));
        amap.put("right", new RightAction(code));
        amap.put("up", new UpAction(code));
        amap.put("down", new DownAction(code));
        amap.put("rotateUp", new RUAction(code));
        amap.put("rotateDown", new RDAction(code));
        amap.put("rotateLeft", new RLAction(code));
        amap.put("rotateRight", new RRAction(code));
        amap.put("changeVisible", new AxisVisibility(code));
        amap.put("lightAction", new LightVisibility(code));
        amap.put("lFoward", new LightFowardAction(code));
        amap.put("lBackward", new LightBackwardAction(code));
        amap.put("lLeft", new LightLeftAction(code));
        amap.put("lRight", new LightRightAction(code));
        amap.put("lUp", new LightUpAction(code));
        amap.put("lDown", new LightDownAction(code));
        //The JFrame requests focus to the Keyboard to accept inputs during runtime
        code.requestFocus();
	}
}
