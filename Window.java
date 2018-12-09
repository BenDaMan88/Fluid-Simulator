import javax.swing.*;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
 * I am using the java3d reference libraries. This class creates the program window with menu and 3d universe.
 * 
 * @author Ben Johnson
 * @version 5-19-2017
 *
 */
public class Window {
	
	private JFrame frame;
	private JFrame docWindow;
	private JFrame particleWindow;
	private JPanel panel;
	private JMenuBar menuBar; 
	private JMenu file;
	private JMenu runStop;
	private JMenu particle;
	private JMenu help;
	private JMenuItem exit;
	private JMenuItem run;
	private JMenuItem stop;
	private JMenuItem step;
	private JMenuItem addParticle;
	private JMenuItem deleteParticles;
	private JMenuItem documentation;
	private JTextField numField;
	private JTextField sizeField;
	private JComboBox<String> colorField;
	private JButton ok;
	private JButton cancel;
	
	private Canvas3D viewPort;
	private SimpleUniverse world;
	private GraphicsConfiguration configuration;
	private BranchGroup group;
	private BranchGroup particlesGroup;
	private BranchGroup objRoot;
	private TransformGroup transformGroupBox;
	private DirectionalLight light1;
	private DirectionalLight light4;
	private PointLight light2;
	private PointLight light3;
	private QuadArray box;
	private ArrayList<Particle> particles;
	
	private Timer timer;
	private TimerTask task;
	
	private double boxSize = 1;
	private boolean timerRunning = false;
	
	/**
	 * Constructor for the window class. Creates the JFrame window for the program and sets all of the initial parameters.
	 */
	public Window()
	{
		//Instantiate main window components
		initializeComponents();
		//Add window components
		panel.setLayout(new BorderLayout());
		setMenuComponents();
		frame.add(panel);
		panel.add(viewPort);
		world.getViewingPlatform().setNominalViewingTransform();
		//Initial parameters
		setWindowParameters();
		setMenuEvents();
		setNavigation();
		addBox();
		initializeParticle();
		addDirectionalLights();
		addPointLights();
		timerSet();	
	}
	
	/**
	 * Sets the Menu components for the main window.
	 */
	public void setMenuComponents()
	{
		//file tab
		file.add(exit);
		menuBar.add(file);
		//run/stop tab
		runStop.add(run);
		runStop.add(stop);
		runStop.add(step);
		menuBar.add(runStop);
		//particle tab
		particle.add(addParticle);
		particle.add(deleteParticles);
		menuBar.add(particle);
		//help tab
		help.add(documentation);
		menuBar.add(help);
		//set main windows menu
		frame.setJMenuBar(menuBar);
	}
	
	/**
	 * Sets the initial main window settings.
	 */
	public void setWindowParameters()
	{
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(700, 700);
		frame.setTitle("Fluid Simulation");
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
	
	/**
	 * Instantiates all of the components of the window and universe.
	 */
	public void initializeComponents()
	{
		//main window
		frame = new JFrame();
		panel = new JPanel();
		menuBar = new JMenuBar();
		//file menu tab
		file = new JMenu("File");
		exit = new JMenuItem("Exit");
		//run/stop menu tab
		runStop = new JMenu("Run/Stop");
		run = new JMenuItem("Run");
		stop = new JMenuItem("Stop");
		step = new JMenuItem("Step");	
		//particle tab
		particle = new JMenu("Particle");
		addParticle = new JMenuItem("Add Particles");
		deleteParticles = new JMenuItem("Delete Particles");
		//help tab
		help = new JMenu("Help");
		documentation = new JMenuItem("Documentation");
		//universe configuration
		configuration = SimpleUniverse.getPreferredConfiguration();
		viewPort = new Canvas3D(configuration);
		world = new SimpleUniverse(viewPort);
	}
	
	/**
	 * Initializes all of the event handlers for the menu buttons.
	 */
	public void setMenuEvents()
	{
		ExitEvent exitEvent = new ExitEvent();
		exit.addActionListener(exitEvent);
		
		RunEvent runEvent = new RunEvent();
		run.addActionListener(runEvent);
		
		StopEvent stopEvent = new StopEvent();
		stop.addActionListener(stopEvent);
		
		StepEvent stepEvent = new StepEvent();
		step.addActionListener(stepEvent);
		
		AddParticleEvent addParticleEvent = new AddParticleEvent();
		addParticle.addActionListener(addParticleEvent);
		
		DeleteParticleEvent deleteParticleEvent = new DeleteParticleEvent();
		deleteParticles.addActionListener(deleteParticleEvent);
		
		DocumentationEvent documentationEvent = new DocumentationEvent();
		documentation.addActionListener(documentationEvent);
	}
	
	/**
	 * creates the mouse navigation
	 */
	public void setNavigation()
	{
		//navigation
		OrbitBehavior orbit = new OrbitBehavior(viewPort, OrbitBehavior.STOP_ZOOM);
		orbit.setSchedulingBounds(new BoundingSphere());
		orbit.setViewingPlatform(world.getViewingPlatform());
		world.getViewingPlatform().setViewPlatformBehavior(orbit);
		orbit.setReverseRotate(true);
		Object[] disable = {false};
		orbit.setMinRadius(.5);
		orbit.TranslateEnable(disable);
		orbit.setRotFactors(.45, .45);
	}
	
	/**
	 * creates a single particle
	 */
	public void initializeParticle()
	{
		particles = new ArrayList<Particle>();
		particlesGroup = new BranchGroup();
		particlesGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		particlesGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		
		//atomic makeup of air - 78% nitrogen, 21% oxygen, 1 % argon
		addParticle(78, .015f, new Color3f(Color.CYAN));
		addParticle(21, .015f, new Color3f(Color.RED));
		addParticle(1, .015f, new Color3f(Color.YELLOW));
	}
	
	/**
	 * adds the specified number of particles to the universe with the given color and size.
	 * 
	 * @param num - number of particles to create
	 * @param size - size of each particle
	 * @param color - color of each particle
	 */
	public void addParticle(int num, float size, Color3f color)
	{
		for(int i = 0; i < num; i++)
		{
			Particle particle = new Particle(size, color, boxSize);
			int count = 0;
			while(count < 10000)
			{
				if(!intersectOtherParticle(particle))
					break;
				else
				{
					particle.setRandomLocation();
					count++;
				}
			}
			if(count == 10000)
			{
				removeLastParticlesFromArrayList(i);
				return;
			}
			particles.add(particle);
		}
		
		for(int i = particles.size()-num; i < particles.size(); i++)
		{
			particlesGroup.addChild(particles.get(i).getParticleBranchGroup());
		}
	}
	
	/**
	 * Removes the last number num particles in the arrayList particles.
	 * 
	 * @param num - the number of particles to be removed.
	 */
	public void removeLastParticlesFromArrayList(int num)
	{
		int end = particles.size() - num + 1;
		while(particles.size() > end)
		{
			particles.remove(particles.size()-1);
		}
	}
	
	/**
	 * Checks if particle intersects with any other particles.
	 * 
	 * @param particle - the particle to compare.
	 * @return boolean - whether particle intersects with any other particle or not.
	 */
	public boolean intersectOtherParticle(Particle particle)
	{
		for(Particle other : particles)
		{
			if(particle.intersectOtherParticle(other))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes all of the particles in the universe.
	 */
	public void deleteParticles()
	{
		for(Particle particle : particles)
		{
			particle.getParticleBranchGroup().detach();
		}
		particles.removeAll(particles);
	}
	
	/**
	 * adds two white directional light to the scene.
	 */
	public void addDirectionalLights()
	{
		Color3f light1Color = new Color3f(.7f, .7f, .7f);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
		Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
		light1 = new DirectionalLight(light1Color, light1Direction);
		light1.setInfluencingBounds(bounds);
		particlesGroup.addChild(light1);
		
		Color3f light4Color = new Color3f(.7f, .7f, .7f);
		Vector3f light4Direction = new Vector3f(-4.0f, 7.0f, 12.0f);
		light4 = new DirectionalLight(light4Color, light4Direction);
		light4.setInfluencingBounds(bounds);
		particlesGroup.addChild(light4);
	}
	
	/**
	 * Creates and adds 2 white point lights to the universe.
	 */
	public void addPointLights()
	{
		Color3f light2Color = new Color3f(2f, 2f, 2f);
		BoundingSphere light2Bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
		light2 = new PointLight(true, light2Color, new Point3f(1f, 0f, 1f), new Point3f(2,1,1));
		light3 = new PointLight(true, light2Color, new Point3f(-1f, 0f, -1f), new Point3f(4,0,0));
		light2.setInfluencingBounds(light2Bounds);
		particlesGroup.addChild(light2);
		light3.setInfluencingBounds(light2Bounds);
		particlesGroup.addChild(light3);
		world.addBranchGraph(particlesGroup);
	}
	
	/**
	 * adds the outlined white bounding box for the particles.
	 */
	public void addBox()
	{
		Appearance boxAppearance = new Appearance();
		boxAppearance.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_NONE, 0));
		Shape3D boxFinal = new Shape3D(createBox(),boxAppearance);
		
		group = new BranchGroup();
		objRoot = new BranchGroup();
		transformGroupBox = new TransformGroup();
		transformGroupBox.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroupBox.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
		transformGroupBox.addChild(boxFinal);
		objRoot.addChild(transformGroupBox);		
		group.addChild(objRoot);
		world.addBranchGraph(group);
	}
	
	/**
	 * Specifics and orders all of the points for a cube with side dimensions of boxSize.
	 * 
	 * @return the array of points with normals to create a cube.
	 */
	public GeometryArray createBox()
	{
		box = new QuadArray(24, QuadArray.COORDINATES);
		
		Point3d a = new Point3d(boxSize/2,boxSize/2,boxSize/2);
		Point3d b = new Point3d(boxSize/2,-boxSize/2,boxSize/2);
		Point3d c = new Point3d(-boxSize/2,-boxSize/2,boxSize/2);
		Point3d d = new Point3d(-boxSize/2,boxSize/2,boxSize/2);
		Point3d e = new Point3d(-boxSize/2,boxSize/2,-boxSize/2);
		Point3d f = new Point3d(boxSize/2,boxSize/2,-boxSize/2);
		Point3d g = new Point3d(boxSize/2,-boxSize/2,-boxSize/2);
		Point3d h = new Point3d(-boxSize/2,-boxSize/2,-boxSize/2);
		
		setBoxFace(0, a, b, c, d);			//first face
		setBoxFace(4, b, g, h, c);			//second face
		setBoxFace(8, c, d, e, h);			//third face
		setBoxFace(12, a, d, e, f);			//fourth face
		setBoxFace(16, g, h, e, f);			//fifth face
		setBoxFace(20, a, b, g, f);			//sixth face
			
		return setBoxNormals();
	}
	
	/**
	 * Generates the normal maps for the box Shape3D.
	 * 
	 * @return GeometryArray - the normal map for the box.
	 */
	public GeometryArray setBoxNormals()
	{
		GeometryInfo boxInfo = new GeometryInfo(box);
		NormalGenerator normals = new NormalGenerator();
		normals.generateNormals(boxInfo);
		GeometryArray result = boxInfo.getGeometryArray();
		return result;
	}
	
	/**
	 * Specifies the vertices for a face on the box Shape3D.
	 * 
	 * @param numStart - the starting coordinate for the face.
	 * @param a - the first vertex.
	 * @param b - the second vertex.
	 * @param c - the third vertex.
	 * @param d - the fourth vertex.
	 */
	public void setBoxFace(int numStart, Point3d a, Point3d b, Point3d c, Point3d d)
	{
		box.setCoordinate(numStart, a);
		box.setCoordinate(numStart+1, b);
		box.setCoordinate(numStart+2, c);
		box.setCoordinate(numStart+3, d);
	}
	
	/**
	 * Creates the particle/add particle window that offers parameters to enter to create new particles.
	 */
	public void makeAddParticleWindow()
	{
		setParticleWindowFrame();
		JLabel message = new JLabel("Specify parameters of new particles.");
		JLabel limits = new JLabel("(number of particles between 0 and 100, size between 0 and .5)");
		JLabel limits2 = new JLabel("           *limit 1000 total particles at once*           ");
		JLabel numParticles = new JLabel("Number of new particles:");
		numField = new JTextField("0",15);
		JLabel sizeParticles = new JLabel("Size of new particles:");
		sizeField = new JTextField("0",15);
		JLabel colorParticles = new JLabel("Color of new particles:");
		String[] colors = {"CYAN", "RED", "YELLOW", "GREEN"};
		colorField = new JComboBox<String>(colors);
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		JPanel particlePanel = new JPanel();
		particlePanel.add(message);
		particlePanel.add(limits);
		particlePanel.add(limits2);
		particlePanel.add(numParticles);
		particlePanel.add(numField);
		particlePanel.add(sizeParticles);
		particlePanel.add(sizeField);
		particlePanel.add(colorParticles);
		particlePanel.add(colorField);
		particlePanel.add(ok);
		particlePanel.add(cancel);
		particleWindow.add(particlePanel);
		setParticleWindowEvents();
	}
	
	/**
	 * Sets the JFrame parameters for the particle window.
	 */
	public void setParticleWindowFrame()
	{
		particleWindow = new JFrame();
		particleWindow.setVisible(true);
		particleWindow.setSize(400, 200);
		particleWindow.setLocationRelativeTo(null);
		particleWindow.setResizable(false);
		particleWindow.setTitle("Add Particles");
		particleWindow.setAlwaysOnTop(true);
	}
	
	/**
	 * Sets the event listeners for the particle window buttons.
	 */
	public void setParticleWindowEvents()
	{
		OkEvent okEvent = new OkEvent();
		ok.addActionListener(okEvent);
		
		CancelEvent cancelEvent = new CancelEvent();
		cancel.addActionListener(cancelEvent);
	}
	
	/**
	 * Creates the help/documentation window with helpful information.
	 */
	public void makeDocumentationWindow()
	{
		docWindow = new JFrame();
		docWindow.setTitle("Documentation");
		docWindow.setSize(300, 400);
		docWindow.setLocationRelativeTo(null);
		docWindow.setVisible(true);
		docWindow.setResizable(false);
		JTextArea instructions = new JTextArea("Fluid Simulation\n"+
											   "By Ben Johnson\n"+
											   "Version: 5-29-2017\n"+
											   "\n"+
											   "This program simulates gases particles and other \nfluids in a closed container.\n"+
											   "The intial simulation represents the approximate \natomic makeup of air. 78% nitorgen, 21% oxygen, \n1% argon."+
											   "\n"+
											   "To Rotate the view about the origin left-click and drag \nthe mouse around.\n"+
											   "The simulation can be stopped, started again, and \niterated through "+
											   "step-by-step with the corresponding \nmenu buttons in the Run/Stop menu.\n"+
											   "You can add a specified amount of particles to the \nsimulation or delete all of the current particles \nthrough the partciles menu.\n"+
											   "I hope you learn and enjoy.", 20, 20);
		JPanel docPanel = new JPanel();
		instructions.setEditable(false);
		docPanel.add(instructions);
		JButton close = new JButton("Close");
		docPanel.add(close);
		docWindow.add(docPanel);
		
		CloseEvent closeEvent = new CloseEvent();
		close.addActionListener(closeEvent);
	}
	
	/**
	 * Creates new particles with the specified parameters.
	 */
	public void addNewParticles()
	{
		int num = (int)Double.parseDouble(numField.getText());
		float size = Float.parseFloat(sizeField.getText());
		Color3f color = new Color3f(getColorFromBox());
		if(num <= 0 || size <= 0 || num > 100 || size > .5 || particles.size() + num > 1000)
			return;
		addParticle(num, size, color);
	}
	
	/**
	 * Sets the correct color from the drop down menu in the add particle window.
	 * 
	 * @return Color - the color in the drop down box.
	 */
	public Color getColorFromBox()
	{
		if(((String)colorField.getSelectedItem()).equals("CYAN"))
			return Color.CYAN;
		else if(((String)colorField.getSelectedItem()).equals("RED"))
			return Color.RED;
		else if(((String)colorField.getSelectedItem()).equals("YELLOW"))
			return Color.YELLOW;
		else
			return Color.GREEN;
	}
	
	/**
	 * Checks to see if two particles are intersecting, if they are fix them.
	 */
	public void checkCollisions(Particle particle)
	{
		if(intersectOtherParticle(particle))
		{
			int i;
			for(i = 0; i < particles.size(); i++)
			{
				if(particle.intersectOtherParticle(particles.get(i)))
					break;
			}
				particle.collisionFix(particles.get(i));
		}
	}
	
	/**
	 * Creates a timer to iterate through the simulation.
	 */
	public void timerSet()
	{
		if(!timerRunning)
		{
			timer = new Timer();
			task = new TimerTask() { public void run() { update(); } };
			timer.scheduleAtFixedRate(task, 10, 25);
			timerRunning = true;
		}
	}
	
	/**
	 * The code that is run every time an iteration of the simulation is calculated.
	 */
	public void update()
	{
		for(Particle p : particles)
		{
			p.addVelocity();
			p.checkBoxIntersection();
			checkCollisions(p);
			p.updatePosition();
		}
	}
		
	/**
	 * The listener for the file/exit button press, closes the program.
	 * 
	 * @author Ben Johnson 
	 */
	public class ExitEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			System.exit(0);
		}
	} 
	
	/**
	 * The listener for the Run/Stop/Run button press, runs the simulation.
	 * 
	 * @author Ben Johnson
	 */
	public class RunEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(timerRunning)
				return;
			else
				timerSet();
		}
	}
	
	/**
	 * The listener for the Run/Stop/Stop button press, stops the simulation.
	 * 
	 * @author Ben Johnson
	 */
	public class StopEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(!timerRunning)
				return;
			else
				timer.cancel();
				timerRunning = false;
		}
	}
	
	/**
	 * The listener for the Run/Stop/Step button press, steps through one iteration of the simulation.
	 * 
	 * @author Ben Johnson
	 */
	public class StepEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(timerRunning)
				return;
			else
				update();
		}
	}
	
	/**
	 * The listener for the help/documentation button press, opens external documentation window.
	 * 
	 * @author Ben Johnson 
	 */
	public class DocumentationEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			timer.cancel();
			timerRunning = false;
			makeDocumentationWindow();
		}
	} 
	
	/**
	 * The listener for the particle/add particles button press, opens external window.
	 * 
	 * @author Ben Johnson 
	 */
	public class AddParticleEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			timer.cancel();
			timerRunning = false;
			makeAddParticleWindow();
		}
	} 
	
	/**
	 * The listener for the particle/delete particles button press, removes all particles.
	 * 
	 * @author Ben Johnson 
	 */
	public class DeleteParticleEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			timer.cancel();
			timerRunning = false;
			deleteParticles();
		}
	} 
	
	/**
	 * Listener for the documentation window close button. Closes documentation window.
	 * 
	 * @author Owner
	 */
	public class CloseEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			docWindow.dispose();
			timerSet();
		}
	}
	
	/**
	 * Listener for the add particle window close button. Closes add particle window.
	 * 
	 * @author Ben Johnson
	 */
	public class CancelEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			particleWindow.dispose();
			timerSet();
		}
	}
	
	/**
	 * Listener for the add particle window ok button. Closes add particle window and creates new particle.
	 * 
	 * @author Ben Johnson
	 */
	public class OkEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			particleWindow.dispose();
			addNewParticles();
			timerSet();
		}
	}
	
	/**
	 * the main method of the program. Sets the system properties for java 3D and creates a window object.
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException
	{
		System.setProperty("sun.awt.noerasebackground", "true");
		new Window();
	}
	
}
