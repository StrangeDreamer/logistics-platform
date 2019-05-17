package cn.tycoding.entity.LogisticsPlatform;
import javax.swing.*;
import java.awt.*;
// 出价日志
public class JFrameBillingLog extends JFrame {
    private Bank bank;
    private LogisticsPlatform lp;
    JPanel jp = new JPanel();
    JTextArea publishingOrders = new JTextArea("",14,26);
    JLabel capital = new JLabel("抢单");
    public JFrameBillingLog(LogisticsPlatform lp) {
        this.lp = lp;
        this.bank = bank;
        setTitle("【平台】抢单出价日志");
        setBounds(850,50,340,400);
//        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        capital.setBounds(140,15,100,30);
        setLayout(null);
        publishingOrders.setLineWrap(true);
        publishingOrders.setForeground(Color.BLACK);
        publishingOrders.setFont(new Font("楷体",Font.ROMAN_BASELINE,12));
        publishingOrders.setBackground(Color.lightGray);
        JScrollPane jsp=new JScrollPane(publishingOrders);
        jsp.setBounds(20,50,300,320);
        add(jsp);
        add(capital);


    }

    public void refresh() {
        publishingOrders.setText("");
        publishingOrders.append(lp.getBillingLog());
    }
}
