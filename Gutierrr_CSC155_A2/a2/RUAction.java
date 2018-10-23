package a2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class RUAction extends AbstractAction 
{
    Code code;
    
    RUAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraU(-1.0f);
    }
}