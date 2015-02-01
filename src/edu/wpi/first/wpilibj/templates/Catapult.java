package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;

/**
 * Controls the two motors that power the catapult by utilizing inputs from the
 * encoder on the gearbox attached to the motors
 */
public class Catapult
{
	Talon motorOneTalon, motorTwoTalon;
	Encoder catapultEncoder;
	Timer loweringTimer;

	int m_firingState;
	int m_stoppingClicks;
	double m_motorPower;

	/**
	 * Holds the state values for the m_firingState state machine
	 */
	public class CatapultState
	{
		public static final int waiting = 0,
				firing = 1,
				lowering = 2,
				zeroing = 3,
				autonomousLowering = 4;
	}

	/**
	 * @param talonOnePort The port number for the talon that controls the first
	 * of the catapult's two motors
	 * @param talonTwoPort The port number for the talon that controls the
	 * second of the catapult's two motors
	 * @param encoderPortA The port number for the connection of the encoder's
	 * blue wire
	 * @param encoderPortB The port number for the connection of the encoder's
	 * yellow wire
	 */
	Catapult(int talonOnePort, int talonTwoPort, int encoderPortA,
			int encoderPortB)
	{
		motorOneTalon = new Talon(talonOnePort);
		motorTwoTalon = new Talon(talonTwoPort);
		catapultEncoder = new Encoder(encoderPortA, encoderPortB, true);
		loweringTimer = new Timer();
		loweringTimer.start();
		catapultEncoder.start();
		m_firingState = CatapultState.waiting;
	}

	/**
	 * Sets the state of the shooter to waiting
	 */
	void reInit()
	{
		m_firingState = CatapultState.waiting;
	}

	/**
	 * Sets the state of the shooter to firing
	 */
	void fire()
	{
		m_firingState = CatapultState.firing;
	}

	/**
	 * Sets the state of the shooter to autonomousLowering
	 */
	void autonomousLower()
	{
		m_firingState = CatapultState.autonomousLowering;
	}

	/**
	 * Sets the number of encoder "clicks" at which the power to the catapult
	 * motors will be cut
	 *
	 * @param clicks The number of encoder "clicks" before power is cut, make
	 * sure this number is within the range of motion of the robots arm first
	 * before firing
	 */
	void setStoppingPoint(int clicks)
	{
		m_stoppingClicks = clicks;
	}

	/**
	 * Sets the magnitude of the power that the Talons controlling the catapult
	 * motors will be set to when the catapult is firing
	 *
	 * @param power A number from -1.0 to 1.0 for the magnitude of the power
	 * that will be sent to the Talons when the catapult is firing
	 */
	void setMotorPower(double power)
	{
		m_motorPower = power;
	}

	/**
	 * Gets the current count of the encoder, the number of "clicks" the encoder
	 * has accumulated
	 *
	 * @returns The number of "clicks" the encoder has accumulated
	 */
	int getEncoderCount()
	{
		return catapultEncoder.get();
	}

	/**
	 * Gets the integer that represents the current state of the m_firingstate
	 * state machine
	 *
	 * @return An integer from 0 to 4 that corrresponds to the state of the
	 * m_firingState state machine
	 */
	int getState()
	{
		return m_firingState;
	}

	/**
	 * Resets the current encoder count to 0
	 */
	void resetEncoder()
	{
		catapultEncoder.reset();
	}

	/**
	 * Resets the value of the lowering timer to 0
	 */
	void resetLoweringTimer()
	{
		loweringTimer.reset();
	}

	/**
	 * Handles the state machine for the catapult and controls all of the
	 * catapult's motors, must be called every loop for the catapult to operate
	 */
	void idle()
	{
		// The state machine that controls the catapult
		switch(m_firingState)
		{
			// Waits to receive a command, motors are set to 0
			case CatapultState.waiting:
				motorOneTalon.set(0.0);
				motorTwoTalon.set(0.0);
				break;

			/* Powers the motors at a previously defined power until
			 the previously defined number of clicks is reached */
			case CatapultState.firing:
				if(catapultEncoder.get() < m_stoppingClicks)
				{
					motorOneTalon.set(m_motorPower);
					motorTwoTalon.set(m_motorPower);
				}
				else
				{
					motorOneTalon.set(0.0);
					motorTwoTalon.set(0.0);
					loweringTimer.reset();
					m_firingState = CatapultState.lowering;
				}
				break;

			// After the catapult is fired, the arm is lowered back down
			case CatapultState.lowering:
				if(loweringTimer.get() < 1.25)
				{
					motorOneTalon.set(-0.07);
					motorTwoTalon.set(-0.07);
				}
				else
				{
					m_firingState = CatapultState.zeroing;
				}
				break;

			/* After the the catapult arm is lowered, the encoder count is reset
			 to 0 */
			case CatapultState.zeroing:
				catapultEncoder.reset();
				m_firingState = CatapultState.waiting;
				break;

			/* Lowers the arm more aggresively, used at the start of autonomous
			 to unfold the robot */
			case CatapultState.autonomousLowering:
				if((loweringTimer.get() < 0.5))
				{
					motorOneTalon.set(-0.3);
					motorTwoTalon.set(-0.3);
				}
				else
				{
					m_firingState = CatapultState.zeroing;
				}
				break;
		}
	}
}
