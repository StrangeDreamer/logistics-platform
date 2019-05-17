package cn.tycoding.entity.LogisticsPlatform;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JFrameUploadAttachment extends JFrame {
    JPanel jp = new JPanel();
    JLabel capital = new JLabel("请选择并上传附件");
    JButton confirm = new JButton("确认上传");
    JLabel t1 = new JLabel("行驶证驾驶证扫描件");
    JLabel t2 = new JLabel("运营证扫描件");
    JLabel t3 = new JLabel("保单扫描件");
    JLabel t4 = new JLabel("身份证扫描件");
    JLabel t5 = new JLabel("营业执照扫描件");
    JLabel t6 = new JLabel("车辆图片扫描件");
    JCheckBox c1 = new JCheckBox(" ", false);    //创建指定文本和状态的复选框
    JCheckBox c2 = new JCheckBox(" ", false);
    JCheckBox c3 = new JCheckBox(" ", false);
    JCheckBox c4 = new JCheckBox(" ", false);
    JCheckBox c5 = new JCheckBox(" ", false);
    JCheckBox c6 = new JCheckBox(" ", false);

    public JFrameUploadAttachment() {
        setTitle("上传附件");
        setBounds(850,50,200,300);
        setVisible(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        int count = 0;
        t1.setBounds(30,30 * count + 50,140,20 );
        c1.setBounds(150,30 * count + 50,50,20 );
        count++;
        t2.setBounds(30,30 * count + 50,140,20 );
        c2.setBounds(150,30 * count + 50,50,20 );
        count++;
        t3.setBounds(30,30 * count + 50,140,20 );
        c3.setBounds(150,30 * count + 50,50,20 );
        count++;
        t4.setBounds(30,30 * count + 50,140,20 );
        c4.setBounds(150,30 * count + 50,50,20 );
        count++;
        t5.setBounds(30,30 * count + 50,140,20 );
        c5.setBounds(150,30 * count + 50,50,20 );
        count++;
        t6.setBounds(30,30 * count + 50,140,20 );
        c6.setBounds(150,30 * count + 50,50,20 );
        count++;
        capital.setBounds(50,10,150,30);
        confirm.setBounds(60,240,80,30);

        confirm.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        add(capital);
        add(confirm);
        add(t1);
        add(t2);
        add(t3);
        add(t4);
        add(t5);
        add(t6);
        add(c1);
        add(c2);
        add(c3);
        add(c4);
        add(c5);
        add(c6);
    }
    public boolean havaChecked(){
        if(c1.isSelected() && c2.isSelected() && c3.isSelected() && c4.isSelected() && c5.isSelected() && c6.isSelected()){
            return true;
        }
        return false;
    }

    public void reset() {
        c1.setSelected(false);
        c2.setSelected(false);
        c3.setSelected(false);
        c4.setSelected(false);
        c5.setSelected(false);
        c6.setSelected(false);
    }

}
