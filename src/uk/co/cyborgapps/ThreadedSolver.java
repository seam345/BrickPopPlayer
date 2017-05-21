package uk.co.cyborgapps;

import java.util.LinkedList;
import java.util.Stack;

/**
 * Program Title:  ThreadedSolver
 * Author:         sean
 * Created:        5/15/17
 * Bangor username:eeu626
 * Version:        1.0
 */

public class ThreadedSolver extends Thread
{
	private int[][][] board;
	private LinkedList<int[]> threadMoves ;
	private int currentScore;
	private Stack<ScoreTurns> answers;
	int depth;
	static int setDepth =0;
	
	ThreadedSolver(int[][][] threadBoard, LinkedList<int[]> threadMoves, int currentScore, Stack<ScoreTurns> answers, int depth)
	{
		this.board = threadBoard;
		this.threadMoves = threadMoves;
		this.currentScore = currentScore;
		this.answers = answers;
		this.depth = depth;
	}
	
	
	
	@Override
	public void run()
	{
		BrickPopBot brickPopBot = new BrickPopBot();
		int[][][] eraseBoard = brickPopBot.boardCopy(board);
		int nbThreads =  Thread.getAllStackTraces().keySet().size();
		while(nbThreads > 50)
		{
		/*	if (depth>setDepth)
			{
				System.out.println(setDepth);
				setDepth= depth;
			}*/
			try
			{
				Thread.sleep(2*depth);
			} catch (InterruptedException e)
			{
				return;
			}
		}
		
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				if (interrupted())
				{
					return;
				}
				if (eraseBoard[i][j][0] != 0)
				{
					int[][][] newBoard = brickPopBot.boardCopy(board);
					
					newBoard = brickPopBot.propogate(newBoard, i, j, newBoard[i][j][0]);
					newBoard = brickPopBot.colapseSpace(newBoard);
					
					eraseBoard = brickPopBot.propogate(eraseBoard, i, j, eraseBoard[i][j][0]);
					
					if (brickPopBot.allZeros(newBoard))
					{
						ScoreTurns scoreTurns = new ScoreTurns();
						
						int[] move = new int[2];
						move[0] = board[i][j][1];
						move[1] = board[i][j][2];
						scoreTurns.moves.add(move);
						scoreTurns.score = brickPopBot.turnScore(board, i, j);
						scoreTurns.moves.addAll(brickPopBot.listToStack(threadMoves));
						scoreTurns.compleated = true;
						answers.add(scoreTurns);
						System.out.println("found solution, in breadth first");
					}
					else
					{
						if (brickPopBot.numberOfZeros(newBoard) == brickPopBot.numberOfZeros(board))
						{
							
//							System.out.printf("%d %d\n",i,j);
						}else
						{
							if (brickPopBot.noSingleColourCubes(newBoard))
							{
								ScoreTurns scoreTurns = new ScoreTurns();
								
								int[] move = new int[2];
								move[0] = board[i][j][1];
								move[1] = board[i][j][2];
								scoreTurns.moves.add(move);
								
								int additionalScore = brickPopBot.turnScore(board, i, j);
								scoreTurns.score = currentScore+additionalScore;
								scoreTurns.moves.addAll(brickPopBot.listToStack(threadMoves));
								
								answers.add(scoreTurns);
								
								LinkedList<int[]> pasableLinkedList = new LinkedList<>();
								pasableLinkedList.addAll(threadMoves);
								pasableLinkedList.add(move);
								
								if (interrupted())
								{
									return;
								}
								brickPopBot.multithreadedSolver(newBoard,pasableLinkedList,currentScore+additionalScore, depth+1);
							}
						}
					}
					
				}
			}
		}
		
		
	}
}
