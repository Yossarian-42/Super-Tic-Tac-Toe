import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Stack;
import java.util.Vector;
import javax.swing.*;

public class XOPre extends JFrame implements KeyListener, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int  human =1;//1 Xplayer Human
	private static int  computer=2;// 2 OPlayer Computer
	private int startPlayer=human;

	private boolean player2; // true if two humans, false if vs computer
	private boolean isRunning; //Check if the computer is running;
	private boolean computer2; //For when it's AI vs AI
	private String computerMode;//Which algorithm to use

	private int turn; // Current turn

	private int bLength; // Board length and height (basically size)
	private int notyetLength;
	private int winLength; // Win streak needed to win
	private int notyetWin;
	private int bestRow; // Best Row for AI
	private int bestCol; // Best Col for AI
	private int level; // Level of AI
	private int notyetLevel;
	private int buttonPushJoke=0; //Easter egg in keyPressed
	private int buttonPushJokeCount = 0;//Easter egg counter

	private MyButton [][] gBoard; // Visual board
	private int [][] lBoard;  //Logic board: 0 = free, 1 = XPlayer, 2 = OPlayer
	private int [][] scoreBoard; // Used for scoring things for the AI
	private GridLayout layout; // The layout
	private JRadioButtonMenuItem[] boardSButton; //Radio button for size menu
	private JRadioButtonMenuItem[] boardWButton;//Radio button for win menu
	private JRadioButtonMenuItem[] startPlay; // Radio button for starting player
	private JRadioButtonMenuItem[] menuMode; // Radio button for game mode
	private JRadioButtonMenuItem[] AIlvl; // Radio button for AI level


	private Vector<TurnClass> turntable; // Keeps track of all the turns
	private Stack<TurnClass> undo; // Undo stack
	private Stack<TurnClass> redo; // Redo stack


	public XOPre(int length, int win, int mode, int start, int lvl, boolean startNow)
	{
		JMenuBar menuBar;
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);// Load with Alt+F
		menuBar.add(menu);
		JMenuItem menuItem = new JMenuItem("New Game");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (layout != null)
				{
					for(int i = 0; i<bLength; i++)
					{
						for(int j = 0; j<bLength; j++)
						{
							remove(gBoard[i][j]);
						}
					}

				}
				initBoard(false);

			}
		});
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem("Save Game");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (layout != null)
					save();
				else
					JOptionPane.showMessageDialog(null,"You don't have a game to save");

			}
		});
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem("Load Game");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				load();
			}
		});
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem("Help");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JOptionPane.showMessageDialog(null,"Welcome to the world of Poke- wait, wrong game");
				JOptionPane.showMessageDialog(null,"This is Tic Tac Toe, on steroids");
				JOptionPane.showMessageDialog(null,"In the File menu you can start a new game with the parameters you give it, save the current game, load a saved game, read this fancy text in the help section, or exit the game");
				JOptionPane.showMessageDialog(null,"In the Board menu, you can choose the size of the board and the length needed to win");
				JOptionPane.showMessageDialog(null,"You can also choose the starting player from there");
				JOptionPane.showMessageDialog(null,"In the Game Mode menu, you choose wether you'd like to fight the superior and amazing Artificial Intelligence that definitely will not incinerate you, and will let you do as you please (except win),");
				JOptionPane.showMessageDialog(null,"Fight the flesh bags you call your 'friends', or watch two equally powerful titans battle it out in the arena (press the screen or push buttons on the keyboard to make the puppets dance to your pleasure)");
				JOptionPane.showMessageDialog(null, "While in game, you could also use the left keyboard button to undo your moves, and the right keyboard button to redo them.");
				JOptionPane.showMessageDialog(null,"Also, a secret easter egg exists! Try to find it!");
				JOptionPane.showMessageDialog(null,"Now, enjoy!");
			}
		});
		menu.add(menuItem);

		menu.addSeparator();
		menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		menuItem.setToolTipText("Click To Exit The Program");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				System.exit(0);
			}
		});
		menu.add(menuItem);

		JMenu board=new JMenu("Board");
		JMenu boardSize=new JMenu("Board Size");
		boardSButton = new JRadioButtonMenuItem[6];
		ButtonGroup sizeGroup = new ButtonGroup();
		for (int i = 0; i<6; i++)
		{
			if (i == 3)
				boardSButton[i] = new JRadioButtonMenuItem(""+(i+5)+"(recommended)");
			else
				boardSButton[i] = new JRadioButtonMenuItem(""+(i+5));

			boardSButton[i].addActionListener(new ALGetSize(i));
			sizeGroup.add(boardSButton[i]);
			boardSize.add(boardSButton[i]);
			if (i<5)
				boardSize.addSeparator();
		}
		board.add(boardSize);
		board.addSeparator();

		JMenu boardWin = new JMenu("Win streak");
		boardWButton = new JRadioButtonMenuItem[6];
		ButtonGroup winGroup = new ButtonGroup();
		for (int i = 0; i<6; i++)
		{
			if (i == 0)
				boardWButton[i] = new JRadioButtonMenuItem(""+(i+5)+"(recommended)");
			else
				boardWButton[i] = new JRadioButtonMenuItem(""+(i+5));

			boardWButton[i].addActionListener(new ALGetWin(i));
			winGroup.add(boardWButton[i]);
			boardWin.add(boardWButton[i]);
			if (i<5)
				boardWin.addSeparator();
		}
		board.add(boardWin);
		board.addSeparator();
		boardSButton[length-5].doClick();
		boardWButton[win-5].doClick();

		JMenu startPlayer = new JMenu("Starting player");
		ButtonGroup startGroup = new ButtonGroup();
		startPlay = new JRadioButtonMenuItem[2];
		startPlay[0]= new JRadioButtonMenuItem("X starts (Human VS Computer)");
		startPlay[0].addActionListener(new ALGetStart(human));
		startPlay[1] = new JRadioButtonMenuItem("O starts (Computer VS Human)");
		startPlay[1].addActionListener(new ALGetStart(computer));
		startGroup.add(startPlay[0]);
		startGroup.add(startPlay[1]);
		startPlayer.add(startPlay[0]);
		startPlayer.addSeparator();
		startPlayer.add(startPlay[1]);
		board.add(startPlayer);
		if (start == human)
			startPlay[0].doClick();
		else
			startPlay[1].doClick();


		JMenu gameMode = new JMenu("Game Mode");
		ButtonGroup modeGroup = new ButtonGroup();
		menuMode = new JRadioButtonMenuItem[3];
		menuMode[0] = new JRadioButtonMenuItem("Human VS AI");
		menuMode[0].addActionListener(new ALGetMode(0));
		menuMode[1] = new JRadioButtonMenuItem("Human VS Human");
		menuMode[1].addActionListener(new ALGetMode(1));
		menuMode[2] = new JRadioButtonMenuItem("AI VS AI");
		menuMode[2].addActionListener(new ALGetMode(2));
		modeGroup.add(menuMode[0]);
		modeGroup.add(menuMode[1]);
		modeGroup.add(menuMode[2]);
		gameMode.add(menuMode[0]);
		gameMode.addSeparator();
		gameMode.add(menuMode[1]);
		gameMode.addSeparator();
		gameMode.add(menuMode[2]);


		JMenu AILevel = new JMenu("AI Level");
		ButtonGroup levelGroup = new ButtonGroup();
		AIlvl = new JRadioButtonMenuItem[3];
		AIlvl[0] = new JRadioButtonMenuItem("Fast and furious (and stupid)");
		AIlvl[0].addActionListener(new ALGetLevel(1));
		AIlvl[1] = new JRadioButtonMenuItem("Normal");
		AIlvl[1].addActionListener(new ALGetLevel(3));
		AIlvl[2] = new JRadioButtonMenuItem("Slow (but not intellectually)");
		AIlvl[2].addActionListener(new ALGetLevel(5));
		levelGroup.add(AIlvl[0]);
		levelGroup.add(AIlvl[1]);
		levelGroup.add(AIlvl[2]);
		AILevel.add(AIlvl[0]);
		AILevel.addSeparator();
		AILevel.add(AIlvl[1]);
		AILevel.addSeparator();
		AILevel.add(AIlvl[2]);

		menuBar.add(board);
		menuBar.add(gameMode);
		menuBar.add(AILevel);

		isRunning = false;
		player2 = false;
		computer2 = false;

		bLength = length;
		winLength = win;
		if (mode == 0)
			menuMode[0].doClick();
		else if (mode == 1)
			menuMode[1].doClick();
		else
			menuMode[2].doClick();
		if (lvl == 1)
			AIlvl[0].doClick();
		else if (lvl == 3)
			AIlvl[1].doClick();
		else
			AIlvl[2].doClick();
		level = lvl;
		notyetLevel = level;
		notyetLength = bLength;
		notyetWin = winLength;


		undo = new Stack<TurnClass>();
		redo = new Stack<TurnClass>();
		setTitle("BEST TIC TAC TOE SIMULATOR 2015");
		this.setFocusable(true);
		this.requestFocusInWindow();
		this.addKeyListener(this);
		if (startNow)
			initBoard(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(bLength*100,bLength*100);
		setVisible(true);
		setLocationRelativeTo(null);
	}


	public void initBoard(boolean isLoad)
	{

		if (!isLoad)
		{
			while(getKeyListeners().length >0)
				removeKeyListener(this);
			bLength = notyetLength;
			winLength = notyetWin;
			level = notyetLevel;
			turntable = new Vector<TurnClass>(bLength*bLength);
			lBoard = new int[bLength][bLength];
			undo = new Stack<TurnClass>();
			redo = new Stack<TurnClass>();
		}

		gBoard = new MyButton[bLength][bLength];

		layout = new GridLayout(bLength,bLength);
		setLayout(layout);

		for( int i=0; i<bLength; i++){
			for(int j=0; j<bLength; j++){
				if (!isLoad)
				{
					lBoard[i][j]=0;
				}
				ImageIcon icon;
				if (lBoard[i][j] == 0)
					icon = new ImageIcon("Back.jpg");
				else if (lBoard[i][j] == 1)
					icon = new ImageIcon("x1.jpg");
				else
					icon = new ImageIcon("o1.jpg");
				Image img = icon.getImage();
				gBoard[i][j]= new MyButton(img)	;
				gBoard[i][j].addActionListener(new AL(i,j));
				gBoard[i][j].setFocusable(true);
				gBoard[i][j].requestFocusInWindow();
				if (!isLoad)
					gBoard[i][j].addKeyListener(this);
				add(gBoard[i][j]);
				gBoard[i][j].repaint();
			}	
		}

		scoreBoard = new int[bLength][bLength];

		for (int i = 0; i<bLength;i++)
		{
			for (int j = 0; j<bLength;j++)
			{
				scoreBoard[i][j] = 0;
			}
		}

		int minScore = 2;
		int multi = 1;
		for (int i = 0; i<(bLength-i);i++)
		{
			for (int j = 0; j<(bLength-j);j++)
			{
				scoreBoard[i][bLength-j-1] = (i+j+minScore+multi);
				scoreBoard[i][j] = (i+j+minScore+multi);
				scoreBoard[bLength-i-1][j] = (i+j+minScore+multi);
				scoreBoard[bLength-i-1][bLength-j-1] = (i+j+minScore+multi);
			}
		}


		turn=startPlayer;
		if(!player2 && !computer2 && startPlayer==computer)
		{
			makeAMove(computer);
		}

		this.setFocusable(true);
		this.requestFocusInWindow();
		this.addKeyListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(bLength*100,bLength*100);
		setVisible(true);
		boardSButton[bLength-5].doClick();
		boardWButton[winLength-5].doClick();

	}


	class ALGetSize implements ActionListener
	{
		private int size;

		public ALGetSize(int i)
		{
			this.size = i;
		}

		public void actionPerformed(ActionEvent e) {
			notyetLength = size+5;
			for (int i = 0; i<6; i++)
				boardWButton[i].setEnabled(true);
			for (int i = size+1; i<6; i++)
			{
				boardWButton[i].setEnabled(false);
			}
			if (notyetWin > notyetLength)
				boardWButton[size].doClick();
		}

	}

	class ALGetWin implements ActionListener
	{
		private int win;

		public ALGetWin(int i)
		{
			this.win = i+5;
		}

		public void actionPerformed(ActionEvent e) {
			notyetWin = win;
		}

	}

	class ALGetStart implements ActionListener
	{
		private int start;

		public ALGetStart(int i)
		{
			this.start = i;
		}

		public void actionPerformed(ActionEvent e) {
			startPlayer = start;
		}

	}

	class ALGetMode implements ActionListener
	{
		private int mode;

		public ALGetMode(int i)
		{
			this.mode = i;
		}

		public void actionPerformed(ActionEvent e) {
			if (mode == 0)
			{
				player2 = false;
				computer2 = false;
			}
			else if (mode == 1)
			{
				player2 = true;
				computer2 = false;
			}
			else if (mode == 2)
			{
				player2 = false;
				computer2 = true;
			}
		}

	}

	class ALGetLevel implements ActionListener
	{
		private int lvl;

		public ALGetLevel(int i)
		{
			this.lvl = i;
		}

		public void actionPerformed(ActionEvent e) {
			notyetLevel = lvl;
			if (lvl == 5)
			{
				menuMode[2].setEnabled(false);
				if (computer2)
					menuMode[0].doClick();
			}
			else if (menuMode[2].isEnabled() == false)
				menuMode[2].setEnabled(true);
		}

	}


	class AL implements ActionListener{
		private int row;
		private int col;

		public AL(int row, int col){
			this.row=row;
			this.col=col;
		}
		public void actionPerformed(ActionEvent e){
			ImageIcon icon;
			Image img;
			MyButton b=(MyButton)e.getSource();
			if (!player2 && !computer2) // Human vs AI
			{
				if(turn==human){
					if( lBoard[row][col]==0){ // free cell

						lBoard[row][col]=human;
						icon=new ImageIcon("x1.jpg");
						img=icon.getImage();
						b.setImg(img);
						b.repaint();
						turntable.add(new TurnClass(row,col,human));
						undo.push(new TurnClass(row,col,human));
						checkBoard(row,col,turn);
						turn=3-turn;

						if( isBoardFull())
							endStartGame();

						else{
							new Thread(new Runnable() {
								public void run() {
									try {
										isRunning = true;
										Thread.sleep(750);
										makeAMove(computer);
										isRunning = false;
									}
									catch(InterruptedException ex) {isRunning=false;}
								}
							}).start();

						}
					}
				}
			}
			else if (player2) // Human vs Human
			{
				if(turn==human){
					if( lBoard[row][col]==0){ // free cell

						lBoard[row][col]=human;
						icon=new ImageIcon("x1.jpg");
						img=icon.getImage();
						b.setImg(img);
						b.repaint();
						turntable.add(new TurnClass(row,col,human));
						undo.push(new TurnClass(row,col,human));
						redo.clear();
						checkBoard(row,col,turn);
						turn=3-turn;

						if( isBoardFull())
							endStartGame();
					}

				}
				else
				{
					if( lBoard[row][col]==0){ // free cell

						lBoard[row][col]=computer;
						icon=new ImageIcon("o1.jpg");
						img=icon.getImage();
						b.setImg(img);
						b.repaint();
						turntable.add(new TurnClass(row,col,computer));
						undo.push(new TurnClass(row,col,computer));
						redo.clear();
						checkBoard(row,col,turn);
						turn=3-turn;

						if( isBoardFull())
							endStartGame();
					}
				}
				if (checkTie(human) && checkTie(computer))
				{
					endStartGame();
					return;
				}
			}
			else if (computer2)
			{
				makeAMove(turn);
			}
		}
	}


	public boolean isBoardFull(){
		for( int i=0; i<bLength; i++){
			for(int j=0; j<bLength; j++){
				if( lBoard[i][j]==0)
					return false;
			}
		}
		return true;
	}


	long negaMax(int depth, int turn)
	{
		long best = Long.MIN_VALUE;
		long val;

		if (depth == 0)
			return -rateBoard(turn);

		for (int i = 0; i<bLength; i++)
		{
			for (int j = 0; j<bLength; j++)
			{
				if (lBoard[i][j] == 0)
				{
					lBoard[i][j] = turn;
					val = -negaMax(depth-1, 3-turn);
					lBoard[i][j] = 0;
					if (val > best)
					{
						best = val;
						if (depth == level)
						{
							bestRow = i;
							bestCol = j;
						}
					}
				}
			}
		}
		return best;
	}

	public void doNegaMax(int player)
	{
		computerMode = "NegaMax";
		bestRow = -1;
		bestCol = -1;
		negaMax(level,player);

		ImageIcon icon;
		Image img;

		lBoard[bestRow][bestCol]=player;
		if (player == computer)
			icon=new ImageIcon("o1.jpg");
		else
			icon=new ImageIcon("x1.jpg");
		img=icon.getImage();
		gBoard[bestRow][bestCol].setImg(img);
		gBoard[bestRow][bestCol].repaint();
		checkBoard(bestRow,bestCol,turn);
		turn=3-turn;
		if(isBoardFull())
			endStartGame();
	}

	long alphaBeta(int depth, int turn, long alpha, long beta)
	{
		long val;
		if (depth == 0)
			return -rateBoard(turn);
		for (int i = 0; i<bLength; i++)
		{
			for (int j = 0; j<bLength; j++)
			{
				if (lBoard[i][j] == 0)
				{
					lBoard[i][j] = turn;
					val = -alphaBeta(depth-1, 3-turn, -beta,-alpha);
					lBoard[i][j] = 0;
					if (val >= beta && depth<level)
						return beta;
					if (val > alpha)
					{
						alpha = val;
						bestRow = i;
						bestCol = j;
					}
				}
			}
		}

		return alpha;

	}

	public void doAlphaBeta(int player)
	{

		computerMode = "AlphaBeta";
		bestRow = -1;
		bestCol = -1;
		alphaBeta(level, player, Long.MIN_VALUE, Long.MAX_VALUE);
		ImageIcon icon;
		Image img;

		lBoard[bestRow][bestCol]=player;
		if (player == computer)
			icon=new ImageIcon("o1.jpg");
		else
			icon=new ImageIcon("x1.jpg");
		img=icon.getImage();
		gBoard[bestRow][bestCol].setImg(img);
		gBoard[bestRow][bestCol].repaint();
		checkBoard(bestRow,bestCol,turn);

		turn=3-turn;
		if(isBoardFull())
			endStartGame();
	}


	public boolean checkToWin(int currentTurn) //Disregard AI, acquire win
	{

		for (int i = 0; i<bLength; i++)
		{
			for (int j = 0; j<bLength; j++)
			{
				if (lBoard[i][j] == 0)
				{
					lBoard[i][j] = currentTurn;
					if (checkRow(i,currentTurn) || checkCol(j,currentTurn) || checkCross(i,j,currentTurn))
					{
						ImageIcon icon;
						if (currentTurn == computer)
							icon =new ImageIcon("o1.jpg");
						else
							icon = new ImageIcon("x1.jpg");
						Image img=icon.getImage();
						gBoard[i][j].setImg(img);
						gBoard[i][j].repaint();
						turntable.add(new TurnClass(i,j,currentTurn));
						undo.push(new TurnClass(i,j,currentTurn));
						redo.clear();
						checkBoard(i,j,currentTurn);
						return true;
					}
					lBoard[i][j] = 0;
				}
			}
		}
		return false;
	}

	public boolean checkToBlock(int currentTurn)
	{
		int notTurn = 3-currentTurn;
		for (int i = 0; i<bLength; i++)
		{
			for (int j = 0; j<bLength; j++)
			{
				if (lBoard[i][j] == 0)
				{
					lBoard[i][j] = notTurn;
					if (checkRow(i,notTurn) || checkCol(j,notTurn) || checkCross(i,j,notTurn))
					{
						lBoard[i][j] = currentTurn;
						ImageIcon icon;
						if (currentTurn == computer)
							icon =new ImageIcon("o1.jpg");
						else
							icon = new ImageIcon("x1.jpg");
						Image img=icon.getImage();
						gBoard[i][j].setImg(img);
						gBoard[i][j].repaint();

						turntable.add(new TurnClass(i,j,currentTurn));
						undo.push(new TurnClass(i,j,currentTurn));
						redo.clear();
						checkBoard(i,j,currentTurn);
						turn=3-turn;
						return true;
					}
					lBoard[i][j] = 0;
				}
			}
		}
		return false;
	}

	public void makeAMove(int currentTurn)
	{

		if (!checkToWin(currentTurn))
		{
			if (!checkToBlock(currentTurn))
			{
				computerMode = "AlphaBeta";

				if (computerMode.equals("AlphaBeta"))
					doAlphaBeta(currentTurn);
				else
					doNegaMax(currentTurn);

				turntable.add(new TurnClass(bestRow,bestCol,computer));
				undo.push(new TurnClass(bestRow,bestCol,computer));
				redo.clear();
			}
		}

		if (checkTie(currentTurn) && checkTie(3-currentTurn))
		{
			endStartGame();
			return;
		}

	}

	public boolean checkTie(int currentTurn)
	{
		int[][] temp = new int[bLength][bLength];

		for (int i = 0; i<bLength; i++)
		{
			for (int j = 0; j<bLength; j++)
			{
				temp[i][j] = lBoard[i][j];
			}
		}
		for (int i = 0; i<bLength; i++)
		{
			for (int j = 0; j<bLength; j++)
			{
				if (lBoard[i][j] == 0)
					lBoard[i][j] = currentTurn;

				if (checkRow(i,currentTurn) || checkCol(j,currentTurn) || checkCross(i,j,currentTurn))
				{
					for (int k = 0; k<bLength; k++)
						for (int l = 0; l<bLength; l++)
							lBoard[k][l] = temp[k][l];
					return false;
				}
			}
		}
		for (int k = 0; k<bLength; k++)
			for (int l = 0; l<bLength; l++)
				lBoard[k][l] = temp[k][l];
		return true;
	}


	public long rateBoard(int player)
	{
		long sum = 0;
		for (int i = 0; i<bLength; i++)
		{
			for (int j = 0; j<bLength; j++)
			{
				if (turn == computer)
					sum+=rateCell(i,j);
				else
					sum+=rateCellH(i,j);
			}
		}
		return sum;

	}

	public long rateCell(int row, int col)
	{
		//make a loop for each direction, from -1 to 1
		int rowT=row, colT=col;
		long PCScore=0, HScore=0;
		//Rate the cell according to each direction. 10^x for each token in direction, multiply by score of cell we're looking at in scoreboard (in each direction). Then substract the score of the other player from current player and return that.
		double intersectCount = 0;
		double intersectCountH = 0;

		for (int i = -1; i<2; i++)
		{
			for (int j = -1; j<2; j++)
			{
				rowT = row;
				colT = col;
				int PC = 0;
				int HS = 0;
				int counter = 0;
				boolean fork = false;
				boolean trueFork = false;
				boolean forkH = false;
				boolean trueForkH = false;
				int forkcountH = 0;
				int forkcount = 0;
				if (!(i == 0 && j == 0)) // No direction
				{
					while((rowT >= 0 && rowT < bLength) && (colT >= 0 && colT < bLength) && counter < winLength)
					{
						counter++;
						if (lBoard[rowT][colT] == computer)
						{
							PCScore++;
							PC++;
							forkH = false;
							forkcountH = 0;
							if (fork)
								forkcount++;
							else
								forkcount = 0;
						}
						else if (lBoard[rowT][colT] == human)
						{
							HScore++;
							HS++;
							fork = false;
							forkcount = 0;
							if (forkH)
								forkcountH++;
							else
								forkcountH = 0;
						}
						else //empty
						{

							fork = true;
							forkH = true;
							if (forkcount == winLength-1)
							{
								trueFork = true;
							}
							if (forkcountH == winLength-2)
							{
								trueForkH = true;
							}
						}
						rowT+=i;
						colT+=j;
					}
					//Warning, lots of complicated math below. Believe in the numbers! Heuristics.
					if (!computerMode.equals("AlphaBeta"))
					{
						if (PC >0 && HS == 0 && counter >= winLength)
						{
							PCScore+= (Math.pow(10, PC))*scoreBoard[row][col]/1.25;
							if (PC == winLength)
							{
								PCScore += (Math.pow(10, PC+2))*scoreBoard[row][col];
							}
							if (PC > 1)
								intersectCount++;
						}
						if (HS > 0 && PC == 0 && counter >= winLength)
						{
							HScore += (Math.pow(10,  HS))*scoreBoard[row][col]*1.7+50;
							if (HS >= winLength-1)
								HScore += (Math.pow(10, HS+2))*scoreBoard[row][col];
							else if (HS >= winLength-2)
								if ((row-i) >= 0 && (row-i) < bLength && (col-j) >= 0 && (col-j) < bLength && lBoard[row-i][col-j] != computer)
									HScore *= 1.7;
							if (HS> 1)
								intersectCountH++;
						}
						if (trueFork)
						{
							PCScore += (Math.pow(10, PC))*scoreBoard[row][col]/2;

						}
						if (trueForkH)
						{
							HScore += (Math.pow(10,  HS+1))*scoreBoard[row][col]/1.5;

						}
					}

					else if (computerMode.equals("AlphaBeta"))
					{
						if (bLength<=8)
						{
							if (PC >0 && HS == 0 && counter >= winLength)
							{
								PCScore+= (Math.pow(10, PC))*scoreBoard[row][col]/3.29761;
								if (PC == winLength)
								{
									PCScore += (Math.pow(10, PC+1))*scoreBoard[row][col];

								}
								if (PC > 1)
									intersectCount++;
							}
							if (HS > 0 && PC == 0 && counter >= winLength)
							{
								HScore += (Math.pow(10,  HS))*scoreBoard[row][col]*3.5;
								if (HS >= winLength-1)
								{
									HScore += (Math.pow(10, HS+2))*scoreBoard[row][col];

								}
								else if (HS >= winLength-2)
									if ((row-i) >= 0 && (row-i) < bLength && (col-j) >= 0 && (col-j) < bLength && lBoard[row-i][col-j] != computer)
										HScore *=2;
									else
										HScore/=1.5;
								if (HS> 1)
									intersectCountH++;
							}
							if (trueFork)
							{
								PCScore += (Math.pow(10, PC))*scoreBoard[row][col]*2;

							}
							if (trueForkH)
							{
								HScore += (Math.pow(10,  HS))*scoreBoard[row][col];

							}
						}
						else
						{
							if (PC >0 && HS == 0 && counter >= winLength)
							{
								PCScore+= (Math.pow(10, PC))*scoreBoard[row][col]/4;
								if (PC == winLength)
								{
									PCScore += (Math.pow(10, PC+1))*scoreBoard[row][col];

								}
								if (PC > 1)
									intersectCount++;
							}
							if (HS > 0 && PC == 0 && counter >= winLength)
							{
								HScore += (Math.pow(10,  HS))*scoreBoard[row][col]*10;
								if (HS >= winLength-1)
								{
									HScore += (Math.pow(10, HS+2))*scoreBoard[row][col];

								}
								else if (HS >= winLength-2)
									if ((row-i) >= 0 && (row-i) < bLength && (col-j) >= 0 && (col-j) < bLength && lBoard[row-i][col-j] != computer)
										HScore *= 10;
								if (HS> 1)
									intersectCountH++;
							}
							if (trueFork)
							{
								PCScore += (Math.pow(10, PC))*scoreBoard[row][col]*4;

							}
							if (trueForkH)
							{
								HScore += (Math.pow(10,  HS))*scoreBoard[row][col];

							}
						}


					}


				}





			}

		}
		if (intersectCount>1)
		{
			PCScore*=1.1;
		}
		if (intersectCountH>1)
		{
			HScore*=5;
		}
		return PCScore-HScore;

	}

	public long rateCellH(int row, int col)
	{
		//make a loop for each direction, from -1 to 1
		int rowT=row, colT=col;
		long PCScore=0, HScore=0;
		//Rate the cell according to each direction. 10^x for each token in direction, multiply by score of cell we're looking at in scoreboard (in each direction). Then substract the score of the other player from current player and return that.
		int intersectCount = 0;
		int intersectCountH = 0;

		for (int i = -1; i<2; i++)
		{
			for (int j = -1; j<2; j++)
			{
				rowT = row;
				colT = col;
				int PC = 0;
				int HS = 0;
				int counter = 0;
				boolean fork = false;
				boolean trueFork = false;
				boolean forkH = false;
				boolean trueForkH = false;
				int forkcountH = 0;
				int forkcount = 0;
				if (!(i == 0 && j == 0))
				{
					while((rowT >= 0 && rowT < bLength) && (colT >= 0 && colT < bLength) && counter < winLength)
					{
						counter++;
						if (lBoard[rowT][colT] == computer)
						{
							PCScore++;
							PC++;
							forkH = false;
							forkcountH = 0;
							if (fork)
								forkcount++;
							else
								forkcount = 0;
						}
						else if (lBoard[rowT][colT] == human)
						{
							HScore++;
							HS++;
							fork = false;
							forkcount = 0;
							if (forkH)
								forkcountH++;
							else
								forkcountH = 0;
						}
						else //empty
						{

							fork = true;
							forkH = true;
							if (forkcount == winLength-1)
							{
								trueFork = true;
							}
							if (forkcountH == winLength-2)
							{
								trueForkH = true;
							}
						}
						rowT+=i;
						colT+=j;
					}

					if (!computerMode.equals("AlphaBeta"))
					{
						if (PC >0 && HS == 0 && counter >= winLength)
						{
							PCScore+= (Math.pow(10, PC))*scoreBoard[row][col]*1.7+100;
							if (PC >= winLength-1)
							{
								PCScore += (Math.pow(10, PC+1))*scoreBoard[row][col];
							}
							else if (HS >= winLength-2)
								HScore *= 1.7;
							intersectCountH++;
						}
						if (HS > 0 && PC == 0 && counter >= winLength)
						{
							HScore += (Math.pow(10,  HS))*scoreBoard[row][col]/1.25;
							if (HS == winLength)
								HScore += (Math.pow(10, HS+1))*scoreBoard[row][col];
							intersectCount++;
						}
						if (trueForkH)
						{
							PCScore += (Math.pow(10, PC))*scoreBoard[row][col]/2;

						}
						if (trueFork)
						{
							HScore += (Math.pow(10,  HS+1))*scoreBoard[row][col]/1.5;

						}
					}

					else if (computerMode.equals("AlphaBeta"))
					{
						if (bLength<= 8)
						{
							if (PC >0 && HS == 0 && counter >= winLength)
							{
								PCScore+= (Math.pow(10, PC))*scoreBoard[row][col]*3;
								if (PC == winLength-1)
								{
									PCScore += (Math.pow(10, PC+2))*scoreBoard[row][col];

								}
								else if (PC >= winLength-2)
									if ((row-i) >= 0 && (row-i) < bLength && (col-j) >= 0 && (col-j) < bLength && lBoard[row-i][col-j] != human)
									{
										HScore *= 2;
									}
								if (PC > 1)
									intersectCountH++;
							}
							if (HS > 0 && PC == 0 && counter >= winLength)
							{
								HScore += (Math.pow(10,  HS))*scoreBoard[row][col]/3.2978;
								if (HS >= winLength)
								{
									HScore += (Math.pow(10, HS+1))*scoreBoard[row][col];

								}

								if (HS> 1)
									intersectCount++;
							}
							if (trueFork)
							{
								PCScore += (Math.pow(10, PC))*scoreBoard[row][col]*2;

							}
							if (trueForkH)
							{
								HScore += (Math.pow(10,  HS))*scoreBoard[row][col];

							}
						}
						else
						{
							if (PC >0 && HS == 0 && counter >= winLength)
							{
								PCScore+= (Math.pow(10, PC))*scoreBoard[row][col]*3;
								if (PC >= winLength-1)
								{
									PCScore += (Math.pow(10, PC+2))*scoreBoard[row][col];

								}
								else if (PC >= winLength-2)
									if ((row-i) >= 0 && (row-i) < bLength && (col-j) >= 0 && (col-j) < bLength && lBoard[row-i][col-j] != human)
										PCScore *= 2.5;
									else
										PCScore/=1.5;
								if (PC > 1)
									intersectCount++;
							}
							if (HS > 0 && PC == 0 && counter >= winLength)
							{
								HScore += (Math.pow(10,  HS))*scoreBoard[row][col]/4;
								if (HS == winLength)
								{
									HScore += (Math.pow(10, HS+1))*scoreBoard[row][col];

								}
								if (HS> 1)
									intersectCountH++;
							}
							if (trueFork)
							{
								PCScore += (Math.pow(10, PC))*scoreBoard[row][col]*4;

							}
							if (trueForkH)
							{
								HScore += (Math.pow(10,  HS))*scoreBoard[row][col];

							}
						}
					}


				}

			}

			if (intersectCount>1)
			{
				PCScore*=5;
			}
			if (intersectCountH>1)
			{
				HScore*=1.1;
			}
		}

		return HScore - PCScore;

	}

	public void endStartGame (){
		if (buttonPushJokeCount != 3)	
			JOptionPane.showMessageDialog(XOPre.this,"It's a tie!");
		dispose();
		dispose();
		int mode = 0;
		if (player2)
			mode = 1;
		else if(computer2)
			mode = 2;
		new XOPre(bLength,winLength,mode, startPlayer, level, true);
	}


	public void checkBoard(int i, int j, int turn)
	{

		if (checkRow(i,turn) || checkCol(j,turn) || checkCross(i,j,turn))
		{
			if (computer2)
			{
				if (turn == 1)
					JOptionPane.showMessageDialog(XOPre.this,"Robot X wins!");
				else
					JOptionPane.showMessageDialog(XOPre.this,"Robot O wins!");
			}
			else if (player2)
			{
				if (turn == 1)
					JOptionPane.showMessageDialog(XOPre.this,"Player 1 wins!");
				else
					JOptionPane.showMessageDialog(XOPre.this,"Player 2 wins!");
			}
			else
			{
				if (turn == 1)
					JOptionPane.showMessageDialog(XOPre.this,"Human wins! This is impossible! \n Be proud of yourself!");
				else
					JOptionPane.showMessageDialog(XOPre.this,"Robot wins!");
			}

			dispose();
			int mode = 0;
			if (player2)
				mode = 1;
			else if(computer2)
				mode = 2;
			new XOPre(bLength,winLength,mode, startPlayer, level, true);


		}
	}

	public boolean checkRow(int i, int turn) // Dynamic
	{

		int counter = 0;
		int jt = 0;
		while (jt< bLength)
		{
			if (lBoard[i][jt] == turn)
				counter++;
			if (lBoard[i][jt] != turn)
			{
				if (counter >= winLength)
					return true;
				counter = 0;
			}

			jt++;	
		}
		if (counter >= winLength)
			return true;
		return false;

	}

	public boolean checkCol(int j, int turn)// Dynamic
	{
		int counter = 0;
		int it = 0;
		while (it< bLength)
		{
			if (lBoard[it][j] == turn)
				counter++;
			if (lBoard[it][j] != turn)
			{
				if (counter >= winLength)
					return true;
				counter = 0;
			}

			it++;	
		}
		if (counter >= winLength)
			return true;
		return false;
	}

	public boolean checkCross(int i, int j, int turn)//Dynamic
	{
		int counter = 0;
		int it = i, jt = j;
		while(it > 0 && jt > 0)
		{
			it--;
			jt--;
		}
		while (it < bLength && jt <bLength)
		{
			if (lBoard[it][jt] == turn)
				counter++;
			if (lBoard[it][jt] != turn)
			{
				if (counter >= winLength)
					return true;
				counter = 0;
			}
			it++;
			jt++;
		}
		if (counter >= winLength)
			return true;

		counter = 0;
		it = i;
		jt = j;
		while (it > 0 && jt < bLength-1)
		{
			it--;
			jt++;
		}
		while (it < bLength && jt >=0)
		{
			if (lBoard[it][jt] == turn)
				counter++;
			if (lBoard[it][jt] != turn)
			{
				if (counter >= winLength)
					return true;
				counter = 0;
			}
			it++;
			jt--;
		}
		if (counter >= winLength)
			return true;


		return false;
	}


	public void undo()
	{
		if (!undo.empty())
		{
			TurnClass previousTurn = undo.pop();
			lBoard[previousTurn.getRow()][previousTurn.getCol()] = 0;
			redo.push(previousTurn);
			if (!player2 && !computer2)
			{
				previousTurn = undo.pop();
				lBoard[previousTurn.getRow()][previousTurn.getCol()] = 0;
				redo.push(previousTurn);
			}

			paintBoard();
		}

	}

	public void redo()
	{
		if (!redo.empty())
		{
			TurnClass previousTurn = redo.pop();
			lBoard[previousTurn.getRow()][previousTurn.getCol()] = previousTurn.getPlayer();
			undo.push(previousTurn);
			if (!player2 && !computer2)
			{
				previousTurn = redo.pop();
				lBoard[previousTurn.getRow()][previousTurn.getCol()] = previousTurn.getPlayer();
				undo.push(previousTurn);
			}
		}


		paintBoard();


	}


	public void keyPressed(KeyEvent e) {
		if (layout != null)
		{
			buttonPushJoke++;
			if (buttonPushJoke > 100)
			{
				buttonPushJokeCount++;
				if (buttonPushJokeCount == 1)
					JOptionPane.showMessageDialog(null,"Please stop");
				else if (buttonPushJokeCount == 2)
					JOptionPane.showMessageDialog(null,"I asked you nicely");
				buttonPushJoke = 0;
				if (buttonPushJokeCount == 3)
				{
					JOptionPane.showMessageDialog(null,"OK FINE! YOU WIN, HAPPY NOW?");
					endStartGame();
				}
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		if (layout != null)
		{

			buttonPushJoke = 0;
			if (!isRunning)
			{
				int key = e.getKeyCode();

				if(key == KeyEvent.VK_LEFT && !computer2)
					undo();

				else if (key == KeyEvent.VK_RIGHT && !computer2)
					redo();

				else if (computer2)
					makeAMove(turn);

				if (player2 && (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_LEFT))
					turn = 3-turn;
			}
		}
	}

	public void keyTyped(KeyEvent e) {
	}


	public void save(){
		try{
			JFileChooser c = new JFileChooser();
			int r = c.showDialog(XOPre.this, "Create file to save object");
			if(r == JFileChooser.APPROVE_OPTION)
			{
				File f = c.getSelectedFile();

				FileOutputStream saveFile=new FileOutputStream(f);

				ObjectOutputStream save = new ObjectOutputStream(saveFile);
				
				save.writeObject(startPlayer);
				save.writeObject(player2);
				save.writeObject(computer2);
				save.writeObject(computerMode);
				save.writeObject(turn);
				save.writeObject(bLength);
				save.writeObject(winLength);
				save.writeObject(level);
				save.writeObject(buttonPushJoke);
				save.writeObject(buttonPushJokeCount);
				save.writeObject(lBoard);
				save.writeObject(scoreBoard);
				save.writeObject(turntable);

				save.close(); 
			}}
		catch(Exception ex){
			ex.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public void load(){
		try{
			JFileChooser c = new JFileChooser();
			int r = c.showDialog(XOPre.this, "Open file to set object");
			if(r == JFileChooser.APPROVE_OPTION){
				File f = c.getSelectedFile();
				FileInputStream loadFile = new FileInputStream(f);

				ObjectInputStream load = new ObjectInputStream(loadFile);

				if (layout != null)
				{
					for(int i = 0; i<bLength; i++)
					{
						for(int j = 0; j<bLength; j++)
						{
							gBoard[i][j].removeKeyListener(this);
							gBoard[i][j].removeAll();
							remove(gBoard[i][j]);

						}
					}

				}
				while(getKeyListeners().length >0)
					removeKeyListener(this);
				this.startPlayer = (Integer) load.readObject();
				this.player2 = (Boolean) load.readObject();
				this.computer2 = (Boolean)load.readObject();
				this.computerMode = (String)load.readObject();
				this.turn = (Integer)load.readObject();
				this.bLength = (Integer)load.readObject();
				this.winLength = (Integer)load.readObject();
				this.level = (Integer)load.readObject();
				this.buttonPushJoke = (Integer)load.readObject();
				this.buttonPushJokeCount = (Integer)load.readObject();
				this.lBoard = (int[][]) load.readObject();
				this.scoreBoard = (int[][]) load.readObject();
				this.turntable = (Vector<TurnClass>) load.readObject();

				clickMenus();
				load.close();

				for(int i = 0; i<bLength; i++)
				{
					for(int j = 0; j<bLength; j++)
					{
						lBoard[i][j] = 0;
					}
				}

				initBoard(true);

				putOnBoard();
				paintBoard();

			}}
		catch(Exception exc){
			exc.printStackTrace();
		}
	}


	public void putOnBoard()
	{
		for (int i = 0; i< turntable.size(); i++)
		{
			lBoard[turntable.get(i).getRow()][turntable.get(i).getCol()] = turntable.get(i).getPlayer();
			undo.push(turntable.get(i));
		}
	}

	public void paintBoard()
	{
		for (int i = 0; i<bLength; i++)
		{
			for (int j = 0; j<bLength; j++)
			{
				if (lBoard[i][j] == 0)
				{
					ImageIcon icon=new ImageIcon("Back.jpg");
					Image img=icon.getImage();
					gBoard[i][j].setImg(img);
				}
				else if (lBoard[i][j] == 1)
				{
					ImageIcon icon=new ImageIcon("x1.jpg");
					Image img=icon.getImage();
					gBoard[i][j].setImg(img);
				}
				else
				{
					ImageIcon icon=new ImageIcon("o1.jpg");
					Image img=icon.getImage();
					gBoard[i][j].setImg(img);
				}

				gBoard[i][j].repaint();
			}
		}
	}

	public void clickMenus()
	{
		boardSButton[bLength-5].doClick();
		boardWButton[winLength-5].doClick();
		startPlay[startPlayer-1].doClick();
		if (player2)
			menuMode[1].doClick();
		else if (computer2)
			menuMode[2].doClick();
		else
			menuMode[0].doClick();

		if (level == 1)
			AIlvl[0].doClick();
		else if (level == 3)
			AIlvl[1].doClick();
		else
			AIlvl[2].doClick();
	}


	public static void main(String[] args) {

		new XOPre (8,5,0,1,3,true);

	}




}
