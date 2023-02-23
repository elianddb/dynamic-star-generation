import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.lang.Math;

/**
 * Creates stars through the use of polygons and circles, and then displays them
 * to the user. Number of vertices can be modified using the slider at the top
 * leftff
 *
 *
 * @author E.Briton
 * @version 4.12.22
 */
public class Main extends Application {
    private final double SCENE_WIDTH = 500;
    private final double SCENE_HEIGHT = 500;
    private final int DOUBLE = 2;
    // Scene starts at 0, 0
    private final double CENTER_X = (SCENE_WIDTH / 2) - 1;
    private final double CENTER_Y = (SCENE_HEIGHT / 2) - 1;

    public static void main(String[] args)
    {
	    launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        Pane pane = new Pane();

        final double TEXT_OFFSET = 15;
        Text temp1 = new Text(CENTER_X - TEXT_OFFSET, CENTER_Y, "ELIAN");
        pane.getChildren().add(temp1);
        final int MIN_TRANS = 50;
        final int MAX_TRANS = 10;

        final double MAX_VERTICES = 20;
        final double MIN_VERTICES = 3;
        final double START_AT = 5;
        Slider slider = new Slider(MIN_VERTICES, MAX_VERTICES, START_AT);
        slider.setPrefWidth(300);
        slider.setMinorTickCount(0);
        slider.setMajorTickUnit(1);
        slider.setBlockIncrement(1);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        pane.getChildren().add(slider);
        final double DEF_SIZE = 400;
        final double NORMAL = 0;

        final int TEMP_VERTICES = (int) slider.getValue();
        final Polygon temp2 = new RegularStar(CENTER_X, CENTER_Y, DEF_SIZE, TEMP_VERTICES, NORMAL).getPolygon();
        pane.getChildren().add(temp2);
        new AnimationPreset(temp2).bob(MIN_TRANS, MAX_TRANS);
        new AnimationPreset(temp1).bob(MIN_TRANS, MAX_TRANS);

        slider.valueProperty().addListener(event ->
        {
            // Animation stuff
            final int OUTER_VERTICES = (int) slider.getValue();
            RegularStar star = new RegularStar(CENTER_X, CENTER_Y, 300, OUTER_VERTICES, NORMAL);
            Polygon polygon = star.getPolygon();
            InnerShadow inner = new InnerShadow(20, Color.BLACK);
            inner.setOffsetX(10);
            inner.setOffsetY(10);
            polygon.setEffect(inner);
            polygon.setFill(Color.LIGHTSLATEGREY);
            Text myName = new Text(CENTER_X - TEXT_OFFSET, CENTER_Y, "ELIAN");
            myName.setStyle("-fx-font-weight: bold");
            myName.setFill(Color.WHITE);
            pane.getChildren().clear();
            pane.getChildren().addAll(polygon, myName, slider);
            AnimationPreset animPoly = new AnimationPreset(polygon);
            animPoly.zoom();
            animPoly.bob(MIN_TRANS, MAX_TRANS);
            animPoly.spin(180);
            AnimationPreset animText = new AnimationPreset(myName);
            animText.zoom();
            animText.bob(MIN_TRANS, MAX_TRANS);
            animText.spin(180);
        });


        Scene scene = new Scene(pane, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setTitle("Hollywood Star");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    class RegularPolygon
    {
        private static final double FULL_CIRCLE = 360;
        private final double POS_X;
        private final double POS_Y;

        // Circle that will contain the polygon.
        private final double CIRCLE_DIAMETER;
        private final double CIRCLE_RADIUS;

        private final int NUM_OF_VERTICES;
        // Angles that point in the outward direction.
        private final double EXTERIOR_RAD;
        private final double RAD_SUM;
        // Angles that point in the inward direction.
        private final double INTERIOR_RAD;

        private final double SIDE_LENGTH;
        private final VertexPoint[] VERTICES;

        /**
         * A regular polygon shape with the number of sides given.
         *
         * @param x position x
         * @param y position y
         * @param s size of polygon
         * @param n number of sides
         * @param deg rotation
         */
        RegularPolygon(double x, double y, double s, int n, double deg)
        {
            POS_X = x;
            POS_Y = y;
            // Circle
            CIRCLE_DIAMETER = s;
            CIRCLE_RADIUS = CIRCLE_DIAMETER / 2;

            // Polygon
            NUM_OF_VERTICES = n;
            // Math trigonometric methods work with radians, needs to converted.
            // Useful for getting points and side lengths.
            EXTERIOR_RAD = Math.toRadians(FULL_CIRCLE / NUM_OF_VERTICES);
            // At least 2 vertices to create a polygon.
            final int MIN_VERTICES = 2;
            // Polygons are built from triangles, more sides added = more triangles.
            final double TRIANGLE_RAD_SUM = Math.toRadians(180);
            RAD_SUM = (NUM_OF_VERTICES - MIN_VERTICES) * TRIANGLE_RAD_SUM;
            // Not used in this class, but useful for star class.
            INTERIOR_RAD = RAD_SUM / NUM_OF_VERTICES;
            // hsinΘ, Hypotenuse = radius.
            SIDE_LENGTH = DOUBLE * (CIRCLE_RADIUS * (Math.sin(EXTERIOR_RAD / 2)));
            VERTICES = new VertexPoint[NUM_OF_VERTICES];
            // Initialize points for polygon with center offset.
            final int FIRST_VERTEX = 0;
            double rad = Math.toRadians(deg);
            System.out.println("Degrees: " + deg);
            for (int v = FIRST_VERTEX; v < NUM_OF_VERTICES; ++v)
            {
                VERTICES[v] = new VertexPoint();
                // Utilizes the unit circle but offsets the start to pi/2.
                // x = opposite, hsinΘ = opposite. Hypotenuse = radius.
                VERTICES[v].x = POS_X + CIRCLE_RADIUS * Math.sin(rad);
                // y = adjacent, hcosΘ = adjacent. Subtract because that's upward in a pane.
                VERTICES[v].y = POS_Y - CIRCLE_RADIUS * Math.cos(rad);
                rad += EXTERIOR_RAD;
                System.out.println("v" + v + ": " + VERTICES[v].x + " " + VERTICES[v].y);

            }
            // Will be utilized to reorder existing points.
            VertexPoint[] VERTICES_COPY = new VertexPoint[NUM_OF_VERTICES];

            // Reorder the vertices, so, it goes from the top-left clockwise.
            int topLeftIndex = FIRST_VERTEX;
            for (int v1 = FIRST_VERTEX; v1 < NUM_OF_VERTICES; ++v1)
            {
                // Might as well initialize the copy here.
                VERTICES_COPY[v1] = VERTICES[v1];

                double highestY = VERTICES[FIRST_VERTEX].y;
                for (int v2 = FIRST_VERTEX; v2 < NUM_OF_VERTICES; ++v2)
                {
                    // Type casting is necessary to get a rough estimate, doubles try
                    // to be too accurate but are incorrect.
                    if (highestY > (int)VERTICES[v2].y)
                    {
                        topLeftIndex = v2;
                        highestY = (int)VERTICES[topLeftIndex].y;
                    }
                }

                if (VERTICES[v1].x < VERTICES[topLeftIndex].x && (int)VERTICES[v1].y == highestY)
                {
                    topLeftIndex = v1;
                }
            }

            // Here we reorder the vertices.
            VERTICES[FIRST_VERTEX] = VERTICES_COPY[topLeftIndex];
            final int LAST_VERTEX = NUM_OF_VERTICES - 1;
            // Index keeps track of the main vertices.
            int v1 = FIRST_VERTEX;
            // Index gets new vertex value.
            int v2 = topLeftIndex;
            do
            {
                VERTICES[v1] = VERTICES_COPY[v2];
                System.out.println("v" + v1 + ": " + VERTICES[v1].x + " " + VERTICES[v1].y);
                // Might have to reset v2 to 0, so, it can finish reading at points in
                // the correct order.
                if (++v2 > LAST_VERTEX)
                {
                    v2 = FIRST_VERTEX;
                }

            } while (++v1 < NUM_OF_VERTICES);
        }

        public Polygon getPolygon()
        {
            Polygon polygon = new Polygon();
            final int START_AT = 0;
            for (int v = START_AT; v < NUM_OF_VERTICES; ++v)
            {
                polygon.getPoints().addAll(VERTICES[v].x, VERTICES[v].y);
            }

            return polygon;
        }

        private class VertexPoint
        {
            private double x;
            private double y;
        }
    }

    class RegularStar {
        private final RegularPolygon OUTER_POLYGON;
        private final RegularPolygon INNER_POLYGON;

        /**
         * Attempts to create an inner and outer polygon that can be utilized
         * to create a regular star shape. A pentagram may be useful when going
         * over this one's description.
         *
         * https://martiancraft.com/blog/2017/03/geometry-of-stars/
         * Has a good example in the pentagon section.
         *
         * @param x position x
         * @param y position y
         * @param s size
         * @param n number of vertices
         * @param deg rotation
         */
        RegularStar(double x, double y, double s, int n, double deg) {
            OUTER_POLYGON = new RegularPolygon(x, y, s, n, deg);
            // Might be a bit over-complicated.
            // Tgl1 refers to the left-half of one of the 5 triangles used to create a star
            // in a pentagram.
            final double TGL1_OPP = OUTER_POLYGON.SIDE_LENGTH / 2;
            final double TGL1_RAD1 = OUTER_POLYGON.INTERIOR_RAD / 2;
            // h = ocscΘ, Θ = RAD1
            final double TGL1_HYP = TGL1_OPP * (1 / Math.sin(TGL1_RAD1));
            // Tgl2 refers to the triangle that creates one of the stars pointers.
            final double STRAIGHT_ANGLE = Math.toRadians(180);
            final double TGL2_RAD1 = STRAIGHT_ANGLE - OUTER_POLYGON.INTERIOR_RAD;
            // a = hcosΘ, Θ = TGL2_RAD1, a = half of inner polygon side
            // TGL1 and TGL2 share the same hypotenuse.
            final double TGL2_ADJ = TGL1_HYP * Math.cos(TGL2_RAD1);
            // TGL3 represents the triangle that creates the side of the inner polygon.
            // Half of exterior outer angle.
            final double TGL3_RAD1 = OUTER_POLYGON.EXTERIOR_RAD / 2;
            // h = ocscΘ, h = inner polygon radius, Θ = TGL3_RAD1, o = TGL2_ADJ
            final double INNER_RADIUS = TGL2_ADJ * (1 / Math.sin(TGL3_RAD1));
            // For some reason 4 vertices breaks it, so quick fix hard code size.
            final double INNER_SIZE = n != 4 ? INNER_RADIUS * DOUBLE : s / 3;
            // Has the side of the inner polygon face upward.
            final double ROTATE = Math.toDegrees(OUTER_POLYGON.EXTERIOR_RAD / 2);
            INNER_POLYGON = new RegularPolygon(x, y, INNER_SIZE, n, deg + ROTATE);
        }

        public Polygon getPolygon()
        {
            Polygon polygon = new Polygon();
            RegularPolygon.VertexPoint outerVP;
            RegularPolygon.VertexPoint innerVP;
            final int FIRST_VERTEX = 0;
            // The basic pattern is inner vertex then outer vertex. Repeatedly following
            // it will create a star shape with most polygons.
            for (int v = FIRST_VERTEX; v < OUTER_POLYGON.NUM_OF_VERTICES; ++v)
            {
                innerVP = INNER_POLYGON.VERTICES[v];
                polygon.getPoints().addAll(innerVP.x, innerVP.y);
                outerVP = OUTER_POLYGON.VERTICES[v];
                polygon.getPoints().addAll(outerVP.x, outerVP.y);
            }

            polygon.setFill(null);
            polygon.setStroke(Color.BLACK);

            return polygon;
        }
    }

    class AnimationPreset
    {
        Node node;
        TranslateTransition tTrans;
        ScaleTransition sTrans;
        RotateTransition rTrans;

        AnimationPreset(Node n)
        {
            node = n;
            tTrans = new TranslateTransition();
            sTrans = new ScaleTransition();
            rTrans = new RotateTransition();
        }

        public void bob(int min, int max)
        {
            final double NO_DELAY = 0;
            bob(min, max, NO_DELAY);
        }

        public void bob(int min, int max, double s)
        {
            tTrans.stop();
            tTrans = new TranslateTransition(new Duration(2000), node);
            final double SECOND = 1000;
            tTrans.setDelay(new Duration(s * SECOND));
            // First transition to position node.
            tTrans.setByY(min);
            tTrans.play();
            // Replaying animation.
            tTrans.setOnFinished(event ->
            {
                final int REPEAT = 2;
                tTrans.setByY(-(min + max));
                tTrans.setCycleCount(REPEAT);
                tTrans.setAutoReverse(true);
                tTrans.play();
            });
        }

        public void zoom()
        {
            sTrans.stop();
            sTrans = new ScaleTransition(new Duration(1000), node);
            sTrans.setAutoReverse(true);
            sTrans.setCycleCount(Animation.INDEFINITE);
            sTrans.setToX(2);
            sTrans.setToY(2);
            sTrans.play();
        }

        public void spin(double deg)
        {
            rTrans.stop();
            rTrans = new RotateTransition(new Duration(2000), node);
            rTrans.setAutoReverse(true);
            rTrans.setCycleCount(Animation.INDEFINITE);
            rTrans.setToAngle(deg);
            rTrans.play();
        }
    }
}
