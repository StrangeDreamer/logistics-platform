package cn.tycoding.entity.LogisticsPlatform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//订单公告牌，展示正在发布中的所有订单
public class testJFrame extends JFrame{

    private LogisticsPlatform lp;
    JPanel jp = new JPanel();
    JTextArea publishingOrders = new JTextArea("",11,23);
    JLabel capital = new JLabel("公告栏");

    public testJFrame(LogisticsPlatform lp) {
        this.lp = lp;
        setTitle("当前正在发布的订单");
        setBounds(850,50,280,400);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        capital.setBounds(130,20,100,30);
        setLayout(null);
        add(capital);
        publishingOrders.setLineWrap(true);
        publishingOrders.setForeground(Color.BLACK);
        publishingOrders.setFont(new Font("楷体",Font.ROMAN_BASELINE,12));
        publishingOrders.setBackground(Color.lightGray);
        JScrollPane jsp=new JScrollPane(publishingOrders);
        jsp.setBounds(20,60,240,300);
        add(jsp);
    }
}