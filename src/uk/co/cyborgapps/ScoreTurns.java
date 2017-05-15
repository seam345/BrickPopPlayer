package uk.co.cyborgapps;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Program Title:  ScoreTurns
 * Author:         sean
 * Created:        5/12/17
 * Bangor username:eeu626
 * Version:        1.0
 */

public class ScoreTurns
{
//	public ArrayList<int[]> moves;
	public Stack<int[]> moves = new Stack<>();
	public int score = 0;
	
	ScoreTurns()
	{
		score = 0;
		moves = new Stack<>();
	}
}
