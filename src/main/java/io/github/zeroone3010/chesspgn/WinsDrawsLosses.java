package io.github.zeroone3010.chesspgn;

record WinsDrawsLosses(int wins, int draws, int losses) {
  int total() {
    return wins + draws + losses;
  }

  float winPercentage() {
    return (float) wins / total();
  }

  float drawPercentage() {
    return (float) draws / total();
  }

  float lossPercentage() {
    return (float) losses / total();
  }

  @Override
  public String toString() {
    if (total() == 0) {
      return "not played";
    }
    return String.format("%d (%.0f%%) wins / %d (%.0f%%) draws / %d (%.0f%%) losses",
        wins, winPercentage() * 100f, draws, drawPercentage() * 100f, losses, lossPercentage() * 100f);
  }
}
