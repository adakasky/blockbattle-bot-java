// Copyright 2015 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package field;

import field.Cell;

/**
 * Field class
 * 
 * Represents the playing field for one player.
 * Has some basic methods already implemented.
 * 
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class Field {
	
	private int width;
	private int height;
	private String initialField;
	private Cell grid[][];

	public Field(int width, int height, String fieldString) {
		this.width = width;
		this.height = height;
		this.initialField = fieldString;
		parse(fieldString);
	}
	
	/**
	 * Parses the input string to get a grid with Cell objects
	 * @param fieldString : input string
	 */
	private void parse(String fieldString) {
		
		this.grid = new Cell[this.width][this.height];
		
		// get the separate rows
		String[] rows = fieldString.split(";");
		for(int y=0; y < this.height; y++) {
			String[] rowCells = rows[y].split(",");
			
			// parse each cell of the row
			for(int x=0; x < this.width; x++) {
				int cellCode = Integer.parseInt(rowCells[x]);
				this.grid[x][y] = new Cell(x, y, CellType.values()[cellCode]);
			}
		}
	}

    private String generateString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                String value = "";
                switch (this.grid[j][i].getState()) {
                    case EMPTY:
                        value = "0";
                        break;
                    case SHAPE:
                        value = "1";
                        break;
                    case SOLID:
                        value = "3";
                        break;
                    case BLOCK:
                        value = "2";
                        break;
                }
                builder.append(value);
                builder.append(",");
            }
            builder.append(";");
        }
        return builder.toString();
    }
	
	public Cell getCell(int x, int y) {
		if(x < 0 || x >= this.width || y < 0 || y >= this.height)
			return null;
		return this.grid[x][y];
	}

    private void setCell(Cell cell) {
        int x = (int) cell.getLocation().getX();
        int y = (int) cell.getLocation().getY();
        if(x < 0 || x >= this.width || y < 0 || y >= this.height)
            return;
        this.grid[x][y].setBlock();
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void addPiece(Shape piece){
        for(Cell single : piece.getBlocks()){
            setCell(single);
        }
    }

    private int columnHeight(int column){
        int r = 0;
        for(; r < this.height && (this.grid[column][r].isEmpty()||this.grid[column][r].isShape()); r++);
        return this.height - r;
    }

    public int aggregateHeight(){
        int total = 0;
        for(int c = 0; c < this.width; c++){
            total += this.columnHeight(c);
        }
        return total;
    }

    private boolean isLine(int row){
        for(int c = 0; c < this.width; c++){
            if (this.grid[c][row].isEmpty() || this.grid[c][row].isSolid()){
                return false;
            }
        }
        return true;
    }

    public int holes(){
        int count = 0;
        for(int c = 0; c < this.width; c++){
            boolean block = false;
            for(int r = 0; r < this.height; r++){
                if (this.grid[c][r].isBlock()) {
                    block = true;
                }else if (this.grid[c][r].isEmpty() && block){
                    count++;
                }
            }
        }
        return count;
    }

    public int bumpiness(){
        int total = 0;
        for(int c = 0; c < this.width - 1; c++){
            total += Math.abs(this.columnHeight(c) - this.columnHeight(c+ 1));
        }
        return total;
    }

    public int lines(){
        int count = 0;
        for(int r = 0; r < this.height; r++){
            if (this.isLine(r)){
                count++;
            }
        }
        return count;
    }

    public boolean canMoveLeft(Shape piece){
        Shape tempPiece = piece.clone();
        tempPiece.oneLeft();
        Cell[] tempBlocks = tempPiece.getBlocks();
        for(Cell single : tempBlocks){
            if(single.hasCollision(this) || single.isOutOfBoundaries(this))
                return false;
        }
        return true;
    }

    public boolean isValid(Shape piece){
        Cell[] tempBlocks = piece.getBlocks();
        for(Cell single : tempBlocks){
            if(single.hasCollision(this) || single.isOutOfBoundaries(this))
                return false;
        }
        return true;
    }

    public boolean isValidTop(Shape piece){
        Cell[] tempBlocks = piece.getBlocks();
        for(Cell single : tempBlocks){
            if(single.hasCollision(this) || single.isOutOfBoundariesTop(this))
                return false;
        }
        return true;
    }

    public boolean tooHigh(int limit) {
        for(int c = 0; c < this.width; c++){
            if (this.columnHeight(c) > this.getHeight() - limit)
                return true;
        }
        return false;
    }

    public boolean canMoveDown(Shape piece){
        Shape tempPiece = piece.clone();
        tempPiece.oneDown();
        Cell[] tempBlocks = tempPiece.getBlocks();
        for(Cell single : tempBlocks){
            if(single.hasCollision(this) || single.isOutOfBoundaries(this))
                return false;
        }
        return true;
    }

    public Field clone(){
        return new Field(this.width, this.height, this.generateString());
    }

    public int getRowTransitions() {
        int transitions = 0;
        Cell last = new Cell();


        for(int r = 0; r < this.height; r++){
            if(this.grid[0][r].isEmpty())
                last.setEmpty();
            else
                last.setBlock();
            for(int c = 0; c < this.width; c++){
                if(this.grid[c][r].isSolid() || this.grid[c][r].isShape())
                    break;
                if (this.grid[c][r].isEmpty() != last.isEmpty() ) {
                    transitions++;
                    if (last.isEmpty())
                        last.setBlock();
                    else
                        last.setEmpty();
                }
                if(c==this.width-1 && this.grid[c][r].isBlock())
                    transitions++;
            }
        }
        return transitions;
    }

    public int getColumnTransitions() {
        int transitions = 0;
        Cell last = new Cell();


        for(int c = 0; c < this.width; c++){
            last.setEmpty();
            for(int r = 0; r < this.height; r++){
                if(this.grid[c][r].isSolid())
                    break;
                if(this.grid[c][r].isShape())
                    continue;
                if (this.grid[c][r].isEmpty() != last.isEmpty() ) {
                    transitions++;
                    if (last.isEmpty())
                        last.setBlock();
                    else
                        last.setEmpty();
                }
            }
        }

        return transitions;
    }

    public int getHoles() {
        int count = 0;
        for(int c = 0; c < this.width; c++){
            boolean block = false;
            for(int r = 0; r < this.height; r++){
                if (this.grid[c][r].isBlock()) {
                    block = true;
                }else if (this.grid[c][r].isEmpty() && block){
                    count++;
                }
            }
        }
        return count;
    }

    public int removeLines() {
        int count = 0;
        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                if (!this.grid[c][r].isBlock())
                    break;
                if (c == this.width - 1) {
                    count++;
                    Cell temp;
                    for (int k = 0; k < this.width; k++) {
                        this.grid[k][r].setEmpty();
                        for (int l = r - 1; l >= 0; l--) {
                            temp = this.grid[k][l + 1];
                            this.grid[k][l + 1] = this.grid[k][l];
                            this.grid[k][l] = temp;
                        }
                    }
                }
            }
        }
        return count;
    }

    public double evaluate(Shape _setPiece, int myCombo){

        double score;

        score = (this.getHeight()-_setPiece.getLocation().getY()-_setPiece.getSize()/2) * -4.500158825082766
                + this.lines() * myCombo * 4.4181268101392694
                + this.getRowTransitions() * -3.2178882868487753
                + this.getColumnTransitions() * -9.348695305445199
                + this.getHoles() * -7.899265427351652
                + this.getWellSums() * -3.3855972247263626;

        return score;
    }

    public int getWellSums() {
        int well_sums = 0;

        for(int r = 0; r < this.height; r++){
            for(int c = 1; c < this.width-1; c++){
                if(this.grid[c][r].isEmpty() && this.grid[c-1][r].isBlock() && this.grid[c+1][r].isBlock()) {
                    well_sums++;
                    for (int k = r+1; k<this.height; k++)
                        if(this.grid[c][k].isEmpty())
                            well_sums++;
                        else
                            break;
                }
            }
        }
        for(int r = 0; r < this.height; r++){
            if(this.grid[0][r].isEmpty() && this.grid[1][r].isBlock()) {
                well_sums++;
                for (int k = r+1; k<this.height; k++)
                    if(this.grid[0][k].isEmpty())
                        well_sums++;
            }
        }

        for(int r = 0; r < this.height; r++){
            if(this.grid[this.width-1][r].isEmpty() && this.grid[this.width-2][r].isBlock()) {
                well_sums++;
                for (int k = r+1; k<this.height; k++)
                    if(this.grid[this.width-1][k].isEmpty())
                        well_sums++;
            }
        }

        return well_sums;
    }
}
