package a1;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.FloatBuffer;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;

import graphicslib3D.*;
import graphicslib3D.GLSLUtils.*;

public class Code extends JFrame implements GLEventListener, ActionListener, KeyListener, MouseWheelListener
{	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	private GLSLUtils util = new GLSLUtils();
	
	private int degree = 0;
	private float scale = 1.0f;
	private float x = 0.0f;
	private float y = 0.0f;
	private float inc_x = 0.01f;
	private float inc_y = 0.01f;
	private float b = 1.0f;
	
	private boolean horiMove, vertMove, circMove = false;

	
	public Code()
	{	
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(BorderLayout.CENTER, myCanvas);
		
		//MENU PANE + BUTTON CONTROLLERS
		Container pane = new Container();
		pane.setLayout(new FlowLayout());
		JButton button_1 = new JButton("Horizontal");
		pane.add(button_1);
		button_1.addActionListener(this);
		button_1.setActionCommand("Horizontal");
		
		JButton button_2 = new JButton("Vertical");
		pane.add(button_2);
		button_2.addActionListener(this);
		button_2.setActionCommand("Vertical");
		
		JButton button_3 = new JButton("Circular");
		pane.add(button_3);
		button_3.addActionListener(this);
		button_3.setActionCommand("Circular");
		
		JButton button_4 = new JButton("Scale Up");
		pane.add(button_4);
		button_4.addActionListener(this);
		button_4.setActionCommand("ScaleU");
		
		JButton button_5 = new JButton("Scale Down");
		pane.add(button_5);
		button_5.addActionListener(this);
		button_5.setActionCommand("ScaleD");
		
		JButton button_6 = new JButton("Color Shift");
		pane.add(button_6);
		button_6.addActionListener(this);
		button_6.setActionCommand("Color");
		
		this.add(BorderLayout.NORTH, pane);
		
		addMouseWheelListener(this);
		JComponent component = (JComponent) this.getContentPane();
		int mapFocus = JComponent.WHEN_IN_FOCUSED_WINDOW;
		KeyStroke C_Key = KeyStroke.getKeyStroke('c');
		component.getInputMap().put(C_Key, "color");
		component.getActionMap().put("color", new ColorAction(this));
		this.requestFocus();
		
		setVisible(true);
		FPSAnimator animator = new FPSAnimator(myCanvas, 30);
		animator.start();
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(rendering_program);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		
		if (vertMove == true) { this.verticalMovement(gl); }
		if (horiMove == true) { this.horizontalMovement(gl); }
		if (circMove == true) { this.circularMovement(gl); }
		
		int state = gl.glGetUniformLocation(rendering_program, "state");
		gl.glProgramUniform1f(rendering_program, state, b);
		int scale_off = gl.glGetUniformLocation(rendering_program, "scale");
		gl.glProgramUniform1f(rendering_program, scale_off, scale);
		
		gl.glDrawArrays(GL_TRIANGLES,0,3);
	}

	public void init(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		System.out.println("OpenGL Version: " + gl.glGetString(gl.GL_VERSION));
		System.out.println("JOGL Version: " + (Package.getPackage("com.jogamp.opengl")).getImplementationVersion());
		System.out.println("Java Version: " + System.getProperty("java.version"));
		
		rendering_program = createShaderProgram();
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}

	private int createShaderProgram()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];

		String vshaderSource[] = util.readShaderSource("a1/vert.shader");
		String fshaderSource[] = util.readShaderSource("a1/frag.shader");
		int lengths[];
		
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glCompileShader(vShader);
		
		checkOpenGLError();
		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] == 1)
		{
			System.out.println("Vertex Compilation Success");
		}
		else
		{
			System.out.println("Vertex Compilation Failure");
			printShaderLog(vShader);
		}
		
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
		gl.glCompileShader(fShader);
		
		checkOpenGLError();  // can use returned boolean if desired
		gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		if (fragCompiled[0] == 1)
		{	
			System.out.println("Fragment Compilation Success");
		}
		else
		{	
			System.out.println("Fragment Compilation Failure");
			printShaderLog(fShader);
		}

		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);

		checkOpenGLError();
		gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] == 1)
		{	
			System.out.println("Linking Success");
		}
		else
		{	
			System.out.println("Linking Failure");
			printProgramLog(vfprogram);
		}
		
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		return vfprogram;
	}

	public void horizontalMovement(GL4 gl)
	{	
		x += inc_x;
		if (x > 1.00f || x < -1.00f) { 
			inc_x = -inc_x; 
		}
		int offset_loc = gl.glGetUniformLocation(rendering_program, "inc_x");
		gl.glProgramUniform1f(rendering_program, offset_loc, x);
	}
	
	public void verticalMovement(GL4 gl)
	{
		y += inc_y;
		if (y > 1.00f || y< -1.00f) { 
			inc_y = -inc_y; 
		}
		int offset_loc = gl.glGetUniformLocation(rendering_program, "inc_y");
		gl.glProgramUniform1f(rendering_program, offset_loc, y);
	}
	
	public void circularMovement(GL4 gl)
	{
		degree += 1;
		degree %= 360;
		double angle = Math.toRadians(degree);
		x = (float) ((0.5f) * Math.cos(angle));
		y = (float) ((0.5f) * Math.sin(angle));
		
		int offset_locX = gl.glGetUniformLocation(rendering_program, "inc_x");
		int offset_locY = gl.glGetUniformLocation(rendering_program, "inc_y");
		gl.glProgramUniform1f(rendering_program, offset_locX, x);
		gl.glProgramUniform1f(rendering_program, offset_locY, y);
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		String action = e.getActionCommand();
		if (action.equals("Horizontal")) { horiMove = !(horiMove); }
		else if (action.equals("Vertical")) { vertMove = !(vertMove); }
		else if (action.equals("Circular")) { circMove = !(circMove); }
		else if (action.equals("ScaleU") && (scale < 2.0f)) { scale += 0.1f; }
		else if (action.equals("ScaleD") && (scale > 0.1f)) { scale -= 0.1f; }
		else if (action.equals("Color")) { b = -b; }
	}
	
	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_C)
		{
			System.out.println("Key Pressed");
			b = -b;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) { System.out.println("Key Typed"); }

	@Override
	public void keyReleased(KeyEvent e) { System.out.println("Key Released"); }

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) 
	{
        int scaleIncrease = e.getWheelRotation();
           if (scaleIncrease < 0 && scale < 2.0f) 
           {
               scale += 0.1f;
           } 
           else if (scale > 0.1f)
           {
               scale -= 0.1f;
           }    
    }
	
	private void printShaderLog(int shader)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	System.out.print((char) log[i]);
			}
		}
	}

	private void printProgramLog(int prog)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;
		
		// determine length of the program compilation log
		gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	System.out.print((char) log[i]);
			}
		}
	}

	private boolean checkOpenGLError()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		boolean foundError = false;
		GLU glu = new GLU();
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR)
		{	
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		return foundError;
	}
	
	public void setColor()
	{
		b = -b;
	}
}