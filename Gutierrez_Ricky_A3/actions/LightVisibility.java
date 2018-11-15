package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class LightVisibility extends AbstractAction 
{
    Code code;
    
    public LightVisibility(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.lightVisible();
    }

}