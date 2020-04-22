const WIDTH = 22
const HEIGHT = 22
const SATURATION = 0.5

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
  return parseInt(document.getElementById("seed").textContent);
}

function getSpeed() {
  return parseInt(document.getElementById("speed").textContent);
}

function populate(cells) {
  createGrid(WIDTH, HEIGHT, cells);
}

function updateTick(tick) {
  document.getElementById("tick").textContent = `${tick}`;
}

function send(s, event) {
  s.send(`{"event": "${event}"}`);
}

window.onload = function(){

  createGrid(WIDTH, HEIGHT);
  document.getElementById("forward").onclick = () => send(s, "forward");
  document.getElementById("back").onclick = () => send(s, "back");
  document.getElementById("pause").onclick = () => send(s, "pause");
  document.getElementById("play").onclick = () => send(s, "play");
  document.getElementById("stop").onclick = () => send(s, "stop");

  const s = new WebSocket("ws://localhost:8080/socket");
  s.onopen = function(event) {
    const payload = {
      width: WIDTH,
      height: HEIGHT,
      seed: getSeed(),
      speed: getSpeed(),
      saturation: SATURATION
    }
    const data = {
      event: "control",
      payload: payload
    }
    s.send(JSON.stringify(data));
  }
  s.onmessage = function (event) {
    if (event.data) {
      const world = JSON.parse(event.data)
      updateTick(world.tick)
      populate(world.cells)
    }
  }

}
