package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.AnalogChannel;

/**
* Controls the motors of the collector by utilizing the input from the ball
* detecting infared sensor and the limit switch at the bottom of the collector's
* motion
*/
public class Collector
{
	Talon liftingTalon, rollerTalon;
	AnalogChannel ballSensor;
	DigitalInput lowerLimitSensor;
	Timer timer;

	int m_collectorMode;
	int m_collectorAutoSubstate;
	int m_manualRaiseDirection;
	double m_manualRollerPower;
	double m_automaticRollerPower;

    /**
    * Holds the state values for the m_collectorMode state machine
    */
	public static class CollectorMode
	{
		public static final int automatic = 0,
				disabled = 1,
				manualRoller = 2,
				manualRaise = 3;
	}

    /**
    * Holds the state values for the m_collectorAutoSubstate state machine
    */
	public static class CollectorAutoSubstate
	{
		public static final int lowering = 0,
				waiting = 1,
				waitForBall = 2,
				raising = 3,
				mellowRaise = 4;
	}

    /**
    * @param liftingTalonPort The port number for the talon that controls the
    * collector's arm's motors
    * @param rollerTalonPort The port number for the talon that controls the
    * collector's roller's motor
    * @param ballSensorPort The port number for the infared sensor that detects
    * how far the ball is into the collector
    * @param lowerLimitSensorPort The port number for the limit swtich that 
    * detects when the collector is at the bottom of its motion
    */
	Collector(int liftingTalonPort, int rollerTalonPort, int ballSensorPort,
        int lowerLimitSensorPort)
	{
		liftingTalon = new Talon(liftingTalonPort);
		rollerTalon = new Talon(rollerTalonPort);
		ballSensor = new AnalogChannel(ballSensorPort);
		lowerLimitSensor = new DigitalInput(lowerLimitSensorPort);
		timer = new Timer();
		timer.start();
		m_collectorMode = CollectorMode.disabled;
	}

    /**
    * Sets the mode of the collector to automatic and sets the automatic mode's
    * substate to lowering
    */
	void run()
	{
		m_collectorMode = CollectorMode.automatic;
		m_collectorAutoSubstate = CollectorAutoSubstate.lowering;
	}

    /**
    * Sets the mode of the collector to manualRoller and sets the power at which
    * the roller motor will be set to the input if the input is not 0
    * 
    * @param manualRollerPower The power at which the motor controlling the
    * roller will be run when the collector is in manualRoller mode
    */
	void manualRoller(int manualRollerPower)
	{
		if(manualRollerPower != 0)
		{
			m_collectorMode = CollectorMode.manualRoller;
			m_manualRollerPower = manualRollerPower;
		}
		else
		{
			m_manualRollerPower = 0;
		}
	}

    /**
    * Sets the power at which the motor controlling the roller will run when the
    * collector is in automatic mode
    *
    * @param automaticRollerPower The power at which the motor controlling the 
    * roller will be run when the collector is in automatic mode
    */
	void setAutomaticRollerPower(double automaticRollerPower)
	{
		m_automaticRollerPower = automaticRollerPower;
	}

    /**
    * Sets the collector's mode to disabled.
    */
	void disable()
	{
		m_collectorMode = CollectorMode.disabled;
	}

    /**
    * Raises the lifting arm at a very low power so it can be assisted by a
    * human in order for the arm to be stored for transport
    */
	void assistedManualRaise()
	{
		liftingTalon.set(0.3);
	}

    /**
    * Sets the collector's mode to manual raise and sets the power and direction
    * at which the arm will moved
    *
    * @param direction The direction the arm will be moved: -1 causes it to move
    * down, 0 causes no motion, and 1 causes the arm to move up
    */
	void manualRaise(int direction)
	{
		if(direction != 0)
		{
			m_collectorMode = CollectorMode.manualRaise;
			m_manualRaiseDirection = direction;
		}
		else
		{
			m_manualRaiseDirection = 0;
		}
	}

    /**
    * Controls the collector's states and sets all motor powers, must be
    * called every loop for the collector to operate
    */ 
	void idle()
	{
        // Controls the different modes of the collector
		switch(m_collectorMode)
		{
			case CollectorMode.automatic:
                
                // Controls the states of the automatic mode of the collector
				switch(m_collectorAutoSubstate)
				{
				    // Lowers the collector until it hits the lower limit switch	
                    case CollectorAutoSubstate.lowering:
						if(lowerLimitSensor.get())
						{
							liftingTalon.set(-0.4);
							rollerTalon.set(0.0);
						}
						else
						{
							m_collectorAutoSubstate = 
                                CollectorAutoSubstate.waiting;
						}
						break;

                    /* Waits for the ball to be pulled into the collector far
                     enough so that the arm can be raised */
					case CollectorAutoSubstate.waiting:
						if(ballSensor.getValue() < 300)
						{
							liftingTalon.set(0.0);
							rollerTalon.set(m_automaticRollerPower);
						}
						else
						{
							timer.reset();
							timer.start();
							m_collectorAutoSubstate = 
                                CollectorAutoSubstate.waitForBall;
						}
						break;

                    /* Waits a little bit of extra time so that the ball is
                     fully in the collector. This state is necessary because of
                     where the ball sensor is placed within the collector. */
					case CollectorAutoSubstate.waitForBall:
						if(timer.get() < 0.3)
						{
							liftingTalon.set(0.0);
							rollerTalon.set(m_automaticRollerPower);
						}
						else
						{
							timer.reset();
							timer.start();
							m_collectorAutoSubstate = 
                                CollectorAutoSubstate.raising;
						}
						break;

                    // Raises the arm so that the ball can enter the catapult 
					case CollectorAutoSubstate.raising:
						if(timer.get() < 0.75)
						{
							liftingTalon.set(0.6);
							rollerTalon.set(m_automaticRollerPower);
						}
						else
						{
							m_collectorAutoSubstate = 
                                CollectorAutoSubstate.lowering;
						}
						break;
				}
				break;

            // Disables all motors and stops the timer
			case CollectorMode.disabled:
				liftingTalon.set(0.0);
				rollerTalon.set(0.0);
				timer.stop();
				break;

            /* Stops the lifting talon and takes manual input from the driver
             for the direction of the roller */
			case CollectorMode.manualRoller:
				rollerTalon.set(-m_manualRollerPower);
				liftingTalon.set(0.0);
				break;

            /* Takes manual input from the driver for the articulation of the 
             arm */
			case CollectorMode.manualRaise:
				if(m_manualRaiseDirection < 0)
				{
					liftingTalon.set(0.45);
				}
				else if(m_manualRaiseDirection > 0)
				{
					liftingTalon.set(-0.4);
				}
				else
				{
					liftingTalon.set(0);
				}
				break;
		}
	}
}
