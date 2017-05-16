package uk.co.cyborgapps;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

/**
 * Program Title:  BrickPopBot
 * Author:         sean
 * Created:        5/13/17
 * Bangor username:eeu626
 * Version:        1.0
 */

public class BrickPopBot
{
	static int test = 0;
	static Stack<Thread> threads = new Stack<>();
	static Stack<ScoreTurns> answers = new Stack<>();
	
	public static void main(String[] args) throws AWTException
	{
		for (int s = 0; s < 100; s++)
		{
			ThreadedSolver.setDepth = 0;
			answers = new Stack<>();
			Robot myRobot = new Robot();
			
			
			ArrayList<Integer> colours = new ArrayList<>(10);
			
			
			BufferedImage screen = myRobot.createScreenCapture(new Rectangle(715, 360, 480, 480));
			
			for (int i = 24; i < screen.getHeight(); i = i + 48)
			{
				for (int j = 24; j < screen.getWidth(); j = j + 48)
				{
					boolean pastColour = false;
					int pixelColour = screen.getRGB(j, i);
					
					for (Integer colour : colours)
					{
						if (colour == pixelColour || pixelColour == -528412)
						{
							{
								pastColour = true;
							}
						}
					}
					
					if (!pastColour)
					{
						colours.add(pixelColour);
					}
				}
			}
			
			int[][][] board = new int[10][10][3]; // [x][y][colour,mousex,mousey]
			for (int i = 0; i < board.length; i++)
			{
				for (int j = 0; j < board[i].length; j++)
				{
					for (int k = 0; k < board[i][j].length; k++)
					{
						board[i][j][k] = 0;
					}
				}
			}
			int x;
			int y = 9;
			
			for (int i = 24; i < screen.getHeight(); i = i + 48)
			{
				x = 0;
				for (int j = 24; j < screen.getWidth(); j = j + 48)
				{
					int pixelColour = screen.getRGB(j, i);
					
					for (int k = 0; k < colours.size(); k++)
					{
						if (pixelColour == -528412)
						{
							board[x][y][0] = 0;
							board[x][y][1] = j;
							board[x][y][2] = i;
						}
						if (pixelColour == colours.get(k))
						{
							board[x][y][0] = k + 1;
							board[x][y][1] = j;
							board[x][y][2] = i;
						}
					}
					x++;
				}
				y--;
			}
			
			
			BrickPopBot brickPopBot = new BrickPopBot();
			
			Long startTime = System.currentTimeMillis();
			brickPopBot.multithreadedSolver(board, new LinkedList<int[]>(), 0, 0);
			ScoreTurns currentBestScore = new ScoreTurns();
			System.out.printf("starting threads = %d \n", Thread.getAllStackTraces().keySet().size());
			int waitLength = 2000;
			
			ThreadedDepthFirst threadedDepthFirst = new ThreadedDepthFirst(brickPopBot.boardCopy(board), answers);
			threadedDepthFirst.start();
			while (startTime + waitLength > System.currentTimeMillis())
			{
				switch (ThreadedSolver.setDepth)
				{
					case 4:
					{
						waitLength = 5000;
						break;
					}
					case 5:
					{
						waitLength = 15000;
						break;
					}
					case 6:
					{
						waitLength = 20000;
						break;
					}
					case 7:
					{
						waitLength = 25000;
						break;
					}
					case 8:
					{
						waitLength = 30000;
						break;
					}
					case 9:
					{
						waitLength = 35000;
						break;
					}
				}
				if (ThreadedSolver.setDepth > 4)
				{
					boolean alive = false;
					Stack<Thread> copyThreads = new Stack<>();
					copyThreads.addAll(threads);
					for (Thread popped : copyThreads)
					{
						if (popped.isAlive())
						{
							alive = true;
							break;
						}
					}
					if (!alive)
					{
						waitLength=0;
					}
				}
				
				try
				{
					ScoreTurns popped = answers.pop();
					if (currentBestScore.compleated)
					{
						if (popped.compleated)
						{
							if (popped.score > currentBestScore.score)
							{
								currentBestScore = popped;
							}
						}
					}
					else
					{
						if (popped.compleated)
						{
							currentBestScore = popped;
							
						}
						else if (popped.score > currentBestScore.score)
						{
							currentBestScore = popped;
						}
					}
					
					
				} catch (EmptyStackException e)
				{
					try
					{
						Thread.sleep(10);
					} catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
				}
			}
			System.out.printf("ending threads = %d \n", Thread.getAllStackTraces().keySet().size());
			
			for (Thread popped : threads)
			{
				if (popped != null && popped.isAlive())
				{
					popped.interrupt();
				}
			}
			
			for (ScoreTurns score : answers)
			{
				if (currentBestScore.compleated)
				{
					if (score.compleated)
					{
						if (score.score > currentBestScore.score)
						{
							currentBestScore = score;
						}
					}
				}
				else
				{
					if (score.compleated)
					{
						currentBestScore = score;
						
					}
					else if (score.score > currentBestScore.score)
					{
						currentBestScore = score;
					}
				}
				
			}
			threadedDepthFirst.interrupt();
			System.out.printf("after purge threads = %d \n", Thread.getAllStackTraces().keySet().size());
			
			
			//			ScoreTurns[][] moves = brickPopBot.bestMoves(board, 0, 10);
			//			int[] point = brickPopBot.maxScore(moves);
			//			Stack<int[]> movesStack = moves[point[0]][point[1]].moves;
			//
			//			Stack<int[]> movesStack = brickPopBot.simpleSolver(board).moves;
			
			Stack<int[]> movesStack = currentBestScore.moves;
			int iteratons = movesStack.size();
			for (int i = 0; i < iteratons; i++)
			{
				int[] pos = movesStack.pop();
				
				myRobot.mouseMove(715 + pos[0], 360 + pos[1]);
				myRobot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				myRobot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				
				
				System.out.printf("%d %d \n", pos[1], pos[0]);
				System.out.println("click");
				if (i < iteratons - 1)
				{
					try
					{
						Thread.sleep(2500);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				
				
			}
			try
			{
				if (currentBestScore.compleated)
				{
					Thread.sleep(30000);
				}
				else
				{
					Thread.sleep(2500);
				}
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	ScoreTurns simpleSolver(int[][][] board)
	{
		ScoreTurns score = new ScoreTurns();
		int[][][] erraseBoard = boardCopy(board);
		
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				if (erraseBoard[i][j][0] != 0)
				{
					int[][][] newBoard = boardCopy(board);
					
					newBoard = propogate(newBoard, i, j, newBoard[i][j][0]);
					newBoard = colapseSpace(newBoard);
					erraseBoard = propogate(erraseBoard, i, j, erraseBoard[i][j][0]);
					
					if (allZeros(newBoard))
					{
						ScoreTurns scoreTurns = new ScoreTurns();
						
						int[] move = new int[2];
						move[0] = board[i][j][1];
						move[1] = board[i][j][2];
						scoreTurns.moves.add(move);
						scoreTurns.score = turnScore(board, i, j) + Integer.MAX_VALUE;
						if (scoreTurns.score < 0)
						{
							scoreTurns.score = Integer.MAX_VALUE;
						}
						
						return scoreTurns;
					}
					else
					{
						if (numberOfZeros(newBoard) == numberOfZeros(board))
						{
						
						}
						else
						{
							if (noSingleColourCubes(newBoard))
							{
								
								ScoreTurns scoreTurns = simpleSolver(newBoard);
								if (scoreTurns != null)
								{
									int[] returnMoves = new int[2];
									returnMoves[0] = board[i][j][1];
									returnMoves[1] = board[i][j][2];
									scoreTurns.moves.add(returnMoves);
									
									if (scoreTurns.score == Integer.MAX_VALUE)
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
	
	boolean multithreadedSolver(int[][][] board, LinkedList<int[]> moves, int currentScore, int depth)
	{
		int[][][] threadBoard = boardCopy(board);
		LinkedList<int[]> threadMoves = movesCopy(moves);
		
		
		Thread threadedSolver = new ThreadedSolver(threadBoard, threadMoves, currentScore, answers, depth);
		threadedSolver.start();
		threads.add(threadedSolver);
		return threadedSolver.isAlive();
		
	}
	
	LinkedList<int[]> movesCopy(LinkedList<int[]> moves)
	{
		ListIterator<int[]> iterator = moves.listIterator(0);
		LinkedList<int[]> copy = new LinkedList<>();
		while (iterator.hasNext())
		{
			int[] move = iterator.next();
			int[] moveCopy = {move[0], move[1]};
			copy.add(moveCopy);
		}
		
		return copy;
	}
	
	Stack<int[]> listToStack(LinkedList<int[]> moves)
	{
		Iterator<int[]> iterator = moves.descendingIterator();
		Stack<int[]> copy = new Stack<>();
		while (iterator.hasNext())
		{
			int[] move = iterator.next();
			int[] moveCopy = {move[0], move[1]};
			copy.add(moveCopy);
		}
		
		return copy;
	}
	
	//todo new plan have a kind of simple solver implementation that returns one solution in a parralel way i will
	//todo process all of the possible outcomes these shall be sent to a timer thread of the score and a flag if it compleated
	//todo flag beats any score and each list of moves is replaced if a score is better so a 10 relaces a 1 but not if flaged as complete
	//todo once the timer ends all child process will be killed and the current moves will be executed if not a compleate set of moves are set then it doesnt matter
	//todo because the next iteration will start again where the last left off
	ScoreTurns[][] bestMoves(int[][][] board, int depth, int maxDepth)
	{
		if (depth == maxDepth)
		{
			ScoreTurns[][] scores = new ScoreTurns[10][10];
			for (int i = 0; i < 10; i++)
			{
				for (int j = 0; j < 10; j++)
				{
					scores[i][j] = new ScoreTurns();
				}
			}
			int[][][] erraseBoard = boardCopy(board);
			
			int numberOfBranches = 0;
			for (int i = 0; i < 10; i++)
			{
				for (int j = 0; j < 10; j++)
				{
					if (erraseBoard[i][j][0] != 0)
					{
						int[][][] newBoard = boardCopy(board);
						
						newBoard = propogate(newBoard, i, j, newBoard[i][j][0]);
						newBoard = colapseSpace(newBoard);
						erraseBoard = propogate(erraseBoard, i, j, erraseBoard[i][j][0]);
						
						if (allZeros(newBoard))
						{
							ScoreTurns scoreTurns = new ScoreTurns();
							
							int[] move = new int[2];
							move[0] = board[i][j][1];
							move[1] = board[i][j][2];
							scoreTurns.moves.add(move);
							scoreTurns.score = turnScore(board, i, j) + Integer.MAX_VALUE;
							if (scoreTurns.score < 0)
							{
								scoreTurns.score = Integer.MAX_VALUE;
							}
							scores[i][j] = scoreTurns;
							return scores;
						}
						else
						{
							if (numberOfZeros(newBoard) == numberOfZeros(board))
							{
								ScoreTurns scoreTurns = new ScoreTurns();
								
								int[] move = new int[2];
								move[0] = board[i][j][1];
								move[1] = board[i][j][2];
								scoreTurns.moves.add(move);
								scoreTurns.score = 0;
								scores[i][j] = scoreTurns;
							}
							else
							{
								//todo could remove branch by crossing off if the new board has only 1 of a colour as i never want a move that does that
								if (noSingleColourCubes(newBoard))
								{
									numberOfBranches++;
									ScoreTurns scoreTurns = new ScoreTurns();
									
									
									int[] returnMoves = new int[2];
									returnMoves[0] = board[i][j][1];
									returnMoves[1] = board[i][j][2];
									scoreTurns.moves.add(returnMoves);
									scoreTurns.score = scoreTurns.score + turnScore(board, i, j);
									if (scoreTurns.score < 0)
									{
										scoreTurns.score = Integer.MAX_VALUE;
									}
									scores[i][j] = scoreTurns;
								}
								else
								{
									ScoreTurns scoreTurns = new ScoreTurns();
									
									int[] move = new int[2];
									move[0] = board[i][j][1];
									move[1] = board[i][j][2];
									scoreTurns.moves.add(move);
									scoreTurns.score = 0;
									scores[i][j] = scoreTurns;
								}
								
							}
						}
						
					}
				}
			}
			return scores;
		}
		ScoreTurns[][] scores = new ScoreTurns[10][10];
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				scores[i][j] = new ScoreTurns();
			}
		}
		int[][][] erraseBoard = boardCopy(board);
		
		int numberOfBranches = 0;
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				if (erraseBoard[i][j][0] != 0)
				{
					int[][][] newBoard = boardCopy(board);
					
					newBoard = propogate(newBoard, i, j, newBoard[i][j][0]);
					newBoard = colapseSpace(newBoard);
					erraseBoard = propogate(erraseBoard, i, j, erraseBoard[i][j][0]);
					
					if (allZeros(newBoard))
					{
						ScoreTurns scoreTurns = new ScoreTurns();
						
						int[] move = new int[2];
						move[0] = board[i][j][1];
						move[1] = board[i][j][2];
						scoreTurns.moves.add(move);
						scoreTurns.score = turnScore(board, i, j) + Integer.MAX_VALUE;
						if (scoreTurns.score < 0)
						{
							scoreTurns.score = Integer.MAX_VALUE;
						}
						scores[i][j] = scoreTurns;
						//						return scores; // cuts the rest of the branch for speed but wont get highest score saying that nor does setting it to integer.max
						
					}
					else
					{
						if (numberOfZeros(newBoard) == numberOfZeros(board))
						{
							ScoreTurns scoreTurns = new ScoreTurns();
							
							int[] move = new int[2];
							move[0] = board[i][j][1];
							move[1] = board[i][j][2];
							scoreTurns.moves.add(move);
							scoreTurns.score = 0;
							scores[i][j] = scoreTurns;
						}
						else
						{
							if (noSingleColourCubes(newBoard))
							{
								numberOfBranches++;
								ScoreTurns[][] scoreTurns = bestMoves(newBoard, depth + 1, maxDepth);
								int[] temp = maxScore(scoreTurns);
								
								int[] returnMoves = new int[2];
								returnMoves[0] = board[i][j][1];
								returnMoves[1] = board[i][j][2];
								scoreTurns[temp[0]][temp[1]].moves.add(returnMoves);
								scoreTurns[temp[0]][temp[1]].score = scoreTurns[temp[0]][temp[1]].score + turnScore(board, i, j);
								if (scoreTurns[temp[0]][temp[1]].score < 0)
								{
									scoreTurns[temp[0]][temp[1]].score = Integer.MAX_VALUE;
								}
								scores[i][j] = scoreTurns[temp[0]][temp[1]];
							}
							else // dont want this branch as it results in a cube of only 1 colour being left
							{
								ScoreTurns scoreTurns = new ScoreTurns();
								
								int[] move = new int[2];
								move[0] = board[i][j][1];
								move[1] = board[i][j][2];
								scoreTurns.moves.add(move);
								scoreTurns.score = 0;
								scores[i][j] = scoreTurns;
							}
							

							/*test++;
							if(test%1000 ==0)
							{
								System.out.printf("%d %d\n", test, depth);
							}*/
							//
						}
					}
					
				}
			}
		}
		
		System.out.printf("%d %d \n", numberOfBranches, depth);
		
		
		return scores;
	}
	
	
	boolean noSingleColourCubes(int[][][] board)
	{
		ArrayList<int[]> occorances = new ArrayList<>(3);
		for (int i = 0; i < board.length; i++)
		{
			for (int j = 0; j < board[i].length; j++)
			{
				if (board[i][j][0] != 0)
				{
					boolean found = false;
					for (int[] occurrence : occorances)
					{
						if (occurrence[0] == board[i][j][0])
						{
							found = true;
							occurrence[1]++;
						}
					}
					if (!found)
					{
						int[] adding = new int[2];
						adding[0] = board[i][j][0];
						adding[1] = 1;
						occorances.add(adding);
					}
				}
			}
		}
		for (int[] occurrence : occorances)
		{
			if (occurrence[1] == 1)
			{
				return false;
			}
		}
		
		return true;
	}
	
	int[] maxScore(ScoreTurns[][] scores)
	{
		int[] point = {0, 0};
		
		for (int i = 0; i < scores.length; i++)
		{
			for (int j = 0; j < scores[i].length; j++)
			{
				if (scores[i][j].score > scores[point[0]][point[1]].score)
				{
					point[0] = i;
					point[1] = j;
				}
			}
		}
		return point;
	}
	
	boolean allZeros(int[][][] board)
	{
		for (int i = 0; i < board.length; i++)
		{
			for (int j = 0; j < board[i].length; j++)
			{
				
				if (board[i][j][0] != 0)
				{
					return false;
				}
				
			}
		}
		return true;
	}
	
	int numberOfZeros(int[][][] board)
	{
		int zeros = 0;
		for (int i = 0; i < board.length; i++)
		{
			for (int j = 0; j < board[i].length; j++)
			{
				
				if (board[i][j][0] == 0)
				{
					zeros++;
				}
				
			}
		}
		return zeros;
	}
	
	int turnScore(int[][][] board, int xplay, int yplay)
	{
		int score = 1;
		int oldZeros = numberOfZeros(board);
		
		
		int[][][] newBoard = boardCopy(board);
		
		
		newBoard = propogate(newBoard, xplay, yplay, board[xplay][yplay][0]);
		int newZeros = numberOfZeros(newBoard);
		
		
		int removed = newZeros - oldZeros;
		for (int i = 0; i < removed; i++)
		{
			score++;
		}
		return score;
	}
	
	int[][][] propogate(int[][][] oldBoard, int x, int y, int colour) //direction 0=up 1=right 2=down 3=left
	{
		int[][][] board = boardCopy(oldBoard);
		if (colour != 0)
		{
			if (y != 9 && board[x][y + 1][0] == colour)
			{
				board[x][y + 1][0] = 0;
				board = propogate(board, x, y + 1, colour);
			}
			if (x != 9 && board[x + 1][y][0] == colour)
			{
				board[x + 1][y][0] = 0;
				board = propogate(board, x + 1, y, colour);
			}
			if (y != 0 && board[x][y - 1][0] == colour)
			{
				board[x][y - 1][0] = 0;
				board = propogate(board, x, y - 1, colour);
			}
			if (x != 0 && board[x - 1][y][0] == colour)
			{
				board[x - 1][y][0] = 0;
				board = propogate(board, x - 1, y, colour);
			}
		}
		
		return board;
	}
	
	int[][][] colapseSpace(int[][][] oldBoard)
	{
		int[][][] board = boardCopy(oldBoard);
		
		for (int x = 0; x < 10; x++)
		{
			for (int y = 9; y >= 0; y--)
			{
				if (board[x][y][0] == 0)
				{
					for (int i = y; i < 9; i++)
					{
						board[x][i][0] = board[x][i + 1][0];
						board[x][i + 1][0] = 0;
						
					}
				}
			}
		}
		
		for (int x = 8; x >= 0; x--)
		{
			if (board[x][0][0] == 0)
			{
				for (int i = x; i < 9; i++)
				{
					for (int y = 0; y < 10; y++)
					{
						board[i][y][0] = board[i + 1][y][0];
						board[i + 1][y][0] = 0;
					}
				}
				
			}
		}
		return board;
	}
	
	int[][][] boardCopy(int[][][] board)
	{
		int[][][] newBoard = new int[10][10][3]; // cheating should make them all length
		for (int i = 0; i < board.length; i++)
		{
			for (int j = 0; j < board[i].length; j++)
			{
				for (int k = 0; k < board[i][j].length; k++)
				{
					newBoard[i][j][k] = board[i][j][k];
				}
			}
		}
		return newBoard;
	}
	
}

