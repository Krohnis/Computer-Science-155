package a2;

import java.io.File;
import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import graphicslib3D.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.shape.Sphere;
import graphicslib3D.Shape3D.*;
import graphicslib3D.shape.Sphere;

public class Code extends JFrame implements GLEventListener
{	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	private int vbo[] = new int[16];
	private float cameraX, cameraY, cameraZ, cameraU, cameraV;
	private float cubeLocX, cubeLocY, cubeLocZ;
	private float pyrLocX, pyrLocY, pyrLocZ;
	private GLSLUtils util = new GLSLUtils();
	private int count = 0;
	private boolean isVisible = true;
	
	//Declares all of the textures for use in the program
	private Texture joglSunTexture;
	private int sunTexture;
	private Texture joglStudentTexuture;
	private int studentTexture;
	private Texture joglEarth;
	private int earthTexture;
	private Texture joglMars;
	private int marsTexture;
	//Used for the axes textures
	private Texture[] joglAxesTexture = new Texture[3];
	private int[] axesTexture = new int[3];
	
	//Used for the Sphere object
	private Sphere sphere = new Sphere();
	
	private	MatrixStack mvStack = new MatrixStack(20);

	public Code()
	{	
		setTitle("Chapter4 - program4");
		setSize(600, 600);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		this.setVisible(true);
		
		this.actionGeneration();
		
		FPSAnimator animator = new FPSAnimator(myCanvas, 50);
		animator.start();
	}
	
	public void init(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		setupVertices();
		cameraX = 0.0f; cameraY = 0.0f; cameraZ = 12.0f;
		cameraU = 1.0f; cameraV = 1.0f;
		cubeLocX = 0.0f; cubeLocY = 0.0f; cubeLocZ = 0.0f;
		pyrLocX = 0.0f; pyrLocY = 0.0f; pyrLocZ = 0.0f;
		
		//Prints the information about the computer
		System.out.println("OpenGL Version: " + gl.glGetString(gl.GL_VERSION));
		System.out.println("JOGL Version: " + (Package.getPackage("com.jogamp.opengl")).getImplementationVersion());
		System.out.println("Java Version: " + System.getProperty("java.version"));
		
		//Initializes all of the textures for use in the program so that I don't get marked down for no comments please
		joglSunTexture = loadTexture("sun.jpg");
		sunTexture = joglSunTexture.getTextureObject();
		joglStudentTexuture = loadTexture("custom.png");
		studentTexture = joglStudentTexuture.getTextureObject();
		joglEarth = loadTexture("earth.jpg");
		earthTexture = joglEarth.getTextureObject();
		joglMars = loadTexture("mars.jpg");
		marsTexture = joglMars.getTextureObject();
		joglAxesTexture[0] = loadTexture("red.png");
		axesTexture[0] = joglAxesTexture[0].getTextureObject();
		joglAxesTexture[1] = loadTexture("blue.png");
		axesTexture[1] = joglAxesTexture[1].getTextureObject();
		joglAxesTexture[2] = loadTexture("orange.png");
		axesTexture[2] = joglAxesTexture[2].getTextureObject();
		//Textures for the solar system objects
		
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glClear(GL_DEPTH_BUFFER_BIT);

		gl.glUseProgram(rendering_program);
		
		int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		Matrix3D pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
		
		//Push the View Matrix onto the Stack push view matrix onto the stack
		mvStack.pushMatrix();
		
		//Handle rotation of the Camera according to the Camera's UV values
		mvStack.rotate(cameraU, 1.0f, 0.0f, 0.0f);
		mvStack.rotate(cameraV, 0.0f, 1.0f, 0.0f);
		//Translate the Camera Position according to the camera's float values
		mvStack.translate(-cameraX, -cameraY, -cameraZ);
		
		double amt = (double)(System.currentTimeMillis())/1000.0;

		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		
		// ----------------------  sphere == sun  
		mvStack.pushMatrix();
		mvStack.translate(pyrLocX, pyrLocY, pyrLocZ);
		mvStack.pushMatrix();
		mvStack.rotate((System.currentTimeMillis())/10.0,0.0,1.0,0.0);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		//Vertices, defined for model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		//Textures, applied to model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, sunTexture);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		int verticesCount = sphere.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, verticesCount); 
		mvStack.popMatrix();
		
		if (isVisible)
		{
			mvStack.pushMatrix();
			mvStack.translate(0.0f, 0.0f, 0.0f);
			gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
			gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[2]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			//Textures
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, axesTexture[0]);
			gl.glDrawArrays(GL_LINES, 0, 9);
			mvStack.popMatrix();
			
			mvStack.pushMatrix();
			mvStack.translate(0.0f, 0.0f, 0.0f);
			gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
			gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[3]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			//Textures
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, axesTexture[1]);
			gl.glDrawArrays(GL_LINES, 0, 9);
			mvStack.popMatrix();
			
			mvStack.pushMatrix();
			mvStack.translate(0.0f, 0.0f, 0.0f);
			gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
			gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[4]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			//Textures
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, axesTexture[2]);
			gl.glDrawArrays(GL_LINES, 0, 9);
			mvStack.popMatrix();
		}
		
		//-----------------------  sphere == planet  
		mvStack.pushMatrix();
		mvStack.translate(Math.sin(amt)*4.0f, 0.0f, Math.cos(amt)*4.0f);
		mvStack.pushMatrix();
		mvStack.rotate((System.currentTimeMillis())/10.0,0.0,1.0,0.0);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		//Vertices, defined for model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		//Textures, applied to model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, earthTexture);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, verticesCount); 
		mvStack.popMatrix();

		//-----------------------  smaller cube == moon
		mvStack.pushMatrix();
		mvStack.translate(0.0f, Math.sin(amt)*2.0f, Math.cos(amt)*2.0f);
		mvStack.rotate((System.currentTimeMillis())/10.0,0.0,0.0,1.0);
		mvStack.scale(0.25, 0.25, 0.25);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		//Vertices, defined for model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		//Textures, applied to model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, studentTexture);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		mvStack.popMatrix();  mvStack.popMatrix();
	
		//----------------------- sphere == better planet
		mvStack.pushMatrix();
		mvStack.translate(Math.sin(amt*1.25)*10.0f, 0.0f, Math.cos(amt*1.25)*10.0f);
		mvStack.pushMatrix();
		mvStack.rotate((System.currentTimeMillis())/10.0,0.0,1.0,0.0);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		//Vertices, defined for model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		//Textures, applied to model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, marsTexture);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, verticesCount); 
		mvStack.popMatrix();
		
		//-----------------------  smaller cube == better moon
		mvStack.pushMatrix();
		mvStack.translate(0.0f, Math.sin(amt*1.5)*3.0f, Math.cos(amt*1.5)*3.0f);
		mvStack.rotate((System.currentTimeMillis())/10.0,0.0,0.0,1.0);
		mvStack.scale(0.25, 0.25, 0.25);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		//Vertices, defined for model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		//Textures, applied to model
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, axesTexture[2]);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		mvStack.popMatrix();  mvStack.popMatrix();  mvStack.popMatrix();
		mvStack.popMatrix();
	}

	private void setupVertices()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		//Creates model objects based on the models from the ModelObject class
		ModelObjects cube = new ModelObjects("cube");
		ModelObjects pyramid = new ModelObjects("pyramid");
		ModelObjects lineX = new ModelObjects("lineX");
		ModelObjects lineY = new ModelObjects("lineY");
		ModelObjects lineZ = new ModelObjects("lineZ");
		ModelObjects triangle = new ModelObjects("triangle");
		
		//Retrieves the vertices and textures that define each object from the related class
		float[] cube_positions = cube.getVertices();
		float[] pyramid_positions = pyramid.getVertices();
		float[] triangle_positions = triangle.getVertices();
				
		//Sphere information, textures and vertices
		Vertex3D[] vertices = sphere.getVertices();
		int[] indices = sphere.getIndices();
		
		float[] pvalues = new float[indices.length*3];
		float[] tvalues = new float[indices.length*2];
		float[] nvalues = new float[indices.length*3];
		
		for (int i = 0; i < indices.length; i++)
		{
			pvalues[i*3] = (float) (vertices[indices[i]]).getX();
			pvalues[i*3+1] = (float) (vertices[indices[i]]).getY();
			pvalues[i*3+2] = (float) (vertices[indices[i]]).getZ();
			tvalues[i*2] = (float) (vertices[indices[i]]).getS();
			tvalues[i*2+1] = (float) (vertices[indices[i]]).getT();
			nvalues[i*3] = (float) (vertices[indices[i]]).getNormalX();
			nvalues[i*3+1]= (float)(vertices[indices[i]]).getNormalY();
			nvalues[i*3+2]=(float) (vertices[indices[i]]).getNormalZ();
		}
		
		//Vertices for each of the axes, all share the same texture so only needs to be retrieved from one
		float[] lineX_positions = lineX.getVertices();
		float[] lineY_positions = lineY.getVertices();
		float[] lineZ_positions = lineZ.getVertices();
		
		//Textures for the objects
		float[] cubeTex = cube.getTextures();
		float[] pyramidTex = pyramid.getTextures();
		float[] triangleTex = triangle.getTextures();
		float[] lineTex = lineX.getTextures();
		
		//Binds all of the vertex points, used when defining the vertices of models
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		//Used for defining the vertices of the cube
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cube_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit()*4, cubeBuf, GL_STATIC_DRAW);
		
		//Used for defining the vertices of the pyramid
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramid_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit()*4, pyrBuf, GL_STATIC_DRAW);
		
		//Used for defining the vertices of the X-Axis
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer lineXBuf = Buffers.newDirectFloatBuffer(lineX_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, lineXBuf.limit()*4, lineXBuf, GL_STATIC_DRAW);
		
		//Used for defining the vertices of the Y-Axis
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer lineYBuf = Buffers.newDirectFloatBuffer(lineY_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, lineYBuf.limit()*4, lineYBuf, GL_STATIC_DRAW);
		
		//Used for defining the vertices of the Z-Axis
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer lineZBuf = Buffers.newDirectFloatBuffer(lineZ_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, lineZBuf.limit()*4, lineZBuf, GL_STATIC_DRAW);
		
		//Used for defining the vertices of the triangle
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer triangleBuf = Buffers.newDirectFloatBuffer(triangle_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, triangleBuf.limit()*4, triangleBuf, GL_STATIC_DRAW);
		
		//vbo for the sphere model object, each handles a different aspect of the sphere
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);

		//Binds all of the texture points, used when applying textures to models
		//Used for defining the texture coordinates of the pyramid
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		FloatBuffer pyramidTexBuf = Buffers.newDirectFloatBuffer(pyramidTex);
		gl.glBufferData(GL_ARRAY_BUFFER, pyramidTexBuf.limit()*4, pyramidTexBuf, GL_STATIC_DRAW);
		
		//Used for defining the texture coordinates of the cube
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		FloatBuffer cubeTexBuf = Buffers.newDirectFloatBuffer(cubeTex);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeTexBuf.limit()*4, cubeTexBuf, GL_STATIC_DRAW);
		
		//Used for defining the texture of the axes
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer axisTexBuf = Buffers.newDirectFloatBuffer(lineTex);
		gl.glBufferData(GL_ARRAY_BUFFER, axisTexBuf.limit()*4, axisTexBuf, GL_STATIC_DRAW);
		
		//Used to texture
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer triangleTexBuf = Buffers.newDirectFloatBuffer(triangleTex);
		gl.glBufferData(GL_ARRAY_BUFFER, triangleTexBuf.limit()*4, triangleTexBuf, GL_STATIC_DRAW);

	}

	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	
		float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		r.setElementAt(3,3,0.0f);
		return r;
	}

	//public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	private int createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		String vshaderSource[] = util.readShaderSource("a2/vert.shader");
		String fshaderSource[] = util.readShaderSource("a2/frag.shader");

		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);

		gl.glCompileShader(vShader);
		gl.glCompileShader(fShader);

		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);
		return vfprogram;
	}
	
	//Handles applying imported textures to models
	public Texture loadTexture(String textureFile)
	{
		Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFile), false); }
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}
	
	public void actionGeneration()
	{
		//Create a JComponent to get focus, from there Input can be read
		JComponent contentPane = (JComponent) this.getContentPane();
        //Gets the focus InputMap from the content pane, allowing for inptus to be read 
        int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap imap = contentPane.getInputMap(mapName);
        //Creates KeyStrokes that can be listened to by the ContentPane
        KeyStroke wKey = KeyStroke.getKeyStroke('w');
        KeyStroke sKey = KeyStroke.getKeyStroke('s');
        KeyStroke aKey = KeyStroke.getKeyStroke('a');
        KeyStroke dKey = KeyStroke.getKeyStroke('d');
        KeyStroke qKey = KeyStroke.getKeyStroke('q');
        KeyStroke eKey = KeyStroke.getKeyStroke('e');
        KeyStroke ruKey = KeyStroke.getKeyStroke("UP");
        KeyStroke rdKey = KeyStroke.getKeyStroke("DOWN");
        KeyStroke rlKey = KeyStroke.getKeyStroke("LEFT");
        KeyStroke rrKey = KeyStroke.getKeyStroke("RIGHT");
        KeyStroke cvKey = KeyStroke.getKeyStroke("SPACE");
        //Inputs placed into the InputMap, when input is noted, returns specific String that will trigger an Action
        imap.put(wKey, "forward");
        imap.put(sKey, "backward");
        imap.put(aKey, "left");
        imap.put(dKey, "right");
        imap.put(qKey, "up");
        imap.put(eKey, "down");
        imap.put(ruKey, "rotateUp");
        imap.put(rdKey, "rotateDown");
        imap.put(rlKey, "rotateLeft");
        imap.put(rrKey, "rotateRight");
        imap.put(cvKey, "changeVisible");
        // Generates the ActionMap from the content pane
        ActionMap amap = contentPane.getActionMap();
        // Puts the related Command into its related command in the content pane
        amap.put("forward", new ForwardAction(this));
        amap.put("backward", new BackAction(this));
        amap.put("left", new LeftAction(this));
        amap.put("right", new RightAction(this));
        amap.put("up", new UpAction(this));
        amap.put("down", new DownAction(this));
        amap.put("rotateUp", new RUAction(this));
        amap.put("rotateDown", new RDAction(this));
        amap.put("rotateLeft", new RLAction(this));
        amap.put("rotateRight", new RRAction(this));
        amap.put("changeVisible", new AxisVisibility(this));
        //The JFrame requests focus to the Keyboard to accept inputs during runtime
        this.requestFocus();
	}
	
	//Handles all of the Actions, modifies the Camera's position and rotation
	//Increases or decreases camera positions and rotate accordingly, and adjusts during <display>'s run
	public void cameraX(float direction) { cameraX += direction; }
	public void cameraY(float direction) { cameraY += direction; }
	public void cameraZ(float direction) { cameraZ += direction; }
	public void cameraU(float rotation) { cameraU += rotation; }
	public void cameraV(float rotation) { cameraV += rotation; }
	public void axisVisibility() { isVisible = !isVisible; System.out.println("Here"); }
}