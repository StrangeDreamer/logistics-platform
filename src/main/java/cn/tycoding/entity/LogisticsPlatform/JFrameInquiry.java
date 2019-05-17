package cn.tycoding.entity.LogisticsPlatform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

public class JFrameInquiry extends JFrame {
    LogisticsPlatform lp;
    private String selectedCargoID = ""; //保存所选择的订单ID
    //建立一个键值对，用来联系车牌号和选项的序号
    private Map<String, Integer> truckIDAndItemID = new HashMap<String, Integer>();

    private JButton b2 = new JButton("当前车辆状况");
    private JButton b3 = new JButton("订单查询");
    private JButton b4 = new JButton("车辆已完成订单");
   // private JLabel showTruckAndCargoNum = new JLabel("模拟器当前有车辆xx辆，历史订单xx个");
    private JLabel card2Title1 = new JLabel("当前车辆状况");
    private JLabel card2Title2 = new JLabel("从右侧选择要查看的车辆");
    private JLabel card3Title1 = new JLabel("订单查询");
    private JLabel card3Title2 = new JLabel("从右侧选择要查看的订单");
    private JLabel card4Title1 = new JLabel("车辆已完成订单");
    private JLabel card4Title2 = new JLabel("从右侧选择要查看的车辆");

    // 选项框
    private JComboBox cmb2 = new JComboBox();
    private JComboBox cmb3 = new JComboBox();
    private JComboBox cmb4 = new JComboBox();
    private JButton back2 = new JButton("返回查询选择界面");
    private JButton back3 = new JButton("返回查询选择界面");
    private JButton back4 = new JButton("返回查询选择界面");

    // 该功能转移到单独的查询框
    //private JButton carrierCheck = new JButton("承运方账单细则");
    //private JButton shipperCheck = new JButton("发货方账单细则");

    // 卡片234 的多行文本域
    private JTextArea card2TextArea = new JTextArea("",11,26);
    private JTextArea card3TextArea = new JTextArea("",9,26);
    private JTextArea card4TextArea = new JTextArea("",11,26);

    // 卡片式布局的面板
    final JPanel cards = new JPanel(new CardLayout());
    final CardLayout cl = (CardLayout)(cards.getLayout());
    JPanel p1=new JPanel();    //面板1
    JPanel p2=new JPanel();    //面板2
    JPanel p3=new JPanel();    //面板3
    JPanel p4=new JPanel();    //面板4

    JFrameInquiry(LogisticsPlatform lp) {
        this.lp = lp;
        setTitle("【平台】信息查询");
        setBounds(850, 500, 330, 300);
        p1.setLayout(null);
        p2.setLayout(null);
        p3.setLayout(null);
        p4.setLayout(null);
        b2.setBounds(110,40,115,40);
        b3.setBounds(110,100,115,40);
        b4.setBounds(110,160,115,40);
        p1.add(b2);
        p1.add(b3);
        p1.add(b4);
        p2.add(new JTextField("用户名文本框",20));
        p2.add(new JTextField("密码文本框",20));
        p2.add(new JTextField("验证码文本框",20));
        cards.add(p1,"card1");    //向卡片式布局面板中添加面板1
        cards.add(p2,"card2");
        cards.add(p3,"card3");
        cards.add(p4,"card4");
        // 初始界面 前往子界面的按钮
        buttonActionSwitchTo(b2,"card2");
        buttonActionSwitchTo(b3,"card3");
        buttonActionSwitchTo(b4,"card4");

        // 各个组件的初始化
        init(p1,p2,p3,p4);

        // 展示主界面
        cl.show(cards,"card1");
        add(cards);
//        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void init(JPanel p1, JPanel p2, JPanel p3, JPanel p4) {
        //卡片界面标题位置的设置
        initCardsTitle(card2Title1,card2Title2,p2);
        initCardsTitle(card3Title1,card3Title2,p3);
        initCardsTitle(card4Title1,card4Title2,p4);

        //多行文本框 的初始化 并添加进相应卡片
        initTextArea(card2TextArea,p2);
        initTextArea(card3TextArea,p3);
        initTextArea(card4TextArea,p4);

        //查询选项栏的位置等信息 初始化
        initComboBox(cmb2,p2);
        initComboBox3(cmb3,p3);
        //货物查询界面单独加一个后缀
        JLabel card3Mark = new JLabel("号货物");
        card3Mark.setBounds(262,35,40,25);
        p3.add(card3Mark);
        initComboBox(cmb4,p4);
        initCMB();

        // 为查询下拉栏添加选中事件
        cmb2.addItemListener(new InquiryCard2());    //下拉列表的事件
        cmb3.addItemListener(new InquiryCard3());    //下拉列表的事件
        cmb4.addItemListener(new InquiryCard4());    //下拉列表的事件
        initCMB();

        //为3个多行文本框添加清空按钮的响应事件 并添加进卡片
        initClearButtonIn(p2,card2TextArea);
        initClearButtonIn(p3,card3TextArea);
        initClearButtonIn(p4,card4TextArea);

        // 返回按钮 初始化并添加进卡片
        initReturnButton(back2,p2);
        initReturnButton(back3,p3);
        initReturnButton(back4,p4);

        // 返回按钮 功能的实现
        buttonActionSwitchTo(back2,"card1");
        buttonActionSwitchTo(back3,"card1");
        buttonActionSwitchTo(back4,"card1");

        // 为账单查询添加承运方账单细则+发货方账单细则 2个按钮+2个点击事件
//        carrierCheck.setBounds(40,210,130,30);
//        shipperCheck.setBounds(160,210,130,30);

//        // 账单细则，承运方只包含最后完成订单的那个
//        carrierCheck.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e) {
//
//                String show = lp.findCargoByID(selectedCargoID).getCarrierCheck();
//                if(!show.equals("")){
//                    JOptionPane.showMessageDialog(null,show,"承运方的账单细则",1);
//                }
//                else {
//                    JOptionPane.showMessageDialog(null,"该订单尚未完成","承运方的账单细则",0);
//                }
//            }
//        });
//
//        shipperCheck.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e) {
//                String show = lp.findCargoByID(selectedCargoID).getShipperCheck();
//                if(!show.equals("")){
//                    JOptionPane.showMessageDialog(null,show,"发货方的账单细则",1);
//                }
//                else {
//                    JOptionPane.showMessageDialog(null,"该订单尚未完成","发货方的账单细则",0);
//                }
//            }
//        });
//
//        // 账单细则按钮转移到 各自的功能上
//        p3.add(carrierCheck);
//        p3.add(shipperCheck);
    }

    // 为跳转窗口按钮添加单击事件
    private void buttonActionSwitchTo(JButton b, final String string) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                cl.show(cards,string);
                initCMB();
                if(b == back2){
                    card2TextArea.setText("");
                    card2TextArea.append(lp.findTruckByID("沪A1000").showTruckOrder());
                }
            }
        });
    }

    // 卡片标题 的位置、字体 初始化
    private void initCardsTitle(JLabel label1, JLabel label2, JPanel p) {
        label1.setFont(new Font("楷体",Font.BOLD,17));
        label1.setBounds(110,5,120,30);
        p.add(label1);
        label2.setFont(new Font("楷体",Font.LAYOUT_NO_LIMIT_CONTEXT,13));
        label2.setBounds(20,35,250,25);
        p.add(label2);
    }

    // 下拉选项栏 的位置等信息 初始化
    private void initComboBox(JComboBox cmb,JPanel p) {
        cmb.setBounds(200,35,120,25);
        p.add(cmb);
    }

    // 货物下拉框
    private void initComboBox3(JComboBox cmb,JPanel p) {
        cmb.setBounds(180,35,80,25);
        p.add(cmb);
    }

    // 为下拉栏添加新选项的函数 供外界添加货车/货物时调用
    public void addCmd2(int i) {
        String str = lp.getTrucksInPlatform().get(i).getID();
        cmb2.addItem(str);
    }
    public void addCmd3(String i) {
        cmb3.addItem(i);
    }
    public void addCmd4(int i) {
        String str = lp.getTrucksInPlatform().get(i).getID();
        cmb4.addItem(str);
    }

    // 添加 查询下拉框 某选项点击选中的事件
    class  InquiryCard2 implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            String str = e.getItem().toString();
            card2TextArea.setText("");
            card2TextArea.append(lp.findTruckByID(str).showTruckOrder());
            card2TextArea.append("订单时间日志：" + lp.findTruckByID(str).getCargoTimeLog());
        }
    }

    class  InquiryCard3 implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            selectedCargoID = e.getItem().toString();
            card3TextArea.setText("");
            card3TextArea.append(lp.findCargoByID(selectedCargoID).getRecodeBidding()
                    + lp.findCargoByID(selectedCargoID).getRecodeSettlement());

            card3TextArea.append("订单时间日志：" + lp.findCargoByID(selectedCargoID).getCargoTimeLog());
        }
    }

    class  InquiryCard4 implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            String str = e.getItem().toString();
            card4TextArea.setText("");
            card4TextArea.append(lp.findTruckByID(str).getCargoRecode());
            card4TextArea.append("\n" + lp.findTruckByID(str).getSpacialCargoEndLog());
            card4TextArea.append("订单时间日志：" + lp.findTruckByID(str).getCargoTimeLog());
        }
    }

    // 为多行可下拉文本框初始化
    private void initTextArea(JTextArea jTextArea, JPanel p) {
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
        jsp.setBounds(10,65,size.width,size.height);
        p.add(jsp);
    }

    // 初始化返回按钮属性 并添加进卡片
    private void initReturnButton(JButton button,JPanel p) {
        button.setBounds(160,240,130,35);
        p.add(button);
    }

    // 添加清空按钮和响应时间
    private void initClearButtonIn(JPanel p, final JTextArea jt) {
        JButton jButton = new JButton("清空");
        jButton.setBounds(30,240,130,35);
        // 清空按钮监听
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jt.setText("");
            }
        });
        p.add(jButton);
    }

    //在cmb2下拉框上删除指定的项
    public void deleteItem(String str){
        cmb2.removeItem(str);
    }

    public void deleteCargo(String str){
        cmb3.removeItem(str);
    }

    public void initCMB(){
        cmb2.setSelectedIndex(-1);
        cmb3.setSelectedIndex(-1);
        cmb4.setSelectedIndex(-1);
    }
}
