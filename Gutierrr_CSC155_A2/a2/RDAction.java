package a2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class RDAction extends AbstractAction 
{
    Code code;
    
    RDAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraU(1.0f);
    }
}