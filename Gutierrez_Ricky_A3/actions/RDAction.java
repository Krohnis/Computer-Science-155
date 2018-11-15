package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class RDAction extends AbstractAction 
{
    Code code;
    
    public RDAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraU(1.0f);
    }
}