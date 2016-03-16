/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author malar
 */
public class Player extends Thread
{

    private final Socket connection;
    private DataInputStream input;
    private DataOutputStream output;

    private final Lobby lobby;
    private Profile profile;

    private boolean loggedIn;
    private Game game;

    /**
     * Constructor, creates a player object and 'connects' them to the lobby
     * @param connection the socket connection to talk over
     * @param lobby contains lists of players and games
     */
    public Player(Socket connection, Lobby lobby)
    {
        this.connection = connection;
        this.lobby = lobby;

        //at thread initialization the user is not logged in
        loggedIn = false;

        //initializing input and output streams
        try
        {
            input = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
            output = new DataOutputStream(connection.getOutputStream());
        } catch (IOException ex)
        {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);

            JOptionPane.showMessageDialog(new JOptionPane(),
                    "Network Connection Error",
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);

            System.exit(-1);
        }
    }
    
    /**
     * Getter to see whether the player is logged in currently.
     * @return true if player is logged in, otherwise false.
     */
    public boolean loggedIn()
    {
    	return loggedIn;
    }

  
    /**
     * Called when the thread is started.
     */
    @Override
    public void run()
    {
        // This is necessary to continuously receive messages from the
        // client, it has to be in a loop
        
    	if(!loggedIn){
    		sendMessage(initialHandshake());
    	}
        
		//loops until login in reached for this player
		while(!loggedIn)
		{	
			//gets the message from the client
			String loginInfo = receiveMessage();
			String[] tokens = JSONServerGeneral.checkType(loginInfo);
			System.out.println(loginInfo);
			
			if(tokens[0].equals("LoginType") && tokens[1].equals("CreateUser")){
				loggedIn = addNewPlayer();
			} else if(tokens[0].equals("LoginType") && tokens[1].equals("Login")){
				loggedIn = loginPlayer();
			}
			
			//read input and check to see if its a login or new acct creation
			if(!loggedIn){
				sendMessage(badLogin());
			} else {
				sendMessage(JSONServerTranslator.loginStatus("Successful"));
			}

		}

		System.out.println("Player Created/Logged In");
			//sends player to select game method
			//selectGame();
        
//        while (true)
//        {
//            String stringToParse = receiveMessage();
//
//            // TODO: in here we parse the message using a JSON parser, and then
//            // Call the proper function based on what we get parsed out to.
//            
//            String[] response = parseMessage(stringToParse);
//            switch (response[0])
//            {
//                // A move for games such as tic tac toe
//                case "move1":
//                    game.makeMove(Integer.valueOf(response[1]), Integer.valueOf(response[2]), profile.getName());
//                    break;
//                // A move for games such as checkers
//                case "move2":
//                    game.makeMove(Integer.valueOf(response[1]), Integer.valueOf(response[2]),Integer.valueOf(response[3]), Integer.valueOf(response[4]), profile.getName());
//                    checkGame();
//                    break;
//                // A move for games such as chutes and ladders
//                case "move3":
//                    game.makeMove(profile.getName());
//                    checkGame();
//                    break;
//                case "description":
//                    profile.SetDescription(response[1]);
//                    break;
//                case "selectGame":
//                    // set up a game for the player here
//                    break;
//                case "newGame":
//                    // set up a new game for players to join here
//                    break;
//            }
//        }
    }
    
    /**
     * Player is leaving the game they're currently in
     */
    public void leaveGame()
    {
    	//TODO: this probably needs more than just this
        game = null;
    }
    
    /**
     * Log the player into the server
     * @param json 
     * @return
     */
    
    private boolean addNewPlayer(){
    	String message = receiveMessage();
    	String[] tokens = JSONServerGeneral.checkType(message);
    	
    	this.profile = new Profile();
    	if(tokens[0].equals("Username") && this.profile.profileExists(tokens[1]))
    		return false;
    	
     	this.profile.createNewProfile(tokens[1]);
    	
    	message = receiveMessage();
    	tokens = JSONServerGeneral.checkType(message);
    	if(tokens[0].equals("Description"))
    		this.profile.SetDescription(tokens[1]);
    	
    	return true;
    }
    
    private boolean loginPlayer(){
    	String message = receiveMessage();
    	String[] tokens = JSONServerGeneral.checkType(message);
    	System.out.println("Logging Player In: " + message);
    	this.profile = new Profile();
    	if(tokens[0].equals("Username") && !this.profile.profileExists(tokens[1]))
    		return false;
    	
    	this.profile.createNewProfile(tokens[1]);
    	return true;
    }
    

    private void goToLobby()
    {
        //needs to push this player thread into the lobby. Just cuz the lobby
        // has a list of players, doesn't mean the thread is running in lobby
        // it's still running in Player and can only run in player.
        lobby.joinLobby(this);
    }

	/**
	 * Send a message to the player socket
	 * @param message the message to be sent (as a JSON string)
	 */
    public void sendMessage(String message)
    {
        try
        {
            output.writeUTF(message);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();

            JOptionPane.showMessageDialog(new JOptionPane(),
                    "Network Connection Error (within Player.sendMessage())",
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * ******************************************************************
     * public String receiveMessage()
     *
     * This method receives the entire JSON string message from a player's
     * client GUI
     *
     *******************************************************************
     */
    public String receiveMessage()
    {
        try
        {
            return input.readUTF();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
        	
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JOptionPane(),
                    "Network Connection Error (within Player.sendMessage())",
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return "";
    }

    /**
     * ******************************************************************
     * public static String initialHandshake()
     *
     * The structure of the message is as follows:
     *
     * <Welcome>
     * "Please send the login info"
     *
     * ------------------------------------------------------------------
     *
     * The intention of this method is to create a JSON message to be used in
     * the intial handshake for when the client first connects to the server.
     *
     *******************************************************************
     */
    private String initialHandshake()
    {
        return JSONServerTranslator.welcomeMessage();
    }

    /**
     * ******************************************************************
     * public static String badLogin()
     *
     * The structure of the message is as follows:
     *
     * <gameServer>
     * <Welcome>
     * "Please send the login info"
     *
     * ------------------------------------------------------------------
     *
     * The intention of this method is to create a JSON message to be used to
     * inform that the username entered is invalid
     *
     *******************************************************************
     */
    private String badLogin()
    {
        return JSONServerTranslator.loginStatus("Failure");
    }
    public void wonGame(String game)
    {
        profile.addWin(game);
    }
    
    public void lostGame(String game)
    {
        profile.addLoss(game);
    }
    
    public String getPlayerName()
    {
    	if (loggedIn)
    		return profile.getName();
    	else
    		return "Logging in...";
    }
}
