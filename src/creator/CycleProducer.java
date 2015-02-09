package creator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import objects.RoutePointInfo;
import objects.Segment;

/*
 * Description: This class produces a map of RoutPointInfo objects for the 
 * frequency that the user selected
 * 
 * Created By: Heinrich Enslin
 * Last Edited: 10/26/14
 * 
 */

public class CycleProducer {
	//The amount of seconds per cycle
	double frequency;
	//The average wait time in seconds at an intersection
    double intersectionWait;
    //The cutoff speed for no-stop highways
    double hiwayCutOff;
	//The maximum acceleration rate of the vehicle
	double maxVehicleAcceleration;
	//Vehicle Mass
	double vehicleMass;
	//Rolling Friction Coefficient
	double rollFricCoef;
	//Front Facing Vehicle Area
	double frontArea;
	//Wind Friction Coefficient
	double windFricCoef;
	//Initial Transmission Friction Power
	double initTranFric;
	//The top speed of the car
	double topSpeed;
	//The speed limit multiplier
	double limitMult;
	//The deviation around the velocity
	double devVel; 
	//Average maximum acceleration
	double maxAccel;
	
	//How many times a second should the acceleration be calculated (HZ)
	final static double ACCELFREQUENCY = 10;   
    

    public HashMap<Integer, RoutePointInfo> routeInfo;
    double maxAcceleration;
    PrintWriter writer;
    Random rand;
    List<Segment> list;
    
    boolean fileCanBeWritten;
    
    public void setOptionValues(double optionValues[]){
    	
    	this.frequency = optionValues[0];
    	this.intersectionWait = optionValues[1];
    	this.hiwayCutOff = optionValues[2];
    	this.maxVehicleAcceleration = optionValues[3];
    	this.vehicleMass = optionValues[4];
    	this.rollFricCoef = optionValues[5];
    	this.frontArea = optionValues[6];
    	this.windFricCoef = optionValues[7];
    	this.initTranFric = optionValues[8];
    	this.topSpeed = optionValues[9] / 3.6; //km/h to m/s;
    	this.limitMult = optionValues[10];
    	this.devVel = optionValues[11];
    	this.maxAccel = optionValues[12];
    }
    
    public CycleProducer(List<Segment> list, double optionValues[]){
        this.list = list;
        
        setOptionValues(optionValues);
        
        rand = new Random();
        
        //Calculates the maximum acceleration for the current session if the driver maximum is greater than the
        //vehicle max acceleration the max vehicle acceleration is used.
        maxAcceleration = Math.min(maxAccel, maxVehicleAcceleration);
        //Creates the map that represents the cycle at each point in the frequency
        routeInfo = new HashMap<Integer, RoutePointInfo>();
        
        //The data is also written to a standard text file
        fileCanBeWritten = true;
        try {
            writer = new PrintWriter("DriveCycleData.txt");
        } catch (IOException ex) {
            System.err.println("Could Not Create Data File " + ex.getMessage());
            fileCanBeWritten = false;
        }
        //Initializes variables for the main for loop
        double oldSpeed = 0;
        double time = 0;
        
        //The main for loop. Loops through the Segment list and at each interval calculates the speed
        for(Segment seg: list){
        	//Sets the distance covered in the current segment to zero
            double distCov = 0;
            
            //Uses a normal distribution based around the segments speed limit to establish a speed 
            //for the vehicle to follow in this segment
            double mainSpeed = ((rand.nextGaussian() * devVel) + ((seg.speedLimit * limitMult) * (1/3.6)));
            //If a segment has an intersection and the road is not a no-stop highway intersection
            //wait time gets added
            if(isIntersection(seg)){
                //Adds or Ignores intersection wait time    
            	double[] vals = addIntersectionWaitTime(time, oldSpeed, distCov, seg);
                    
            	//Checks if  intersection wait time was added
                if(time != vals[0]){
                	//If it was it sets the speed to 0 m/s and time to the new calculated time
                	oldSpeed = 0;
                    time = vals[0];
                    }
                //The distance covered due to deceleration is then added to total distance covered in
                //this segment
                distCov = vals[1];                   
            }
            
            //This loop continues making RoutePointInfos until the entire Segment has been covered
            while(distCov < seg.segmentLength()){
            	//Calculates the speed based on a normal distribution around the mainSpeed for segment
                double speed = rand.nextGaussian() * devVel + mainSpeed;               
                //If the speed is less than zero we make the speed zero
                if (speed < 0){
                    speed = 0;
                }
                
                //If the new speed varies from the speed of the previous interval an acceleration or
                //deceleration needs to take place
                if(speed != oldSpeed){
                    HashMap<Double, Double> velList = getAccelerationVelocities(oldSpeed, speed, time, 
                    		(speed != 0 && oldSpeed != 0 && (oldSpeed > speed)));
                    Map<Double, Double> sortedMap = new TreeMap<Double, Double>(velList);
                    Iterator<Entry<Double, Double>> it = sortedMap.entrySet().iterator();
                    while(it.hasNext()){
                        double oldTime = time;
                        Map.Entry<Double, Double> entry = (Map.Entry<Double, Double>)it.next();
                        time = entry.getKey();
                        double velocity = entry.getValue();                
                        if(fileCanBeWritten){
                        	writer.println(time + " " + velocity);
                        }
                        distCov += velocity * (time - oldTime);     
                        routeInfo.put((int)time, new RoutePointInfo(time, velocity, getCoordinate(seg, distCov)));
                    }
                }
                if(fileCanBeWritten){
                	writer.println(time + " " + speed);
                }
                //Adds the RoutePointInfo into the map and increments the speed and distance covered.      
                distCov += (speed * frequency);
                routeInfo.put((int)time, new RoutePointInfo(time, speed, getCoordinate(seg, distCov)));
                oldSpeed = speed;
                time += frequency;
            }
        }
        if(fileCanBeWritten){
        	writer.close();
        }
    }
    
    //This function decides whether or not a segment contains an intersection.
    public boolean isIntersection(Segment seg){
    	//First checks if OSM actually registered this segment as an intersection
    	if(seg.intersection){
    		//Checks to see if the speed of the segment is greater than the highway
    		//speed cutoff
    		if(seg.speedLimit > this.hiwayCutOff){
    			return false;
    		}
    		//Checks the surrounding speeds of the segments to see if it was a sudden drop
    		int i;
    		for(i = 0; i < list.size(); i++){
    			if(list.get(i).latStart == seg.latStart && list.get(i).lonStart == seg.lonStart){
    				break;
    			}
    		}
    		for(int j = i - 2; j < i + 2 && j < list.size(); j++){
    			if(j >= 0 && j != i){
    				if(list.get(j).speedLimit > this.hiwayCutOff){
    					return false;
    				}
    			}
    		}
    		return true;
    	}else{
    		return false;
    	}
    }
    
    
    //This function calculates the velocity at each interval of the Acceleration Frequency while there
    //is need to accelerate then returns it in a map of time vs velocity   
    public HashMap<Double, Double> getAccelerationVelocities(double initalVel, double finalVel, double time, boolean coasting){
        HashMap<Double, Double> velList = new HashMap<Double, Double>();
        double currVel = initalVel;
        velList.put(time, currVel);
        double currTime = time + 1/ACCELFREQUENCY;
        if(finalVel > initalVel){
            while( currVel < finalVel){
            	double acel = getAcceleration(currVel);
                currVel = currVel + (acel * 1/ACCELFREQUENCY);
                velList.put(currTime, currVel);
                currTime += + 1/ACCELFREQUENCY;
            }
        }else{
            while( currVel > finalVel){
            	double acel;
            	if(coasting){
            		acel = getCoastingDeceleration(currVel);
            	}else{
            		acel = getAcceleration(currVel);
            	}
                 
                currVel = currVel - (acel * 1/ACCELFREQUENCY);;
                velList.put(currTime, currVel);
                currTime += + 1/ACCELFREQUENCY;
            }
        }
        return velList;
    }
    
    
    
    public double[] addIntersectionWaitTime(double currentTime, double currentSpeed, 
            double distance, Segment currSeg){
        double time = currentTime;
        double addedTime = (rand.nextGaussian() * this.intersectionWait);
        if (addedTime < 0){
            addedTime = 0;
        }
        if(addedTime > 0){
            HashMap<Double, Double> velList = getAccelerationVelocities(currentSpeed, 0, time, false);
            Map<Double, Double> sortedMap = new TreeMap<Double, Double>(velList);
            Iterator<Entry<Double, Double>> it = sortedMap.entrySet().iterator();
            while(it.hasNext()){
                double oldTime = time;
                Map.Entry<Double, Double> entry = (Map.Entry<Double, Double>)it.next();
                time = entry.getKey();
                double velocity = entry.getValue();      
                writer.println(time + " " + velocity);
                distance += velocity * (time - oldTime);        
                routeInfo.put((int)time, new RoutePointInfo(time, velocity, getCoordinate(currSeg, distance)));
            }

            for(int i = 0; i <= addedTime; i += frequency){
                routeInfo.put((int)time, new RoutePointInfo(time, 0, getCoordinate(currSeg, distance)));
                writer.println(time + " 0");
                time += frequency;
            }
                    
        }
        double returnArray[] = new double[2];
        returnArray[0] = time;
        returnArray[1] = distance;
        return returnArray;
    }
    
    
    double getAcceleration(double currSpeed){
    	
        //Randomly chooses the probability the value of the distribution is going
        //to come from
        double randProb = rand.nextDouble();
        
        //Calculates an extreme value distribution function value based on the random
        //Probability generated in the previous step
        double scaling = 0.89;
        double deviation = 1;
        double mean = 1.3;
        
        
        double accel = (scaling/deviation) * Math.log(-Math.log(1 - randProb)) + mean;
        
        //If the value is below zero, the car did not accelerate this interval
        if(accel < 0){
            accel = 0;
        }
        
        //Compares the maximum acceleration possible based on the linear maximum/velocity relationship to the
        //generated acceleration. The adjusted maximum acceleration can not be exceeded 
       
        accel = Math.min(accel, maxAcceleration * ((topSpeed - currSpeed)/topSpeed));
        
        //Deal with extremely small accelerations
        if(accel < 1e-12){
        	accel = 0.01;
        }
        
        return accel;     
    }
    
    double getCoastingDeceleration(double currSpeed){
    	//First we calculate the force of friction on the 
    	//car due to Transmission, Wind and Rolling Friction
    	//Still needs to be expanded ref: http://physics.stackexchange.com/questions/13062/simple-friction-formula-for-a-car
    		
    	//Force of gravity
    	double GRAVITY = 9.81; //m/s^2
    	
    	//Air Density. Approx. Can be further calculated based on temperature and pressure
    	double AIRDEN = 1.25; //kg/m^3;

    	double frictionForce = this.rollFricCoef * this.vehicleMass * GRAVITY //Constant rolling Friction
    							+ AIRDEN * this.windFricCoef * this.frontArea * 
    								Math.pow(currSpeed, 2) //Wind Resistance
    							+ this.initTranFric / currSpeed; //Transmission Friction
    	
    	//Calculate the deceleration from the mass and force. F=ma
    	return frictionForce / this.vehicleMass;
    	
    }
    
    //Returns coordinate based on distance driven on a segment
    public Coordinate getCoordinate(Segment seg, double distDriven)
    {
    	//If it is the last segment of the trip it returns the initial lat and lon
    	if(seg.order >= list.size() - 1){
    		return new Coordinate(seg.latStart, seg.lonStart);
    	}
    	
    	
    	double R = 6378.137 * 1000; // Radius of earth in M
    	
    	//Calculate Bearing 
    	double startLon =  seg.lonStart * Math.PI / 180;
    	double endLon = list.get(seg.order + 1).lonStart * Math.PI / 180;
    	
    	double startLat = seg.latStart * Math.PI / 180;
    	double endLat = list.get(seg.order + 1).latStart * Math.PI / 180;
    	
    	double y = Math.sin(endLon - startLon) 
    			* Math.cos(startLat);
    	double x = Math.cos(startLat)*Math.sin(endLat) -
    	        Math.sin(startLat)*Math.cos(endLat)
    	        *Math.cos(endLon - startLon);
    	double brng = Math.atan2(y, x);
    	
    	//Calculate new latitude
    	double lat = Math.asin( Math.sin(startLat)*Math.cos(distDriven/R) +
                Math.cos(startLat)*Math.sin(distDriven/R)*Math.cos(brng) );
    	
    	//Calculate new longitude
    	double lon = startLon + Math.atan2(Math.sin(brng)*Math.sin(distDriven/R)*Math.cos(startLat),
                     Math.cos(distDriven/R)-Math.sin(startLat)*Math.sin(lat));
    	
    	return new Coordinate(lat * 180 / Math.PI, lon * 180 / Math.PI);
   	
    }
    
}
