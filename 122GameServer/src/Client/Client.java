package Client;

import static java.lang.Math.toIntExact;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.scene.input.MouseEvent;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;

public class Client implements Runnable{
	public String serverIP;
	public int port;
	public Socket socket;
	private MainStage gui;
	private GameData gameData;
	private boolean myTurn = false;
	private boolean madeMove = false;
	private ArrayList<Integer> move = new ArrayList<Integer>();
	private String clientName;
	private String opponentName;
	private String gameName;
	private boolean isRunning = true;
	private String winner;
	private boolean choseGame = false;

	
	///////////////////////////////////////////
	//// client constructor
	public Client(String serverip, int portnum, MainStage inputgui)
	{
		serverIP = serverip;
		port = portnum;
		gameData = new GameData();
		gui = inputgui;

		setupBoard(); // will need to do this when parsing game state
		(new Thread(this)).start();
	}
	
	public void setupBoard(){
		gui.setBoard(0, 0);
		setupMouseListeners();
	}
	
	public void setupBoard(int rows, int columns)
	{
		gui.setBoard(rows, columns);
		setupMouseListeners();
	}
	
	////////////////////////////////////////////////////
	/// adds the gui to the client
	public void setGui(MainStage inputgui)
	{
		gui = inputgui;
	}
	
	
	
	//////////////////////////////////////////////////////
	///sets up mouse listener on gui
	public void setupMouseListeners()
	{
		for(int i=0;i<gui.getRows();i++)
        	for(int j=0;j<gui.getColumns();j++){
        		gui.getBoard().getTile(i, j).setOnMouseClicked((MouseEvent e) -> {
        			Tile t = (Tile)e.getSource();
                	int xloc= t.getXlocation();
                	int yloc= t.getYlocation();
                	gui.logger("Mouse clicked: "+xloc+","+yloc,true);
                	if (myTurn)
                	{
                		t.setText("X");
                		setMove(-1, -1, xloc, yloc);
                	}
                	else
                	{
                		gui.logger("Not Your Turn", true);
                	}
                });
        	}
	}
	
	
	
	///////////////////////////////////////////////////////////////////
	////// function that is used by mouse listeners.
	public void setMove(int xOrigin, int yOrigin, int xDest, int yDest)
	{
		move.add(xOrigin);
		move.add(yOrigin);
		move.add(xDest);
		move.add(yDest);

		System.out.println(move);
		madeMove = true;
	}
	
	
/////////////////////////////////////////////////////////////////////////////////////////////
///////////functions that send information through the socket///////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////
	/////sends a request that states which game the player wants to join
	public void sendGameRequest(String game)
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "gameRequest");
		obj.put("game", game);
		
		try 
		{
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
		    out.write(obj.toJSONString());
		    out.flush();
		    out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/////////////////////////////////////////////////////////////////
	//// requests the board size
	public void requestBoardSize()
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "requestBoardSize");

		try 
		{
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
		    out.write(obj.toJSONString());
		    out.flush();
		    out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	//////////////////////////////////////////////////////////
	/// sends a string representing that the button on the GUI has been pressed
	public void sendButtonPressed()
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "ButtonPressed");

		try 
		{
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
		    out.write(obj.toJSONString());
		    out.flush();
		    out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	
	////////////////////////////////////////////////////////////////
	///// sends a string through the socket requesting players in the server
	public void requestPlayerList()
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "requestPlayerList");

		try 
		{
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
		    out.write(obj.toJSONString());
		    out.flush();
		    out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	////////////////////////////////////////////
	///// sends a string through the socket requesting gamelist
	public void requestGameList()
	{	
		JSONObject obj = new JSONObject();
		obj.put("type", "requestGameList");
		try 
		{
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
		    out.write(obj.toJSONString());
		    out.flush();
		    out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	////////////////////////////////////////////////////
	///// sends location of piece that has been selected
//	public void sendSelectPiece(int x, int y)
//	{
//		JSONObject obj = new JSONObject();
//		obj.put("type", "selectPiece");
//		obj.put("xVal", x);
//		obj.put("yVal", y);
//		
//		try 
//		{
//			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
//		    out.write(obj.toJSONString());
//		    out.flush();
//		    out.close();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
	
	
	
	////////////////////////////////////////////////
	/// sends the location that the piece is supposed to move 
	public void sendMove()
	{
		
		JSONObject obj = new JSONObject();
		obj.put("type", "movePiece");
		obj.put("xOrigin", move.get(0)); // xOrigin
		obj.put("yOrigin", move.get(1)); // yOrigin
		obj.put("xDest", move.get(2));   // xDest
		obj.put("yDest", move.get(3));   // yDest
		
		try 
		{
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
		    out.write(obj.toJSONString());
		    out.flush();
		    out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}	
		
		madeMove = false;
	}
	
	
/////////////////////////////////////////////////////////////////////////////////////////////
///////////functions that parse information from the socket///////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////	
	
	////////////////////////////////////////////////////////////
	//// parses board state
	public void parseGameState()
	{
		String jsonString = "";
		String input;
		while (true)
		{
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while ((input = br.readLine()) != null) {
					jsonString += input;
				}
				if (!jsonString.equals("")){ // if it received input break out of loop
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		
		JSONBoard state = new JSONBoard(jsonString);
		
		
		/////////////////////////////////////////////// 
		// set up board from server information
		setupBoard(state.getRowNum(), state.getColumnNum());
		for (int i = 0; i < state.getRowNum(); i ++)
		{
			for (int j = 0 ; j < state.getColumnNum(); j ++)
			{
				Tile t = gui.getBoard().getTile(i, j);
				int[] rgb = state.getTileColor(i, j);
            	t.setBackgroundColor(rgb[0], rgb[1], rgb[2]);

            	for (int x = 0 ; x < 2 ; x ++)
            	{
            		String shape = state.getPieceShape(i, j, x);
            		if (!shape.equals("empty"))
            		{
            			int[] color = state.getPieceColor(i, j, x);
            			String layer = state.getPieceLayer(i, j, x);
            			String type = state.getPieceType(i, j, x);
            			Piece piece = new Piece(color, shape , layer , type.charAt(0));
            			t.addPiece(piece);
            		}
            		
            	}
			}
		}
		
		/////////////////////////////////////////////////////
		////// set turn
		if (state.getCurrentTurn().equals(clientName))
		{
			myTurn = true;
		}
		
		//// set running state
		isRunning = state.getIsRunning();
		
		
		//// game is not running set winner
		if (!isRunning){
			winner = state.getWinner();
		}
		

	}
	
	
	public void parseValidMove()
	{
		madeMove = false;
		
		String jsonString = "";
		String input;
		while (true)
		{
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while ((input = br.readLine()) != null) {
					jsonString += input;
				}
				if (!jsonString.equals("")){ // if it received input break out of loop
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	

	    
	    try {
	    	JSONParser parser = new JSONParser();
	    	Object object = parser.parse(jsonString);
	    	JSONObject jsonObject = (JSONObject) object;

	    	String valid = (String) jsonObject.get("valid");
	    	
	    	if (valid.equals("true"))
	    	{
	    		myTurn = false;
	    	}


	    } catch (ParseException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	}
	
	///////////////////////////////////////////////////////////////////
	////// parses gamelist that is sent from server
	public void parseGameList()
	{
		String jsonString = "";
		String input;
		while (true)
		{
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while ((input = br.readLine()) != null) {
					jsonString += input;
				}
				if (!jsonString.equals("")){ // if it received input break out of loop
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	    try {
	    	JSONParser parser = new JSONParser();
	    	Object object = parser.parse(jsonString);
	    	JSONObject jsonObject = (JSONObject) object;

	    	JSONArray gameArray = (JSONArray) jsonObject.get("games");
	    	for (int i = 0 ; i < gameArray.size(); i ++)
	    	{
	    		JSONObject obj = (JSONObject)gameArray.get(i);
	    		String name = (String)obj.get("name");
	    		gameData.addGame(name);
	    	}
	    	
	    	
	    	JSONArray playerArray = (JSONArray) jsonObject.get("players");
	    	for (int i = 0 ; i < gameArray.size(); i ++)
	    	{
	    		JSONObject obj = (JSONObject)gameArray.get(i);
	    		String player = (String)obj.get("name");
	    		gameData.addPlayer(player);
	    	}
	    	
	    	


	    } catch (ParseException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	}

	
	public void parsePlayerList()
	{
		
	}



	@Override
	public void run() {
		try{
			socket = new Socket(InetAddress.getByName(serverIP), port);
			System.out.println("Successful connection.");
			
			requestGameList(); // request list of games
			parseGameList();
			requestPlayerList(); // request list of players
			parsePlayerList();
			displayServer(); // displays game list and players online
			
			
			while (true)
			{
				if (choseGame)
				{
					sendGameRequest(gameName);
					parseGameState();
					break;
				}
			}
			
			while (isRunning)
			{
				while (myTurn)
				{
					if (madeMove)
					{
						sendMove(); // sends move;
						parseValidMove(); // listens for server response .sets myTurn;
					}
				}
				
				while (!myTurn)
				{
					parseGameState();
				}
			}
			
		}
		catch (IOException e)
		{
			System.out.println("failed to connect");
			e.printStackTrace();
		}
		
		/*while(true){
		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++){
					gui.getBoard().getTile(i, j).setBackgroundColor(255, 0, 127);
					try{Thread.sleep(100);}catch(InterruptedException e){}
			}
		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++){
					gui.getBoard().getTile(j, i).setBackgroundColor(127, 0, 255);
					try{Thread.sleep(100);}catch(InterruptedException e){}
			}
		}*/
	}

}
