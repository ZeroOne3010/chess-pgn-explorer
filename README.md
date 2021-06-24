Chess PGN Analyzer
==================

This application reads in a [PGN](https://en.wikipedia.org/wiki/Portable_Game_Notation) file of chess games,
generates a tree of moves from all the games in it, and allows you to browse it
using a web interface. The games are observed from the point of view of a given
player (let's say you), so you can see how you fare in a certain position as both
black and white.

For example, assume the opening 1.e4 d5. The application starts from the initial
position, and you would then click on those moves to advance the positions. The
application will then show you how many times you have been in this position
as both black and white player, and how those games have ended for you (win/draw/loss).

I created this application in order to study my openings, i.e. whether I fall
into the same traps over and over again, or if I play some opening badly as white
whereas other players would always play it better if I was playing as black.

This application uses Java 16 and the
[Javalin](https://github.com/tipsy/javalin) and
[Chesslib](https://github.com/bhlangonijr/chesslib) libraries.
On Chrome, the Web Speech API is supported, so you can just click on the page
and utter the move you'd like to make (such as "queen takes c5" for "Qxc5"),
and the speech recognition thingie will hopefully understand you and advance the situation.

