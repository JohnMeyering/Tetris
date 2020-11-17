package tetris;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CustomButton extends Group {
	Rectangle bg;
	Label label;
	
	public CustomButton(String text) {
		bg = new Rectangle();
		bg.setFill(Tetris.blue);
		bg.setArcHeight(10);
		bg.setArcWidth(10);
		bg.setStroke(new Color(192.0/255, 0/255, 0/255, 1.0));
		
		label = new Label(text);
		label.setTextFill(Color.WHITE);
		label.setFont(Tetris.font);
		label.setFocusTraversable(true);
		
		getChildren().add(bg);
		getChildren().add(label);
		
		setOnMousePressed((MouseEvent event) -> {
			label.setTextFill(Tetris.red);
		});
		setOnMouseReleased((MouseEvent event) -> {
			label.setTextFill(Color.WHITE);
		});
	}
	
	public void setBG(double x, double y, double width, double height) {
		bg.setX(x);
		bg.setY(y);
		bg.setWidth(width);
		bg.setHeight(height);
	}
	public void setLabel(double x, double y) {
		label.setLayoutX(x + 10);
		label.setLayoutY(y - 10); // convert from inkscape to javafx
	}
}
