package fr.inria.peerunit.onstree.stationTree.mtr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.inria.peerunit.util.Util;



public class MTRLib
{

	static
	{
		try
		{
			// System.load("/home/booba/MTR/libmtr.so");
			// String
			// lib="./mtrLib.jar:"+System.getProperty("java.library.path");
			// System.setProperty("java.library.path",lib);
			System.out.println(System.getProperty("java.library.path"));
			// System.loadLibrary("mtr");
			InputStream stream = MTRLib.class
					.getResourceAsStream("/mtrLib/libmtr.so");
			File mtrLibFile = new File("libmtr");
			FileOutputStream outputStream = new FileOutputStream(mtrLibFile);
			byte[] data = new byte[stream.available()];
			int c;
			while ((c = stream.read(data)) != -1)
			{
				outputStream.write(data, 0, c);
			}
			outputStream.close();
			stream.close();

			System.load(mtrLibFile.getAbsolutePath());
		} catch (UnsatisfiedLinkError error)
		{
			error.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static native List getMTRRoute(String ip);

	public static String getRoute(String ip)
	{
		return getRoute(ip,0);
	}
	/**
	 * 
	 * @param ip
	 * @param tryCount
	 * @return
	 */
	public static String getRoute(String ip, int tryCount)
	{
//		Logger logger = Util.getLogger();
		List<RouteElement> route = getMTRRoute(ip);
		StringBuffer routers = new StringBuffer();
		for (RouteElement routeElement : route)
		{
			if (Util.isAValidIP(routeElement.getIp()))
			{
				if (routers.length() == 0)
				{
					routers.append(routeElement.getIp());
				} else
				{
					routers.append("/" + routeElement.getIp());
				}
				/*
				 * int loss=routeElement.getLoss(); if(loss==100) {
				 * if(--tryCount>0) { logger.info("New try"); StringBuffer
				 * newList=new StringBuffer(getRoute(ip,++tryCount));;
				 * if(newList.length()>routers.length()) { routers=newList; } }
				 * else { return routers.toString(); } } }
				 */
			}
		}
		return routers.toString();
	}

	public static void main(String[] args)
	{
		MTRLib lib = new MTRLib();
		ArrayList list = (ArrayList) lib.getMTRRoute("www.google.com");
		System.out.println("Tableau de " + list.size() + " éléments");
		ArrayList<RouteElement> elements = list;
		for (RouteElement routeElement : elements)
		{
			System.out.print("Elément " + (elements.indexOf(routeElement) + 1)
					+ " : ");
			System.out.println(" ip=" + routeElement.getIp() + "  loss="
					+ routeElement.getLoss());
		}
		System.out.println("Test Route" + lib.getRoute("www.yahoo.fr"));
	}
}
