#version 430

uniform float inc_x;
uniform float inc_y;
uniform float scale;
uniform float state;
out vec4 initialColor;

void main(void)
{ 
	if (state == 1.0f)
	{
		if (gl_VertexID == 0) 
		{
			initialColor = vec4(0.0, 0.0, 1.0, 1.0);
			gl_Position = vec4( (0.25*scale) + inc_x, (-0.25*scale) + inc_y, 0.0, 1.0);
		}
	  	else if (gl_VertexID == 1) 
	  	{
	  		initialColor = vec4(0.0, 1.0, 0.0, 1.0);
	  		gl_Position = vec4( (-0.25*scale) + inc_x,(-0.25*scale) + inc_y, 0.0, 1.0);
	  	}
		else 
		{
			initialColor = vec4(1.0, 0.0, 0.0, 1.0);
			gl_Position = vec4( (0.25*scale) + inc_x, (0.25*scale) + inc_y, 0.0, 1.0);
		}
	}
	else
	{
		if (gl_VertexID == 0) 
			gl_Position = vec4( 0.25 + inc_x,-0.25 + inc_y, 0.0, 1.0);
		else if (gl_VertexID == 1) 
			gl_Position = vec4(-0.25 + inc_x,-0.25 + inc_y, 0.0, 1.0);
		else gl_Position = 
			vec4( 0.25 + inc_x, 0.25 + inc_y, 0.0, 1.0);
			initialColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
}