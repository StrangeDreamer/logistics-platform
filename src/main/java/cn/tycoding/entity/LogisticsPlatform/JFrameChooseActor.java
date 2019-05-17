package cn.tycoding.entity.LogisticsPlatform;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 实际全都在构造的时候就把所有窗口创建了，只不过在点击的时候置为可见
public class JFrameChooseActor extends JFrame {
    private JFrameBankLog jFrameBankLog;
    private JFrameBillingLog jFrameBillingLog;
    private JFrameForShipper jFrameForShipper;
    private JFrameForShipperMoneyLog jFrameForShipperMoneyLog;
    private JFrameForReceiver jFrameForReceiver;
    private JFrameForTruckMoneyLog jFrameForTruckMoneyLog;
    private JFrameForTruck jFrameForTruck;
    private JFrameGuarantorLog jFrameGuarantorLog;
    private JFrameInquiry jFrameInquiry;
    private JFrameParticipantList jFrameParticipantList;
    private JFramePlatformProfit jFramePlatformProfit;
    private JFramePublishing jFramePublishing;
    private JFrameAbnormalOrder jFrameAbnormalOrder;
    private JFrameOrderConfirm jFrameOrderConfirm;
    private LogisticsPlatform lp;
    private Bank bank;
    private Guarantor guarantor;
    private JButton LPButton = new JButton("平台");
    private JButton truckButton = new JButton("承运方");
    private JButton shipperButton = new JButton("发货方");
    private JButton receiverButton = new JButton("收货方");

    // 手动测试切换
    private JCheckBox handModel = new JCheckBox("手动模式 ", true);

    JFrameChooseActor(LogisticsPlatform lp, Bank bank, Guarantor guarantor) {
        this.lp = lp;
        this.bank = bank;
        this.guarantor = guarantor;

        jFrameBankLog = new JFrameBankLog(lp,bank);
        jFrameBillingLog = new JFrameBillingLog(lp);
        jFrameGuarantorLog = new JFrameGuarantorLog(lp,guarantor);
        jFrameInquiry = new JFrameInquiry(lp);
        jFrameParticipantList = new JFrameParticipantList(lp);
        jFramePlatformProfit = new JFramePlatformProfit(lp,bank);
        jFramePublishing = new JFramePublishing(lp);
        jFrameForShipper = new JFrameForShipper(lp);
        jFrameForShipperMoneyLog = new JFrameForShipperMoneyLog(lp);
        jFrameForReceiver = new JFrameForReceiver(lp);
        jFrameForTruckMoneyLog = new JFrameForTruckMoneyLog(lp);
        jFrameForTruck = new JFrameForTruck(lp);
        jFrameAbnormalOrder = new JFrameAbnormalOrder(lp);
        jFrameOrderConfirm = new JFrameOrderConfirm(lp);

//        jFrameBankLog.setVisible(false);
//        jFrameBillingLog.setVisible(false);
//        jFrameForShipper.setVisible(false);
//        jFrameForReceiver.setVisible(false);
//        jFrameGuarantorLog.setVisible(false);
//        jFrameInquiry.setVisible(false);
//        jFrameParticipantList.setVisible(false);
//        jFramePlatformProfit.setVisible(false);
//        jFramePublishing.setVisible(false);

        this.lp = lp;
        setTitle("角色选择");
        setBounds(20, 20, 220, 300);
        this.setLayout(null);
        int interval = 50;
        LPButton.setBounds(60,30 + 0 * interval,90,35);
        truckButton.setBounds(60,30 + 1 * interval,90,35);
        shipperButton.setBounds(60,30 + 2 * interval,90,35);
        receiverButton.setBounds(60,30 + 3 * interval,90,35);
        handModel.setBounds(60,30 + 4 * interval,90,35);

        // 平台的展示内容
        LPButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                jFrameBankLog.setVisible(true);
                jFrameBillingLog.setVisible(true);
                jFrameGuarantorLog .setVisible(true);
                jFrameInquiry.setVisible(true);
                jFrameParticipantList.setVisible(true);
                jFramePlatformProfit.setVisible(true);
                jFrameAbnormalOrder.setVisible(true);
            }
        });

        // 承运方的展示内容
        truckButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                jFramePublishing.setVisible(true);
                jFrameForTruck.setVisible(true);
                jFrameForTruckMoneyLog.setVisible(true);
            }
        });

        // 发货方的展示内容
        shipperButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                jFrameForShipper.setVisible(true);
                jFrameForShipperMoneyLog.setVisible(true);
            }

        });

        // 收货方的展示内容
        receiverButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                jFrameForReceiver.setVisible(true);
                jFrameOrderConfirm.setVisible(true);
            }
        });

        add(LPButton);
        add(truckButton);
        add(shipperButton);
        add(receiverButton);
        add(handModel);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public JFrameBankLog getjFrameBankLog() {
        return jFrameBankLog;
    }

    public void setjFrameBankLog(JFrameBankLog jFrameBankLog) {
        this.jFrameBankLog = jFrameBankLog;
    }

    public JFrameBillingLog getjFrameBillingLog() {
        return jFrameBillingLog;
    }

    public void setjFrameBillingLog(JFrameBillingLog jFrameBillingLog) {
        this.jFrameBillingLog = jFrameBillingLog;
    }

    public JFrameForShipper getjFrameForShipper() {
        return jFrameForShipper;
    }

    public void setjFrameForShipper(JFrameForShipper jFrameForShipper) {
        this.jFrameForShipper = jFrameForShipper;
    }

    public JFrameForReceiver getjFrameForReceiver() {
        return jFrameForReceiver;
    }

    public void setjFrameForReceiver(JFrameForReceiver jFrameForReceiver) {
        this.jFrameForReceiver = jFrameForReceiver;
    }

    public JFrameGuarantorLog getjFrameGuarantorLog() {
        return jFrameGuarantorLog;
    }

    public void setjFrameGuarantorLog(JFrameGuarantorLog jFrameGuarantorLog) {
        this.jFrameGuarantorLog = jFrameGuarantorLog;
    }

    public JFrameInquiry getjFrameInquiry() {
        return jFrameInquiry;
    }

    public void setjFrameInquiry(JFrameInquiry jFrameInquiry) {
        this.jFrameInquiry = jFrameInquiry;
    }

    public JFrameParticipantList getjFrameParticipantList() {
        return jFrameParticipantList;
    }

    public void setjFrameParticipantList(JFrameParticipantList jFrameParticipantList) {
        this.jFrameParticipantList = jFrameParticipantList;
    }

    public JFramePlatformProfit getjFramePlatformProfit() {
        return jFramePlatformProfit;
    }

    public void setjFramePlatformProfit(JFramePlatformProfit jFramePlatformProfit) {
        this.jFramePlatformProfit = jFramePlatformProfit;
    }

    public JFramePublishing getjFramePublishing() {
        return jFramePublishing;
    }

    public void setjFramePublishing(JFramePublishing jFramePublishing) {
        this.jFramePublishing = jFramePublishing;
    }

    public LogisticsPlatform getLp() {
        return lp;
    }

    public void setLp(LogisticsPlatform lp) {
        this.lp = lp;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public Guarantor getGuarantor() {
        return guarantor;
    }

    public void setGuarantor(Guarantor guarantor) {
        this.guarantor = guarantor;
    }

    public JButton getLPButton() {
        return LPButton;
    }

    public void setLPButton(JButton LPButton) {
        this.LPButton = LPButton;
    }

    public JButton getTruckButton() {
        return truckButton;
    }

    public void setTruckButton(JButton truckButton) {
        this.truckButton = truckButton;
    }

    public JButton getShipperButton() {
        return shipperButton;
    }

    public void setShipperButton(JButton shipperButton) {
        this.shipperButton = shipperButton;
    }

    public JButton getReceiverButton() {
        return receiverButton;
    }

    public void setReceiverButton(JButton receiverButton) {
        this.receiverButton = receiverButton;
    }

    public JFrameForShipperMoneyLog getjFrameForShipperMoneyLog() {
        return jFrameForShipperMoneyLog;
    }

    public void setjFrameForShipperMoneyLog(JFrameForShipperMoneyLog jFrameForShipperMoneyLog) {
        this.jFrameForShipperMoneyLog = jFrameForShipperMoneyLog;
    }

    public JFrameForTruckMoneyLog getjFrameForTruckMoneyLog() {
        return jFrameForTruckMoneyLog;
    }

    public void setjFrameForTruckMoneyLog(JFrameForTruckMoneyLog jFrameForTruckMoneyLog) {
        this.jFrameForTruckMoneyLog = jFrameForTruckMoneyLog;
    }

    public JFrameForTruck getjFrameForTruck() {
        return jFrameForTruck;
    }

    public void setjFrameForTruck(JFrameForTruck jFrameForTruck) {
        this.jFrameForTruck = jFrameForTruck;
    }

    public JFrameAbnormalOrder getjFrameAbnormalOrder() {
        return jFrameAbnormalOrder;
    }

    public void setjFrameAbnormalOrder(JFrameAbnormalOrder jFrameAbnormalOrder) {
        this.jFrameAbnormalOrder = jFrameAbnormalOrder;
    }

    public JFrameOrderConfirm getjFrameOrderConfirm() {
        return jFrameOrderConfirm;
    }

    public void setjFrameOrderConfirm(JFrameOrderConfirm jFrameOrderConfirm) {
        this.jFrameOrderConfirm = jFrameOrderConfirm;
    }

    public JCheckBox getHandModel() {
        return handModel;
    }

    public void setHandModel(JCheckBox handModel) {
        this.handModel = handModel;
    }
}
