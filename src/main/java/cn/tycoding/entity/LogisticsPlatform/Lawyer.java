package cn.tycoding.entity.LogisticsPlatform;

/**
 * Lawyer      律师：接案，办案，赔偿
 * 属性：
 * 方法：律师介入
 */
public class Lawyer implements Actor {

    private String ID = "律师";
    //物流过程运到异常需要律师介入
    public void lawyerInvolvement() {
        System.out.println("律师介入");
    }

    @Override
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
