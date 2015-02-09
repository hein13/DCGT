package userinterface;

import java.awt.Font;
import java.awt.Point;
import java.awt.Polygon;

public class PopUpCoord extends Polygon{
	private static final long serialVersionUID = 1L;
	int width = 120;
    int height = 50;
    int arrowHeight = 20;
    int arrowWidth = 40;
    CoordString string;
    LineGraph graph;
    
    PopUpCoord(int x, int y, LineGraph graph){
        super();
        this.graph = graph;
        this.addPoint(x - (width/2), y -  ( height + arrowHeight));
        this.addPoint(x - (width/2), y - arrowHeight);
        this.addPoint(x - (arrowWidth/2), y - arrowHeight);
        this.addPoint(x, y);
        this.addPoint(x + (arrowWidth/2), y - arrowHeight);
        this.addPoint(x + (width/2), y - arrowHeight);
        this.addPoint(x + (width/2), y -  ( height + arrowHeight));
        string = new CoordString(x, y);       
    }
    
    class CoordString{
        String str1, str2;
        Point loc1, loc2;
        CoordString(int x, int y){
            Font font = new Font("TimesRoman", Font.PLAIN, 14);
            str1 = "Time = " + Math.round(graph.swingCoordToGraph(new Point(x, y))[0]) + " s";                        
            str2 = "Velocity = " + Math.round(graph.swingCoordToGraph(new Point(x, y))[1]) + " m/s";
            int sx = x - graph.getFontMetrics(font).stringWidth(str1)/2;
            int sy = y - height - arrowHeight + graph.getFontMetrics(font).getHeight();
            loc1 = new Point(sx, sy);
            sx = x - graph.getFontMetrics(font).stringWidth(str2)/2;
            sy = sy + graph.getFontMetrics(font).getHeight();
            loc2 = new Point(sx, sy);
        }   
    }
}
