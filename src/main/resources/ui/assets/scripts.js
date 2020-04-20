
const WIDTH = 22
const HEIGHT = 22
const SEED = 123456
const SATURATION = 0.5

var tick = 0

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

function populate(tick) {
  fetch(`http://localhost:8080/seed/${SEED}/${SATURATION}/${WIDTH}/${HEIGHT}/${tick}`).then(r => r.json())
  .then(data => {
    createGrid(WIDTH, HEIGHT, data.cells);
  })
  .catch(e => console.log(e))
}

function back() {
  tick = Math.max(0, tick - 1)
  populate(tick)
}

function forward() {
  tick = tick + 1
  populate(tick)
}

createGrid(WIDTH, HEIGHT);
populate(tick);

window.onload = function(){
  document.getElementById("forward").onclick = forward
  document.getElementById("back").onclick = back
}
