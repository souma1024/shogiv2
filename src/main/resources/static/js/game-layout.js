const board = document.getElementById('shogiBoard');

// セルを動的に生成（81個）
for (let y = 1; y <= 9; y++) {
  for (let x = 1; x <= 9; x++) {
    const cell = document.createElement('div');
    cell.className = 'cell';
    cell.dataset.x = x;
    cell.dataset.y = y;
    cell.addEventListener('click', () => {
      console.log(`Clicked cell (${x}, ${y})`);
    });
    board.appendChild(cell);
  }
}
