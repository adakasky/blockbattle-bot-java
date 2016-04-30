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

import field.Field;
import field.Shape;
import field.ShapeType;
import moves.MoveType;

/**
 * BotStarter class
 * <p>
 * This class is where the main logic should be. Implement getMoves() to
 * return something better than random moves.
 *
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class BotStarter {

    public BotStarter() {
    }

    /**
     * Returns a random amount of random moves
     *
     * @param state   : current state of the bot
     * @param timeout : time to respond
     * @return : a list of moves to execute
     */
    public ArrayList<MoveType> getMoves(BotState state, long timeout) {
        ArrayList<MoveType> moves = new ArrayList<MoveType>();

        Field field = state.getMyField();
        ShapeType current = state.getCurrentShape();
        ShapeType next = state.getNextShape();
        int myCombo = state.getMyCombo();

        Shape piece = new Shape(current, field, state.getShapeLocation());
        Shape nextPiece = new Shape(next, field, (next == ShapeType.O) ? new Point(4, -1) : new Point(3, -1));

        Best best = getBestFitness(field, piece, myCombo, nextPiece);

        int bestRot = best.bestRot;
        int bestLeft = best.bestLeft;

        for (; bestRot > 0; bestRot--)
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

    Best getBestFitness(Field field, Shape piece, int combo, Shape nextPiece) {

        Best best = new Best();
        best.fitness = -1000;

        for (int rotation = 0; rotation < 4; rotation++) {
            int left = 0;

            if (rotation != 0)
                piece.turnRight();

            Shape piece_copy = piece.clone();
            while (field.hasLeft(piece_copy)) {
                piece_copy.oneLeft();
                left++;
            }

            while (field.isValid(piece_copy)) {
                Shape piece_tmp = piece_copy.clone();

                while (!field.reachedBottom(piece_tmp)) {
                    piece_tmp.oneDown();
                }

                if (field.isValidTop(piece_tmp)) {

                    double fitness;

                    Field field_copy = field.clone();
                    field_copy.addPiece(piece_tmp);

                    fitness = field_copy.fitness(piece_tmp, combo * 2);

                    if (nextPiece != null) {
                        int removed = field_copy.removeLines();
                        Shape next = nextPiece.clone();
                        Best secondBest = getBestFitness(field_copy, next, combo + removed, null);
                        fitness += secondBest.fitness;
                    }

                    if (fitness >= best.fitness || best.fitness == 0.0) {

                        best.fitness = fitness;
                        best.bestLeft = left;
                        best.bestRot = rotation;
                    }
                }

                left--;
                piece_copy.oneRight();
            }
        }
        return best;
    }

    private class Best {
        double fitness;
        int bestLeft;
        int bestRot;
    }

    public static void main(String[] args) {
        BotParser parser = new BotParser(new BotStarter());
        parser.run();
    }
}
