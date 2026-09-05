package com.entropire.simplefactions.chat.font;

public enum MinecraftFont {
    A('A', 5),
    B('B', 5),
    C('C', 5),
    D('D', 5),
    E('E', 5),
    F('F', 5),
    G('G', 5),
    H('H', 5),
    I('I', 3),
    J('J', 5),
    K('K', 5),
    L('L', 5),
    M('M', 5),
    N('N', 5),
    O('O', 5),
    P('P', 5),
    Q('Q', 5),
    R('R', 5),
    S('S', 5),
    T('T', 5),
    U('U', 5),
    V('V', 5),
    W('W', 5),
    X('X', 5),
    Y('Y', 5),
    Z('Z', 5),
    a('a', 5),
    b('b', 5),
    c('c', 5),
    d('d', 5),
    e('e', 5),
    f('f', 4),
    g('g', 5),
    h('h', 5),
    i('i', 1),
    j('j', 5),
    k('k', 5),
    l('l', 1),
    m('m', 5),
    n('n', 5),
    o('o', 5),
    p('p', 5),
    q('q', 5),
    r('r', 5),
    s('s', 5),
    t('t', 3),
    u('u', 5),
    v('v', 5),
    w('w', 5),
    x('x', 5),
    y('y', 5),
    z('z', 5),
    ZERO('0', 5),
    ONE('1', 5),
    TWO('2', 5),
    THREE('3', 5),
    FOUR('4', 5),
    FIVE('5', 5),
    SIX('6', 5),
    SEVEN('7', 5),
    EIGHT('8', 5),
    NINE('9', 5),
    SPACE(' ', 4),
    LEFT_PARENTHESIS('(', 3),
    RIGHT_PARENTHESIS(')', 3),
    DASH('-', 5),
    SLASH('/', 5),
    BACKSLASH('\\', 5),
    UNDERSCORE('_', 5),
    EXCLAMATION('!', 1),
    QUESTION('?', 5),
    BULLET('•', 5),
    DOT('.', 1),
    COMMA(',', 1),
    COLON(':', 1),
    SEMICOLON(';', 1),
    PLUS('+', 5),
    EQUALS('=', 5),
    ASTERISK('*', 5),
    QUOTE('"', 3),
    APOSTROPHE('\'', 1),
    HORIZONTAL_LINE('─', 6),
    UNKNOWN('?', 5);

    private final char character;
    private final int width;


    MinecraftFont(char character, int width) {
        this.character = character;
        this.width = width;
    }


    public char getCharacter() {
        return character;
    }


    public int getWidth() {
        return width;
    }


    public static MinecraftFont get(char c) {
        for (MinecraftFont font : values()) {
            if (font.character == c) {
                return font;
            }
        }

        return UNKNOWN;
    }
}