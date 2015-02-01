package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Timer;

/**
 * Reduces unwanted rotation of mecanum chassis by using a gyro sensor and a PI
 * loop
 */
public class GyroDrive
{
	static double error;
	static double cummulativeError = 0;
	static double adjustedRotationValue;
	static double oldX = 0;
	static double oldY = 0;
	static double time;
	static double oldTime = 0;
	static double timeDifference;

	/**
	 * Takes the current movement commands for the chassis, PI constants, and
	 * current gyroValue
	 *
	 * @param x The current x power for the robot's movement
	 * @param y The current y power for the robot's movement
	 * @param rotation The current rotation power for the robot's movement
	 * @param pConstant The P constant for the PI loop
	 * @param iConstant The I constant for the PI loop
	 * @param spinThreshold The gyro value at which the PI loop will stop trying
	 * to correct the robot's spin
	 * @param gyroValue The current value of the gyro sensor
	 * @return The adjusted rotation value for correcting the robot's rotation
	 */
	static double getAdjustedRotationValue(double x, double y, double rotation,
			double pConstant, double Iconstant, double spinThreshold,
			double gyroValue)
	{
		error = Math.abs(gyroValue);
		time = Timer.getFPGATimestamp(); // Reads the system's time
		// If this is the first loop, set timeDifference to 0
		if(oldTime == 0)
		{
			timeDifference = 0;
		}
		else
		{
			timeDifference = time - oldTime;
		}
		// If the joystick input has changed, reset cummulativeError
		if(Math.abs(oldX - x) > 0.01 || Math.abs(oldY - y) > 0.01)
		{
			cummulativeError = 0.0;
		}
		else
		{
			cummulativeError += error;
		}
		/* Check to see if the robot's spinning really fast due to a driver
		 induced spin or if the driver is trying to rotate, and if so, don't 
		 change the current rotation value */
		if((error > spinThreshold) || rotation <= -0.15 || rotation >= 0.15)
		{
			adjustedRotationValue = rotation;
		}
		/* Check the direction of the gyroValue to know which way the chassis
		 needs to rotate in to compensate */
		else if(gyroValue > 0)
		{
			adjustedRotationValue = -(error * pConstant
					+ cummulativeError * Iconstant * timeDifference);
		}
		else if(gyroValue < 0)
		{
			adjustedRotationValue = error * pConstant
					+ cummulativeError * Iconstant * timeDifference;
		}
		else
		{
			adjustedRotationValue = 0;
		}
		// Set up the "old" values for the next loop
		oldX = x;
		oldY = y;
		oldTime = time;
		return adjustedRotationValue;
	}

	/**
	 * Resets the cummulativeError and oldTime variables
	 */
	static void reinit()
	{
		cummulativeError = 0;
		oldTime = 0;
	}
}
