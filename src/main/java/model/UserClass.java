package model;

/**
 * Created by Tjin on 6/18/2016.
 */
public class UserClass {
    private Integer classA;
    private Integer classB;
    private Integer classC;

    public UserClass(Integer classA, Integer classB, Integer classC){
        this.classA = classA;
        this.classB = classB;
        this.classC = classC;
    }

    public Integer getClassA() {
        return classA;
    }

    public void setClassA(Integer classA) {
        this.classA = classA;
    }

    public Integer getClassC() {
        return classC;
    }

    public void setClassC(Integer classC) {
        this.classC = classC;
    }

    public Integer getClassB() {
        return classB;
    }

    public void setClassB(Integer classB) {
        this.classB = classB;
    }
}
