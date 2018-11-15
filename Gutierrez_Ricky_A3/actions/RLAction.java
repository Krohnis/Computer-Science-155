package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class RLAction extends AbstractAction 
{
    Code code;
    
    public RLAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraV(-1.0f);
    }
}