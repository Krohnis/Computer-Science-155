package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import a3.Code;

public class AxisVisibility extends AbstractAction 
{
    Code code;
    
    public AxisVisibility(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.axisVisibility();
    }
}