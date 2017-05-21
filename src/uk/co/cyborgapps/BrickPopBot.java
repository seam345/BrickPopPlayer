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
	private static Stack<Thread> threads = new Stack<>();
	private static Stack<ScoreTurns> answers = new Stack<>();
	
	public static void main(String[] args) throws AWTException
	{
		for (int s = 0; s < 100; s++)
		{
			ThreadedSolver.setDepth = 0;
			answers = new Stack<>();
			Robot myRobot = new Robot();
			
			
			BrickPopBot brickPopBot = new BrickPopBot();
			
			
			int[][][] board = brickPopBot.getBoard();
			
			
			Long startTime = System.currentTimeMillis();
			brickPopBot.multithreadedSolver(board, new LinkedList<>(), 0, 0);
			ScoreTurns currentBestScore = new ScoreTurns();
			ScoreTurns bestUnsolvedScore = new ScoreTurns();
			System.out.printf("starting threads = %d \n", Thread.getAllStackTraces().keySet().size());
			int waitLength = 2000;
			
			
			ThreadedDepthFirst threadedDepthFirst = new ThreadedDepthFirst(brickPopBot.boardCopy(board), answers);
			threadedDepthFirst.start();
			
			while (startTime + waitLength > System.currentTimeMillis())
			{
				try
				{
					ScoreTurns popped = answers.pop();
					if (currentBestScore.completed)
					{
						if (popped.completed)
						{
							if (popped.score > currentBestScore.score)
							{
								currentBestScore = popped;
							}
						}
					}
					else
					{
						if (popped.completed)
						{
							currentBestScore = popped;
							
						}
						else if (popped.score > currentBestScore.score)
						{
							currentBestScore = popped;
							bestUnsolvedScore = popped;
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
			
//			System.out.printf("before purge threads =\t%d \n", Thread.getAllStackTraces().keySet().size());
			
			for (Thread popped : threads)
			{
				if (popped != null && popped.isAlive())
				{
					popped.interrupt();
				}
			}
			threadedDepthFirst.interrupt();
			
			for (ScoreTurns score : answers)
			{
				if (currentBestScore.completed)
				{
					if (score.completed)
					{
						if (score.score > currentBestScore.score)
						{
							currentBestScore = score;
						}
					}
				}
				else
				{
					if (score.completed)
					{
						currentBestScore = score;
						
					}
					else if (score.score > currentBestScore.score)
					{
						currentBestScore = score;
						bestUnsolvedScore = score;
					}
				}
				
			}
			
//			System.out.printf("after purge threads =\t%d \n", Thread.getAllStackTraces().keySet().size());
			System.out.println("sorted score =\t"+ currentBestScore.score);
			System.out.println("unsorted score  =\t"+ bestUnsolvedScore.score);
			
			if (bestUnsolvedScore.score != 0 && bestUnsolvedScore.score/ bestUnsolvedScore.moves.size()  > currentBestScore.score/ currentBestScore.moves.size())
			{
				System.out.println("checking unsolved");
				Stack<int[]> moves = new Stack<>();
				moves.addAll(bestUnsolvedScore.moves);
				board = brickPopBot.getBoard();
				int iterations = moves.size();
				for (int i = 0; i < iterations; i++)
				{
					int[] pos = moves.pop();
					
					//9- for y because y was read from the top but i put it into the array at the bottom
					board = brickPopBot.propagate(board, (pos[0]-24)/48, (9-((pos[1]-24)/48)), board[(pos[0]-24)/48][ (9-((pos[1]-24)/48))][0]);
					board = brickPopBot.colapseSpace(board);
					
					
				}
				
				threadedDepthFirst = new ThreadedDepthFirst(board,answers);
				threadedDepthFirst.start();
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				
				threadedDepthFirst.interrupt();
				if (!answers.empty())
				{
					System.out.println("selecting unsolved");
					currentBestScore = bestUnsolvedScore;
				}else
				{
					System.out.println("not selected unsolved");
				}
			}
			
			
			Stack<int[]> movesStack = currentBestScore.moves;
			int iteratons = movesStack.size();
			if (currentBestScore.completed)
			{
				if (iteratons > 5)
				{
					iteratons = 3;
				}
			}
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
				if (currentBestScore.completed && movesStack.empty())
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
			System.out.printf("\n\n\n\n\n\n\n\n");
		}
		
		
		
	}
	
	private int[][][] getBoard() throws AWTException
	{
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
		return board;
	}
	
	void multithreadedSolver(int[][][] board, LinkedList<int[]> moves, int currentScore, int depth)
	{
		int[][][] threadBoard = boardCopy(board);
		LinkedList<int[]> threadMoves = movesCopy(moves);
		
		
		Thread threadedSolver = new ThreadedSolver(threadBoard, threadMoves, currentScore, answers, depth);
		threadedSolver.start();
		threads.add(threadedSolver);
		threadedSolver.isAlive();
		
	}
	
	private LinkedList<int[]> movesCopy(LinkedList<int[]> moves)
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
	
	
	boolean noSingleColourCubes(int[][][] board)
	{
		ArrayList<int[]> occorances = new ArrayList<>(3);
		for (int[][] aBoard : board)
		{
			for (int[] anABoard : aBoard)
			{
				if (anABoard[0] != 0)
				{
					boolean found = false;
					for (int[] occurrence : occorances)
					{
						if (occurrence[0] == anABoard[0])
						{
							found = true;
							occurrence[1]++;
						}
					}
					if (!found)
					{
						int[] adding = new int[2];
						adding[0] = anABoard[0];
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
	
	boolean allZeros(int[][][] board)
	{
		for (int[][] aBoard : board)
		{
			for (int[] anABoard : aBoard)
			{
				
				if (anABoard[0] != 0)
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
		for (int[][] aBoard : board)
		{
			for (int[] anABoard : aBoard)
			{
				
				if (anABoard[0] == 0)
				{
					zeros++;
				}
				
			}
		}
		return zeros;
	}
	
	int turnScore(int[][][] board, int xplay, int yplay)
	{
		int score ;
		int oldZeros = numberOfZeros(board);
		
		
		int[][][] newBoard = boardCopy(board);
		
		
		newBoard = propagate(newBoard, xplay, yplay, board[xplay][yplay][0]);
		int newZeros = numberOfZeros(newBoard);
		
		
		int removed = newZeros - oldZeros;
		score = removed * (removed-1);
//
		return score;
	}
	
	int[][][] propagate(int[][][] oldBoard, int x, int y, int colour) //direction 0=up 1=right 2=down 3=left
	{
		int[][][] board = boardCopy(oldBoard);
		if (colour != 0)
		{
			if (y != 9 && board[x][y + 1][0] == colour)
			{
				board[x][y + 1][0] = 0;
				board = propagate(board, x, y + 1, colour);
			}
			if (x != 9 && board[x + 1][y][0] == colour)
			{
				board[x + 1][y][0] = 0;
				board = propagate(board, x + 1, y, colour);
			}
			if (y != 0 && board[x][y - 1][0] == colour)
			{
				board[x][y - 1][0] = 0;
				board = propagate(board, x, y - 1, colour);
			}
			if (x != 0 && board[x - 1][y][0] == colour)
			{
				board[x - 1][y][0] = 0;
				board = propagate(board, x - 1, y, colour);
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
				System.arraycopy(board[i][j], 0, newBoard[i][j], 0, board[i][j].length);
			}
		}
		return newBoard;
	}
	
}

