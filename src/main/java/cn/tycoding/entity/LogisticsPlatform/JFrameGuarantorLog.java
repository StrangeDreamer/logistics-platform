package cn.tycoding.entity.LogisticsPlatform;
import javax.swing.*;
import java.awt.*;

// 担保额度变更日志
public class JFrameGuarantorLog extends JFrame{
    private Guarantor guarantor;
    private LogisticsPlatform lp;
    JPanel jp = new JPanel();
    JTextArea publishingOrders = new JTextArea("",14,26);
    JLabel capital = new JLabel("担保额度");
    public JFrameGuarantorLog(LogisticsPlatform lp, Guarantor guarantor) {
        this.guarantor = guarantor;
        this.lp = lp;
        setTitle("【平台】担保额度变更日志");
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
    public void refresh(){
        publishingOrders.setText("");
        publishingOrders.append(guarantor.getMoneyLog());
    }
}
