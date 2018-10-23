package a2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class RightAction extends AbstractAction 
{
    Code code;
    
    RightAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraX(1.0f);
    }

}