package a2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class LeftAction extends AbstractAction 
{
    Code code;
    
    LeftAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraX(-1.0f);
    }

}