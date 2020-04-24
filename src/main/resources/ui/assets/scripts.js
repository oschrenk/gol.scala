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

function populate(cells) {
  createGrid(WIDTH, HEIGHT, cells);
}

function getSeed() {
  return parseInt(document.getElementById("seed").textContent);
}

function getSpeed() {
  return parseInt(document.getElementById("speed").textContent);
}

function getTick() {
  return parseInt(document.getElementById("tick").textContent);
}

function updateTick(tick) {
  document.getElementById("tick").textContent = `${tick}`;
}

function updateSeed(seed) {
  document.getElementById("seed").textContent = `${seed}`;
}

function updateSpeed(speed) {
  document.getElementById("speed").textContent = `${speed}`;
}

function send(s, event) {
  s.send(`{"event": "${event}"}`);
}

function registerEditable(socket, id, fetch, update) {
  document.getElementById(id).addEventListener("focus", (e) => {
    send(socket, "pause");
    const oldValue = fetch()

    document.getElementById(id).addEventListener("blur", (e) => {
      const currentValue = fetch()
      if (Number.isInteger(currentValue)) {
        if (oldValue != currentValue) {
          const payload = {
            width: WIDTH,
            height: HEIGHT,
            seed: getSeed(),
            speed: getSpeed(),
            saturation: SATURATION,
            tick: getTick()
          }
          const data = {
            event: "control",
            payload: payload
          }
          socket.send(JSON.stringify(data));
        }
      } else {
        update(oldValue) // default to old value
      }
    }, { once: true })
  })
  document.getElementById(id).addEventListener('keypress', (e) => {
    if (e.which === 13) {
      e.preventDefault();
    }
  });
}

window.onload = function(){

  createGrid(WIDTH, HEIGHT);
  document.getElementById("forward").onclick = () => send(s, "forward");
  document.getElementById("back").onclick = () => send(s, "back");
  document.getElementById("pause").onclick = () => send(s, "pause");
  document.getElementById("play").onclick = () => send(s, "play");
  document.getElementById("stop").onclick = () => send(s, "stop");

  const s = new WebSocket(`ws://${location.hostname}${location.port ? ':'+location.port: ''}/socket`);
  s.onopen = function(event) {
    const payload = {
      width: WIDTH,
      height: HEIGHT,
      seed: getSeed(),
      speed: getSpeed(),
      saturation: SATURATION,
      tick: 0
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

  registerEditable(s, "tick", getTick, updateTick)
  registerEditable(s, "seed", getSeed, updateSeed)
  registerEditable(s, "speed", getSpeed, updateSpeed)

}
