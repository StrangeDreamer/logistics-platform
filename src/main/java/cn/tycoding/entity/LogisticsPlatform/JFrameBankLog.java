package cn.tycoding.entity.LogisticsPlatform;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 银行账户资金流通日志
public class JFrameBankLog extends JFrame {
    private Bank bank;
    private LogisticsPlatform lp;
    JPanel jp = new JPanel();
    JTextArea publishingOrders = new JTextArea("",14,26);
    JLabel capital = new JLabel("银行资金");

    JLabel bankMoneyJLabel = new JLabel("平台初始资金设置");
    JTextField bankMoneyJTextField = new JTextField();
    JButton bankMoneyJButton = new JButton("确定");

    public JFrameBankLog(LogisticsPlatform lp,Bank bank) {
        this.lp = lp;
        this.bank = bank;
        setTitle("【平台】银行账户资金流通日志");
        setBounds(850,50,340,440);
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

        bankMoneyJLabel.setBounds(40,380,120,30);
        bankMoneyJTextField.setBounds(170,380,50,30);
        bankMoneyJButton.setBounds(230,383,50,25);
        add(bankMoneyJLabel);
        add(bankMoneyJTextField);
        add(bankMoneyJButton);

        bankMoneyJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double bankMoney = Double.parseDouble(bankMoneyJTextField.getText());
                TruckLogistics.getBank().register(lp,bankMoney);
            }
        });

    }
    public void refresh() {
        publishingOrders.setText("");
        publishingOrders.append(bank.getMoneyLog());
    }

}
