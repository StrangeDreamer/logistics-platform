package cn.tycoding.enums;

/**
 * @auther qlXie
 * @date 2019-06-10 16:51
 */
public enum CargoSatusEnum {

    SUCCESS(1, "订单完成"),
    END(0, "创建未发布"),
    REPEAT_KILL(-1,"订单异常"),
    INNER_ERROR(-2, "订单超时");


    private int status;
    private String statusInfo;

    CargoSatusEnum(int status, String statusInfo) {
        this.status = status;
        this.statusInfo = statusInfo;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public static CargoSatusEnum statusOf(int index){
        for (CargoSatusEnum status : values()){
            if (status.getStatus() == index){
                return status;
            }
        }
        return null;
    }
}
