package a3;

public class ModelObjects 
{
	private String objectName;
	private float[] vertices;
	private float[] textures;
	
	public ModelObjects(String objectName)
	{
		this.objectName = objectName;
		if (objectName.equals("cube")) { cube(); }
		else if (objectName.equals("pyramid")) { pyramid(); }
		else if (objectName.equals("triangle")) { triangle(); }
		else if (objectName.equals("lineX")) { line(256.0f, 0.0f, 0.0f); }
		else if (objectName.equals("lineY")) { line(0.0f, 256.0f, 0.0f); }
		else if (objectName.equals("lineZ")) { line(0.0f, 0.0f, 256.0f); }
	}
	
	public void cube()
	{
		//Vertices of the cube
		float[] cube_positions =
		{	
			-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,	//Bottom Front
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,	//Top Front
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};
		//Texture coordinates of the cube
		float[] cubeTex =
		{
			0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,	//Front
			0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,	//Right
			0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,	//Left
			0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,	//Back
			0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,	//Bottom
			0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f	//Top
		};
		
		//Assigns the new textures to the global arrays, so that they can be returned
		vertices = cube_positions;
		textures = cubeTex;
	}
	public void pyramid()
	{
		//Vertices of the pyramid
		float[] pyramid_positions =
		{	
			-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front
			1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
			-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
			-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
			1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
		};
		//Texture coordinates of the pyramid
		float[] pyramidTex =
		{	
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
		};
		
		//Assigns the new textures to the global arrays, so that they can be returned
		vertices = pyramid_positions;
		textures = pyramidTex;
	}
	
	public void triangle()
	{
		//Vertices of the triangle
		float srqt8o9 = (float) Math.sqrt(8f / 9f);
		float srqt2o9 = (float) Math.sqrt(2f / 9f);
		float srqt2o3 = (float) Math.sqrt(2f / 9f);
		float negOneOthree = -1f / 3f;
		float[] triangle_positions = { -srqt2o9, -srqt2o3, negOneOthree, srqt8o9, 0, negOneOthree, 0, 0, 1, srqt8o9, 0,
				negOneOthree, -srqt2o9, srqt2o3, negOneOthree, 0, 0, 1, -srqt2o9, srqt2o3, negOneOthree, -srqt2o9,
				-srqt2o3, negOneOthree, 0, 0, 1, -srqt2o9, srqt2o3, negOneOthree, srqt8o9, 0, negOneOthree,
				-srqt2o9, -srqt2o3, negOneOthree };

		float[] triangleTex = { 0, 0, 1, 0, .5f, 1, 0, 0, 1, 0, .5f, 1, 0, 0, 1, 0, .5f, 1, 0, 0, 1, 0, .5f, 1 };
		
		vertices = triangle_positions;
		textures = triangleTex;
	}
	
	public void line(float x, float y, float z)
	{
		//Vertices of the line
		float[] line = { 0.0f, 0.0f, 0.0f, x, y, z};
		//Texture coordinates of the line
		float[] lineTex = { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f };
		//Assigns the new textures to the global arrays, so that they can be returned
		vertices = line;
		textures = lineTex;
	}
	
	public float[] getVertices()
	{
		return vertices;
	}
	
	public float[] getTextures()
	{
		return textures;
	}
}
