package io.github.zeroone3010.chesspgn;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.bhlangonijr.chesslib.game.GameResult.BLACK_WON;
import static com.github.bhlangonijr.chesslib.game.GameResult.DRAW;
import static com.github.bhlangonijr.chesslib.game.GameResult.WHITE_WON;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class ChessPgnExplorer {

  private static final Predicate<MoveOfGame> BLACK_WON_PREDICATE = state -> Objects.equals(state.game().getResult(), BLACK_WON);
  private static final Predicate<MoveOfGame> WHITE_WON_PREDICATE = state -> Objects.equals(state.game().getResult(), WHITE_WON);
  private static final Predicate<MoveOfGame> DRAW_PREDICATE = state -> Objects.equals(state.game().getResult(), DRAW);

  private String playerName;

  private final Map<String, List<MoveOfGame>> states = new HashMap<>();
  private long numberOfGames = 0;

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  long parse(PgnHolder pgnHolder) throws Exception {
    numberOfGames = 0;
    states.clear();
    if (pgnHolder == null) {
      return 0;
    }

    final long startTime = System.currentTimeMillis();

    pgnHolder.loadPgn();

    for (final Game game : pgnHolder.getGames()) {
      numberOfGames++;
      game.loadMoveText();
      final MoveList moves = game.getHalfMoves();
      final Board board = new Board();
      for (int i = 0; i < moves.size(); i++) {
        final Move move = moves.get(i);
        final String currentFen = board.getFen(false);
        states.merge(currentFen, List.of(new MoveOfGame(i, move, game)), moveListMergeFunction());
        board.doMove(move);
      }
      final String currentFen = board.getFen(false);
      states.merge(currentFen, List.of(new MoveOfGame(moves.size(), null, game)), moveListMergeFunction());
    }
    return System.currentTimeMillis() - startTime;
  }

  private static BiFunction<List<MoveOfGame>, List<MoveOfGame>, List<MoveOfGame>> moveListMergeFunction() {
    return (a, b) -> {
      final List<MoveOfGame> result = new ArrayList<>();
      result.addAll(a);
      result.addAll(b);
      return result;
    };
  }

  List<PositionStatistics> analyze(String fen) throws Exception {
    final List<MoveOfGame> listOfMoves = states.get(fen);
    if (listOfMoves == null) {
      return List.of();
    }
    final Map<String, List<MoveOfGame>> movesInGames = listOfMoves.stream()
        .collect(groupingBy(state -> Optional.ofNullable(state.move()).map(Move::getSan).orElse("")));

    final Board board = new Board();
    board.loadFromFen(fen);

    return movesInGames.entrySet().stream()
        .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
        .map(entry -> {
          final String move = entry.getKey();
          final List<MoveOfGame> moves = entry.getValue();

          final int numAll = moves.size();
          final Map<MoveOfGame, Long> winsAsBlack = groupMoves(moves, playerIsBlack().and(BLACK_WON_PREDICATE));
          final Map<MoveOfGame, Long> lossesAsBlack = groupMoves(moves, playerIsBlack().and(WHITE_WON_PREDICATE));
          final Map<MoveOfGame, Long> drawsAsBlack = groupMoves(moves, playerIsBlack().and(DRAW_PREDICATE));

          final Map<MoveOfGame, Long> winsAsWhite = groupMoves(moves, playerIsWhite().and(WHITE_WON_PREDICATE));
          final Map<MoveOfGame, Long> lossesAsWhite = groupMoves(moves, playerIsWhite().and(BLACK_WON_PREDICATE));
          final Map<MoveOfGame, Long> drawsAsWhite = groupMoves(moves, playerIsWhite().and(DRAW_PREDICATE));

          return new PositionStatistics(move, numAll,
              new WinsDrawsLosses(winsAsWhite.size(), drawsAsWhite.size(), lossesAsWhite.size()),
              new WinsDrawsLosses(winsAsBlack.size(), drawsAsBlack.size(), lossesAsBlack.size()));
        })
        .collect(Collectors.toList());
  }

  public long getNumberOfGames() {
    return numberOfGames;
  }

  private Map<MoveOfGame, Long> groupMoves(List<MoveOfGame> moves, Predicate<MoveOfGame> predicate) {
    return moves.stream().filter(predicate).collect(groupingBy(identity(), counting()));
  }

  private Predicate<MoveOfGame> playerIsBlack() {
    return state -> Objects.equals(playerName, state.game().getBlackPlayer().getName());
  }

  private Predicate<MoveOfGame> playerIsWhite() {
    return state -> Objects.equals(playerName, state.game().getWhitePlayer().getName());
  }

  public static void main(String[] args) throws Exception {
    if (args == null || args.length != 2) {
      System.out.println("""
          This program requires two arguments:
            1. the name of the player
            2. the path to the PGN file""");
      return;
    }
    final ChessPgnExplorer explorer = new ChessPgnExplorer();
    explorer.setPlayerName(args[0]);
    final PgnHolder pgnHolder = new PgnHolder(args[1]);
    explorer.parse(pgnHolder);

    final MoveList moveList = new MoveList();
    moveList.loadFromSan("1. e4");
    final Board board = new Board();
    final String fen = moveList.getFen(0, false);
    final List<PositionStatistics> positionStatistics = explorer.analyze(fen);
    board.loadFromFen(fen);
    System.out.println(board.toString() + '\n');
    positionStatistics.forEach(System.out::println);
  }
}
