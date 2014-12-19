package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;

/**
 * Controls the power of the drive motors.
 */
public class Chassis
{
	RobotDrive drivetrain;

	double m_xValue;
	double m_yValue;
	double m_twistValue;

	/**
	 * @param leftFrontTalonPort The port number for the talon that controls the
	 * left front drive motor.
	 * @param leftRearTalonPort The port number for the talon that controls the
	 * left rear drive motor.
	 * @param rightFrontTalonPort The port number for the talon that controls
	 * the right front drive motor.
	 * @param rightRearTalonPort The port number for the talon that controls the
	 * right rear drive motor.
	 */
	Chassis(int leftFrontTalonPort, int leftRearTalonPort,
			int rightFrontTalonPort, int rightRearTalonPort)
	{
		Talon leftFrontTalon, leftRearTalon, rightFrontTalon, rightRearTalon;
		leftFrontTalon = new Talon(leftFrontTalonPort);
		leftRearTalon = new Talon(leftRearTalonPort);
		rightFrontTalon = new Talon(rightFrontTalonPort);
		rightRearTalon = new Talon(rightRearTalonPort);
		drivetrain = new RobotDrive(leftFrontTalon, leftRearTalon,
				rightFrontTalon, rightRearTalon);
		drivetrain.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
		drivetrain.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
		drivetrain.setInvertedMotor(RobotDrive.MotorType.kFrontRight, false);
		drivetrain.setInvertedMotor(RobotDrive.MotorType.kRearRight, false);
	}

	/**
	 * Accepts the data from the joystick, makes it negative so the robot will
	 * drive in the correct direction, and then sets it to member variables that
	 * we can use in the idle.
	 *
	 * @param x The horizontal value from the joystick.
	 * @param y The vertical value from the joystick.
	 * @param twist The rotational value from the joystick.
	 */
	void setJoystickData(double x, double y, double twist)
	{
		m_xValue = -(x);
		m_yValue = -(y);
		m_twistValue = -(twist);
	}

	/**
	 * Maps the joystick inputs quadratically and then passes the mapped values
	 * to the drivetrain object.
	 */
	void idle()
	{
		double mappedX;
		double mappedY;
		double mappedTwist;
		/**
		 * Maps the joystick inputs quadratically to reduce deadband and aids in
		 * low speed control of robot
		 */
		if(m_xValue < 0)
		{
			mappedX = -(m_xValue * m_xValue);
		}
		else
		{
			mappedX = m_xValue * m_xValue;
		}
		if(m_yValue < 0)
		{
			mappedY = -(m_yValue * m_yValue);
		}
		else
		{
			mappedY = m_yValue * m_yValue;
		}
		if(m_twistValue < 0)
		{
			mappedTwist = -(m_twistValue * m_twistValue);
		}
		else
		{
			mappedTwist = m_twistValue * m_twistValue;
		}
		/**
		 * Inputs the mapped values into the cartesian mecanum drive method of
		 * the drivetrain object, which will set the Talons for us
		 */
		drivetrain.mecanumDrive_Cartesian(mappedX, mappedY, mappedTwist, 0.0);
	}
}
