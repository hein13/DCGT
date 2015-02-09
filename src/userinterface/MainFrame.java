package userinterface;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;

import creator.CycleProducer;
import creator.Router;
import objects.RoutePointInfo;
import objects.Segment;

public class MainFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	List<Segment> segmentList;
    HashMap<Integer, RoutePointInfo> routeInfo;
    OptionsPanel panel;
    JFrame frame;
    Router route = null;
    CycleProducer producerInfo = null;
    
    boolean clearScreen;
    boolean first;
    
    JMapViewer map;
    
    public MainFrame(){
        super("Drive Cycle Creator Tool");       
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        first = true;
        createOptionsView();        
    }
    
    void createOptionsView(){ 
    	if(!first){
    		this.getContentPane().removeAll();
            panel.setValue(route.coordinateList);
        }else{
        	panel = new OptionsPanel(this);
        	List<Coordinate> defaultCoords = new ArrayList<Coordinate>();
        	defaultCoords.add(new Coordinate(35.894979,-78.867919));
        	defaultCoords.add(new Coordinate(35.881976,-78.645545));
            panel.setValue(defaultCoords);
            first = false;
        }  
        this.setSize(panel.getSize());
        this.setLayout(null);
        this.getContentPane().add(panel);
             
        //frame.add(panel);
        //frame.setSize(panel.getSize());
        this.setVisible(true);
        //frame.setResizable(false);   
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        
    }
    
    
    
    public void createResultView(){
        this.getContentPane().removeAll();
        this.setVisible(false);
        this.setResizable(false);
        
        
        setLayout(new BorderLayout());

        map = new JMapViewer();
        

        this.getContentPane().add(map, BorderLayout.NORTH);
        final JButton backButton  = new JButton("Go Back"); 
        final JButton playButton = new JButton("Simulate Drive");
        
        final LineGraph lineGraph = new LineGraph(routeInfo, map, playButton);
        lineGraph.setSize(Toolkit.getDefaultToolkit().getScreenSize().width,
                Toolkit.getDefaultToolkit().getScreenSize().height/2 - 80); 
        this.getContentPane().add(lineGraph, BorderLayout.CENTER);       
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(backButton, BorderLayout.EAST);
        buttonPanel.add(playButton, BorderLayout.WEST);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        backButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
                createOptionsView();
			}
        });
        
        playButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(playButton.getText().equals("Simulate Drive")){
					lineGraph.timer = new Timer();
					playButton.setText("Stop Simulation");
					lineGraph.timer.scheduleAtFixedRate(
							new PlayMovingCar(lineGraph.mover, lineGraph, lineGraph.timer, playButton), 0, 20);							
				}else{
					lineGraph.timer.cancel();
					playButton.setText("Simulate Drive");
				}
			}
        });
        
        
        
        map.setDisplayPositionByLatLon(segmentList.get(0).latStart, segmentList.get(0).lonStart, 16);
        for(int i = 0; i < segmentList.size(); i++){
            CustomMarker dot = new CustomMarker(segmentList.get(i).latStart, 
                    segmentList.get(i).lonStart, segmentList.get(i));
            if(segmentList.get(i).intersection){
                dot.setBackColor(Color.BLUE);
                map.addMapMarker(dot);
                
            }
            
            if(i < segmentList.size() - 1){
                Coordinate one = new Coordinate(segmentList.get(i).latStart, segmentList.get(i).lonStart);
                Coordinate two = new Coordinate(segmentList.get(i + 1).latStart, segmentList.get(i + 1).lonStart);
                List<Coordinate> route = new ArrayList<Coordinate>(Arrays.asList(one, two, two));
                MapPolygonImpl line = new MapPolygonImpl(route);
                map.addMapPolygon(line);
            }
            
        }
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, 
                Toolkit.getDefaultToolkit().getScreenSize().height - 30);  
        this.revalidate();
        this.repaint();
        this.setVisible(true);
        map.setDisplayToFitMapMarkers();
    }
}
    
    class CustomMarker extends MapMarkerDot{
        Segment seg;
        
        CustomMarker(double lat, double lon, Segment seg){         
            super(lat, lon);
            this.seg = seg;
        }
        @Override
        public void paint(Graphics g,
         Point position,
         int radio){
        	g.setFont(new Font("Courier New", Font.BOLD, 15));
            //g.drawString(Integer.toString((int)(seg.speedLimit * .621)),  position.x, position.y);
            super.paint(g, position, radio);
        }
    }
    
    
    class PlayMovingCar extends TimerTask {
        TimeVehicleMover mover;
        LineGraph graph;
        Timer timer;
        JButton button;
        public PlayMovingCar(TimeVehicleMover mover, LineGraph graph, Timer timer, JButton button){
        	this.mover = mover;
            this.graph = graph;      
            this.timer = timer;
            this.button = button;
        }
        
        @Override
        public void run() {
            mover.currXPos += 1;            
            if(mover.currXPos > (int)(graph.getFinalTime() / graph.sPerPixel) + LineGraph.SPACING){
                timer.cancel();
                button.setEnabled(true);
                mover.currXPos = LineGraph.SPACING;
            }
            graph.repaint();
        }
    }   

    class TimeVehicleMover implements MouseMotionListener{
        boolean newDrag;
        boolean rectangle;
        int PENTAGONHEIGHT = 20;
        int PENTAGONWIDTH = 10;
        int currXPos = LineGraph.SPACING;
        Point endPoint;
        Point origPoint;
        
        LineGraph graph;
        Polygon pentagon;
        JMapViewer map;
        HashMap<Integer, RoutePointInfo> routeInfo;
        MapMarkerDot marker;
        public TimeVehicleMover(LineGraph graph, JMapViewer map, HashMap<Integer, RoutePointInfo> routeInfo){
            this.graph = graph;
            newDrag = true;
            rectangle = false;
            this.map = map;
            this.routeInfo = routeInfo;
            marker = null;
        }
        
        public void drawRectangle(Graphics2D g2d){
        	int x, y, width, height;
        	if(origPoint.x > endPoint.x){
        		x = endPoint.x;
        		width = origPoint.x - endPoint.x;
        	}else{
        		x = origPoint.x;
        		width = endPoint.x - origPoint.x;
        	}
        	if(origPoint.y > endPoint.y){
        		y = endPoint.y;
        		height = origPoint.y - endPoint.y;
        	}else{
        		y = origPoint.y;
        		height = endPoint.y - origPoint.y;
        	}
        	Color prevColor = g2d.getColor();
            g2d.setColor(Color.CYAN);
            
            Stroke prev = g2d.getStroke();
            g2d.setStroke(new BasicStroke(5));
        	g2d.drawRect(x, y, width, height);
        	
        	g2d.setColor(prevColor);
        	g2d.setStroke(prev);
        }
        
        public void drawPolygon(Graphics2D g2d){
            //Draw Pentagon
            pentagon = new Polygon();
            pentagon.addPoint(currXPos, LineGraph.SPACING);
            pentagon.addPoint(currXPos - PENTAGONWIDTH/2, LineGraph.SPACING - PENTAGONHEIGHT/2);
            pentagon.addPoint(currXPos - PENTAGONWIDTH/2, LineGraph.SPACING - PENTAGONHEIGHT);
            pentagon.addPoint(currXPos + PENTAGONWIDTH/2, LineGraph.SPACING - PENTAGONHEIGHT);
            pentagon.addPoint(currXPos + PENTAGONWIDTH/2, LineGraph.SPACING - PENTAGONHEIGHT/2);
            g2d.drawPolygon(pentagon);
            Color prevColor = g2d.getColor();
            g2d.setColor(Color.BLACK);
            g2d.fillPolygon(pentagon);
            
            Stroke prev = g2d.getStroke();
            g2d.setStroke(new BasicStroke(5));
            g2d.drawLine(currXPos, LineGraph.SPACING, currXPos, 
                    (int)(graph.getMaxSpeed() / graph.mpsPerPixel) + LineGraph.SPACING);
            
            
            g2d.setStroke(prev);
            String time = String.valueOf((int)graph.swingCoordToGraph(new Point(currXPos, LineGraph.SPACING + 10))[0]);
            g2d.drawString(time, currXPos - PENTAGONWIDTH/2, LineGraph.SPACING - PENTAGONHEIGHT - 5);
            g2d.setColor(prevColor);
            if(routeInfo.containsKey((int)graph.swingCoordToGraph(new Point(currXPos, LineGraph.SPACING + 10))[0])){
                RoutePointInfo info = routeInfo.get((int)graph.swingCoordToGraph(new Point(currXPos, LineGraph.SPACING + 10))[0]);
                if(marker != null){
                    map.removeMapMarker(marker);
                }
                try{
                	marker = new MapMarkerDot(info.latitude, info.longitude);                	
                }catch(NullPointerException e){
                	System.out.println("hello");
                }
                map.addMapMarker(marker);               
            }
            
        }
        

		@Override
		public void mouseDragged(MouseEvent e) {
			if((pentagon.contains(e.getX() , LineGraph.SPACING - PENTAGONHEIGHT/2) || newDrag == false)
					&& !rectangle){
                if(e.getX() < LineGraph.SPACING){
                    currXPos = LineGraph.SPACING;
                }else if(e.getX() > (int)((graph.finalTimeZoom - graph.getInitialTime()) / graph.sPerPixel) + LineGraph.SPACING){
                    currXPos = (int)((graph.finalTimeZoom - graph.getInitialTime())  / graph.sPerPixel) + LineGraph.SPACING;
                }else{
                    currXPos = e.getX();
                }              
                newDrag = false;
            }else{
            	Point pt;
            	 if(e.getX() < LineGraph.SPACING){
            		 pt = new Point(LineGraph.SPACING, e.getY());
            	 }else if(e.getX() > (int)((graph.finalTimeZoom - graph.getInitialTime()) / graph.sPerPixel) + LineGraph.SPACING){
                     pt = new Point((int)((graph.finalTimeZoom - graph.getInitialTime())  / graph.sPerPixel) + LineGraph.SPACING, e.getY());
            	 }else{
            		 pt = new Point(e.getX(), e.getY());
            	 }
            	if(!rectangle){
            		origPoint = pt;
            		rectangle = true;
            	}
            	endPoint = pt;
            }
			graph.repaint();
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
   
    }
    class ClickableLine implements MouseListener{
        LineGraph graph; 
        JButton playButton;
        ClickableLine(LineGraph graph, JButton playButton){
            this.graph = graph;
            this.playButton = playButton;
            
        }

        @Override
        public void mouseClicked(MouseEvent me) { 
            for(Line2D line: graph.lineList){
                if(line.ptSegDist(me.getX(), me.getY()) < 5){
                    graph.currPopUp = new PopUpCoord(me.getX(), me.getY(), graph);
                    
                    graph.windowOpen = true;
                    graph.repaint(); 
                    return;
                }
            }if(graph.windowOpen){
                graph.windowOpen = false;
                graph.repaint();
            }
         
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if(graph.mover.pentagon.contains(me.getX() , 
                    LineGraph.SPACING - graph.mover.PENTAGONHEIGHT/2)){
                graph.mover.newDrag = false;
                graph.timer.cancel();
                playButton.setText("Simulate Drive");
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            graph.mover.newDrag = true;
            if(graph.mover.rectangle){
            	graph.mover.rectangle = false;
            	if(graph.mover.origPoint.x > graph.mover.endPoint.x){
            		graph.finalTimeZoom = (int) graph.swingCoordToGraph(graph.mover.origPoint)[0];
            		graph.initalTime = (int) graph.swingCoordToGraph(graph.mover.endPoint)[0];           		
            	}else if(graph.mover.origPoint.x < graph.mover.endPoint.x){
            		graph.finalTimeZoom = (int) graph.swingCoordToGraph(graph.mover.endPoint)[0];
            		graph.initalTime = (int) graph.swingCoordToGraph(graph.mover.origPoint)[0];          		
            	}
            }
            graph.repaint();
            
        }

        @Override
        public void mouseEntered(MouseEvent me) {
        }

        @Override
        public void mouseExited(MouseEvent me) {
        }
}
    
    class Zoomer implements MouseWheelListener{

    	LineGraph graph;
    	Zoomer(LineGraph graph){
    		this.graph = graph;
    	}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int initRot = (int) ((graph.swingCoordToGraph(new Point(e.getX(), 0))[0] / graph.finalTimeZoom) 
					* e.getWheelRotation() * 2);
			int finalRot = e.getWheelRotation() * 2 - initRot;
			System.out.println(finalRot);
			if(graph.finalTimeZoom - finalRot -
					graph.initalTime + initRot <= 2){
				System.out.println("hello");
			}
			if(graph.finalTimeZoom - finalRot -
					graph.initalTime + initRot <= 50 && e.getWheelRotation() > 0){

			}else if(graph.initalTime + initRot >= 0 && 
					graph.finalTimeZoom - finalRot <= graph.getFinalTime() ){
				graph.initalTime += initRot;
				graph.finalTimeZoom -= finalRot;
			}else{
				graph.initalTime = 0;
				graph.finalTimeZoom = (int) graph.getFinalTime();
			}
			graph.windowOpen = false;
			graph.repaint();	
		}
    	
    }
    
