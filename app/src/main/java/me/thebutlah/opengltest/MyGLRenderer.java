package me.thebutlah.opengltest;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.thebutlah.perlinnoise.PerlinNoise;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private final MainActivity mainActivity;

    private ShaderProgram shaderProgram;
    //an array is used for two reasons: because there can be more than one buffer if desired, and because arrays are pass by reference
    private final int[] vboHandle = new int[1];

    /*private final float[] vertices = {
            0.0f,  0.5f,  0.0f,  1.0f, //each line represents (x,y,z,w) of a single vertex. This, for example, is the top of the triangle
            -0.5f,  0.0f,  0.0f,  1.0f,
            0.5f,  0.0f,  0.0f,  1.0f
    };*/
    private final float[] vertices;

    private final StaticMesh mesh;

    public final Camera camera = new Camera(1,2,-1,0,0,0);

    private float[] viewProjectionMatrix = new float[16];
    private float[] projectionMatrix = new float[16];

    public MyGLRenderer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        PerlinNoise pgen = new PerlinNoise(1337l);
        vertices = new float[3*100*100];
        for (int y=0; y<100 - 1; y++){
            for (int x=0; x<50; x++) {
                setVertex((x*2), (y), vertices, pgen);
                setVertex((x*2), (y+1), vertices, pgen);
                setVertex((x*2+1), (y), vertices, pgen);
                setVertex((x*2+1), (y+1), vertices, pgen);
            }
        }
        mesh = new StaticMesh(vertices);
        mesh.setScale(.1f,.1f,.1f);
    }

    private static void setVertex(int x, int y, float[] vertices, PerlinNoise pgen) {
        vertices[3*(y*100+x)] = x;
        vertices[3*(y*100+x)+1] = (float) (.05*pgen.perlinNoise2D(x/100.0, y/100.0,6, 2, .5));
        vertices[3*(y*100+x)+2] = y;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        Log.v(MainActivity.LOGGER_TAG,"me.thebutlah.opengltest.MyGLRenderer.onSurfaceCreated() called!");

        {//Initialize Shaders
            InputStream basicVertexShaderStream = mainActivity.getResources().openRawResource(R.raw.basic_vertexshader);
            Shader basicVertexShader = new Shader(GLES20.GL_VERTEX_SHADER, basicVertexShaderStream);
            InputStream basicFragmentShaderStream = mainActivity.getResources().openRawResource(R.raw.basic_fragmentshader);
            Shader basicFragmentShader = new Shader(GLES20.GL_FRAGMENT_SHADER, basicFragmentShaderStream);
            try {
                basicVertexShaderStream.close();
                basicFragmentShaderStream.close();
            } catch(IOException e) {
                Log.e(MainActivity.LOGGER_TAG, "Could not close one or more shader streams! However, we have already loaded the shaders so it doesn't really matter!", e);
            }
            shaderProgram = new ShaderProgram(basicVertexShader, basicFragmentShader);
        }

        mesh.initialize(shaderProgram);
        /*{//Set up VBOs
            //Represents the vertex data, but stored in a native buffer in RAM. Note that this is not a VBO.
            //vertices.length * 4 because 1 float = 4 bytes
            FloatBuffer nativeVertices = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            //load the vertex data from the Java heap into the native heap
            nativeVertices.put(vertices);
            //set the pointer to the data as the first element
            nativeVertices.position(0);

            //Get the handle for a VBO, store it in vboHandle
            GLES20.glGenBuffers(1, vboHandle, 0);
            //Set the VBO as active, future calls will be referring to it.
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle[0]);
            //Actually load the VBO, this places the data onto the GPU memory instead of RAM. The native and Java data can now be GCed
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 4 * nativeVertices.capacity(), nativeVertices, GLES20.GL_STATIC_DRAW);
            //Unbind the VBO. Think of this as setting the VBO that is currently active to null. Future OpenGL calls will now not affect VBO.
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        }*/


        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, camera.getViewMatrix(), 0);

        mesh.draw(viewProjectionMatrix);
        /*
        //Use the shader program
        GLES20.glUseProgram(shaderProgram.programID);
        //Bind the VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle[0]);

        //Get a handle for the variable "vPosition" within the shader program. This will be used to reference that attribute.
        int positionAttrib = GLES20.glGetAttribLocation(shaderProgram.programID, "vPosition");
        //Enable the attribute. Unsure why this is really needed, but it is so don't question it.
        GLES20.glEnableVertexAttribArray(positionAttrib);

        //Tell the attribute how the VBO is formatted
        GLES20.glVertexAttribPointer(positionAttrib, 4, GLES20.GL_FLOAT, false, 0, 0);

        int mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram.programID, "mMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, viewProjectionMatrix, 0);
        //Draw effyching
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length);

        //Disable the attribute. Again, don't understand purpose.
        GLES20.glDisableVertexAttribArray(positionAttrib);
        //Unbind the VBO, although in this usage case it isnt even necessary.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        */
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = ((float) width)/height;
        //Set up the Projection Matrix so that it squishes the scene properly so as to appear correct when phone is rotated.
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1f, 10);

    }

}
