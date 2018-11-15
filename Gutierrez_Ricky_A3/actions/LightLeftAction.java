package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class LightLeftAction extends AbstractAction 
{
    Code code;
    
    public LightLeftAction(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.lightX(-0.5f);
    }
}