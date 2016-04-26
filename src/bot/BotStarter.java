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

package bot;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import field.Field;
import field.Shape;
import field.ShapeType;
import moves.MoveType;

/**
 * BotStarter class
 * 
 * This class is where the main logic should be. Implement getMoves() to
 * return something better than random moves.
 * 
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class BotStarter {

	public BotStarter() {}
	
	/**
	 * Returns a random amount of random moves
	 * @param state : current state of the bot
	 * @param timeout : time to respond
	 * @return : a list of moves to execute
	 */
	public ArrayList<MoveType> getMoves(BotState state, long timeout) {
		ArrayList<MoveType> moves = new ArrayList<MoveType>();

		Field grid = state.getMyField();
		ShapeType workingPiece = state.getCurrentShape();
		ShapeType workingNextPiece = state.getNextShape();
		int myCombo = state.getMyCombo();

        Shape piece = new Shape(workingPiece, grid, state.getShapeLocation());
        Shape nextPiece = new Shape(workingNextPiece, grid, (workingNextPiece == ShapeType.O) ? new Point(4, -1) : new Point(3, -1));

        Best best = getBestLookahead(grid, piece, myCombo, nextPiece);

        int bestRotation = best.bestRotation;
        int bestLeft = best.bestLeft;

        for (; bestRotation > 0; bestRotation--)
            moves.add(MoveType.TURNRIGHT);
        if (bestLeft < 0)
            for (; bestLeft < 0; bestLeft++)
                moves.add(MoveType.RIGHT);
        else
            for (; bestLeft > 0; bestLeft--)
                moves.add(MoveType.LEFT);

        moves.add(MoveType.DROP);
        return moves;
	}

    Best getBestLookahead(Field grid, Shape piece, int combo, Shape nextPiece) {

        Best best = new Best();
        best.score = -1000;
        
        for(int rotation = 0; rotation < 4; rotation++) {
            int left = 0;
            
            if(rotation !=0)
                piece.turnRight();
            
            Shape _piece = piece.clone();
            while(grid.canMoveLeft(_piece)){
                _piece.oneLeft();
                left++;
            }
            
            while(grid.isValid(_piece)){
                Shape _setPiece = _piece.clone();
                
                while(grid.canMoveDown(_setPiece)){
                    _setPiece.oneDown();
                }

                if(grid.isValidTop(_setPiece)) {

                    double score;
                    int totalPoints;

                    Field _grid = grid.clone();
                    _grid.addPiece(_setPiece);
                    
                    score = _grid.evaluate(_setPiece, combo * 2);

                    totalPoints = _grid.lines() + combo;
                    
                    if (nextPiece != null) {
                        int removed = _grid.removeLines();
                        Shape next = nextPiece.clone();
                        Best secondBest = getBestLookahead(_grid, next, combo + removed, null);
                        score += secondBest.score;
                        totalPoints += secondBest.points;
                    }
                    
                    if (score >= best.score || best.score == 0.0) {

                        best.score = score;
                        best.bestLeft = left;
                        best.bestRotation = rotation;
                    }
                }

                left--;
                _piece.oneRight();
            }
        }
        return best;
    }

    private class Best {
        double score;
        int bestLeft;
        int bestRotation;
        int points;

        public void Best() {
            this.score = 0.0;
            this.bestLeft = 0;
            this.bestRotation = 0;
            this.points = 0;
        }
    }
	
	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}
}
