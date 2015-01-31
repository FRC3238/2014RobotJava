package edu.wpi.first.wpilibj.templates;

import com.sun.squawk.microedition.io.FileConnection;
import com.sun.squawk.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.microedition.io.Connector;

/**
* Reads the contents of a text file into a vector
*/
public class FileReader
{
    /**
    * Reads the contents of the file into a vector with each line placed into
    * its own element
    */
	public static Vector getFileContents(String filename)
	{
		String path = "file:///" + filename;
		FileConnection file = null;
		BufferedReader reader = null;
		Vector contents = null;
		try
		{
			file = (FileConnection) Connector.open(path, Connector.READ);
			reader = new BufferedReader(
					new InputStreamReader(file.openInputStream()));
			contents = new Vector();
			String line = "";
			while((line = reader.readLine()) != null)
			{
				contents.addElement(line);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(reader != null)
				{
					reader.close();
				}
				if(file != null)
				{
					file.close();
				}
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
		return contents;
	}
}
