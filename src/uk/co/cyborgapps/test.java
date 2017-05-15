package uk.co.cyborgapps;

import java.awt.MouseInfo;
import java.awt.Point;

/**
 * Program Title:  test
 * Author:         sean
 * Created:        5/12/17
 * Bangor username:eeu626
 * Version:        1.0
 */

public class test
{
	public static void main(String[] args)
	{
		while (true)
		{
			Point p = MouseInfo.getPointerInfo().getLocation();
			System.out.println(p);
			try
			{
				Thread.sleep(2000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
