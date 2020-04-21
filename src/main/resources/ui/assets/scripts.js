
const WIDTH = 22
const HEIGHT = 22
const SATURATION = 0.5

var tick = 0
var playing = null

function createGrid(width, height, points) {
    const parent = document.getElementById("world")
    parent.textContent = '';

    for (var i = 1; i <= height ; i++) {
        for (var j = 1; j <= width; j++) {
            var cell = document.createElement('div');
            if (points && points.some(p => p.x === j && p.y === i)){
              cell.classList.add("alive");
            }
            parent.appendChild(cell);
        }
    }
}

function getSeed() {
  return document.getElementById("seed").textContent;
}

function populate(tick) {
  fetch(`http://localhost:8080/seed/${getSeed()}/${SATURATION}/${WIDTH}/${HEIGHT}/${tick}`).then(r => r.json())
  .then(data => {
    createGrid(WIDTH, HEIGHT, data.cells);
  })
  .catch(e => console.log(e))
}

function updateTick(tick) {
  document.getElementById("tick").textContent = `${tick}`;
}

function back() {
  tick = Math.max(0, tick - 1)
  populate(tick)
  updateTick(tick)
}

function forward() {
  tick = tick + 1
  populate(tick)
  updateTick(tick)
}

function play(s) {
  s.send('{"event": "play" }');
}

function pause() {
  if (playing) {
    clearInterval(playing)
  }
}

function stop(s) {
  tick = 0;
  updateTick(tick)
  populate(tick);
  s.send('{"event": "stop" }');
}


window.onload = function(){

  createGrid(WIDTH, HEIGHT);
  document.getElementById("forward").onclick = forward
  document.getElementById("back").onclick = back
  document.getElementById("pause").onclick = pause
  document.getElementById("play").onclick = () => play(s)
  document.getElementById("stop").onclick = () => stop(s)

  var s = new WebSocket("ws://localhost:8080/socket");
  s.onmessage = function (event) {
    console.log(event.data);
  }

  populate(tick);
}
