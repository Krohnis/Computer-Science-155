package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class LightRightAction extends AbstractAction 
{
    Code code;
    
    public LightRightAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.lightX(0.5f);
    }
}