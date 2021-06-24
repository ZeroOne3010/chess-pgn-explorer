var SpeechRecognition = SpeechRecognition || webkitSpeechRecognition
var SpeechGrammarList = SpeechGrammarList || webkitSpeechGrammarList
var SpeechRecognitionEvent = SpeechRecognitionEvent || webkitSpeechRecognitionEvent

const notation = {'K': 'King ', 'Q': 'Queen ', 'R': 'Rook ', 'N': 'Knight ', 'B': 'Bishop ',
  'x': ' takes ', '+': ' check', 'O-O': 'Castle king side', 'O-O-O': 'Castle queen side'};

function setMoves(moves) {
  var grammar = '#JSGF V1.0; grammar moves; public <move> = ' + moves.map(move => {
    Object.keys(notation).forEach(key => {move = move.replace(key, notation[key])});
    return move;
  }).join(' | ') + ' ;'
  console.log("Grammar set: ", grammar);

  var recognition = new SpeechRecognition();
  var speechRecognitionList = new SpeechGrammarList();
  speechRecognitionList.addFromString(grammar, 1);
  recognition.grammars = speechRecognitionList;
  recognition.continuous = false;
  recognition.lang = 'en-US';
  recognition.interimResults = false;
  recognition.maxAlternatives = 1;

  document.body.onclick = function() {
    recognition.start();
    console.log('Ready to receive a move.');
  }

  recognition.onresult = function(event) {
    var move = event.results[0][0].transcript;
    console.log('Move received: ' + move + '; Confidence: ' + event.results[0][0].confidence);
    move = move.toLowerCase();
    Object.entries(notation).forEach(entry => {
      const key = entry[0];
      const value = entry[1];
      move = move.replace(value.toLowerCase().trimStart(), key);
    });
    if(moves.includes(move)) {
      console.log('Valid move: ' + move);
      window.location.href = window.location + ' ' + move;
    } else {
      console.log('Unknown move: ' + move);
    }
  }

  recognition.onspeechend = function() {
    recognition.stop();
  }

  recognition.onnomatch = function(event) {
    console.log("Move not recognized.");
  }

  recognition.onerror = function(event) {
    console.log('Error occurred in recognition: ' + event.error);
  }
};

