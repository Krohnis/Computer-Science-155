package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class LightFowardAction extends AbstractAction 
{
    Code code;
    
    public LightFowardAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.lightZ(-0.5f);
    }
}