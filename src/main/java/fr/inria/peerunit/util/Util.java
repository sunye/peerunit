package fr.inria.peerunit.util;

/**
 * 
 * @author Jeremy Masson
 *
 */
public class Util
{
	private static int iColor;
	
	public static boolean areInSameNet(String ip1, String ip2)
	{
		int index1=ip1.lastIndexOf(".");
		int index2=ip2.lastIndexOf(".");
		if((index1<5) || (index2<5))
			return false;
		String ip1Net=ip1.substring(0,index1-1);		
		String ip2Net=ip2.substring(0,index2-1);
		return ip1Net.equals(ip2Net);
	}
	
	/**
	 * Initiate the color number to 1.
	 */
	public static void initColor()
	{
		iColor = 0;
	}
	
	/**
	 * return a string color 
	 * @param i color number 
	 * @return white by default
	 */
	public static String getColor()
	{
		if(iColor++ > 20)
		{
			initColor();
		}
		switch(iColor)
		{
		case 1:
			return "green";
		case 2:
			return "red";
		case 3:
			return "blue";
		case 4:
			return "yellow";
		case 5:
			return "hotpink";
		case 6:
			return "indigo";
		case 7:
			return "khaki";
		case 8:
			return "limegreen";
		case 9:
			return "lightcyan4";
		case 10:
			return "khaki1";
		case 11:
			return "lightcyan";
		case 12:
			return "lightsalmon";
		case 13:
			return "hotpink3";
		case 14:
			return "olivedrab1";
		case 15:
			return "peachpuff2";
		case 16:
			return "sienna3";
		case 17:
			return "violet";
		case 18:
			return "whitesmoke";
		case 19:
			return "yellowgreen";
		case 20:
			return "dodgerblue4";
		}
		return "white";
	}
}
