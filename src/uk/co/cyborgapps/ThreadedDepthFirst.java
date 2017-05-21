package uk.co.cyborgapps;

import java.util.Stack;

/**
 * Program Title:  ThreadedDepthFirst
 * Author:         sean
 * Created:        5/15/17
 * Bangor username:eeu626
 * Version:        1.0
 */

public class ThreadedDepthFirst extends Thread
{
	private int[][][] board;
	private Stack<ScoreTurns> answers;
	
	ThreadedDepthFirst(int[][][] board, Stack<ScoreTurns> answers)
	{
		this.board = board;
		this.answers = answers;
	}
	
	@Override
	public void run()
	{
		ScoreTurns score =simpleSolver(board);
		if (score != null)
		{
			answers.add(score);
		}
		
	}
	private ScoreTurns simpleSolver(int[][][] board)
	{
		BrickPopBot brickPopBot = new BrickPopBot();
		int[][][] erraseBoard = brickPopBot.boardCopy(board);
		
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				if (interrupted())
				{
					return null;
				}
				if (erraseBoard[i][j][0] != 0)
				{
					int[][][] newBoard = brickPopBot.boardCopy(board);
					
					newBoard = brickPopBot.propagate(newBoard, i, j, newBoard[i][j][0]);
					newBoard = brickPopBot.colapseSpace(newBoard);
					erraseBoard = brickPopBot.propagate(erraseBoard, i, j, erraseBoard[i][j][0]);
					
					if (brickPopBot.allZeros(newBoard))
					{
						ScoreTurns scoreTurns = new ScoreTurns();
						
						int[] move = new int[2];
						move[0] = board[i][j][1];
						move[1] = board[i][j][2];
						scoreTurns.moves.add(move);
						scoreTurns.score = brickPopBot.turnScore(board, i, j);
						scoreTurns.completed = true;
						System.out.println("found solution");
						return scoreTurns;
					}
					else
					{
						if (brickPopBot.numberOfZeros(newBoard) != brickPopBot.numberOfZeros(board))
						{
							if (brickPopBot.noSingleColourCubes(newBoard))
							{
								if (interrupted())
								{
									return null;
								}
								ScoreTurns scoreTurns = simpleSolver(newBoard);
								if (scoreTurns != null)
								{
									int[] returnMoves = new int[2];
									returnMoves[0] = board[i][j][1];
									returnMoves[1] = board[i][j][2];
									scoreTurns.moves.add(returnMoves);
									scoreTurns.score = scoreTurns.score + brickPopBot.turnScore(board, i, j);
									if (scoreTurns.completed)
									{
										return scoreTurns;
									}
								}
								
								
							}
							
						}
					}
					
				}
			}
		}
		
		return null;
	}
	
}
