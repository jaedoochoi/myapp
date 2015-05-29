package jdchoi.nextree.co.kr.railalarm.enm;

/**
 * Created by Administrator on 2015-05-29.
 */
public enum Line {
    ONE("1", "1호선"),
    TWO("2", "2호선"),
    THREE("3", "3호선"),
    FOUR("4", "4호선");

    Line(String no, String name){
        this.no = no;
        this.name = name;
    }

    private String no;
    private String name;

    public String getName() {
        return name;
    }

    public String getNo() {
        return no;
    }
}
