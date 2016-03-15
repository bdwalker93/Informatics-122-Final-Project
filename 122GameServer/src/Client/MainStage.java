package Client;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainStage extends Stage{
	//turns on debug messages in the console.
	private boolean Debug = true;
	//text area logger.
	private TextArea TAlog;

	private Board gameboard;
//	private Scene scene = new Scene(new BorderPane());
//	private MenuBar mb = new MenuBar();
//	private BorderPane bottom = new BorderPane();

	private Button actionButton;

	
	//main GUI setup
	public MainStage(){
		super();

		setTitle("INF 122 Game Client");
        Scene scene = new Scene(new BorderPane());
        //Menus
        MenuBar mb = new MenuBar();
        Menu servermenu = new Menu("Server");
        MenuItem MIselectserv = new MenuItem("Select Server");
        MenuItem MIplayerprofile = new MenuItem("Player Profile");
        MenuItem MIExit = new MenuItem("Exit");
        servermenu.getItems().addAll(MIselectserv,MIplayerprofile,new SeparatorMenuItem(),MIExit);
        
        Menu gamemenu = new Menu("Game");
        MenuItem MIrequestgame = new MenuItem("Request Game");
        MenuItem MIquitgame = new MenuItem("Quit Game");
        gamemenu.getItems().addAll(MIrequestgame,MIquitgame);
        
        Menu windowmenu = new Menu("Window");
        MenuItem MIoptions = new MenuItem("Options");
        windowmenu.getItems().addAll(MIoptions);
        
        Menu helpmenu = new Menu("Help");
        MenuItem MIabout = new MenuItem("About");
        helpmenu.getItems().addAll(MIabout);
        
        mb.getMenus().addAll(servermenu,gamemenu,windowmenu,helpmenu);
        //Board
        gameboard = new Board(3, 3 );

        //text area
        TAlog = new TextArea();
        TAlog.setMaxHeight(100);

        //Button
        actionButton = new Button("Button");
        actionButton.setMinSize(100,100);
        
        //construct bottom
        BorderPane bottom = new BorderPane();
        bottom.setCenter(TAlog);
        BorderPane bottomright = new BorderPane();
        bottomright.setCenter(actionButton);
        bottom.setRight(bottomright);
        
		((BorderPane) scene.getRoot()).setTop(mb);
		((BorderPane) scene.getRoot()).setCenter(gameboard);
		((BorderPane) scene.getRoot()).setBottom(bottom);
		setScene(scene);
		show();
        
		
	}

	public int getRows()
	{
		return gameboard.getRows();
	}
	
	public int getColumns()
	{
		return gameboard.getColumns();
	}
	
	public void setBoard(int rows, int columns)
	{
//		gameboard = new Board(rows, columns);
		gameboard.setDimensions(rows, columns);

	}
	
	//Add a string to the logger. Set debug to true if its a debugger line.
	//Debugger lines should be lines not shown to the player.
	public void logger(String s, boolean debuggerline){
		if(Debug || (!Debug && !debuggerline))
			TAlog.appendText("\n" + s);
	}
	public void setDebug(boolean input){
		Debug = input;
	}
	
	public Board getBoard(){
		return gameboard;
	}
	public void setBoard(Board b){
		gameboard = b; 
	}
	
	public Button getButton(){
		return actionButton;
	}
	
}
