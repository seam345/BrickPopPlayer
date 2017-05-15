package uk.co.cyborgapps;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Main
{
	static int test = 0;
	public static void main(String[] args) throws AWTException, IOException
	{
		
		Robot myRobot = new Robot();
		
		
		int colourCount = 0;
		ArrayList<Integer> colours = new ArrayList<>(10);
		ArrayList<int[]> positions = new ArrayList<>(10);
		
		
		BufferedImage screen = myRobot.createScreenCapture(new Rectangle(715, 360, 480, 480));
		
		for (int i = 24; i < screen.getHeight() - 21; i = i + 48)
		{
			for (int j = 24; j < screen.getWidth() - 21; j = j + 48)
			{
				boolean pastColour = false;
				int pixelColour = screen.getRGB(j, i);
				
				for (Integer colour : colours)
				{
					if (colour == pixelColour && pixelColour != -528412)
					{
						{
							pastColour = true;
						}
					}
				}
				
				if (!pastColour)
				{
					colours.add(pixelColour);
					int[] pos = {i, j};
					positions.add(pos);
					colourCount++;
				}
			}
		}
		
		int[][][] board = new int[10][10][3]; // [x][y][colour,mousex,mousey]
		int x;
		int y = 9;
		
		for (int i = 24; i < screen.getHeight() - 21; i = i + 48)
		{
			x = 0;
			for (int j = 24; j < screen.getWidth() - 21; j = j + 48)
			{
				int pixelColour = screen.getRGB(j, i);
				
				for (int k = 0; k < colours.size(); k++)
				{
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
		
		
		Main main = new Main();
		
		//				System.out.println(main.turnScore(board,2,9));
		
		//				board = main.simulateTurn(board,1,9);
		
		ScoreTurns[][] moves = main.bestMoves(board);
		for (int i = 9; i > -0; i--)
		{
			for (int j = 0; j < 10; j++)
			{
				if(moves[i][j] == null)
				{
					System.out.println(i);
					System.out.println(j);
					System.out.println();
				}
			}
			System.out.println();
		}
		int[] point = main.maxScore(moves);
		
		
		
		Stack<int[]> movesStack = moves[point[0]][point[1]].moves;
		
		for (int i = 0; i < movesStack.size(); i++)
		{
			int[] pos = movesStack.pop();
			myRobot.mouseMove(715 + pos[0], 360 + pos[1]);
			
//			myRobot.mouseMove(360 + pos[1], 715 + pos[0]);
			System.out.printf("%d %d \n", pos[1], pos[0]);
			System.out.println("click");
			try
			{
				Thread.sleep(50);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
		}
		
		System.out.println(moves[point[0]][point[1]].score);
		
		for (int i = 9; i > -0; i--)
		{
			for (int j = 0; j < 10; j++)
			{
				System.out.printf("%d, ", moves[j][i].score);
//				System.out.println(moves[i][j].score);
			}
			System.out.println();
		}
		System.out.println();
		for (int i = 9; i > -0; i--)
		{
			for (int j = 0; j < 10; j++)
			{
				System.out.printf("%d, ", board[j][i][0]);
			}
			System.out.println();
		}
		
		/*
		System.out.println(colours);
		System.out.println(colourCount);
		
		for (int[] pos : positions)
		{
			myRobot.mouseMove(715 + pos[1], 360 + pos[0]);
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}*/
		
		
	}
	
	ScoreTurns[][] bestMoves(int[][][] board)
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
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				if (erraseBoard[i][j][0] != 0)
				{
					int[][][] newBoard = boardCopy(board);
					newBoard = simulateTurn(newBoard, i, j, board[i][j][0]);
					erraseBoard = propogate(erraseBoard,i,j, erraseBoard[i][j][0]);
					
					if (allZeros(newBoard))
					{
						ScoreTurns scoreTurns = new ScoreTurns();
						
						int[] move = new int[2];
						move[0] = board[i][j][1];
						move[1] = board[i][j][2];
						scoreTurns.moves.add(move);
						scoreTurns.score = turnScore(board, i, j);
						scores[i][j] = scoreTurns;
						//						return scoreTurns;
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
							scoreTurns.score = turnScore(board, i, j) +scoreTurns.score;
							scores[i][j] = scoreTurns;
							//							return scoreTurns;
						}
						else
						{
							System.out.println();
							for (int a= 9; a > -0; a--)
							{
								for (int s = 0; s < 10; s++)
								{
									System.out.printf("%d, ", newBoard[s][a][0]);
								}
								System.out.println();
							}
							//							ScoreTurns[][] scoreTurns = bestMoves(newBoard);
							//							int[] temp = maxScore(scoreTurns);
							//
							//							int[] returnMoves = new int[2];
							//							returnMoves[0] = i;
							//							returnMoves[1] = j;
							//							scoreTurns[temp[0]][temp[1]].moves.add(returnMoves);
							//							scoreTurns[temp[0]][temp[1]].score = scoreTurns[temp[0]][temp[1]].score + turnScore(board, i, j);
							//							scores[i][j] = scoreTurns[temp[0]][temp[1]];
							//							return scoreTurns;
							
							test++;
							System.out.println(test);
						}
					}
					
				}
			}
		}
		
		
		return scores;
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
			score = score + score;
		}
		return score;
	}
	
	int[][][] simulateTurn(int[][][] board, int xplay, int yplay, int colour)
	{
//		int colour = board[xplay][yplay][0];
		
		
		int[][][] newBoard = boardCopy(board);
		newBoard =propogate(newBoard, xplay, yplay, colour);
		newBoard = colapseSpace(newBoard);
		return newBoard;
		
		
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
	
	int[][][] propogate(int[][][] oldBoard, int x, int y, int colour) //direction 0=up 1=right 2=down 3=left
	{
		int[][][] board = boardCopy(oldBoard);
		if(colour != 0)
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
			for (int y = 0; y < 9; y++)
			{
				if (board[x][y][0] == 0)
				{
					
						board[x][y][0] = board[x][y + 1][0];
					
					board[x][y + 1][0] = 0;
					
				}
			}
			
		}
		
		for (int x = 0; x < 9; x++)
		{
			
			if (board[x][0][0] == 0)
			{
				
					for (int y = 0; y < 10; y++)
					{
						board[x][y][0] = board[x + 1][y][0];
						board[x + 1][y][0] = 0;
					}
					
				
			}
			
			
		}
		return board;
	}
	
	
}
