package cn.tycoding.entity.LogisticsPlatform;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//发货方的货物展示
public class JFrameForTruck extends JFrame{

    LogisticsPlatform lp;
    Truck choosedTruck = null;
    Cargo choosedCargo = null;

    private JLabel capital = new JLabel("承运方货物信息展示");
    private JLabel jLabel1 = new JLabel("请输入承运方ID:");
    private JTextField choosedTruckID = new JTextField("");
    private JButton chooseTruck = new JButton("确认");
    private JTextArea jTextArea = new JTextArea("",11,26);
    private JLabel jLabel2 = new JLabel("查看订单ID:");
    private JTextField choosedCargoID = new JTextField("");
    private JButton chooseCargo = new JButton("确认");

    JFrameForTruck(LogisticsPlatform lp) {
        this.lp = lp;
        setTitle("【承运方】货物信息列表");
        setBounds(850, 500, 330, 300);
        this.setLayout(null);
        init();
//        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void init(){
        jLabel1.setBounds(40,38,130,30);
        add(jLabel1);
        choosedTruckID.setBounds(150,38,50,30);
        add(choosedTruckID);
        jLabel2.setBounds(40,245,130,30);
        add(jLabel2);
        choosedCargoID.setBounds(150,245,50,30);
        add(choosedCargoID);

        initTextArea(jTextArea);
        initTitle(capital);
        initChooseTruck(chooseTruck);
        initChooseCargo(chooseCargo);
    }


    // 选中发货方 合理则展示所有订单信息,再次点击则刷新
    private void initChooseTruck(JButton jButton){
        jButton.setBounds(200,38,80,32);
        setChoosedTruck(jButton);
        add(jButton);
    }

    // 选中订单 合理则展示订单完成信息
    private void initChooseCargo(JButton jButton){
        jButton.setBounds(200,245,80,32);
        setChoosedCargo(jButton);
        add(jButton);
    }

    // 选中承运方 合理则展示所有订单信息
    private void setChoosedTruck(JButton b) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                jTextArea.setText("");
                choosedTruck = null;
                if(!lp.haveTruckByID(choosedTruckID.getText())){
                    JOptionPane.showMessageDialog(null,"该承运方方未在平台注册","承运方ID错误",0);
                    return;
                }
                choosedTruck = lp.findTruckByID(choosedTruckID.getText());
                for(int i = 0; i < lp.getCargosInPlatform().size(); i++) {
                    Cargo tempCargo = lp.getCargosInPlatform().get(i);
                    if(tempCargo.getMyTruck() == choosedTruck){
                        jTextArea.append("\n订单" + tempCargo.getID() + ":接单价" + tempCargo.getOrderPrice() +"赔偿金" + tempCargo.getLiquidatedDamages()
                                + "\n目的地(" + tempCargo.getDestinationX() +"," + tempCargo.getDestinationY()+") 剩余时间" + tempCargo.getRemainHour());
                    }
                }
                jTextArea.append("\n" + choosedTruck.getSpacialCargoEndLog());

                jTextArea.append("\n" + "\n订单时间日志" + choosedTruck.getCargoTimeLog());
            }
        });
    }

    // 选中订单 合理则展示订单完成信息
    private void setChoosedCargo(JButton b) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                if(!lp.haveCargoByID(choosedCargoID.getText())){
                    JOptionPane.showMessageDialog(null,"找不到该订单","货物ID错误",0);
                    return;
                }
                choosedCargo = lp.findCargoByID(choosedCargoID.getText());
                if (choosedCargo.getMyTruck() != choosedTruck) {
                    JOptionPane.showMessageDialog(null,"您没有完成该订单","货物ID错误",0);
                    return;
                }
                JOptionPane.showMessageDialog(null,choosedCargo.getMyEnd() + "\n订单时间日志" + choosedCargo.getCargoTimeLog(),"订单完成情况",1);
            }
        });
    }

    private void initTextArea(JTextArea jTextArea){
        // 设置文本域中的文本为自动换行
        jTextArea.setLineWrap(true);
        // 设置组件的背景色
        jTextArea.setForeground(Color.BLACK);
        // 修改字体样式
        jTextArea.setFont(new Font("楷体",Font.ROMAN_BASELINE,13));
        // 设置背景色
        jTextArea.setBackground(Color.lightGray);
        // 将文本域放入滚动窗口
        JScrollPane jsp = new JScrollPane(jTextArea);
        // 获得文本域的首选大小
        Dimension size = jTextArea.getPreferredSize();
        jsp.setBounds(10,70,size.width,size.height);
        add(jsp);
    }

    // 卡片标题 的位置、字体 初始化
    private void initTitle(JLabel label1) {
        label1.setFont(new Font("楷体",Font.BOLD,17));
        label1.setBounds(90,5,180,30);
        add(label1);
    }
}
