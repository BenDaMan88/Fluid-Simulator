import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.geometry.*;

/**
 * portions Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 * 
 * Creates a node tree for the universe in the window class that makes a sphere that is able to move.
 * 
 * @author Ben Johnson
 * @version 5-24-2017
 *
 */
public class Particle {
	
	public static final double INTIALSPEEDCAP = .05;
	public static final int X = 0, Y = 1, Z = 2;
	
	private double radius, diameter;
	private double boxSize;
	
	private BranchGroup particleBranchGroup;
	private TransformGroup particleTransformGroup;
	private Transform3D particleTransform;
	private Sphere sphere;
	private Appearance appearance;
	private Material shader;
	private Color3f color;
	
	private Vector3d positionVector;
	private Vector3d velocityVector;
	private Vector3d accelerationVector;
	
	/**
	 * Constructor for the particle class. Sets all of the properties of the particle and the node tree for the particle.
	 * 
	 * @param radius - the radius of the particle.
	 * @param colorInput - the color of the particle.
	 */
	public Particle(float radius, Color3f colorInput, double boxsize)
	{
		this.radius = radius;
		diameter = radius * 2;
		boxSize = boxsize;
		color = colorInput;
		sphere = new Sphere(radius);
		appearance = new Appearance();
		shader = new Material();
		particleTransform = new Transform3D();
		
		shader.setDiffuseColor(color);
		appearance.setMaterial(shader);
		sphere.setAppearance(appearance);
		
		setRandomLocation();
		setRandomVelocity();
		setInitialAccelerationVector();
		setNodeGroup();
	}
	
	/**
	 * Sets up the node tree for the sphere to attach to the universe while being able to move. 
	 */
	public void setNodeGroup()
	{
		particleBranchGroup = new BranchGroup();
		particleBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		particleTransformGroup = new TransformGroup();
		
		particleTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		particleTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		particleTransformGroup.setTransform(particleTransform);
		
		particleTransformGroup.addChild(sphere);
		particleBranchGroup.addChild(particleTransformGroup);
	}
	
	/**
	 * Checks to see if the particle is at the edge of the boundary and if it is, the particle's velocity is corrected.
	 */
	public void checkBoxIntersection()
	{
		double[] positionComponents = new double[3];
		double[] velocityComponents = new double[3];
		positionVector.get(positionComponents);
		velocityVector.get(velocityComponents);
		
		if(Math.abs(positionComponents[X])+radius >= boxSize/2)
		{
			positionVector = new Vector3d(positionComponents[X]-velocityComponents[X], positionComponents[Y]-velocityComponents[Y], positionComponents[Z]-velocityComponents[Z]);
			velocityVector = new Vector3d(velocityComponents[X]*-1, velocityComponents[Y], velocityComponents[Z]);
		}
		if(Math.abs(positionComponents[Y])+radius >= boxSize/2)
		{
			positionVector = new Vector3d(positionComponents[X]-velocityComponents[X], positionComponents[Y]-velocityComponents[Y], positionComponents[Z]-velocityComponents[Z]);
			velocityVector = new Vector3d(velocityComponents[X], velocityComponents[Y]*-1, velocityComponents[Z]);
		}
		if(Math.abs(positionComponents[Z])+radius >= boxSize/2)
		{
			positionVector = new Vector3d(positionComponents[X]-velocityComponents[X], positionComponents[Y]-velocityComponents[Y], positionComponents[Z]-velocityComponents[Z]);
			velocityVector = new Vector3d(velocityComponents[X], velocityComponents[Y], velocityComponents[Z]*-1);
		}
	}
	
	/**
	 * Corrects the velocities of the 
	 * 
	 * @param Particle - the other intersecting particle.
	 */
	public void collisionFix(Particle other)
	{
		Vector3d myVelocity = new Vector3d();
		Vector3d otherVelocity = new Vector3d();
		myVelocity.set(velocityVector);
		otherVelocity.set(other.getVelocityVector());
		double myLength = myVelocity.length();
		double otherLength = otherVelocity.length();
		double avgLength = (myLength+otherLength)/2;
		otherVelocity.scale(avgLength/otherLength);
		myVelocity.scale(avgLength/myLength);
		velocityVector = otherVelocity;
		other.setVelocityVector(myVelocity);
	}
	
	/**
	 * Checks if 2 particles are intersecting.
	 * 
	 * @param Particle - the other particle.
	 * @return boolean - whether or not the particles are intersecting.
	 */
	public boolean intersectOtherParticle(Particle other)
	{
		Vector3d myPosition = new Vector3d();
		Vector3d otherPosition = new Vector3d();
		myPosition.set(positionVector);
		otherPosition.set(other.getPositionVector());
		Vector3d length = new Vector3d();
		length.sub(myPosition, otherPosition);
		double distance = length.length();
		if(distance < radius + other.getRadius())
			return true;
		return false;
	}
	
	/**
	 * Assigns a random position vector to the particle to be placed in the universe.
	 */
	public void setRandomLocation()
	{
		double x = Math.random()*(boxSize-diameter) - (boxSize-diameter)/2;
		double y = Math.random()*(boxSize-diameter) - (boxSize-diameter)/2;
		double z = Math.random()*(boxSize-diameter) - (boxSize-diameter)/2;
		
		positionVector = new Vector3d(x,y,z);
		particleTransform.setTranslation(positionVector);
	}
	
	/**
	 * Assigns a random velocity vector for the particle.
	 */
	public void setRandomVelocity()
	{
		double x = Math.random()*(INTIALSPEEDCAP)*randomSign();
		double y = Math.random()*(INTIALSPEEDCAP)*randomSign();
		double z = Math.random()*(INTIALSPEEDCAP)*randomSign();
		
		velocityVector = new Vector3d(x,y,z);
	}
	
	/**
	 * Updates the position of the particle by reassigning the updated position vector.
	 */
	public void updatePosition()
	{
		particleTransform.setTranslation(positionVector);
		particleTransformGroup.setTransform(particleTransform);
	}
	
	/**
	 * Assigns a zero vector for the initial acceleration vector for the particle.
	 */
	public void setInitialAccelerationVector()
	{
		accelerationVector = new Vector3d(0,0,0);
	}
	
	/**
	 * Adds the velocity to the position of the particle.
	 */
	public void addVelocity()
	{
		positionVector.add(velocityVector);
	}
	
	/**
	 * Subtracts the velocity from the position of the particle.
	 */
	public void subtractVelocity()
	{
		positionVector.sub(velocityVector);
	}
	
	/**
	 * Adds the acceleration to the velocity of the particle.
	 */
	public void addAcceleration()
	{
		velocityVector.add(accelerationVector);
	}
	
	/**
	 * Generates a random sign, positive or negative. There is a 50% chance for both signs.
	 * 
	 * @return int - either a positive 1 or a negative 1;
	 */
	public int randomSign()
	{
		double num = Math.random();
		if(Math.round(num) == 1)
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}
	
	/**
	 * Accessor method for the radius.
	 * 
	 * @return double - the particle's radius.
	 */
	public double getRadius()
	{
		return radius;
	}
	
	/**
	 * Accessor method for the position vector of the particle.
	 * 
	 * @return Vector3d - the position vector of the particle.
	 */
	public Vector3d getPositionVector()
	{
		return positionVector;
	}
	
	/**
	 * Accessor method for the velocity vector of the particle.
	 * 
	 * @return Vector3d - the velocity vector of the particle.
	 */
	public Vector3d getVelocityVector()
	{
		return velocityVector;
	}
	
	/**
	 * Mutator method for the velocity vector of the particle.
	 * 
	 * @return Vector3d - the new velocity vector of the particle.
	 */
	public void setVelocityVector(Vector3d vec)
	{
		velocityVector = vec;
	}
	
	/**
	 * Accessor method for the overall particle BranchGroup.
	 * 
	 * @return BranchGroup - the node tree to be linked with the universe.
	 */
	public BranchGroup getParticleBranchGroup()
	{
		return particleBranchGroup;
	}
			
	/**
	 * Accessor method for the size of the particle.
	 * 
	 * @return double - the size of the particle.
	 */
	public double getSize()
	{
		return radius;
	}
	
	/**
	 * Accessor method for the actual sphere 3D shape of the particle.
	 * 
	 * @return Sphere - the 3D shape of the particle.
	 */
	public Sphere getSphere()
	{
		return sphere;
	}
		
}
