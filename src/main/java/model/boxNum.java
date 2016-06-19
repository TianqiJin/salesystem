package model;

/**
 * Created by Tjin on 5/23/2016.
 */
public class BoxNum {
    private int box;
    private int residualTile;

    public BoxNum(boxNumBuilder builder){
        this.box = builder.box;
        this.residualTile = builder.residualTile;
    }

    public static class boxNumBuilder{
        private int box = 0;
        private int residualTile = 0;

        public boxNumBuilder boxNum(int boxNum){
            this.box = boxNum;
            return this;
        }
        public boxNumBuilder residualTileNum(int residualTileNum){
            this.residualTile = residualTileNum;
            return this;
        }
        public BoxNum build(){
            return new BoxNum(this);
        }
    }

    public int getBox() {
        return box;
    }

    public void setBox(int box) {
        this.box = box;
    }

    public int getResidualTile() {
        return residualTile;
    }

    public void setResidualTile(int residualTile) {
        this.residualTile = residualTile;
    }

//    public String toString(){
//        return new StringBuilder().append(this.box).append("-").append(this.residualTileNum).toString();
//    }
}
