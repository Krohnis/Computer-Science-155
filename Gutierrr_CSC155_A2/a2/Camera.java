package a2;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;

public class Camera 
{
	private Vector3D position = new Vector3D(0, 0, 0);
	private Vector3D uAxis = new Vector3D(1, 0, 0);
	private Vector3D vAxis = new Vector3D(0, 1, 0);
	private Vector3D nAxis = new Vector3D(0, 0, 1);
	
	private Matrix3D perspectiveMatrix;
	private Matrix3D translationMatrix = new Matrix3D();
	private Matrix3D rotationMatrix = new Matrix3D();
	private Matrix3D viewMatrix = new Matrix3D();
	
	private float aspectRatio;
	private float fov = 60;
	private float minClip = 0.1f;
	private float maxClip = 100000.0f;
	
	public Camera(int width, int height)
	{
		aspectRatio = (float) width / (float) height;
		perspectiveMatrix = perspective(fov, aspectRatio, minClip, maxClip);
	}
	
	public void combinedMatrix()
	{
		viewMatrix.setToIdentity();
		viewMatrix.concatenate(rotationMatrix);
		viewMatrix.concatenate(translationMatrix);
	}
	
	public void translate(float x, float y, float z)
	{
		Vector3D xTranslate = uAxis.mult(x);
		Vector3D yTranslate = vAxis.mult(y);
		Vector3D zTranslate = nAxis.mult(z);
		
		position = position.add(xTranslate).add(yTranslate).add(zTranslate);
		translationMatrix.setToIdentity();
		translationMatrix.translate(position.getX(), position.getY(), position.getZ());
		combinedMatrix();
	}
	
	public void rotate(float u, float v, float n)
	{
		Matrix3D rotation = new Matrix3D();
		rotation.rotate(u, uAxis);
		rotation.rotate(v, vAxis);
		rotation.rotate(n, nAxis);
		uAxis = uAxis.mult(rotation);
		vAxis = vAxis.mult(rotation);
		nAxis = nAxis.mult(rotation);
		
		rotationMatrix.concatenate(rotation.inverse());
		combinedMatrix();
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
		r.setElementAt(3, 3, 0.0f);
		return r;
	}
	
	public Matrix3D getPerspectiveMatrix()
	{
		return perspectiveMatrix;
	}
	
	public Matrix3D getCombinedMatrix()
	{
		return viewMatrix;
	}
}
