package io.github.zeroone3010.chesspgn;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import io.javalin.Javalin;
import io.javalin.http.Handler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class ChessPgnExplorerServer {

  public static void main(String[] args) throws Exception {
    if (args != null && (args.length != 2 && args.length != 0)) {
      System.out.println("""
          This program requires either zero or two arguments.
          Either:
            1. the name of the player and
            2. the path to the PGN file
          Or:
            No arguments at all, so the data can be entered using the web form.""");
      return;
    }
    final ChessPgnExplorer explorer = new ChessPgnExplorer();
    if (args != null && args.length == 2) {
      explorer.setPlayerName(args[0]);
      final PgnHolder pgnHolder = new PgnHolder(args[1]);
      final long parseTime = explorer.parse(pgnHolder);
      System.out.println("Positions for player " + args[0] + " parsed in " + parseTime + " ms.");
    }

    final Javalin app = Javalin.create().start(7000);
    app.config.addStaticFiles("/files");
    app.get("/", handleIndexPage());
    app.post("/", handlePgnFilePost(explorer));
    app.get("/moves/*", handleMoves(explorer));
    app.exception(Exception.class, (e, ctx) -> {
      e.printStackTrace();
      ctx.render("/500.mustache", Map.of("exception", e.getMessage()));
    });
  }

  private static Handler handleIndexPage() {
    return ctx -> {
      ctx.render("/index.mustache");
    };
  }

  private static Handler handleMoves(final ChessPgnExplorer explorer) {
    return ctx -> {
      final String rawMoves = ctx.splat(0) != null ? ctx.splat(0) : "";
      final MoveList moveList = new MoveList();
      moveList.loadFromSan(rawMoves);
      final Board board = new Board();
      final String fen = moveList.getFen();
      if (fen != null) {
        board.loadFromFen(fen);
      }
      final List<PositionStatistics> stats = explorer.analyze(board.getFen(false));

      final Map<String, Object> params = new HashMap<>();
      params.put("stats", stats);
      params.put("allPossibleMoves", "['" + stats.stream().map(PositionStatistics::move).collect(joining("', '")) + "']");
      params.put("moves", moveList.toSanWithMoveNumbers());
      params.put("numberOfGames", explorer.getNumberOfGames());
      params.put("fen", board.getFen(false));
      Arrays.stream(Square.values()).forEach(square ->
          params.put(square.value().toLowerCase(Locale.ROOT),
              board.getPiece(square) != Piece.NONE
                  ? board.getPiece(square).getFanSymbol()
                  : ""));

      ctx.render("/game.mustache", params);
    };
  }

  private static Handler handlePgnFilePost(final ChessPgnExplorer explorer) {
    return ctx -> {
      ctx.uploadedFiles().forEach(file -> {
        try {
          final File tempFile = File.createTempFile("chess-pgn-explorer-", "-upload");
          file.getContent().transferTo(new FileOutputStream(tempFile, false));
          final PgnHolder uploadedPgnHolder = new PgnHolder(tempFile.getAbsolutePath());
          System.out.println("Temp file " + tempFile.getAbsolutePath() + " created.");
          final long totalGames = uploadedPgnHolder.countGamesInPgnFile();
          System.out.println("\tThe file has " + totalGames + " games.");
          final String player = ctx.formParam("player-name");
          explorer.setPlayerName(player);
          final long parsingTime = explorer.parse(uploadedPgnHolder);
          final long gamesPerSecond = (long) ((double) totalGames / (double) parsingTime * 1000d);
          System.out.println(String.format("Games for '%s' loaded and parsed in %d ms (that's %d games per second).\n", player,
              parsingTime, gamesPerSecond));
          ctx.redirect("/moves/");
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      });
    };
  }
}
