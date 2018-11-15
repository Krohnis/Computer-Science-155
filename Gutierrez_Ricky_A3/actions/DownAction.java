package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class DownAction extends AbstractAction 
{
    Code code;
    
    public DownAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.cameraY(-0.05f);
    }

}