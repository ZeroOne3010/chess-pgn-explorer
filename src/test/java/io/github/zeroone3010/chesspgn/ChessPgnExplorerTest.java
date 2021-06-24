package io.github.zeroone3010.chesspgn;

import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChessPgnExplorerTest {
  @Test
  void simple() throws Exception {
    final ChessPgnExplorer explorer = new ChessPgnExplorer();
    explorer.setPlayerName("Player 1");
    explorer.parse(new PgnHolder(ChessPgnExplorerTest.class.getClassLoader().getResource("1_black_wins_time_forfeit.pgn").getFile()));
    final List<PositionStatistics> result = explorer.analyze("8/1R4pk/P6p/q7/8/8/1PP1n3/1K6 b - -");
    assertEquals(1, result.size());
    assertEquals(0, result.get(0).whiteStats().wins());
    assertEquals(0, result.get(0).whiteStats().draws());
    assertEquals(1, result.get(0).whiteStats().losses());
    assertEquals(0, result.get(0).blackStats().wins());
    assertEquals(0, result.get(0).blackStats().draws());
    assertEquals(0, result.get(0).blackStats().losses());
    assertEquals("Qxa6", result.get(0).move());
  }

  @Test
  void lastMove() throws Exception {
    final ChessPgnExplorer explorer = new ChessPgnExplorer();
    explorer.setPlayerName("Player 1");
    explorer.parse(new PgnHolder(ChessPgnExplorerTest.class.getClassLoader().getResource("1_black_wins_time_forfeit.pgn").getFile()));
    final List<PositionStatistics> result = explorer.analyze("8/1R4pk/q6p/8/8/8/1PP1n3/1K6 w - -");
    assertEquals(1, result.size());
    assertEquals(0, result.get(0).whiteStats().wins());
    assertEquals(0, result.get(0).whiteStats().draws());
    assertEquals(1, result.get(0).whiteStats().losses());
    assertEquals(0, result.get(0).blackStats().wins());
    assertEquals(0, result.get(0).blackStats().draws());
    assertEquals(0, result.get(0).blackStats().losses());
    assertEquals("", result.get(0).move());
  }
}
