package a2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class DownAction extends AbstractAction 
{
    Code code;
    
    DownAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraY(-1.0f);
    }

}