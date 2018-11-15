package a3;

import graphicslib3D.*;
import graphicslib3D.light.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.shape.*;

import java.io.File;
import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import actions.Controller;

import com.jogamp.common.nio.Buffers;

public class Code extends JFrame implements GLEventListener
{	
	// primary functionality
	private GLCanvas myCanvas;
	private Material thisMaterial;
	private String[] vBlinn1ShaderSource, vBlinn2ShaderSource, fBlinn2ShaderSource;
	private int rendering_program1, rendering_program2;
	private int vao[] = new int[1];
	private int vbo[] = new int[20];
	private int width = 800, height = 800;
	private int mv_location, proj_location, vertexLoc, n_location;
	private float aspect;
	private GLSLUtils util = new GLSLUtils();
	
	// view and controller creation
	private Camera camera = new Camera(width, height);
	private Controller controller = new Controller(this);
	
	// location of torus and camera
	private Point3D torusLoc = new Point3D(1.6, 0.0, -0.3);
	private Point3D pyrLoc = new Point3D(-1.0, 0.1, 0.3);
	private Point3D sphLoc = new Point3D(0.0, 2.0, 0.0);
	private Point3D lightLoc = new Point3D(-3.8f, 2.2f, 1.1f);
	
	// scene matrices
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	
	// light stuff
	private float [] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	private PositionalLight currentLight = new PositionalLight();
	private boolean lightVisible = true;
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[1];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();

	// model stuff
	private ImportedModel pyramid = new ImportedModel("pyr.obj");
	private Sphere mySphere = new Sphere();
	private Torus myTorus = new Torus(0.6f, 0.4f, 48);
	private int numPyramidVertices, numTorusVertices, numSphereVertices;
	private boolean isVisible = true;
	
	// material stuff
	private Material JADE = new Material();
	
	// declares all of the textures for use in the program
	private Texture joglSunTexture;
	private Texture joglEarth;
	private Texture joglMars;
	private Texture[] joglAxesTexture = new Texture[3];
	private int sunTexture;
	private int studentTexture;
	private int earthTexture;
	private int marsTexture;
	private int[] axesTexture = new int[3];
	
	public Code()
	{	
		setTitle("Program 3");
		setSize(width, height);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		setVisible(true);
		FPSAnimator animator = new FPSAnimator(myCanvas, 30);
		animator.start();
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		currentLight.setPosition(lightLoc);
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = camera.getPerspectiveMatrix();
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts
		
		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program1);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
		
		lightV_matrix = lookAt(currentLight.getPosition(), origin, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		// draw the torus
		
		m_matrix.setToIdentity();
		m_matrix.translate(torusLoc.getX(),torusLoc.getY(),torusLoc.getZ());
		m_matrix.rotateX(25.0);
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);
		
		int shadow_location = gl.glGetUniformLocation(rendering_program1, "shadowMVP");
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);
		
		// ---- draw the pyramid
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(40.0);

		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumVertices());
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program2);

		// draw the torus
		
		thisMaterial = graphicslib3D.Material.GOLD;		
		
		mv_location = gl.glGetUniformLocation(rendering_program2, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program2, "proj_matrix");
		n_location = gl.glGetUniformLocation(rendering_program2, "normalMat");
		int shadow_location = gl.glGetUniformLocation(rendering_program2,  "shadowMVP");
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(torusLoc.getX(),torusLoc.getY(),torusLoc.getZ());
		m_matrix.rotateX(25.0);

		//  build the VIEW matrix
		v_matrix.setToIdentity();
		v_matrix.concatenate(camera.getCombinedMatrix());
		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up torus textures buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, marsTexture);
		
		// set up torus normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);
		
		// draw the pyramid
		
		thisMaterial = graphicslib3D.Material.SILVER;		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(40.0);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// set up texture buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, sunTexture);
		
		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumVertices());
		
		// draw the sphere
		
		thisMaterial = JADE;
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(sphLoc.getX(), sphLoc.getY(), sphLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(40.0);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// set up texture buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, earthTexture);
		
		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
		
		// draw the axes
		
		if (isVisible) 
		{
			//  build the MODEL matrix
			m_matrix.setToIdentity();
			m_matrix.translate(0.0, 0.0, 0.0);
			m_matrix.rotateX(30.0);
			m_matrix.rotateY(40.0);

			//  build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);
			
			shadowMVP2.setToIdentity();
			shadowMVP2.concatenate(b);
			shadowMVP2.concatenate(lightP_matrix);
			shadowMVP2.concatenate(lightV_matrix);
			shadowMVP2.concatenate(m_matrix);
			gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
			
			//  put the MV and PROJ matrices into the corresponding uniforms
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
			
			// set up vertices buffer
			gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[6]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			
			// set up texture buffer
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, axesTexture[0]);
			gl.glDrawArrays(GL_LINES, 0, 2);
			
			// set up vertices buffer
			gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[7]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			
			// set up texture buffer
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, axesTexture[1]);
			gl.glDrawArrays(GL_LINES, 0, 2);
			
			// set up vertices buffer
			gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[8]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			
			// set up texture buffer
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, axesTexture[2]);
			gl.glDrawArrays(GL_LINES, 0, 2);
		}
	}

	public void init(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		createShaderPrograms();
		setupVertices();
		setupShadowBuffers();
		setupTextures();
				
		b.setElementAt(0,0,0.5);b.setElementAt(0,1,0.0);b.setElementAt(0,2,0.0);b.setElementAt(0,3,0.5f);
		b.setElementAt(1,0,0.0);b.setElementAt(1,1,0.5);b.setElementAt(1,2,0.0);b.setElementAt(1,3,0.5f);
		b.setElementAt(2,0,0.0);b.setElementAt(2,1,0.0);b.setElementAt(2,2,0.5);b.setElementAt(2,3,0.5f);
		b.setElementAt(3,0,0.0);b.setElementAt(3,1,0.0);b.setElementAt(3,2,0.0);b.setElementAt(3,3,1.0f);
		
		// jade declaration
		float [ ] matAmb = new float[ ] { .135f, .2225f, .1575f, .95f };
		float [ ] matDif = new float[ ] { .54f, .89f, .63f, .95f };
		float [ ] matSpec = new float[ ] { .3162f, .3162f, .3162f, .95f };
		JADE.setAmbient(matAmb);
		JADE.setDiffuse(matDif);
		JADE.setSpecular(matSpec);
		JADE.setShininess(12.8f);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		System.out.println("OpenGL Version: " + gl.glGetString(gl.GL_VERSION));
		System.out.println("JOGL Version: " + (Package.getPackage("com.jogamp.opengl")).getImplementationVersion());
		System.out.println("Java Version: " + System.getProperty("java.version"));
	}
	
	public void setupShadowBuffers()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadow_buffer, 0);
	
		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
	}
	
	public void setupTextures()
	{
		joglSunTexture = loadTexture("sun.jpg");
		sunTexture = joglSunTexture.getTextureObject();
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
	}

// -----------------------------
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		camera.setPerspective(width, height);
		setupShadowBuffers();
	}

	private void setupVertices()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		// pyramid definition
		Vertex3D[] pyramid_vertices = pyramid.getVertices();
		numPyramidVertices = pyramid.getNumVertices();

		float[] pyramid_vertex_positions = new float[numPyramidVertices*3];
		float[] pyramid_textures = new float[numPyramidVertices*2];
		float[] pyramid_normals = new float[numPyramidVertices*3];

		for (int i=0; i<numPyramidVertices; i++)
		{	
			// object vertex positions
			pyramid_vertex_positions[i*3]   = (float) (pyramid_vertices[i]).getX();			
			pyramid_vertex_positions[i*3+1] = (float) (pyramid_vertices[i]).getY();
			pyramid_vertex_positions[i*3+2] = (float) (pyramid_vertices[i]).getZ();
			// object texture positions
			pyramid_textures[i*2] = (float) (pyramid_vertices[i]).getS();
			pyramid_textures[i*2+1] = (float) (pyramid_vertices[i]).getT();
			// object normal positions
			pyramid_normals[i*3]   = (float) (pyramid_vertices[i]).getNormalX();
			pyramid_normals[i*3+1] = (float) (pyramid_vertices[i]).getNormalY();
			pyramid_normals[i*3+2] = (float) (pyramid_vertices[i]).getNormalZ();
		}

		// torus definition
		Vertex3D[] torus_vertices = myTorus.getVertices();
		
		int[] torus_indices = myTorus.getIndices();	
		float[] torus_fvalues = new float[torus_indices.length*3];
		float[] torus_tvalues = new float[torus_indices.length*2];
		float[] torus_nvalues = new float[torus_indices.length*3];
		
		for (int i=0; i<torus_indices.length; i++)
		{	
			// object vertex positions
			torus_fvalues[i*3]   = (float) (torus_vertices[torus_indices[i]]).getX();			
			torus_fvalues[i*3+1] = (float) (torus_vertices[torus_indices[i]]).getY();
			torus_fvalues[i*3+2] = (float) (torus_vertices[torus_indices[i]]).getZ();
			// object texture positions
			torus_tvalues[i*2] = (float) (torus_vertices[torus_indices[i]]).getS();
			torus_tvalues[i*2+1] = (float) (torus_vertices[torus_indices[i]]).getT();
			// object normal positions
			torus_nvalues[i*3]   = (float) (torus_vertices[torus_indices[i]]).getNormalX();
			torus_nvalues[i*3+1] = (float) (torus_vertices[torus_indices[i]]).getNormalY();
			torus_nvalues[i*3+2] = (float) (torus_vertices[torus_indices[i]]).getNormalZ();
		}
		
		numTorusVertices = torus_indices.length;
		
		// sphere definition
		
		Vertex3D[] sphere_vertices = mySphere.getVertices();
		
		int[] sphere_indices = myTorus.getIndices();	
		float[] sphere_fvalues = new float[sphere_indices.length*3];
		float[] sphere_tvalues = new float[sphere_indices.length*2];
		float[] sphere_nvalues = new float[sphere_indices.length*3];
		
		for (int i=0; i<torus_indices.length; i++)
		{	
			// object vertex positions
			sphere_fvalues[i*3]   = (float) (sphere_vertices[sphere_indices[i]]).getX();			
			sphere_fvalues[i*3+1] = (float) (sphere_vertices[sphere_indices[i]]).getY();
			sphere_fvalues[i*3+2] = (float) (sphere_vertices[sphere_indices[i]]).getZ();
			// object texture positions
			sphere_tvalues[i*2] = (float) (sphere_vertices[sphere_indices[i]]).getS();
			sphere_tvalues[i*2+1] = (float) (sphere_vertices[sphere_indices[i]]).getT();
			// object normal positions
			sphere_nvalues[i*3]   = (float) (sphere_vertices[sphere_indices[i]]).getNormalX();
			sphere_nvalues[i*3+1] = (float) (sphere_vertices[sphere_indices[i]]).getNormalY();
			sphere_nvalues[i*3+2] = (float) (sphere_vertices[sphere_indices[i]]).getNormalZ();
		}
		
		numSphereVertices = sphere_indices.length;

		// line defintions
		ModelObjects lineX = new ModelObjects("lineX");
		ModelObjects lineY = new ModelObjects("lineY");
		ModelObjects lineZ = new ModelObjects("lineZ");
		
		float[] lineX_positions = lineX.getVertices();
		float[] lineY_positions = lineY.getVertices();
		float[] lineZ_positions = lineZ.getVertices();
		float[] lineTex = lineX.getTextures();
		
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(20, vbo, 0);

		//  put the Torus vertices into the first buffer,
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(torus_fvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		//  load the pyramid vertices into the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer pyrVertBuf = Buffers.newDirectFloatBuffer(pyramid_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrVertBuf.limit()*4, pyrVertBuf, GL_STATIC_DRAW);
		
		// load the torus normal coordinates into the third buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer torusNorBuf = Buffers.newDirectFloatBuffer(torus_nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torusNorBuf.limit()*4, torusNorBuf, GL_STATIC_DRAW);
		
		// load the pyramid normal coordinates into the fourth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer pyrNorBuf = Buffers.newDirectFloatBuffer(pyramid_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrNorBuf.limit()*4, pyrNorBuf, GL_STATIC_DRAW);

		// load the torus texture coordinates into the fifth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(torus_tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		// load the torus texture coordinates into the fifth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer torusTexBuf = Buffers.newDirectFloatBuffer(torus_tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torusTexBuf.limit()*4, torusTexBuf, GL_STATIC_DRAW);

		// load the pyramid texture coordinates into the sixth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer pyrTexBuf = Buffers.newDirectFloatBuffer(pyramid_textures);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrTexBuf.limit()*4, pyrTexBuf, GL_STATIC_DRAW);
		
		// load the line Z vertices into the seventh buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer lineZBuf = Buffers.newDirectFloatBuffer(lineZ_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, lineZBuf.limit()*4, lineZBuf, GL_STATIC_DRAW);
		
		// load the line Y vertices into the eight buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer lineYBuf = Buffers.newDirectFloatBuffer(lineY_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, lineYBuf.limit()*4, lineYBuf, GL_STATIC_DRAW);
		
		// load the line X vertices into the ninth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer lineXBuf = Buffers.newDirectFloatBuffer(lineX_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, lineXBuf.limit()*4, lineXBuf, GL_STATIC_DRAW);
		
		// load the line textures into the tenth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer axisTexBuf = Buffers.newDirectFloatBuffer(lineTex);
		gl.glBufferData(GL_ARRAY_BUFFER, axisTexBuf.limit()*4, axisTexBuf, GL_STATIC_DRAW);
		
		// load the sphere into the buffer
		// load the sphere vertices into the eleventh buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer sphereVertBuf = Buffers.newDirectFloatBuffer(sphere_fvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereVertBuf.limit()*4, sphereVertBuf, GL_STATIC_DRAW);
		
		// load the sphere texture into the twelfth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer sphereTexBuf = Buffers.newDirectFloatBuffer(sphere_tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereTexBuf.limit()*4, sphereTexBuf, GL_STATIC_DRAW);
		
		// load the sphere normal into the thirteenth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer sphereNorBuf = Buffers.newDirectFloatBuffer(sphere_nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereNorBuf.limit()*4, sphereNorBuf, GL_STATIC_DRAW);
	}
	
	private void installLights(int rendering_program, Matrix3D v_matrix)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = new Material();
		currentMaterial = thisMaterial;
		
		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);
		
		float [] currLightPos = new float[] { (float) lightPv.getX(), (float) lightPv.getY(), (float) lightPv.getZ() };

		// get the location of the global ambient light field in the shader
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
	
		// set the current globalAmbient settings
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);

		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");

		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);
	
		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}

	public static void main(String[] args) { new Code(); }

	@Override
	public void dispose(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) drawable.getGL();
		gl.glDeleteVertexArrays(1, vao, 0);
	}

//-----------------
	private void createShaderPrograms()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];

		//vBlinn1ShaderSource = util.readShaderSource("a3/blinnVert1.shader");
		//vBlinn2ShaderSource = util.readShaderSource("a3/blinnVert2.shader");
		//fBlinn2ShaderSource = util.readShaderSource("a3/blinnFrag2.shader");
		vBlinn1ShaderSource = util.readShaderSource("a3/vertexShadow.shader");
		vBlinn2ShaderSource = util.readShaderSource("a3/vert.shader");
		fBlinn2ShaderSource = util.readShaderSource("a3/frag.shader");

		int vertexShader1 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader2 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader2 = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vertexShader1, vBlinn1ShaderSource.length, vBlinn1ShaderSource, null, 0);
		gl.glShaderSource(vertexShader2, vBlinn2ShaderSource.length, vBlinn2ShaderSource, null, 0);
		gl.glShaderSource(fragmentShader2, fBlinn2ShaderSource.length, fBlinn2ShaderSource, null, 0);

		gl.glCompileShader(vertexShader1);
		gl.glCompileShader(vertexShader2);
		gl.glCompileShader(fragmentShader2);

		rendering_program1 = gl.glCreateProgram();
		rendering_program2 = gl.glCreateProgram();

		gl.glAttachShader(rendering_program1, vertexShader1);
		gl.glAttachShader(rendering_program2, vertexShader2);
		gl.glAttachShader(rendering_program2, fragmentShader2);

		gl.glLinkProgram(rendering_program1);
		gl.glLinkProgram(rendering_program2);
	}

//------------------
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
	
	//Handles applying imported textures to models
	public Texture loadTexture(String textureFile)
	{
		Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFile), false); }
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}
	
	public Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y)
	{	
		Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return(look);
	}
	
	//Handles all of the Actions, modifies the Camera's position and rotation
	//Increases or decreases camera positions and rotate accordingly, and adjusts during <display>'s run
	// camera controls
	public void cameraX(float direction) { camera.translate(-direction, 0.0f, 0.0f); }
	public void cameraY(float direction) { camera.translate(0.0f, -direction, 0.0f); }
	public void cameraZ(float direction) { camera.translate(0.0f, 0.0f, -direction); }
	public void cameraU(float rotation) { camera.rotate(-rotation, 0.0f, 0.0f); }
	public void cameraV(float rotation) { camera.rotate(0.0f, -rotation, 0.0f); }
	// light controls
	public void lightX(float direction) { lightLoc.setX(lightLoc.getX() + direction); }
	public void lightY(float direction) { lightLoc.setY(lightLoc.getY() + direction); }
	public void lightZ(float direction) { lightLoc.setZ(lightLoc.getZ() + direction); }
	// visibility controls
	public void axisVisibility() { isVisible = !isVisible; }
	public void lightVisible() 
	{ 
		float[] amb;
		if (lightVisible) {
			amb = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
		} else {
			amb = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
		}
		currentLight.setAmbient(amb);
		lightVisible = !lightVisible; 
	}
}