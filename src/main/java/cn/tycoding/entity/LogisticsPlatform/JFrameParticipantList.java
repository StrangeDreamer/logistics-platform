package cn.tycoding.entity.LogisticsPlatform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

//参与者清单，所有参与者的总览
public class JFrameParticipantList extends JFrame{

    LogisticsPlatform lp;
    private String selectedCargoID = ""; //保存所选择的订单ID
    //建立一个键值对，用来联系车牌号和选项的序号
    private Map<String, Integer> truckIDAndItemID = new HashMap<String, Integer>();

    private JButton b2 = new JButton("注册承运方列表");
    private JButton b3 = new JButton("注册发货方列表");
    private JButton b4 = new JButton("注册收货方列表");
    // private JLabel showTruckAndCargoNum = new JLabel("模拟器当前有车辆xx辆，历史订单xx个");

    private JLabel card2Title1 = new JLabel("注册承运方列表");
    private JLabel card3Title1 = new JLabel("注册发货方列表");
    private JLabel card4Title1 = new JLabel("注册收货方列表");
    private JButton back2 = new JButton("刷新");
    private JButton back3 = new JButton("刷新");
    private JButton back4 = new JButton("刷新");

    // 卡片234 的多行文本域
    private JTextArea card2TextArea = new JTextArea("",11,26);
    private JTextArea card3TextArea = new JTextArea("",11,26);
    private JTextArea card4TextArea = new JTextArea("",11,26);

    // 卡片式布局的面板
    final JPanel cards = new JPanel(new CardLayout());
    final CardLayout cl = (CardLayout)(cards.getLayout());
    JPanel p1=new JPanel();    //面板1
    JPanel p2=new JPanel();    //面板2
    JPanel p3=new JPanel();    //面板3
    JPanel p4=new JPanel();    //面板4

    //上方放置3个按钮进行切换
    private JButton truckToTruck = new JButton("承运方列表");
    private JButton truckToShipper = new JButton("发货方列表");
    private JButton truckToReceiver = new JButton("收货方列表");
    private JButton shipperToTruck = new JButton("承运方列表");
    private JButton shipperToShipper = new JButton("发货方列表");
    private JButton shipperToReceiver = new JButton("收货方列表");
    private JButton receiverToTruck = new JButton("承运方列表");
    private JButton receiverToShipper = new JButton("发货方列表");
    private JButton receiverToReceiver = new JButton("收货方列表");

    JFrameParticipantList(LogisticsPlatform lp) {
        this.lp = lp;
        setTitle("【平台】注册列表总览");
        setBounds(850, 500, 330, 300);

        p1.setLayout(null);
        p2.setLayout(null);
        p3.setLayout(null);
        p4.setLayout(null);

        //主界面的按钮位置
        b2.setBounds(110,40,115,40);
        b3.setBounds(110,100,115,40);
        b4.setBounds(110,160,115,40);
        p1.add(b2);
        p1.add(b3);
        p1.add(b4);
        //子界面各个按钮的位置
        truckToTruck.setBounds(0,0,115,40);
        truckToShipper.setBounds(110,0,115,40);
        truckToReceiver.setBounds(220,0,113,40);
        p2.add(truckToTruck);p2.add(truckToShipper);p2.add(truckToReceiver);

        shipperToTruck.setBounds(0,0,115,40);
        shipperToShipper.setBounds(110,0,115,40);
        shipperToReceiver.setBounds(220,0,113,40);
        p3.add(shipperToTruck);p3.add(shipperToShipper);p3.add(shipperToReceiver);

        receiverToTruck.setBounds(0,0,115,40);
        receiverToShipper.setBounds(110,0,115,40);
        receiverToReceiver.setBounds(220,0,113,40);
        p4.add(receiverToTruck);p4.add(receiverToShipper);p4.add(receiverToReceiver);

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
        //子界面互相跳转按钮
        buttonActionSwitchTo(truckToShipper,"card3");
        buttonActionSwitchTo(truckToReceiver,"card4");
        buttonActionSwitchTo(shipperToTruck,"card2");
        buttonActionSwitchTo(shipperToReceiver,"card4");
        buttonActionSwitchTo(receiverToTruck,"card2");
        buttonActionSwitchTo(receiverToShipper,"card3");

        // 各个组件的初始化
        init(p1,p2,p3,p4);

        // 展示主界面
        cl.show(cards,"card1");
        add(cards);
//        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void init(JPanel p1, JPanel p2, JPanel p3, JPanel p4){

        //卡片界面标题位置的设置
        initCardsTitle(card2Title1,p2);
        initCardsTitle(card3Title1,p3);
        initCardsTitle(card4Title1,p4);

        //多行文本框 的初始化 并添加进相应卡片
        initTextArea(card2TextArea,p2);
        initTextArea(card3TextArea,p3);
        initTextArea(card4TextArea,p4);

        //为3个多行文本框添加清空按钮的响应事件 并添加进卡片
        initClearButtonIn(p2,card2TextArea);
        initClearButtonIn(p3,card3TextArea);
        initClearButtonIn(p4,card4TextArea);

        // 返回按钮 初始化并添加进卡片
        initReturnButton(back2,p2);
        initReturnButton(back3,p3);
        initReturnButton(back4,p4);
        // 返回按钮 功能的实现
        truckListAction(back2);
        shipperListAction(back3);
        receiverListAction(back4);
    }
    // 为注册承运方刷新添加单击事件
    private void truckListAction(JButton b) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                card2TextArea.setText("");
                for(int i = 0; i < lp.getTrucksInPlatform().size(); i++) {
                    card2TextArea.append(lp.getTrucksInPlatform().get(i).showOverview());
                    card2TextArea.append("\n");
                }
            }
        });
    }

    // 为注册发货方刷新添加单击事件
    private void shipperListAction(JButton b) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                card3TextArea.setText("");
                for(int i = 0; i < lp.getShippersInPlatform().size(); i++) {
                    card3TextArea.append(lp.getShippersInPlatform().get(i).showOverview());
                    card3TextArea.append("\n");
                }
            }
        });
    }

    // 为注册收货方刷新添加单击事件
    private void receiverListAction(JButton b) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                card4TextArea.setText("");
                for(int i = 0; i < lp.getReceiversInPlatform().size(); i++) {
                    card4TextArea.append(lp.getReceiversInPlatform().get(i).showOverview());
                    card4TextArea.append("\n");
                }
            }
        });
    }

    // 为跳转窗口按钮添加单击事件
    private void buttonActionSwitchTo(JButton b, final String string) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                cl.show(cards,string);
                card2TextArea.setText("");
                for(int i = 0; i < lp.getTrucksInPlatform().size(); i++) {
                    card2TextArea.append(lp.getTrucksInPlatform().get(i).showOverview());
                    card2TextArea.append("\n");
                }
                card3TextArea.setText("");
                for(int i = 0; i < lp.getShippersInPlatform().size(); i++) {
                    card3TextArea.append(lp.getShippersInPlatform().get(i).showOverview());
                    card3TextArea.append("\n");
                }
                card4TextArea.setText("");
                for(int i = 0; i < lp.getReceiversInPlatform().size(); i++) {
                    card4TextArea.append(lp.getReceiversInPlatform().get(i).showOverview());
                    card4TextArea.append("\n");
                }
            }
        });
    }

    // 卡片标题 的位置、字体 初始化
    private void initCardsTitle(JLabel label1, JPanel p) {
        label1.setFont(new Font("楷体",Font.BOLD,17));
        label1.setBounds(110,37,120,30);
        p.add(label1);
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
}
