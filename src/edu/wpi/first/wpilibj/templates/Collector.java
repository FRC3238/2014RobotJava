package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.DriverStationLCD;

public class Collector
{
    Talon liftingTalon, rollerTalon;
    AnalogChannel ballSensor;
    DigitalInput upperLimitSensor, lowerLimitSensor;
    Timer timer;
    DriverStationLCD driverStationLCD;

    int m_collectorMode;
    int m_collectorState;
    int m_manualRaiseDirection;
    double m_manualRollerPower;
    double m_automaticRollerPower;

    public static class CollectorMode
    {
	public static final int standard = 0,
		disabled = 1,
		manualRoller = 2,
		manualRaise = 3;
    }

    public static class CollectorState
    {
	public static final int lowering = 0,
		waiting = 1,
		///waitForBall = 2,
		raising = 3,
		mellowRaise = 4;
    }

    Collector(int liftingTalonPort, int rollerTalonPort, int ballSensorPort,
	    int upperLimitSensorPort, int lowerLimitSensorPort)
    {
	liftingTalon = new Talon(liftingTalonPort);
	rollerTalon = new Talon(rollerTalonPort);
	ballSensor = new AnalogChannel(ballSensorPort);
	upperLimitSensor = new DigitalInput(upperLimitSensorPort);
	lowerLimitSensor = new DigitalInput(lowerLimitSensorPort);
	driverStationLCD = DriverStationLCD.getInstance();
	timer = new Timer();
	driverStationLCD.println(DriverStationLCD.Line.kUser1, 1, "timer was made");
	driverStationLCD.updateLCD();
	timer.start();
	m_collectorMode = CollectorMode.disabled;
    }

    void run()
    {
	m_collectorMode = CollectorMode.standard;
	m_collectorState = CollectorState.lowering;
    }

    void manualRoller(int manualRollerPower)
    {
	if(manualRollerPower == 1)
	{
	    m_collectorMode = CollectorMode.manualRoller;
	    m_manualRollerPower = manualRollerPower;
	}
    }

    void setAutomaticRollerPower(double automaticRollerPower)
    {
	m_automaticRollerPower = automaticRollerPower;
    }

    void disable()
    {
	m_collectorMode = CollectorMode.disabled;
    }

    void assistedManualRaise()
    {
	liftingTalon.set(0.3);
    }

    void manualRaise(int direction)
    {
	if(!(direction == 0))
	{
	    m_collectorMode = CollectorMode.manualRaise;
	    m_manualRaiseDirection = direction;
	}
    }

    void idle()
    {
	switch(m_collectorMode)
	{
	    case CollectorMode.standard:
		switch(m_collectorState)
		{
		    case CollectorState.lowering:
			driverStationLCD.clear();
			if(lowerLimitSensor.get())
			{
			    driverStationLCD.println(DriverStationLCD.Line.kUser3,
				    1, "3");
			    liftingTalon.set(-0.4);
			    rollerTalon.set(0.0);
			}
			else
			{
			    driverStationLCD.println(DriverStationLCD.Line.kUser4,
				    1, "4");
			    m_collectorState = CollectorState.waiting;
			}
			driverStationLCD.updateLCD();
			break;

		    case CollectorState.waiting:
			if(ballSensor.getValue() < 300)
			{
			    liftingTalon.set(0.0);
			    rollerTalon.set(m_automaticRollerPower);
			}
			else
			{
			    driverStationLCD.println(DriverStationLCD.Line.kUser5,
				    1, Double.toString(timer.get())+" Before Reset");
			    timer.reset();
			    driverStationLCD.println(DriverStationLCD.Line.kUser6,
				    1, Double.toString(timer.get())+" After Reset");
			    timer.start();
			    m_collectorState = CollectorState.raising;
			}
			break;

//		    case CollectorState.waitForBall:
//			if(timer.get() < 0.3)
//			{
//			    liftingTalon.set(0.0);
//			    rollerTalon.set(m_automaticRollerPower);
//			}
//			else
//			{
//			    timer.reset();
//			    m_collectorState = CollectorState.raising;
//			}
//			break;
		    case CollectorState.raising:
			driverStationLCD.println(DriverStationLCD.Line.kUser1,
				1, Double.toString(timer.get())+" beginning of raising state");
			driverStationLCD.updateLCD();
			if(timer.get() < 0.75)
			{
			    liftingTalon.set(0.6);
			    rollerTalon.set(m_automaticRollerPower);
			}
			else
			{
			    m_collectorState = CollectorState.lowering;
			}
			break;

		    case CollectorState.mellowRaise:
			if(ballSensor.getValue() > 500)
			{
			    liftingTalon.set(0.3);
			    rollerTalon.set(m_automaticRollerPower);
			}
			else
			{
			    m_collectorState = CollectorState.lowering;
			}
			break;
		}
		break;

	    case CollectorMode.disabled:
		liftingTalon.set(0.0);
		rollerTalon.set(0.0);
		timer.stop();
		break;

	    case CollectorMode.manualRoller:
		rollerTalon.set(m_manualRollerPower);
		liftingTalon.set(0.0);
		timer.stop();
		break;

	    case CollectorMode.manualRaise:
		if(m_manualRaiseDirection < 0)
		{
		    liftingTalon.set(0.45);
		}
		else
		{
		    liftingTalon.set(-0.4);
		}
		break;
	}
    }
}
