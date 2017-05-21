package uk.co.cyborgapps;

import java.util.Stack;

/**
 * Program Title:  ScoreTurns
 * Author:         sean
 * Created:        5/12/17
 * Bangor username:eeu626
 * Version:        1.0
 */

class ScoreTurns
{
//	public ArrayList<int[]> moves;
	Stack<int[]> moves = new Stack<>();
	int score = 0;
	boolean completed = false;
	
	ScoreTurns()
	{
		score = 0;
		moves = new Stack<>();
		completed = false;
	}
}
