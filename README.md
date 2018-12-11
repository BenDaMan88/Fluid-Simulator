# Fluid-Simulator

This program simulates the behvior of individual atoms in a fluid, mostly a gas, in a closed container. This program is made in Java and uses the Java3D libraries and APIs. 

The initial simulation shown is the average make up of air, 78% nitrogen, 21% oxygen, 1 % argon. A screenshot of this simulation is shown in the image below. 

The program is made of two main classes, a Window class and a Particle class. The Window class generates the visual window and holds the main canvas for the simulation rendering. The Window class also runs the main simulation updating the screen all of the particles multiple times every second. 

The particle class represents an indivdual particle in the simulation. It holds the particle's position, velocity and acceleration vectors along with all of the visual components of the particle, size and color. The main bulk of the collision detection is handled in this class definition. 

The main jar file included, Fluid_Sim_v5-29-2017.jar, holds program and can be run. **Note to run the program Java3D must be installed on your machine.**

This Program is based off of the ideas and program **Atoms In Motion** created by Scott Johnson. **DO NOT copy for commercial use.**

![alt text](https://github.com/BenDaMan88/Fluid-Simulator/blob/master/Main_screen.PNG)
