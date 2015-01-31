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
    */ 
	static double getAdjustedRotationValue(double x, double y, double rotation,
			double pConstant, double Iconstant, double gyroValue)
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
        /* Check the direction of the gyroValue to know which way the chassis
         needs to rotate in to compensate */
		if(gyroValue > 0)
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
