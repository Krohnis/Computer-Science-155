package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class ForwardAction extends AbstractAction 
{
    Code code;
    
    public ForwardAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraZ(-0.05f);
    }

}