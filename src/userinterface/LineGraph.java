package userinterface;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JPanel;

import objects.RoutePointInfo;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

public class LineGraph extends JPanel{

	private static final long serialVersionUID = 1L;


    
    double mpsPerPixel;
    double sPerPixel;
    
    public static int SPACING = 50;
    int MARKINGLEN = 10;
    double SHOWSECOND = 1;
    double SHOWSPEED = 1;
    int TEXTBORDER = 10;
    int initalTime;
    int finalTimeZoom;
    List<Line2D> lineList;
    public Timer timer;
    
    
    boolean windowOpen = false;
    PopUpCoord currPopUp = null;
    
    Point lastClick = new Point(0,0);
    
    HashMap<Integer, RoutePointInfo> routeInfo;
    
    TimeVehicleMover mover;
    
    LineGraph(HashMap<Integer, RoutePointInfo> routeInfo, JMapViewer map, JButton playButton){
        this.routeInfo = routeInfo;
        mover = new TimeVehicleMover(this, map, routeInfo);
        lineList = new ArrayList<Line2D>();
        
        this.addMouseListener(new ClickableLine(this, playButton));
        this.addMouseMotionListener(mover);
        this.addMouseWheelListener(new Zoomer(this));
        initalTime = 0;
        finalTimeZoom = (int) getFinalTime();
        
        
    }
    
    double getInitialTime(){
    	return initalTime;
    }
    
    double getFinalTime(){
        double time = 0;
        Iterator<Entry<Integer, RoutePointInfo>> it = routeInfo.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Integer, RoutePointInfo> entry = (Entry<Integer, RoutePointInfo>)it.next();
            if(entry.getValue().time > time){
                time = entry.getValue().time;
            }
        }
        return time;
    }
    
    Point graphCoordToSwing(double x, double y){
        int sx = (int)((x - getInitialTime()) / sPerPixel) + SPACING;
        int sy = (int)((getMaxSpeed() - y) / mpsPerPixel) + SPACING;
        return new Point(sx,sy);
    }
    
    final double[] swingCoordToGraph(Point pt){
        double coord[] = new double[2];
        coord[0] = (((double)(pt.x) - SPACING) * sPerPixel) + getInitialTime() ;
        coord[1] = ((((double)(pt.y) - SPACING) * mpsPerPixel) - getMaxSpeed()) * -1;
        return coord;
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        
        //Set font for LineGraph
        Font font = new Font("TimesRoman", Font.PLAIN, 15);
        g2d.setFont(font); 
        
        //Calculate the seconds per pixel
        sPerPixel = (finalTimeZoom - getInitialTime())/(this.getWidth()-(2*SPACING));
        if(sPerPixel < 0){
        	System.out.println("less than zero");
        }
        
        //Calculate meters per second per pixel
        double x1 = this.getHeight()-(2*SPACING);
        double x2 = getMaxSpeed();
        mpsPerPixel = x2/x1;
        
        //Draw the location mover
        mover.drawPolygon(g2d);
        
        //Draw the zoom rectangle if user requested it
        if(mover.rectangle){
        	mover.drawRectangle(g2d);
        }

        //X Axis On Bottom
        
        //Draw the inital x axis
        g.drawLine(SPACING ,(int)(getMaxSpeed() / mpsPerPixel) + SPACING,
                (int)((finalTimeZoom - initalTime) / sPerPixel) + SPACING, 
                (int)(getMaxSpeed() / mpsPerPixel) + SPACING);
        
        //Checks if the second string for each tick is bigger than the spacing between ticks
        //If it is the interval gets increased
        SHOWSECOND = 1;
        if(1/sPerPixel <= 
                this.getFontMetrics(font).stringWidth(Integer.toString((int)finalTimeZoom))
                + TEXTBORDER){
            while((1/sPerPixel) * SHOWSECOND <= 
                this.getFontMetrics(font).stringWidth(Integer.toString((int)finalTimeZoom))
                    + TEXTBORDER){
                    SHOWSECOND++;
                }
        }
        
        int dimLine = SPACING;
        int second = (int) getInitialTime();
        while(dimLine < (finalTimeZoom- getInitialTime())/sPerPixel + SPACING){
            g.drawLine(dimLine, SPACING, dimLine,  (int)(getMaxSpeed() /mpsPerPixel + MARKINGLEN/2) 
            		+ SPACING);
            g.drawString(Integer.toString(second), dimLine - 
                    this.getFontMetrics(font).stringWidth(
                    Integer.toString(second))/2 - TEXTBORDER/4, 
                    (int)(getMaxSpeed() / mpsPerPixel + MARKINGLEN/2) 
                    + this.getFontMetrics(font).getHeight() + SPACING);
            second += (int)SHOWSECOND;
            dimLine += Math.round(SHOWSECOND / sPerPixel);
        }
        g.drawString("Time (s)", (int)(((finalTimeZoom - getInitialTime()) / sPerPixel + SPACING - 
                this.getFontMetrics(font).stringWidth("Time (s)")) / 2) + SPACING, 
                (int)(getMaxSpeed() / mpsPerPixel + MARKINGLEN/2) 
                    + this.getFontMetrics(font).getHeight() * 2 + SPACING);
        
        //Y Axis on Left
        g.drawLine(SPACING,SPACING,SPACING,(int)(getMaxSpeed() / mpsPerPixel) + SPACING);
        if(1/mpsPerPixel <= 
                this.getFontMetrics(font).getHeight() + TEXTBORDER){
            while(SHOWSPEED/mpsPerPixel <= 
                this.getFontMetrics(font).getHeight() + TEXTBORDER){
                    SHOWSPEED++;
                }
        }
        dimLine = SPACING;
        int speed = getMaxSpeed();
        while(dimLine <= getMaxSpeed() / mpsPerPixel + SPACING){
            if(speed !=0){
                g.drawLine(SPACING - MARKINGLEN/2, dimLine, 
                        SPACING + (int)((finalTimeZoom- getInitialTime()) / sPerPixel),  dimLine);
                String velocity = Integer.toString(speed);
                g.drawString(velocity, SPACING - MARKINGLEN/2 - 
                        this.getFontMetrics(font).stringWidth(velocity) - 5, 
                        dimLine + this.getFontMetrics(font).getHeight()/3);
            }
            speed -= SHOWSPEED;
            dimLine += SHOWSPEED / mpsPerPixel;
        }
        g2d.rotate(-Math.PI/2, 15,(int)((getMaxSpeed() / mpsPerPixel) /2 + 
                this.getFontMetrics(font).stringWidth("Velocity (m/s)")/2) + SPACING); 
        g2d.drawString("Velocity (m/s)", 20,(int)((getMaxSpeed() / mpsPerPixel) /2 + 
                this.getFontMetrics(font).stringWidth("Velocity (m/s)")/2) + SPACING);
        g2d.rotate(Math.PI/2, 15,(int)((getMaxSpeed() / mpsPerPixel) /2 + 
                this.getFontMetrics(font).stringWidth("Velocity (m/s)")/2) + SPACING); 
        
        g.setColor(Color.RED);
        Map<Integer, RoutePointInfo> sortedMap = new TreeMap<Integer, RoutePointInfo>(routeInfo);
        Iterator<Entry<Integer, RoutePointInfo>> it = sortedMap.entrySet().iterator();
        RoutePointInfo prev = null;
        RoutePointInfo next;
        int lineDrawSkip = (int) ((sortedMap.size() - getInitialTime()) /500);
        int lineNum = 1;
        lineList.clear();
        Map.Entry<Integer, RoutePointInfo> ent;
        while((ent = it.next()).getKey() < getInitialTime());
        while(it.hasNext()){
        	if(ent.getKey() > finalTimeZoom){
        		break;
        	}
            next = (RoutePointInfo)(ent.getValue());
            
            //g.drawOval(graphCoordToSwing(next.time, next.speed).x, graphCoordToSwing(next.time, next.speed).y, 1, 1);
            if(lineNum >= lineDrawSkip){
                if(prev !=  null){

                        g2d.setStroke(new BasicStroke(5));
                        Line2D line = new Line2D.Double(graphCoordToSwing(prev.time, prev.speed).x, graphCoordToSwing(prev.time, prev.speed).y,
                                graphCoordToSwing(next.time, next.speed).x, graphCoordToSwing(next.time, next.speed).y);
                        lineList.add(line);
                        g2d.draw(line);
                        lineNum = 0;
                    }
                   prev = next; 
            }
            lineNum++;     
            ent = it.next();
        }
        
        if(windowOpen){
            g2d.setFont(new Font("TimesRoman", Font.PLAIN, 14));
            g2d.setColor(Color.GRAY);
            g2d.draw(currPopUp);
            g2d.fillPolygon(currPopUp);
            g2d.setColor(Color.WHITE);
            g2d.drawString(currPopUp.string.str1, currPopUp.string.loc1.x, currPopUp.string.loc1.y);
            g2d.drawString(currPopUp.string.str2, currPopUp.string.loc2.x, currPopUp.string.loc2.y);
            g2d.setFont(font);
        }
        
        

    }
    
    int getMaxSpeed(){
        int maxSpeed = 0;
        Iterator<Entry<Integer, RoutePointInfo>> it = routeInfo.entrySet().iterator();
        while(it.hasNext()){
            RoutePointInfo next = (RoutePointInfo)((Map.Entry<Integer, RoutePointInfo>)it.next()).getValue();
            
            if(next.speed + 1 > maxSpeed){
                maxSpeed = (int)(next.speed + 1);                   
            }
        }  
        return maxSpeed;
    }
}
