package a2;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class AxisVisibility extends AbstractAction 
{
    Code code;
    
    AxisVisibility(Code c) 
    {
        this.code = c;
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        code.axisVisibility();
    }
}