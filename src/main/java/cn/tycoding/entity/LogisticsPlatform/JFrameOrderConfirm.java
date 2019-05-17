package cn.tycoding.entity.LogisticsPlatform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 收货方收货确认
public class JFrameOrderConfirm extends JFrame{

    LogisticsPlatform lp;
    Receiver choosedReceiver = null;
    Cargo choosedCargo = null;

    private JLabel capital = new JLabel("收货方货物无损确认");
    private JLabel jLabel1 = new JLabel("请输入收货方ID:");
    private JTextField choosedReceiverID = new JTextField("");
    private JButton chooseReceiver = new JButton("确认");
    private JTextArea jTextArea = new JTextArea("",11,26);

    private JLabel jLabel2 = new JLabel("订单收货确认");
    private JTextField choosedCargoID = new JTextField("");
    private JButton chooseCargo = new JButton("收货确认");

    JFrameOrderConfirm(LogisticsPlatform lp) {
        this.lp = lp;
        setTitle("【收货方】收货确认");
        setBounds(850, 500, 330, 300);
        this.setLayout(null);
        init();
//        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void init(){
        jLabel1.setBounds(40,38,130,30);
        add(jLabel1);
        choosedReceiverID.setBounds(150,38,50,30);
        add(choosedReceiverID);
        jLabel2.setBounds(40,245,100,30);
        add(jLabel2);
        choosedCargoID.setBounds(120,245,50,30);
        add(choosedCargoID);

        initTextArea(jTextArea);
        initTitle(capital);
        initChooseReceiver(chooseReceiver);
        initChooseCargo(chooseCargo);
    }


    // 选中收货方 合理则展示所有订单信息,再次点击则刷新
    private void initChooseReceiver(JButton jButton){
        jButton.setBounds(200,38,80,32);
        setChoosedReceiver(jButton);
        add(jButton);
    }

    // 选中订单 合理则展示订单完成信息
    private void initChooseCargo(JButton jButton){
        jButton.setBounds(200,245,80,32);
        setChoosedCargo(jButton);
        add(jButton);
    }

    // 选中收货方 合理则展示所有订单信息
    private void setChoosedReceiver(JButton b) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                jTextArea.setText("");
                choosedReceiver = null;
                if(!lp.haveReceiverByID(choosedReceiverID.getText())){
                    JOptionPane.showMessageDialog(null,"该收货方未在平台注册","错误",0);
                    return;
                }
                choosedReceiver = lp.findReceiverByID(choosedReceiverID.getText());
                if(choosedReceiver.getCargos().size() == 0) {
                    jTextArea.append("收货方暂无订单");
                    return;
                }
                for (int i = 0; i < choosedReceiver.getCargos().size(); i++) {
                    Cargo cargo = choosedReceiver.getCargos().get(i);
                    jTextArea.append("\n订单 " + cargo.getID() + "，  当前状态： " + cargo.getMyStatus());
                }

            }
        });
    }

    // 选中订单 合理则展示订单完成信息
    private void setChoosedCargo(JButton b) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                if(!lp.haveCargoByID(choosedCargoID.getText())){
                    JOptionPane.showMessageDialog(null,"该货物未在平台注册","货物ID错误",0);
                    return;
                }
                choosedCargo = lp.findCargoByID(choosedCargoID.getText());
                if(choosedCargo.getReceiver() != choosedReceiver){
                    JOptionPane.showMessageDialog(null,"您没有该订单","货物ID错误",0);
                    return;
                }


                // 订单运到才可以验货
                if (choosedCargo.getCargoStatus() != 4) {
                    JOptionPane.showMessageDialog(null,"您现在不能对该货物验货","错误",0);
                    return;
                }

                int choose = JOptionPane.showOptionDialog(null,
                        "收货方进行验货", "验货", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] {"验货通过", "验货不通过"}, "验货不通过");
                if (choose == 0){
                    choosedCargo.setAlreadyCheck(true);
                    choosedCargo.setReceiverConfirm(true);
                }else {
                    choosedCargo.setAlreadyCheck(true);
                    choosedCargo.setReceiverConfirm(false);
                }


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
