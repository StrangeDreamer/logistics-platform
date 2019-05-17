package cn.tycoding.entity.LogisticsPlatform;
import javax.swing.*;
import java.awt.*;

//展示给平台的异常订单列表
public class JFrameAbnormalOrder extends JFrame {
    private LogisticsPlatform lp;
    JPanel jp = new JPanel();
    JTextArea publishingOrders = new JTextArea("",14,26);
    JLabel capital = new JLabel("异常清单");
    public JFrameAbnormalOrder(LogisticsPlatform lp) {
        this.lp = lp;
        setTitle("【平台】异常清单列表");
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
        if(lp.getAbnormalOrderLog().equals("")){
            publishingOrders.append("平台目前暂无异常清单");
        } else {
            publishingOrders.append(lp.getAbnormalOrderLog());
        }
    }
}
