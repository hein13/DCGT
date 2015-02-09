package creator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.InstructionList;

import objects.Segment;
import userinterface.OptionsPanel;

/*
 * Description: This class creates a route from starting coordinated to ending coordinates and 
 * creates a list of Segments to represent the route
 * 
 * Created By: Heinrich Enslin
 * Last Edited: 10/15/14
 * 
 */

public class Router {
    
	public boolean success;
    public List<Segment> segmentList;
    public List<Coordinate> coordinateList;
            
    String OSMFILE = "/Users/Heinrich/Documents/Research/"
                + "Drive-Cycle-Creator/graphhopper/graphhopper/"
                + "north-america_us_north-carolina.pbf";
    //Location of map file. Will need to be made downloadable
    
    
    String GHLOCATION = "/Users/Heinrich/Documents/Research/"
            + "Drive-Cycle-Creator/graphhopper";
    //Change this to make configurable or usable on any computer
    
    public Router(String osmFile, List<Coordinate> coordinateList, 
            OptionsPanel parent)
    {
        this.coordinateList = coordinateList;
        
        //List that will store the route
        List<GPXEntry> list = new ArrayList<GPXEntry>();
        segmentList = new ArrayList<Segment>(); 
        
        //Set up graphhopper to do a full intensive routing
        GraphHopper hopper = new GraphHopper().forServer(); 
        
        //Saves the files hopper creates locally to speed up future routing
        hopper.setInMemory(true); 
        
        //Sets the map file
        hopper.setOSMFile(OSMFILE);
        
        // Sets the location where the generated files are to be saved.
        hopper.setGraphHopperLocation(GHLOCATION); 
        
        //Set how the route is to be generated. This will be done with a car for Drive Cycle Creator
        hopper.setEncodingManager(new EncodingManager("car"));

        //Depending on first use of the map or not it either loads old data or imports data from the map
        hopper.importOrLoad();
        
        
        
        //Increment through all the points and waypoints
        for(int i = 0; i < coordinateList.size() - 1; i++){
       
	        //Make a request to route
	        GHRequest req = new GHRequest(coordinateList.get(i).getLat(), coordinateList.get(i).getLon(),
	        		coordinateList.get(i + 1).getLat(), coordinateList.get(i + 1).getLon()).setVehicle("car");
	        
	        //Import a response from GraphHopper
	        GHResponse rsp = hopper.route(req);
	
	        //If there are any errors it is mentioned to the user
	        if(rsp.hasErrors()) {
	        	JOptionPane.showMessageDialog(parent , "There Was an Error in Routing");
	        	success = false;
	        	return;
	        }
	
	        // If a route could not be found it reports it to the user
	        else if(!rsp.isFound()) {
	        	JOptionPane.showMessageDialog(parent , "A Route Between the Two Points Could not be Found");
	        	success = false;
	        	return;
	        }
	
	        // Otherwise we mark the operation as successful
	        else{
	        	success = true;
	        }
	        
	        //This section creates a list of segments out of the instruction list
	        InstructionList il = rsp.getInstructions();
	        list.addAll(list.size(), il.createGPXList());
        }
        
        
        
        for(GPXEntry gpx : list){
            QueryResult closestCoord = hopper.getLocationIndex().
                    findClosest(gpx.lat, gpx.lon, EdgeFilter.ALL_EDGES);  
            EdgeIteratorState edge = closestCoord.getClosestEdge();
            
            int speed = (int) hopper.getEncodingManager().getSingle().getSpeed(edge.getFlags());
            boolean intersection = closestCoord.getSnappedPosition() == 
                    QueryResult.Position.TOWER;
            segmentList.add(
                new Segment(intersection, speed, gpx.lat, gpx.lon, segmentList));
        }
        
        
    }
}

