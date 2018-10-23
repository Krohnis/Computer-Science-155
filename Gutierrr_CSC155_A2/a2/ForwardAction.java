package a2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ForwardAction extends AbstractAction 
{
    Code code;
    
    ForwardAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraZ(-1.0f);
    }

}