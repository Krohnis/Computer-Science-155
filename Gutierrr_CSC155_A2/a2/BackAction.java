package a2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class BackAction extends AbstractAction 
{
    Code code;
    
    BackAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraZ(1.0f);
    }

}