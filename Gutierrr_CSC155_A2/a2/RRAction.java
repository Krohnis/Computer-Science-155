package a2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class RRAction extends AbstractAction 
{
    Code code;
    
    RRAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraV(1.0f);
    }
}