package io.github.zeroone3010.chesspgn;

import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;

record MoveOfGame(int halfMoveNumber, Move move, Game game) {
}
