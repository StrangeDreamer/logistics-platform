package cn.tycoding.entity.LogisticsPlatform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//订单公告牌，展示正在发布中的所有订单
public class JFramePublishing extends JFrame{
    private LogisticsPlatform lp;
    //存在新的还未处理的手动出价
    private Boolean haveUnprocessedBid = false;
    //手动出价的货物
    private Cargo cargo = null;
    //手动出价的承运方
    private Truck truck = null;
    //该承运方对该货物的手动出价金额数值
    private int biddingPrice = 0;
    JTextArea publishingOrders = new JTextArea("",11,23);
    JLabel caption = new JLabel("公告栏");
    JLabel result = new JLabel("————-----订单信息-----————");

    JLabel captionNew = new JLabel("手动出价");
    JLabel jLabelCargoID = new JLabel("订单ID");
    JLabel jLabelTruckID = new JLabel("承运方ID");
    JLabel jLabelBiddingPrice = new JLabel("出价金额");
    JButton confirm = new JButton("确认出价");

    JTextField jTextFieldCargoID = new JTextField();
    JTextField jTextFieldTruckID = new JTextField();
    JTextField jTextFieldBiddingPrice = new JTextField();

    // 有新的未完成的报价要进行处理
    private boolean haveNewBidding = false;

    public JFramePublishing(LogisticsPlatform lp) {
        this.lp = lp;
        setTitle("【承运方】当前正在发布的订单");
        setBounds(850,50,280,480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        caption.setBounds(120,20,100,30);
        result.setBounds(20,330,250,30);
        captionNew.setBounds(10,360,120,30);
        jLabelCargoID.setBounds(10,380,60,30);
        jTextFieldCargoID.setBounds(70,380,60,30);
        jLabelTruckID.setBounds(140,380,60,30);
        jTextFieldTruckID.setBounds(200,380,60,30);
        jLabelBiddingPrice.setBounds(10,410,60,30);
        jTextFieldBiddingPrice.setBounds(70,410,60,30);
        confirm.setBounds(150,410,100,32);
        setLayout(null);

        publishingOrders.setLineWrap(true);
        publishingOrders.setForeground(Color.BLACK);
        publishingOrders.setFont(new Font("楷体",Font.ROMAN_BASELINE,12));
        publishingOrders.setBackground(Color.lightGray);
        JScrollPane jsp=new JScrollPane(publishingOrders);
        jsp.setBounds(20,60,240,260);
        add(jsp);
        add(caption);
        add(result);
        add(captionNew);
        add(jLabelCargoID);
        add(jTextFieldCargoID);
        add(jLabelTruckID);
        add(jTextFieldTruckID);
        add(jLabelBiddingPrice);
        add(jTextFieldBiddingPrice);
        add(confirm);

        // 平台的展示内容
        confirm.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                String cargoID = jTextFieldCargoID.getText();
                String truckID = jTextFieldTruckID.getText();
                String bidding = jTextFieldBiddingPrice.getText();

                if (cargoID.equals("") || truckID.equals("") || bidding.equals("")) {
                    JOptionPane.showMessageDialog(null,"请输入相应参数","错误",0);
                    return;
                }
                if(! lp.havePublishingCargoByID(cargoID)) {
                    JOptionPane.showMessageDialog(null,"该货物当前未在平台上发布","错误",0);
                    return;
                }
                if(! lp.haveTruckByID(truckID)) {
                    JOptionPane.showMessageDialog(null,"平台上没有找到该承运方","错误",0);
                    return;
                }

                cargo = lp.findPublishingCargoByID(cargoID);
                truck = lp.findTruckByID(truckID);
                if (!truck.isActivated()) {
                    JOptionPane.showMessageDialog(null,"该承运方尚未激活！","错误",0);
                    return;
                }
                if (cargo.getTempbid().containsKey(truck)){
                    JOptionPane.showMessageDialog(null,"该承运方已经对该货物出价！","错误",0);
                    return;
                }
                biddingPrice = Integer.parseInt(bidding);
                haveNewBidding = true;
            }
        });
    }

    public void refresh(){
        publishingOrders.setText("");
        for(int i = 0; i < lp.getCargoPublishing().size(); i++) {
            publishingOrders.append("\n"+lp.getCargoPublishing().get(i).cargoDescriptionWithoutOrderPrice());
        }
    }



    public void setResult(String s){
        result.setText(s);
    }

    public LogisticsPlatform getLp() {
        return lp;
    }

    public void setLp(LogisticsPlatform lp) {
        this.lp = lp;
    }

    public Boolean getHaveUnprocessedBid() {
        return haveUnprocessedBid;
    }

    public void setHaveUnprocessedBid(Boolean haveUnprocessedBid) {
        this.haveUnprocessedBid = haveUnprocessedBid;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }


    public JTextArea getPublishingOrders() {
        return publishingOrders;
    }

    public void setPublishingOrders(JTextArea publishingOrders) {
        this.publishingOrders = publishingOrders;
    }

    public JLabel getCaption() {
        return caption;
    }

    public void setCaption(JLabel caption) {
        this.caption = caption;
    }

    public JLabel getResult() {
        return result;
    }

    public void setResult(JLabel result) {
        this.result = result;
    }

    public JLabel getCaptionNew() {
        return captionNew;
    }

    public void setCaptionNew(JLabel captionNew) {
        this.captionNew = captionNew;
    }

    public JLabel getjLabelCargoID() {
        return jLabelCargoID;
    }

    public void setjLabelCargoID(JLabel jLabelCargoID) {
        this.jLabelCargoID = jLabelCargoID;
    }

    public JLabel getjLabelTruckID() {
        return jLabelTruckID;
    }

    public void setjLabelTruckID(JLabel jLabelTruckID) {
        this.jLabelTruckID = jLabelTruckID;
    }


    public JLabel getjLabelBiddingPrice() {
        return jLabelBiddingPrice;
    }

    public void setjLabelBiddingPrice(JLabel jLabelBiddingPrice) {
        this.jLabelBiddingPrice = jLabelBiddingPrice;
    }

    public JTextField getjTextFieldCargoID() {
        return jTextFieldCargoID;
    }

    public void setjTextFieldCargoID(JTextField jTextFieldCargoID) {
        this.jTextFieldCargoID = jTextFieldCargoID;
    }

    public JButton getConfirm() {
        return confirm;
    }

    public void setConfirm(JButton confirm) {
        this.confirm = confirm;
    }

    public JTextField getjTextFieldTruckID() {
        return jTextFieldTruckID;
    }

    public void setjTextFieldTruckID(JTextField jTextFieldTruckID) {
        this.jTextFieldTruckID = jTextFieldTruckID;
    }

    public JTextField getjTextFieldBiddingPrice() {
        return jTextFieldBiddingPrice;
    }

    public void setjTextFieldBiddingPrice(JTextField jTextFieldBiddingPrice) {
        this.jTextFieldBiddingPrice = jTextFieldBiddingPrice;
    }

    public boolean isHaveNewBidding() {
        return haveNewBidding;
    }

    public void setHaveNewBidding(boolean haveNewBidding) {
        this.haveNewBidding = haveNewBidding;
    }

    public int getBiddingPrice() {
        return biddingPrice;
    }

    public void setBiddingPrice(int biddingPrice) {
        this.biddingPrice = biddingPrice;
    }
}
