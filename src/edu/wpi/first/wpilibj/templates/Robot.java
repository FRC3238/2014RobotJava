package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.Timer;

public class Robot extends IterativeRobot
{
    Joystick joystick;
    Chassis chassis;
    Catapult catapult;
    Collector collector;
    AnalogChannel ultrasonicSensor;
    Timer autonomousTimer;

    boolean m_buzzerShot;
    boolean m_autoFired;
    boolean m_unfoldingDone;
    boolean m_collectorStarted;
    int m_unfoldingState;
    int m_autonomousState;
    int m_correctRangeLoops;
    int m_pidError;
    int m_cummulativePIDError;

    public static class UnfoldingState
    {
	public static final int collectorLower = 0,
		catapultLower = 1,
		waitingForCatapult = 2,
		done = 3;
    }

    public static class AutonomousState
    {
	public static final int waiting = 0,
		running = 1,
		stopped = 2;
    }

    Robot()
    {
	// Talon Ports
	final int LEFTFRONTTALONPORT = 6;
	final int LEFTREARTALONPORT = 8;
	final int RIGHTFRONTTALONPORT = 7;
	final int RIGHTREARTALONPORT = 3;
	final int CATAPULTTALONONEPORT = 4;
	final int CATAPULTTALONTWOPORT = 5;
	final int LIFTINGTALONPORT = 1;
	final int ROLLERTALONPORT = 2;
	// Digital Inputs
	final int CATAPULTENCODERPORTA = 5;
	final int CATAPULTENCODERPORTB = 6;
	final int UPPERLIMITSENSORPORT = 4;
	final int LOWERLIMITSENSORPORT = 3;
	// Analog Inputs
	final int BALLSENSORPORT = 7;
	final int ULTRASONICSENSORPORT = 1;
	// Driver Station Inputs
	final int JOYSTICKPORT = 1;

	joystick = new Joystick(JOYSTICKPORT);
	chassis = new Chassis(LEFTFRONTTALONPORT, LEFTREARTALONPORT,
		RIGHTFRONTTALONPORT, RIGHTREARTALONPORT);
	catapult = new Catapult(CATAPULTTALONONEPORT, CATAPULTTALONTWOPORT,
		CATAPULTENCODERPORTA, CATAPULTENCODERPORTB);
	collector = new Collector(LIFTINGTALONPORT, ROLLERTALONPORT,
		BALLSENSORPORT, UPPERLIMITSENSORPORT, LOWERLIMITSENSORPORT);
	ultrasonicSensor = new AnalogChannel(ULTRASONICSENSORPORT);
	autonomousTimer = new Timer();
    }

    public void robotInit()
    {
	catapult.setMotorPower(1.0);
    }

    public void disabledInit()
    {
	catapult.reInit();
	autonomousTimer.stop();
    }

    public void autonomousInit()
    {
	m_autoFired = false;
	autonomousTimer.reset();
	autonomousTimer.start();
	m_unfoldingState = UnfoldingState.collectorLower;
	m_autonomousState = AutonomousState.waiting;
	m_unfoldingDone = false;
	m_collectorStarted = false;
	m_correctRangeLoops = 0;
	m_pidError = 0;
	m_cummulativePIDError = 0;
	catapult.resetEncoder();
	catapult.setMotorPower(1.0);
	collector.setAutomaticRollerPower(0.65);
	collector.run();
    }

    public void teleopInit()
    {
	autonomousTimer.stop();
	collector.setAutomaticRollerPower(1.0);
	catapult.setMotorPower(1.0);
    }
    int loop = 0;

    public void disabledPeriodic()
    {
	catapult.setStoppingPoint(140);
	collector.disable();
    }

    public void autonomousPeriodic()
    {
	switch(m_unfoldingState)
	{
	    case UnfoldingState.collectorLower:
		if(!(autonomousTimer.get() < 1.5))
		{
		    catapult.resetLoweringTimer();
		    m_unfoldingState = UnfoldingState.catapultLower;
		}
		break;

	    case UnfoldingState.catapultLower:
		catapult.autonomousLower();
		m_unfoldingState = UnfoldingState.waitingForCatapult;
		break;

	    case UnfoldingState.waitingForCatapult:
		if(catapult.getState() == 4)
		{
		    m_unfoldingState = UnfoldingState.done;
		}
		break;

	    case UnfoldingState.done:
		if(!(autonomousTimer.get() < 4))
		{
		    collector.disable();
		    m_unfoldingDone = true;
		}
	}
	switch(m_autonomousState)
	{
	    case AutonomousState.waiting:
		if(autonomousTimer.get() < 1)
		{
		    chassis.setJoystickData(0, 0, 0);
		}
		else
		{
		    m_autonomousState = AutonomousState.running;
		}
		break;

	    case AutonomousState.running:
		if(autonomousTimer.get() > 7 && !m_autoFired && m_unfoldingDone)
		{
		    chassis.setJoystickData(0, 0, 0);
		    catapult.resetEncoder();
		    catapult.setMotorPower(1.0);
		    catapult.setStoppingPoint(151);
		    catapult.fire();
		    m_autoFired = true;
		}
		else if(ultrasonicSensor.getValue() > 95)
		{
		    m_pidError = ultrasonicSensor.getValue() - 95;
		    m_cummulativePIDError += m_pidError;
		    double y = -(((m_pidError) * (0.006))
			    + (m_cummulativePIDError * 0.00005));
		    chassis.setJoystickData(0, y, 0);
		}
		else
		{
		    m_autonomousState = AutonomousState.stopped;
		}
		break;

	    case AutonomousState.stopped:
		chassis.setJoystickData(0, 0, 0);
		break;
	}
	chassis.idle();
	catapult.idle();
	collector.idle();
    }

    public void teleopPeriodic()
    {
	double x = joystick.getRawAxis(1);
	double y = joystick.getRawAxis(2);
	double twist = joystick.getRawAxis(3);
	chassis.setJoystickData(x, y, twist);
	if(joystick.getRawButton(1))
	{
	    catapult.fire();
	}
	if(joystick.getRawButton(2))
	{
	    collector.run();
	}
	if(joystick.getRawButton(3))
	{
	    collector.disable();
	}
	if(joystick.getRawButton(4))
	{
	    catapult.resetEncoder();
	}
	if(joystick.getRawButton(5))
	{
	    catapult.setStoppingPoint(69);
	}
	if(joystick.getRawButton(6))
	{
	    catapult.setStoppingPoint(151);
	}
	if(joystick.getRawButton(9))
	{
	    catapult.setStoppingPoint(100);
	}
	if(joystick.getRawButton(10))
	{
	    catapult.setStoppingPoint(131);
	}
	collector.manualRaise((int) (joystick.getRawAxis(6)));
	collector.manualRoller((int) (joystick.getRawAxis(5)));
	catapult.idle();
	chassis.idle();
	collector.idle();
    }

    public void testPeriodic()
    {
	collector.assistedManualRaise();
	catapult.reInit();
	chassis.setJoystickData(0, 0, 0);
	catapult.idle();
	chassis.idle();
    }
}