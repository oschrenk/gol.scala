window.onload = function(){
  const WIDTH = 22
  const HEIGHT = 22
  const SATURATION = 0.5

  const protocol = location.hostname == "localhost" ? "ws" : "wss"
  const host = `${location.hostname}${location.port ? ':'+location.port: ''}`

  var drawing = false
  var color = "black"
  var lastElement = undefined

  function createGrid(width, height, points) {
    const parent = document.getElementById("world")
    parent.textContent = '';

    for (var x = 1; x <= height ; x++) {
      for (var y = 1; y <= width; y++) {
        var cell = document.createElement('div');
        cell.classList.add("cell");
        if (points && points.some(p => p.x === x && p.y === y)){
          cell.classList.add("alive");
        } else {
          cell.classList.add("dead");
        }
        cell.dataset.x = x
        cell.dataset.y = y
        parent.appendChild(cell);
      }
    }
  }

  function getCells() {
    const parent = document.getElementById("world")
    const cells = [...parent.getElementsByClassName("alive")].map( c => {
      return {
        x: c.dataset.x,
        y: c.dataset.y,
      }
    })
    return { cells: cells }
  }

  function populate(cells) {
    createGrid(WIDTH, HEIGHT, cells);
  }

  function getSeed() {
    const seed = document.getElementById("seed").textContent
    return seed === "custom" ? "custom" : parseInt(seed)
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

  function getControl() {
    return {
      width: WIDTH,
      height: HEIGHT,
      seed: getSeed(),
      speed: getSpeed(),
      saturation: SATURATION,
      tick: getTick()
    }
  }

  function send(s, event, payload) {
    const data = {
      event : event,
      ...(payload ? { payload: payload } : {})
    }
    s.send(JSON.stringify(data));
  }

  function registerEditable(socket, id, fetch, update) {
    document.getElementById(id).addEventListener("focus", (e) => {
      send(socket, "pause");
      const oldValue = fetch()

      document.getElementById(id).addEventListener("blur", (e) => {
        const currentValue = fetch()
        if (Number.isInteger(currentValue) || currentValue === "custom") {
          if (oldValue != currentValue) {
            send(socket, "control", getControl())
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

  function update(state) {
    updateTick(state.world.tick)
    updateSpeed(state.rules.speed)
    const seed = state.rules.seed.random ? state.rules.seed.random.value : "custom"
    updateSeed(seed)
    populate(state.world.cells)
  }

  const s = new WebSocket(`${protocol}:${host}/socket`);
  s.onopen = function(_) {
    send(s, "control", getControl());
  }

  s.onmessage = function (event) {
    if (event.data) {
      update(JSON.parse(event.data))
    }
  }
  s.onerror = function (event) {
    console.log(event)
  }

  document.addEventListener('mousemove', function(e) {
    if (drawing) {
      const currentElement = document.elementFromPoint(e.pageX, e.pageY);
      if (lastElement !== currentElement) {
        lastElement = currentElement
        if (currentElement.classList.contains("cell")) {
          if (color === "white") {
            currentElement.classList.remove('alive');
            currentElement.classList.add('dead');
          }
          if (color === "black") {
            currentElement.classList.remove('dead');
            currentElement.classList.add('alive');
          }
        }
      }
    }
  })

  document.addEventListener('mousedown', function(e) {
    send(s, "pause");
    const currentElement = document.elementFromPoint(e.pageX, e.pageY);
    lastElement = currentElement
    if (currentElement && currentElement.classList.contains("cell")) {
      if (color === "white") {
        currentElement.classList.remove('alive');
        currentElement.classList.add('dead');
      }
      if (color === "black") {
        currentElement.classList.remove('dead');
        currentElement.classList.add('alive');
      }
      // only start drawing in grid
      drawing = true
    }
  })

  document.addEventListener('mouseup', function(e) {
    if (drawing) {
      drawing = false
      lastElement = undefined
      send(s, "painted", getCells())
    }
  })

  function clear(s) {
    [...document.getElementsByClassName("cell")].forEach( c => {
      c.classList.remove('alive');
      c.classList.add('dead');
    })
    send(s, "painted", getCells());
  }

  function drawDead() {
    color = "white"
    document.getElementById("black").classList.remove("selected");
    document.getElementById("white").classList.add("selected");
  }

  function drawAlive() {
    color = "black"
    document.getElementById("white").classList.remove("selected");
    document.getElementById("black").classList.add("selected");
  }

  // initialize grid
  createGrid(WIDTH, HEIGHT);

  // register buttons
  document.getElementById("forward").onclick = () => send(s, "forward");
  document.getElementById("back").onclick = () => send(s, "back");
  document.getElementById("pause").onclick = () => send(s, "pause");
  document.getElementById("play").onclick = () => send(s, "play");
  document.getElementById("stop").onclick = () => send(s, "stop");
  document.getElementById("clear").onclick = () => clear(s);
  document.getElementById("white").onclick = () => drawDead();
  document.getElementById("black").onclick = () => drawAlive();

  // regist editables
  registerEditable(s, "tick", getTick, updateTick)
  registerEditable(s, "seed", getSeed, updateSeed)
  registerEditable(s, "speed", getSpeed, updateSpeed)
}
