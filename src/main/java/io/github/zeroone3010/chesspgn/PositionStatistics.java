package io.github.zeroone3010.chesspgn;

public record PositionStatistics(String move,
                                 int totalNumberOfGames,
                                 WinsDrawsLosses whiteStats,
                                 WinsDrawsLosses blackStats) {
}
