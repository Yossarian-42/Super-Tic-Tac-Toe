import java.io.Serializable;


public class TurnClass implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int row;
	private int col;
	private int player;
	
	public TurnClass(int row, int col, int player)
	{
		this.row = row;
		this.col = col;
		this.player = player;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		this.player = player;
	}
	
	

}
