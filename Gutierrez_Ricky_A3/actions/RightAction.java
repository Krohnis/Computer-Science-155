package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class RightAction extends AbstractAction 
{
    Code code;
    
    public RightAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraX(0.05f);
    }

}