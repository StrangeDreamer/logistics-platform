package cn.tycoding.entity.LogisticsPlatform;

import com.github.rinde.rinsim.geom.Point;

/**
 * 工具  用来提供一些通用的方法
 * 方法：
 */
public class MyTool {
    public static int timeChangeRatio = 100;

    // 用来计算point之间的直线距离
    public    double getDistandOfTwoPoint(Point a, Point b) {
        double d = 0;
        d = (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y);
        d = Math.sqrt(d);
        return d;
    }

    // 用来计算point之间的直线距离，并且转化为自己设置的坐标系
    public  static int getCoordinatesPointDistance(Point a, Point b) {
        double d = 0;
        d = (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y);
        d = Math.sqrt(d);
        int t = (int)(d/10000);
        return t;
    }

    //将鲁汶市地图的点转换为直观的 坐标点
    //地图可以近似处理后看成15*15的坐标系
    public static Point getCoordinates (Point point) {
        Point Coordinates = new Point((int)((point.x-3210000)/10000), (int)(15 - (point.y-25640000)/10000));
        return  Coordinates;
    }





    //毫秒数 转换为 天数、小时、分钟、秒                 在演示的时,用不到天数、小时；关注分钟和秒
    public static void dateChangeToTime(long time,int[] a){
        //获取秒数
        double totalSecond = (double)(time/1000);
        double totalMinute = totalSecond/60;
        double totalHour = totalMinute/60;
        double totalDay = totalHour/24;
        a[0] = (int)totalDay;                                        // day
        a[1] = (int)(totalHour/24);                                 //hour
        a[2] = (int)(totalMinute/(24*60));                            //minute
        a[3] = (int)(totalSecond/(24*60*60));                         //second
    }

    /**运输类型，表示该车辆可以运输什么类型的货物 可继续添加
     * 1 --- 常规物品       ---常规车
     * 2 --- 需要冷藏物品   ---冷链车
     * 3 --- 危险品        ---可运危险品的车
     */
    // 通过数字获取运输类型字符串  map可实现，但由于是几个类通用的，就专门写个方法了
    public static String getTypeString(int i) {
        if (i == 1){
            return "常规";
        } else if (i == 2) {
            return "冷链";
        } else if (i == 3) {
            return "危险品";
        } else {
            return "运输类型不合法";
        }
    }

    //从货物数组中删除指定货物，  货车、平台类中的删除要用到。
    //货物到达目的地，更新平台当前货物信息
    public static boolean deleteCargo(Cargo[] cargosArray,Cargo cargo) {
        int currentCargoNum = cargosArray.length;
        boolean ret = false;
        int index = -1;
        //首先找到要删除的货物下标
        for (int i = 0; i < currentCargoNum; i++) {
            if (cargo == cargosArray[i]) {
                index = i;
                ret = true;
                break;
            }
        }

        if (currentCargoNum > 0) {
            if ((index + 1) == currentCargoNum){//如果最后一项，则只需要数量减一即可
                currentCargoNum--;
            } else {
                for (int i = index;i < (currentCargoNum - 1);i++) {
                    cargosArray[i] = cargosArray[i+1];
                }
                currentCargoNum--;
            }
        }
        return ret;
    }

    // 产生指定上下限内的随机数
    public static double getRandom(double lowerLimits,double upperLimite) {
        double ans;
        ans = lowerLimits + (upperLimite - lowerLimits) * Math.random();
        return ans;
    }

    public static int getRandom(int lowerLimits,int upperLimite) {
        int ans;
        ans = (int)(lowerLimits + (upperLimite - lowerLimits) * Math.random() );
        return ans;
    }

    public static Cargo getReturnCargo(Cargo cargo) {
        Cargo newCargo = cargo;
        newCargo.setDestinationX(cargo.getDepartureX());
        newCargo.setDestinationY(cargo.getDepartureY());
        newCargo.setDepartureX(cargo.getDestinationX());
        newCargo.setDepartureY(cargo.getDestinationY());
        return newCargo;
    }

    public static int tickChangeToHour(int i ){
        return i/timeChangeRatio;
    }
    public static int hourChangeToTick(int i ){
        return i*timeChangeRatio;
    }
}
