package a1;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ColorAction extends AbstractAction 
{

    Code code;

    ColorAction(Code code) 
    {
        this.code = code;
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
    	code.setColor();
    }
}