package objects;

import org.openstreetmap.gui.jmapviewer.Coordinate;

public class RoutePointInfo {
	public double time;
    public double speed;
    public double latitude; 
    public double longitude;
    
    
    public RoutePointInfo(double time, double speed, Coordinate coord){
        this.time = time;
        this.speed = speed;
        this.latitude = coord.getLat();
        this.longitude = coord.getLon();
    }
}
