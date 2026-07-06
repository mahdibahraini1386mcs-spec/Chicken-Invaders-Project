public class Cell {
    private int row;
    private int col;
    private int x;
    private int y;
    private int counter;
    private String enemyType;

    public Cell(int row, int col, int x, int y, int counter, String enemyType) {
        this.row = row;
        this.col = col;
        this.x = x;
        this.y = y;
        this.counter = counter;
        this.enemyType = enemyType;
    }

    public void decreaseCounter() {
        if (counter > 0) {
            counter--;
        }
    }

    public int getCounter() {
        return counter;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getEnemyType() {
        return enemyType;
    }
}