export function promote(piece) {
    return piece + 100 * Math.sign(piece);
}

export function isPromoted(piece) {
    return Math.abs(piece) > 100; 
}

const pieceNameMap = {
    1: "fu", 2: "kyo", 3: "kei", 4: "gin", 5: "kin",
    6: "kaku", 7: "hisya", 8: "uma", 9: "ryu", 77: "gyoku"
};

export function getPieceImage(piece) {
    if (piece === 0) return "";
    const abs = Math.abs(piece);
    const isProm = isPromoted(piece);
    const base = isProm ? abs - 100 : abs;
    const name = pieceNameMap[base];
    const prefix = piece > 0 ? "sente" : "gote";
    return `<img src="/images/piece/${prefix}_${isProm && base !== 5 ? "promoted_" + name : name}.png" class="piece-image ${prefix}-image" />`;
}

export function toUnpromoted(piece) {
    if (!isPromoted(piece)) return piece;
    return piece - 100 * Math.sign(piece);
}