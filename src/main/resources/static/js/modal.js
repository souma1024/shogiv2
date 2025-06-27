document.getElementById('openModalBtn').addEventListener('click', () => {
    document.getElementById('modal').classList.remove('hidden');
});

document.getElementById('closeModalBtn').addEventListener('click', () => {
     document.getElementById('modal').classList.add('hidden');
});

document.getElementById('createRoomForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const timeLimit = e.target.timeLimit.value;
    console.log('ルーム作成: 持ち時間 = ' + timeLimit + '分');

    // AjaxやFetchでSpring Bootにルーム作成リクエストを送る処理を追加してもOK
    document.getElementById('modal').classList.add('hidden');
});
