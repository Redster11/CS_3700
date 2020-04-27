import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import mpi.*;
class RPS //this is to get score from playing against one opponent
{
	private int[] computer;
	private int[] opponent1;
	private int[] opponent2;
	private int rank;
	private int games;
	public RPS(int numGames, int[] computer, int[] opponent1, int[] opponent2, int rank)
	{
		this.computer = computer;
		this.opponent1 = opponent1;
		this.opponent2 = opponent2;
		this.rank = rank;
		this.games = numGames;
	}
	public int play()
	{
		/*
			0 = rock
			1 = paper
			2 = scissors
		*/
		int score = 0;
		for(int i = 0; i < this.games; i++)
		{
			if(this.computer[i]+this.opponent1[i]+this.opponent2[i] == 3)
			{
				//this means that all people win therefore no one wins or it is a tie which is still no points
				continue;
			}
			else if((this.computer[i] == 0 && this.opponent1[i] == 2)||(this.computer[i] == 1 && this.opponent1[i] == 0)||(this.computer[i] == 2 && this.opponent1[i] == 1))
			{
				System.out.print("Process-"+rank+" has won with the choice ");
				if(this.computer[i] == 0)
				{
					System.out.println("rock");
				}
				else if(this.computer[i] == 1)
				{
					System.out.println("paper");
				}
				else
				{
					System.out.println("scissors");
				}
				score += 1;
			}
			if((this.computer[i] == 0 && this.opponent2[i] == 2)||(this.computer[i] == 1 && this.opponent2[i] == 0)||(this.computer[i] == 2 && this.opponent2[i] == 1))
			{
				System.out.print("Process-"+rank+" has won with the choice ");
				if(this.computer[i] == 0)
				{
					System.out.println("rock");
				}
				else if(this.computer[i] == 1)
				{
					System.out.println("paper");
				}
				else
				{
					System.out.println("scissors");
				}
				score += 1;
			}
			else
			{
				System.out.print("process-"+rank+" has Lost with the choice ");
				if(this.computer[i] == 0)
				{
					System.out.println("rock");
				}
				else if(this.computer[i] == 1)
				{
					System.out.println("paper");
				}
				else
				{
					System.out.println("scissors");
				}
			}
		}
		return score;
	}
	
}
class RPS_Player
{
	private int[] choices;
	public RPS_Player(int games)
	{
		choices = new int[games];
		for(int i = 1; i < games; i++)
		{
			Random rand = new Random();
			this.choices[i] = rand.nextInt(3);
		}
	}
	public int[] getChoices()
	{
		return this.choices;
	}
}
public class RockPaperScissors
{
	public static void main(String args[]) throws Exception
	{
		MPI.Init(args);
		int process_rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		//Scanner kb = new Scanner(System.in);
		int numGames = 30;
		//if(process_rank == 0)
		//{
		//	System.out.println("Please enter the amount of games to play");
		//	numGames = kb.nextInt();
		//}	
		int Player1Score = 0;
		int Player2Score = 0;
		int Player3Score = 0;
		
		if(numGames != 0) 
		{
			if(process_rank == 0)
			{
				System.out.println("Hi from process-" + process_rank);
				int[] player1Moves = new int[numGames];
				int[] player2Moves = new int[numGames];
				RPS_Player player0 = new RPS_Player(numGames);
				MPI.COMM_WORLD.Recv(player1Moves, 0, numGames, MPI.INT, 1, 0); // recieve moves from player1
				MPI.COMM_WORLD.Recv(player2Moves, 0, numGames, MPI.INT, 2, 0); // recieve moves from player2
				MPI.COMM_WORLD.Send(player0.getChoices(), 0, numGames, MPI.INT, 1, 1);// send player1
				MPI.COMM_WORLD.Send(player0.getChoices(), 0, numGames, MPI.INT, 2, 2);//send player2
				TimeUnit.MILLISECONDS.sleep(3);
				/*
				System.out.println("Here is the values passed to "+process_rank);
				System.out.println("Player0Moves");
				for(int i = 0; i < player0.getChoices().length; i++)
				{
					System.out.print(player0.getChoices()[i] + " ");
				}
				System.out.println();
				System.out.println("Player1Moves");
				for(int i = 0; i < player1Moves.length; i++)
				{
					System.out.print(player1Moves[i] + " ");
				}
				System.out.println();
				System.out.println("Player2Moves");
				for(int i = 0; i < player2Moves.length; i++)
				{
					System.out.print(player2Moves[i] + " ");
				}
				System.out.println();
				*/
				RPS match = new RPS(numGames, player0.getChoices(), player1Moves, player2Moves, MPI.COMM_WORLD.Rank());
				Player1Score += match.play();
				TimeUnit.MILLISECONDS.sleep(10);
				System.out.println("Player-"+process_rank+" Score is: "+Player1Score);
			}
			if(process_rank == 1)
			{
				System.out.println("Hi from process-" + process_rank);
				int[] player0Moves = new int[numGames];
				int[] player2Moves = new int[numGames];
				RPS_Player player1 = new RPS_Player(numGames);
				MPI.COMM_WORLD.Send(player1.getChoices(), 0, numGames, MPI.INT, 0, 0);//send moves to player0
				MPI.COMM_WORLD.Recv(player0Moves, 0, numGames, MPI.INT, 0, 1); // recieves moves from player0
				MPI.COMM_WORLD.Recv(player2Moves, 0, numGames, MPI.INT, 2, 1); // recieves moves from player2
				MPI.COMM_WORLD.Send(player1.getChoices(), 0, numGames, MPI.INT, 2, 2);//send moves to player2
				TimeUnit.MILLISECONDS.sleep(1);
				/*
				System.out.println("Here is the values passed to "+process_rank);
				System.out.println("Player1Moves");
				for(int i = 0; i < player1.getChoices().length; i++)
				{
					System.out.print(player1.getChoices()[i] + " ");
				}
				System.out.println();
				System.out.println("Player0Moves");
				for(int i = 0; i < player0Moves.length; i++)
				{
					System.out.print(player0Moves[i] + " ");
				}
				System.out.println();
				System.out.println("Player2Moves");
				for(int i = 0; i < player2Moves.length; i++)
				{
					System.out.print(player2Moves[i] + " ");
				}
				System.out.println();
				*/
				RPS match = new RPS(numGames, player1.getChoices(), player0Moves,player2Moves, MPI.COMM_WORLD.Rank());
				Player2Score += match.play();
				TimeUnit.MILLISECONDS.sleep(10);
				System.out.println("Player-"+process_rank+" Score is: "+Player2Score);
			}
			if(process_rank == 2)
			{
				System.out.println("Hi from process-" + process_rank);
				int[] player0Moves = new int[numGames];
				int[] player1Moves = new int[numGames];
				RPS_Player player2 = new RPS_Player(numGames);
				MPI.COMM_WORLD.Send(player2.getChoices(), 0, numGames, MPI.INT, 0, 0); // send moves to player0
				MPI.COMM_WORLD.Send(player2.getChoices(), 0, numGames, MPI.INT, 1, 1); // send moves to player1
				MPI.COMM_WORLD.Recv(player0Moves, 0, numGames, MPI.INT, 0, 2); // recieve moves from player0
				MPI.COMM_WORLD.Recv(player1Moves, 0, numGames, MPI.INT, 1, 2); // recieve moves from player1
				//TimeUnit.MILLISECONDS.sleep(50);
				/*
				System.out.println("Here is the values passed to "+process_rank);
				System.out.println("Player2Moves");
				for(int i = 0; i < player2.getChoices().length; i++)
				{
					System.out.print(player2.getChoices()[i] + " ");
				}
				System.out.println();
				System.out.println("Player0Moves");
				for(int i = 0; i < player0Moves.length; i++)
				{
					System.out.print(player0Moves[i] + " ");
				}
				System.out.println();
				System.out.println("Player1Moves");
				for(int i = 0; i < player1Moves.length; i++)
				{
					System.out.print(player1Moves[i] + " ");
				}
				System.out.println();
				*/
				RPS match = new RPS(numGames, player2.getChoices(), player0Moves,player1Moves, MPI.COMM_WORLD.Rank());
				Player3Score += match.play();
				TimeUnit.MILLISECONDS.sleep(10);
				System.out.println("Player-"+process_rank+" Score is: "+Player3Score);
			}
			MPI.Finalize();
		}
	}
}

