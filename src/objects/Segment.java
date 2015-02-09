package objects;

import java.util.List;

/*
 * Description: This class is a representation of a single segment in the 
 * route produced by the Router Class
 * 
 * Created By: Heinrich Enslin
 * Last Edited: 10/15/14
 * 
 */

public class Segment {
	public int order;
	public boolean intersection;
	public int speedLimit;
	public double latStart;
	public double lonStart; 
	private List<Segment> list;
	
	
	public Segment(boolean intersection, int speedLimit, double latStart,
            double lonStart, List<Segment> list)
	{
		 this.order = list.size();
		 this.intersection = intersection;
		 this.speedLimit = speedLimit;
		 this.latStart = latStart;
		 this.lonStart = lonStart;
		 this.list = list;
    }  

	//Returns the length of the segment based on the ending and starting coordinates
	public double segmentLength()
	{
		 if(order >= list.size() - 1){
		     return 0;
		 }
		 double R = 6378.137; // Radius of earth in KM
		 double dLat = (list.get(order + 1).latStart - latStart) * 
		         Math.PI / 180;
		 double dLon = (list.get(order + 1).lonStart - lonStart) * 
		         Math.PI / 180;
		 double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		         Math.cos(latStart * Math.PI / 180) * 
		         Math.cos(list.get(order + 1).latStart * Math.PI / 180) *
		         Math.sin(dLon/2) * Math.sin(dLon/2);
		 double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		 double d = R * c;
		 return d * 1000; // meters
	}
}
