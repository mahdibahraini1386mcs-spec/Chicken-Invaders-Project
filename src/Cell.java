public class Cell {
    private int row, col, x, y, counter;
    private String enemyType;

    public Cell(int row, int col, int x, int y, int counter, String enemyType) {
        this.row = row;
        this.col = col;
        this.x = x;
        this.y = y;
        this.counter = counter;
        this.enemyType = enemyType;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; } // این را اضافه کنید

    public int getY() { return y; }
    public void setY(int y) { this.y = y; } // این را اضافه کنید

    public int getCounter() { return counter; }
    public void decreaseCounter() { this.counter--; }

    public String getEnemyType() { return enemyType; }
}