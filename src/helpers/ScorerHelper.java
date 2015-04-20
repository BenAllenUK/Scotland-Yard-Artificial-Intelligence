package helpers;

import models.DataPosition;
import models.DataSave;
import models.ScoreElement;
import scotlandyard.Colour;
import scotlandyard.Move;
import scotlandyard.ScotlandYardView;
import solution.ScotlandYardMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by benallen on 10/04/15.
 */
public class ScorerHelper {
    private final ScotlandYardView viewController;
	private DataSave mGraphData;
	private ScotlandYardMap gameMap;
	private final DataParser mDataParser;

    public ScorerHelper(ScotlandYardView vc, ScotlandYardMap gm){
        viewController = vc;
        gameMap = gm;
		mDataParser = new DataParser();

		try {
			File file = new File("resources/custom_data");
			mGraphData = mDataParser.loadV3Data(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /**
     * Will produce a list of scores for the current position
     * @param location the current location
     * @param moves the available moves
     * @param currentPlayer the current player
     * @return the element that was tested and its score i.e distance, move availability etc
     */
    public HashMap<ScoreElement, Float> score(int location, Set<Move> moves, Colour currentPlayer, HashMap<Colour, Integer> otherPlayerPositions){

        HashMap<ScoreElement, Float> scoreMap = new HashMap<ScoreElement, Float>();
        // Rating based on distance
        float distanceScore = getDistanceScore(location, currentPlayer, otherPlayerPositions);
        scoreMap.put(ScoreElement.DISTANCE,distanceScore);

        // Rating based on number of available moves
        float movesScore = getMovesScore(moves);
        scoreMap.put(ScoreElement.MOVE_AVAILABILITY, movesScore);

        return scoreMap;
    }

    /**
     * Gets a score for the moves
     * @param moves the future moves
     * @return the score
     */
    public float getMovesScore(Set<Move> moves) {
        return moves.size() / Constants.MAX_CONNECTIONS_PER_NODE;
    }

    /**
     * Will get the distance between players and calculate a score
     * @param location the current testing location
     * @param currentPlayer the current player to test on
     * @return the score for this location
     */
    public float getDistanceScore(int location, Colour currentPlayer, HashMap<Colour, Integer> otherPlayerPositions) {
        float averageDistanceFromTargets;

        // Is this player MRX or not?
        if(currentPlayer != Constants.MR_X_COLOUR){

            // If its a detective then calculate the distance between the player and mrX use this to calculate the score
            ArrayList<DataPosition> distanceBetween = findDistanceBetween(location, otherPlayerPositions.get(Constants.MR_X_COLOUR));

            // If the list of nodes is 0 then the distance must be 0
            if(distanceBetween == null) {
                averageDistanceFromTargets = 0;
            } else {
                averageDistanceFromTargets = distanceBetween.size() - 1;
            }
        } else {
            float averageDistanceFromPlayers = 0.0f;


            // Otherwise calculate the average from mrX and all the other players
            for (Colour player : viewController.getPlayers()) {

                // Avoid checking MrX's distance to himself
                if (player != Constants.MR_X_COLOUR) {

                    // Get their location
                    int playerLocation = otherPlayerPositions.get(player);

                    // Get a list of their nodes
					ArrayList<DataPosition> distanceBetween = findDistanceBetween(location, playerLocation);

                    int distanceBetweenNodes;

                    // If there is no distance then the game is over
                    if (distanceBetween == null) {
                        distanceBetweenNodes = 0;
                    } else {
                        distanceBetweenNodes = distanceBetween.size() - 1;
                    }
                    // Get the distance between the nodes and then add it onto the running total
//					System.out.println("averageDistanceFromPlayers = " + averageDistanceFromPlayers);
					averageDistanceFromPlayers = averageDistanceFromPlayers + distanceBetweenNodes;
                }
            }

//			System.out.println("averageDistanceFromPlayers = " + averageDistanceFromPlayers);
            // Calculate the average

            averageDistanceFromTargets = averageDistanceFromPlayers / (viewController.getPlayers().size() - 1);
        }
        // Calculate score on distance
		return averageDistanceFromTargets / Constants.MAX_DISTANCE_BETWEEN_NODES;
    }

    /**
     * Get a list of nodes between to nodes that are the shortest path
     * @param targetLocation the furthest node
     * @param location the current node
     * @return a list of nodes that form the shortest path between the two nodes
     */
    public ArrayList<DataPosition> findDistanceBetween(int targetLocation, int location){

        // Now calculate the shortest distance
        ArrayList<DataPosition> finalPositions = ShortestPathHelper.shortestPath(location, targetLocation, mGraphData.positionList, mGraphData.pathList);

        // Return the number of paths that this is
        return finalPositions;
    }
}
