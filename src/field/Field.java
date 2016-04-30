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

/**
 * Field class
 * <p>
 * Represents the playing field for one player.
 * Has some basic methods already implemented.
 *
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class Field {

    private int width;
    private int height;
    private Cell grid[][];

    public Field(int width, int height, String fieldString) {
        this.width = width;
        this.height = height;
        parse(fieldString);
    }

    /**
     * Parses the input string to get a grid with Cell objects
     *
     * @param fieldString : input string
     */
    private void parse(String fieldString) {

        this.grid = new Cell[this.width][this.height];

        // get the separate rows
        String[] rows = fieldString.split(";");
        for (int y = 0; y < this.height; y++) {
            String[] rowCells = rows[y].split(",");

            // parse each cell of the row
            for (int x = 0; x < this.width; x++) {
                int cellCode = Integer.parseInt(rowCells[x]);
                this.grid[x][y] = new Cell(x, y, CellType.values()[cellCode]);
            }
        }
    }

    private String newState() {
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
                    case BLOCK:
                        value = "2";
                        break;
                    case SOLID:
                        value = "3";
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
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
            return null;
        return this.grid[x][y];
    }

    private void setCell(Cell cell) {
        int x = (int) cell.getLocation().getX();
        int y = (int) cell.getLocation().getY();
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
            return;
        this.grid[x][y].setBlock();
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void addPiece(Shape piece) {
        for (Cell single : piece.getBlocks()) {
            setCell(single);
        }
    }

    private boolean isLine(int row) {
        for (int c = 0; c < this.width; c++) {
            if (this.grid[c][row].isEmpty() || this.grid[c][row].isSolid()) {
                return false;
            }
        }
        return true;
    }

    public int lines() {
        int count = 0;
        for (int r = 0; r < this.height; r++) {
            if (this.isLine(r)) {
                count++;
            }
        }
        return count;
    }

    public boolean hasLeft(Shape piece) {
        Shape tempPiece = piece.clone();
        tempPiece.oneLeft();
        Cell[] tempBlocks = tempPiece.getBlocks();
        for (Cell single : tempBlocks) {
            if (single.hasCollision(this) || single.isOutOfBoundaries(this))
                return false;
        }
        return true;
    }

    public boolean isValid(Shape piece) {
        Cell[] tempBlocks = piece.getBlocks();
        for (Cell single : tempBlocks) {
            if (single.hasCollision(this) || single.isOutOfBoundaries(this))
                return false;
        }
        return true;
    }

    public boolean isValidTop(Shape piece) {
        Cell[] tempBlocks = piece.getBlocks();
        for (Cell single : tempBlocks) {
            if (single.hasCollision(this) || single.isOutOfBoundaries(this) || single.isOutOfTop())
                return false;
        }
        return true;
    }

    public boolean reachedBottom(Shape piece) {
        Shape tempPiece = piece.clone();
        tempPiece.oneDown();
        Cell[] tempBlocks = tempPiece.getBlocks();
        for (Cell single : tempBlocks) {
            if (single.hasCollision(this) || single.isOutOfBoundaries(this))
                return true;
        }
        return false;
    }

    public Field clone() {
        return new Field(this.width, this.height, this.newState());
    }

    public int getHoles() {
        int count = 0;
        for (int c = 0; c < this.width; c++) {
            boolean block = false;
            for (int r = 0; r < this.height; r++) {
                if (this.grid[c][r].isBlock()) {
                    block = true;
                } else if (this.grid[c][r].isEmpty() && block) {
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

    public double fitness(Shape piece, int myCombo) {
        return  (this.getHeight() - piece.getLocation().getY() - piece.getSize()) * -5
                + this.lines() * myCombo * 3
                + this.getHoles() * -10;
    }
}
