package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Timer;

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

	static double getAdjustedRotationValue(double x, double y, double rotation, 
			double pConstant, double Iconstant, double gyroValue)
	{
		error = Math.abs(gyroValue);
        time = Timer.getFPGATimestamp();
        if(oldTime == 0)
        {
           timeDifference = 0;
        }
        else
        {
            timeDifference = time - oldTime;
        }
		if(Math.abs(oldX - x) > 0.01 || Math.abs(oldY - y) > 0.01)
		{
			cummulativeError = 0.0;
		}
		else
		{
			cummulativeError += error;
		}
		if(gyroValue > 0)
		{
			adjustedRotationValue = -(error * pConstant + 
					cummulativeError * Iconstant * timeDifference);
		}
		if(gyroValue < 0)
		{
			adjustedRotationValue = error * pConstant + 
					cummulativeError * Iconstant * timeDifference;
		}
		oldX = x;
		oldY = y;
        oldTime = time;
		return adjustedRotationValue;
	}

	static void reinit()
	{
		cummulativeError = 0;
        oldTime = 0;
	}
}
