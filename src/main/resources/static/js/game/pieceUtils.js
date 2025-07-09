export function promote(piece) {
    return piece > 0 ? piece + 100 : piece - 100;
}

export function isPromoted(piece) {
    return Math.abs(piece) > 100; 
}

const pieceNameMap = {
    1: "fu", 2: "kyo", 3: "kei", 4: "gin", 5: "kin",
    6: "kaku", 7: "hisya", 77: "gyoku"
};

export function getPieceImage(piece) {
    if (piece === 0) return "";
    const abs = Math.abs(piece);
    const isProm = isPromoted(piece);
    const name = pieceNameMap[abs];
    const prefix = piece > 0 ? "sente" : "gote";
    return `<img src="/images/piece/${prefix}_${isProm ? "promoted_" + name : name}.png" class="piece-image ${prefix}-image" />`;
}

export function toUnpromoted(piece) {
    if (!isPromoted(piece)) return piece;
    return piece - 100 * Math.sign(piece);
}

export function isPromotionZone(piece, from, to) {
    const kind = Math.abs(piece);
    const promotable = [1, 2, 3, 4, 6, 7];

    if (!promotable.includes(kind)) return false;

    if (piece > 0) {
        return from[1] <= 2 || to[1] <= 2;
    } else {
        return from[1] >= 6 || to[1] >= 6;
    }
    
}