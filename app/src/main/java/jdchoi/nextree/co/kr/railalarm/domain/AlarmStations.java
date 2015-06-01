package jdchoi.nextree.co.kr.railalarm.domain;

import java.util.List;

/**
 * Created by Administrator on 2015-06-01.
 */
public class AlarmStations {
    /***
     * 알림시간에 포함된 역들의 ID의 리스트
     */
    private List<String> stId;
    /***
     * 알림시간에 포함된 역들의 Name의 리스트
     */
    private List<String> stName;
    /***
     * 알림시간에 포함된 역들의 EngName의 리스트
     */
    private List<String> stEngName;
    /***
     * 알림시간에 포함된 역들의 소요시간의 리스트
     */
    private List<Integer> etime;
    /***
     * 알림시간에 포함된 역들의 특별소요시간의 리스트
     */
    private List<Integer> extime;

    public List<Integer> getEtime() {
        return etime;
    }

    public void setEtime(List<Integer> etime) {
        this.etime = etime;
    }

    public List<Integer> getExtime() {
        return extime;
    }

    public void setExtime(List<Integer> extime) {
        this.extime = extime;
    }

    public List<String> getStEngName() {
        return stEngName;
    }

    public void setStEngName(List<String> stEngName) {
        this.stEngName = stEngName;
    }

    public List<String> getStId() {
        return stId;
    }

    public void setStId(List<String> stId) {
        this.stId = stId;
    }

    public List<String> getStName() {
        return stName;
    }

    public void setStName(List<String> stName) {
        this.stName = stName;
    }
}
