package cn.tycoding.entity.LogisticsPlatform;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.AbstractGraph;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Point;
import org.apache.commons.math3.random.RandomGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static com.github.rinde.rinsim.examples.LogisticsPlatform.TruckLogistics.getWITHDRAWALFEERATIO;
import static java.lang.Thread.sleep;

public class JFrameUseCaseTest extends JFrame {
    LogisticsPlatform lp;
    RoadModel roadModel;
    RandomGenerator rng;
    Bank bank;
    Guarantor guarantor;

    private int testEvent = -1;
    boolean havaUpload = false;
    public String para1 = "-" ;//保存车牌号
    public int u0Weight = 0;
    public int u0Volumn = 0;
    public int para2 = -1;//保存冷链类型
    public int para3 = -1;//保存担保额度
    public Truck para4 = null;//保存要被注销的承运方
    public String para5 = "-";//保存要注册的发货方ID
    public Shipper para6 = null;//保存要注销的发货方
    public String para7 = "-";//保存要注册的收货方ID
    public Receiver para8 = null;//保存要注销的收货方ID
    public Cargo useCaseTest6Cargo = null;//保存订单提交中要提交的订单
    public Cargo useCaseTest7Cargo; //保存撤单的订单
    public Cargo useCaseTest8Cargo; //保存抢单所需的订单ID
    public JFrameUploadAttachment jFrameUploadAttachment = new JFrameUploadAttachment();
    boolean timeSwitch = false;//抢单参数
    int timeCount = 0; //抢单参数
    public Cargo useCaseTest9Cargo = null;  //订单转手的订单
    public int useCaseTest9CargoOfferMoney = 0; //转手订单的报价
    boolean orderCanbeChange = false; //订单可以转手
    public Truck useCaseTest9Truck = null; //订单转手车辆
    JTextArea usecase10_intime = new JTextArea("",5,23);
    JTextArea usecase10_overtime= new JTextArea("",6,23);

    public double u0BankMoney = -1;
    public double u2BankMoney = -1;


    //用例测试名
    private String[] useCaseNames = new String[12];

    //用例测试选择界面的按钮
    private JButton[] selectedUseCaseButtons = new JButton[12];

    //卡片界面标题
    private JLabel[] useCaseTitleLables = new JLabel[12];

    //卡片中的选项框
    private JComboBox[] jComboBoxes = new JComboBox[12];

    //返回主界面的按钮 貌似一个就够了？ 测试结果是，不可以一起用，只有最有一个起作用。
    private JButton[] backButtons = new JButton[12];

    //卡片中的输出框----多行文本域
    private JTextArea[] showTextAreas = new JTextArea[12];

    // 卡片式布局
    private final JPanel cards = new JPanel(new CardLayout());
    private final CardLayout cl = (CardLayout)(cards.getLayout());
    // 卡片式布局的面板 因为要包含选择界面所以多一个，用最后一个 即 card12 作为选择界面
    private JPanel[] jPanels = new JPanel[13];

    JFrameUseCaseTest(LogisticsPlatform lp
    , RoadModel roadModel, RandomGenerator rng, Bank bank, Guarantor guarantor
    ) {
        this.lp = lp;
        this.roadModel = roadModel;
        this.rng = rng;
        this.bank = bank;
        this.guarantor = guarantor;
        setTitle("用例测试");
        setBounds(950, 500, 300, 400);

        //卡片布局均置为null
        for(int i = 0; i <= 12; i++){
            System.out.println("开始计数");
            jPanels[i] = new JPanel();
            jPanels[i].setLayout(null);
            System.out.println(i);
        }

        //为用例设置名称
        useCaseNames[0] = " 用例 承运方注册 ";
        useCaseNames[1] = " 用例 承运方注销";
        useCaseNames[2] = " 用例 发货方注册 ";
        useCaseNames[3] = " 用例 发货方注销 ";
        useCaseNames[4] = " 用例 收货方注册 ";
        useCaseNames[5] = " 用例 收货方注销 ";
        useCaseNames[6] = " 用例 订单提交 ";
        useCaseNames[7] = " 用例 撤单 ";
        useCaseNames[8] = " 用例 抢单 ";
        useCaseNames[9] = " 用例 订单转手 ";
        useCaseNames[10] = " 用例 订单完成 ";
        useCaseNames[11] = " 用例 订单超时 ";

        //组件设置：将用例名称 写入选择按钮以及卡片的标题，
        for (int i = 0; i <= 11; i++) {
            selectedUseCaseButtons[i] = new JButton(useCaseNames[i]);
            useCaseTitleLables[i] = new JLabel(useCaseNames[i]);
        }

        //添加组件：为每个panal添加所需组件：下拉选择框、返回按钮 、多行文本清空按钮
        for (int i = 0; i <= 11; i++) {
            //初始化返回按钮
            backButtons[i] = new JButton("返回选择界面");
            initReturnButton(backButtons[i],jPanels[i]);
            //返回功能的实现
            buttonActionSwitchTo(backButtons[i],"card12");

            //添加清空按钮
            // initClearButtonIn(jPanels[i],showTextAreas[i]);
            //最后将panel放入card布局中
            cards.add(jPanels[i],"card"+i);
        }

        //设置起始界面：为起始选择界面添加选择按钮，为这些按钮设置位置
        for (int i = 0; i <= 7; i++) {
            selectedUseCaseButtons[i].setBounds(90, 35 * i+ 15,120,20);
            buttonActionSwitchTo(selectedUseCaseButtons[i],"card"+i);
            jPanels[12].add(selectedUseCaseButtons[i]);
        }
        selectedUseCaseButtons[9].setBounds(90, 35 * 8+ 15,120,20);
        buttonActionSwitchTo(selectedUseCaseButtons[9],"card"+9);
        jPanels[12].add(selectedUseCaseButtons[9]);
        selectedUseCaseButtons[10].setBounds(90, 35 * 9+ 15,120,20);
        buttonActionSwitchTo(selectedUseCaseButtons[10],"card"+10);
        jPanels[12].add(selectedUseCaseButtons[10]);
        cards.add(jPanels[12],"card12");

        //设置 用例 承运方注册 界面
        initUseCase0(jPanels[0]);
        //设置 用例 承运方注销 界面
        initUseCase1(jPanels[1]);
        //设置 用例 发货方注册 界面
        initUseCase2(jPanels[2]);
        //设置 用例 发货方注销 界面
        initUseCase3(jPanels[3]);
        //设置 用例 收货方注册 界面
        initUseCase4(jPanels[4]);
        //设置 用例 收货方注销 界面
        initUseCase5(jPanels[5]);
        //设置 用例 订单提交 撤单 抢单 订单转手 订单完成 订单超时
        initUseCase6(jPanels[6]);
        initUseCase7(jPanels[7]);

        //抢单界面已经写好，但应该是用不到
        //initUseCase8(jPanels[8]);
        initUseCase9(jPanels[9]);
        initUseCase10(jPanels[10]);
        //initUseCase11(jPanels[11]);

        // 展示主界面
        cl.show(cards,"card12");
        add(cards);

        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // 为跳转窗口按钮添加单击事件
    private void buttonActionSwitchTo(JButton b, final String string) {
        b.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                cl.show(cards,string);
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

    // 为多行可下拉文本框初始化
    private void initTextArea(JTextArea jTextArea, JPanel p,int y) {
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
        jsp.setBounds(10,y,size.width,size.height);
        p.add(jsp);
    }

    // 初始化返回按钮属性 并添加进卡片
    private void initReturnButton(JButton button,JPanel p) {
        button.setBounds(70,345,180,33);
        p.add(button);
        timeCount = 0;
        timeSwitch = false;
    }

    // 添加清空按钮和响应时间
    private void initClearButtonIn(JPanel p, final JTextArea jt) {
        JButton jButton = new JButton("清空");
        jButton.setBounds(10,340,100,35);
        // 清空按钮监听
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jt.setText("");
            }
        });
        p.add(jButton);
    }

    //用例本身与模拟器是有很大脱节的，比如用例注册，用例考虑的资质检查模拟器是很难体现的，
    //而模拟器所必要的车辆容量体积数据，在用例承运方注册中，是不会提交的。
    //设置 用例 承运方注册 界面
    /**
     *  承运方注册
     */
    void initUseCase0(JPanel jPanel){

        // 请输入要进行注册的承运方信息，承运方ID，运输类型，担保额度，
        // 车辆基础信息（车辆的容量和体积和机车动力（时间约束））；
        // 司机的行驶证号和驾驶证号；车辆或公司的运营证；ID可以为公司名称或者车牌号或两者都包含；
        // 保单信息；同意平台协议；银行账号；
        // 资质审查通过，担保额审查通过，注册成功。
        JLabel caption = new JLabel("用例 承运方注册");
        JLabel caption2 = new JLabel("请输入要进行注册的车辆信息");
        JLabel caption3 = new JLabel("（车牌号和公司名称请至少填写一项）");
        JLabel para1JLabel = new JLabel("车牌号");
        JLabel para10JLabel = new JLabel("公司名称");

        JLabel para11JLabel = new JLabel("载重/吨");
        JLabel para12JLabel = new JLabel("体积/平方米");
        JLabel para13JLabel = new JLabel("动力/马力");
        JLabel para14JLabel = new JLabel("银行账号");
        JLabel para2JLabel = new JLabel("运输类型");
        JLabel para21JLabel = new JLabel("行驶证号");
        JLabel para22JLabel = new JLabel("驾驶证号");
        JLabel para23JLabel = new JLabel("初始资金");
        JLabel para3JLabel = new JLabel("担保额度");
        JLabel para31JLabel = new JLabel("请在此处上传资格审查附件材料");
        JLabel para32JLabel = new JLabel("本人已阅读并同意用户协议");

        final JTextField t1 = new JTextField(2);
        final JTextField t10 = new JTextField(2);
        final JTextField t11 = new JTextField(2);
        final JTextField t12 = new JTextField(2);
        final JTextField t13 = new JTextField(2);
        final JTextField t14 = new JTextField(2);

        ButtonGroup group1 = new ButtonGroup();                 //JRadioButton 通常位于一个 ButtonGroup 按钮组中
        final JRadioButton group1Rb1 = new JRadioButton("一般");    //创建JRadioButton对象
        final JRadioButton group1Rb2 = new JRadioButton("冷链");
        final JRadioButton group1Rb3 = new JRadioButton("危险品");

        final JTextField t21 = new JTextField(2);
        final JTextField t22 = new JTextField(2);
        final JTextField t23 = new JTextField(2);

        final JTextField t3 = new JTextField(2);

        final JLabel result1JLabel = new JLabel("---------------------------");
        final JLabel result2JLabel = new JLabel();

        //组件位置设置
        caption.setBounds(100,10,100,30);
        caption2.setBounds(60,40,200,30);
        caption3.setBounds(45,60,250,30);

        para1JLabel.setBounds(20,90,70,20);
        t1.setBounds(90,90,50,20);
        para10JLabel.setBounds(150,90,70,20);
        t10.setBounds(220,90,60,20);

        para11JLabel.setBounds(20,110,70,20);
        t11.setBounds(90,110,50,20);
        para12JLabel.setBounds(150,110,90,20);
        t12.setBounds(230,110,50,20);
        para13JLabel.setBounds(20,130,70,20);
        t13.setBounds(90,130,50,20);
        para14JLabel.setBounds(150,130,70,20);
        t14.setBounds(220,130,50,20);

        para2JLabel.setBounds(20,155,150,20);

        group1Rb1.setBounds(80,153,60,25);
        group1Rb2.setBounds(140,153,60,25);
        group1Rb3.setBounds(200,153,90,25);

        para21JLabel.setBounds(20,180,70,20);
        t21.setBounds(90,180,50,20);
        para22JLabel.setBounds(150,180,70,20);
        t22.setBounds(220,180,50,20);

        para23JLabel.setBounds(20,200,70,20);
        t23.setBounds(90,200,50,20);
        para3JLabel.setBounds(150,200,70,20);
        t3.setBounds(220,200,50,20);

        para31JLabel.setBounds(30,225,190,30);

        para32JLabel.setBounds(35,250,180,30);
        JCheckBox chkbox1 = new JCheckBox(" ", false);    //创建指定文本和状态的复选框
        chkbox1.setBounds(10,250,150,30);

        result1JLabel.setBounds(50,275,280,20);
        result2JLabel.setBounds(50,295,150,20);

        group1.add(group1Rb1);
        group1.add(group1Rb2);
        group1.add(group1Rb3);

        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(caption3);
        jPanel.add(para1JLabel);
        jPanel.add(para10JLabel);
        jPanel.add(para11JLabel);
        jPanel.add(para12JLabel);
        jPanel.add(para13JLabel);
        jPanel.add(para14JLabel);
        jPanel.add(para2JLabel);
        jPanel.add(para21JLabel);
        jPanel.add(para22JLabel);
        jPanel.add(para23JLabel);
        jPanel.add(para3JLabel);
        jPanel.add(para31JLabel);
        jPanel.add(para32JLabel);
        jPanel.add(t1);
        jPanel.add(t10);
        jPanel.add(t11);
        jPanel.add(t12);
        jPanel.add(t13);
        jPanel.add(t14);
        jPanel.add(t21);
        jPanel.add(t22);
        jPanel.add(t23);
        jPanel.add(group1Rb1);
        jPanel.add(group1Rb2);
        jPanel.add(group1Rb3);
        jPanel.add(t3);
        jPanel.add(result1JLabel);
        jPanel.add(result2JLabel);
        jPanel.add(chkbox1);

        //添加上传按钮
        JButton uploadAttachment = new JButton("上传");
        uploadAttachment.setBounds(210,225,50,30);
        // 上传按钮监听
        uploadAttachment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jFrameUploadAttachment .setVisible(true);
                havaUpload = true;
                //                JOptionPane.showMessageDialog(null,"请上传以下附件\n" +
//                        "1行驶证驾驶证扫描件\n2运营证扫描件\n3保单扫描件\n4身份证扫描件\n" +
//                        "5营业执照扫描件\n6车辆图片扫描件\n...","审核材料上传",1);
            }
        });

        jPanel.add(uploadAttachment);

        //添加查看协议按钮
        JButton checkseregulations = new JButton("查看协议");
        checkseregulations.setBounds(210,250,80,30);
        // 查看按钮监听
        checkseregulations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"用户协议的各项条例\n1....\n2....\n...","用户协议",1);
            }
        });
        jPanel.add(checkseregulations);

        //添加注册按钮
        JButton register = new JButton("注册");
        register.setBounds(50,310,100,35);
        // 注册按钮监听
        register.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {


                u0Weight = Integer.parseInt(t11.getText());
                u0Volumn = Integer.parseInt(t12.getText());

                if(!jFrameUploadAttachment.havaChecked()){
                    JOptionPane.showMessageDialog(null,"请上传所有的附件","请重新设置",0);
                    return;
                }
                if(t10.getText().equals("")){
                    para1 = t1.getText();
                } else if(t1.getText().equals("")){
                    para1 = t10.getText();
                }else {
                    para1 = t10.getText()+"-"+t1.getText();
                }
                if(para1.equals("-")||para1.equals("")){
                    JOptionPane.showMessageDialog(null,"请至少输入个人名称或公司名称中的一个","请重新设置",0);
                    jFrameUploadAttachment.reset();
                    return;
                }
                if(!chkbox1.isSelected()){
                    JOptionPane.showMessageDialog(null,"请先阅读并同意用户协议","注册失败",1);

                    jFrameUploadAttachment.reset();
                    return;
                }
                if( t3.getText().equals("") ) {
                    JOptionPane.showMessageDialog(null,"参数不能为空","请重新设置",0);
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("承运方注册失败！");
                    return;
                }
                if(!havaUpload){
                    JOptionPane.showMessageDialog(null,"请上传有效附件","附件未上传",0);
                    jFrameUploadAttachment.reset();
                    return;
                }

                if(group1Rb1.isSelected()) para2 = 1;
                if(group1Rb2.isSelected()) para2 = 2;
                if(group1Rb3.isSelected()) para2 = 3;

                if(lp.haveTruckByID(para1)) {
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("承运方注册失败！");
                    JOptionPane.showMessageDialog(null,"该承运方已经注册","请重新设置",0);
                    jFrameUploadAttachment.reset();
                    return;
                }
                para3 = Integer.parseInt(t3.getText());
                if( para1.equals(null) || para2 == -1 || para3 < 0 ) {
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("承运方注册失败！");
                    JOptionPane.showMessageDialog(null,"信息设置错误","请重新设置",0);
                    jFrameUploadAttachment.reset();
                    return;
                } else {
                    u0BankMoney = Double.parseDouble(t23.getText());
                    result1JLabel.setText("车辆"+para1+"进行注册");
                    result2JLabel.setText("承运方注册成功！");
                    JOptionPane.showMessageDialog(null,"等待审查通过后激活！","注册成功！",1);
                    testEvent = 0;
                    jFrameUploadAttachment.reset();
                }
            }
        });
        jPanel.add(register);

        //添加激活按钮
        JButton activation = new JButton("激活");
        activation.setBounds(170,310,100,35);
        // 激活按钮监听
        activation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(t10.getText().equals("")){
                    para1 = t1.getText();
                } else if(t1.getText().equals("")){
                    para1 = t10.getText();
                }else {
                    para1 = t10.getText()+"-"+t1.getText();
                }

                if(para1.equals("-")||para1.equals("")){
                    JOptionPane.showMessageDialog(null,"请至少输入个人名称或公司名称中的一个","请重新设置",0);
                    return;
                }

                if( !lp.haveTruckByID(para1)) {
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("承运方激活失败！");
                    JOptionPane.showMessageDialog(null,"该承运方未注册","无法激活",0);
                    return;
                }
                if( lp.findTruckByID(para1).isActivated()) {
                    result1JLabel.setText("请勿重复激活");
                    result2JLabel.setText("该承运方已经激活！");
                    JOptionPane.showMessageDialog(null,"该承运方已经激活","请勿重复激活",0);
                    return;
                }
                result1JLabel.setText("激活成功");
                result2JLabel.setText("该承运方审核通过！");
                JOptionPane.showMessageDialog(null,"激活成功","该承运方各项资格审查通过",1);
                lp.findTruckByID(para1).setActivated(true);
            }
        });
        jPanel.add(activation);
    }

    /**
     *  承运方注销
     */
    void initUseCase1(JPanel jPanel){
        JLabel caption = new JLabel("用例 承运方注销");
        JLabel caption2 = new JLabel("请输入要进行注销的承运方车牌号");
        final JTextField truckIdJText = new JTextField(2);
        final JLabel result1JLabel = new JLabel("--------------------——-");
        final JLabel result2JLabel = new JLabel();
        //组件位置设置
        caption.setBounds(90,10,100,30);
        caption2.setBounds(50,120,200,30);
        truckIdJText.setBounds(90,160,90,30);
        result1JLabel.setBounds(40,215,250,20);
        result2JLabel.setBounds(40,235,250,20);
        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(truckIdJText);
        jPanel.add(result1JLabel);
        jPanel.add(result2JLabel);
        //添加注销按钮
        JButton jButton = new JButton("注销");
        jButton.setBounds(100,270,100,35);
        // 按钮监听
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean found = false;
                for(int i = 0; i < lp.getTrucksInPlatform().size(); i++) {
                    if(lp.getTrucksInPlatform().get(i).getID().equals(truckIdJText.getText())){
                        found = true;
                        if(lp.getTrucksInPlatform().get(i).isEmpty()){
                            para4 = lp.getTrucksInPlatform().get(i);
                            testEvent = 1;
                            JOptionPane.showMessageDialog(null,"注销成功","成功",1);
                            result1JLabel.setText("承运方"+truckIdJText.getText()+"注销成功！");
                            result2JLabel.setText("");

                        }else {
                            result1JLabel.setText("承运方"+truckIdJText.getText()+"注销失败！");
                            result2JLabel.setText("原因是：该承运方尚有未完成的订单！");
                            JOptionPane.showMessageDialog(null,"注销失败","失败",0);
                        }
                    }
                }
                if(!found)  {
                    result1JLabel.setText("该承运方当前未在平台注册");
                    result2JLabel.setText("");
                    JOptionPane.showMessageDialog(null,"该承运方当前未在平台注册","失败",0);
                }
            }
        });
        jPanel.add(jButton);
    }

    /**
     *  发货方注册
     */
    void initUseCase2(JPanel jPanel){
        // 个人或公司名称；
        // 个人身份证号或公司社会统一代码（和生成的ID绑定）；
        // 行业类别；同意平台协议；备注；银行账号；
        // 手机号或座机号；地址；
        JLabel caption = new JLabel("用例 发货方注册");
        JLabel caption2 = new JLabel("请输入要进行注册的发货方方信息");
        JLabel caption3 = new JLabel("（车牌号和公司名称请至少填写一项）");
        JLabel para1JLabel = new JLabel("个人名称");
        JLabel para10JLabel = new JLabel("公司名称");

        JLabel para11JLabel = new JLabel("个人身份证");
        JLabel para12JLabel = new JLabel("公司统一代码");
        JLabel para13JLabel = new JLabel("行业类别");
        JLabel para2JLabel = new JLabel("银行账号");
        JTextField t2 = new JTextField("");
        JLabel para21JLabel = new JLabel("地址");
        JLabel para22JLabel = new JLabel("手机/座机号");
        JLabel para23JLabel = new JLabel("初始资金");
        JLabel para32JLabel = new JLabel("本人已阅读并同意用户协议");

        final JTextField t1 = new JTextField(2);
        final JTextField t10 = new JTextField(2);
        final JTextField t11 = new JTextField(2);
        final JTextField t12 = new JTextField(2);
        final JTextField t13 = new JTextField(2);

        final JTextField t21 = new JTextField(2);
        final JTextField t22 = new JTextField(2);
        final JTextField t23 = new JTextField(2);

        final JLabel result1JLabel = new JLabel("---------------------------");
        final JLabel result2JLabel = new JLabel();

        //组件位置设置
        caption.setBounds(100,10,100,30);
        caption2.setBounds(60,40,200,30);
        caption3.setBounds(45,60,250,30);

        para1JLabel.setBounds(20,100,70,20);
        t1.setBounds(90,100,50,20);
        para10JLabel.setBounds(150,100,70,20);
        t10.setBounds(220,100,60,20);

        para11JLabel.setBounds(20,130,70,20);
        t11.setBounds(90,130,50,20);
        para12JLabel.setBounds(150,130,90,20);
        t12.setBounds(230,130,50,20);

        para13JLabel.setBounds(20,160,70,20);
        t13.setBounds(90,160,50,20);
        para2JLabel.setBounds(150,160,150,20);
        t2.setBounds(220,160,50,20);

        para21JLabel.setBounds(20,190,70,20);
        t21.setBounds(90,190,50,20);
        para22JLabel.setBounds(150,190,130,20);
        t22.setBounds(240,190,50,20);

        para23JLabel.setBounds(20,220,70,20);
        t23.setBounds(90,220,150,20);

        para32JLabel.setBounds(35,250,180,30);
        JCheckBox chkbox1 = new JCheckBox(" ", false);    //创建指定文本和状态的复选框
        chkbox1.setBounds(10,250,150,30);

        result1JLabel.setBounds(50,275,280,20);
        result2JLabel.setBounds(50,295,150,20);

        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(caption3);
        jPanel.add(para1JLabel);
        jPanel.add(para10JLabel);
        jPanel.add(para11JLabel);
        jPanel.add(para12JLabel);
        jPanel.add(para13JLabel);
        jPanel.add(para2JLabel);
        jPanel.add(para21JLabel);
        jPanel.add(para22JLabel);
        jPanel.add(para23JLabel);
        jPanel.add(t2);
        jPanel.add( para32JLabel);
        jPanel.add(t1);
        jPanel.add(t10);
        jPanel.add(t11);
        jPanel.add(t12);
        jPanel.add(t13);
        jPanel.add(t21);
        jPanel.add(t22);
        jPanel.add(t23);
        jPanel.add(result1JLabel);
        jPanel.add(result2JLabel);
        jPanel.add(chkbox1);

        //添加查看协议按钮
        JButton checkseregulations = new JButton("查看协议");
        checkseregulations.setBounds(210,250,80,30);
        // 查看按钮监听
        checkseregulations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"用户协议的各项条例\n1....\n2....\n...","用户协议",1);
            }
        });
        jPanel.add(checkseregulations);

        //添加注册按钮
        JButton register = new JButton("注册");
        register.setBounds(50,310,100,35);
        // 注册按钮监听
        register.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(t10.getText().equals("")){
                    para5 = t1.getText();
                } else if(t1.getText().equals("")){
                    para5 = t10.getText();
                }else {
                    para5 = t10.getText()+"-"+t1.getText();
                }

                if(para5.equals("-")||para5.equals("")){
                    JOptionPane.showMessageDialog(null,"请至少输入个人名称或公司名称中的一个","请重新设置",0);
                    return;
                }
                if(!chkbox1.isSelected()){
                    JOptionPane.showMessageDialog(null,"请先阅读并同意用户协议","注册失败",0);
                    return;
                }
                if( t2.getText().equals("") ) {
                    JOptionPane.showMessageDialog(null,"参数不能为空","请重新设置",0);
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("发货方注册失败！");
                    return;
                }
                if(lp.haveShipperByID(para5)) {
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("发货方注册失败！");
                    JOptionPane.showMessageDialog(null,"该发货方已经注册","请重新设置",0);
                    return;
                }
                result1JLabel.setText("发货方"+para5+"进行注册");
                result2JLabel.setText("发货方注册成功！");
                u2BankMoney = Double.parseDouble(t23.getText());
                JOptionPane.showMessageDialog(null,"等待审查通过后激活！","注册成功！",1);
                testEvent = 2;
            }
        });
        jPanel.add(register);

        //添加激活按钮
        JButton activation = new JButton("激活");
        activation.setBounds(170,310,100,35);
        // 激活按钮监听
        activation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(t10.getText().equals("")){
                    para5 = t1.getText();
                } else if(t1.getText().equals("")){
                    para5 = t10.getText();
                }else {
                    para5 = t10.getText()+"-"+t1.getText();
                }

                if(para5.equals("-")||para5.equals("")){
                    JOptionPane.showMessageDialog(null,"请至少输入个人名称或公司名称中的一个","请重新设置",0);
                    return;
                }
                if(  t2.getText().equals("") ) {
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("发货方激活失败！");
                    JOptionPane.showMessageDialog(null,"参数不能为空","请重新设置",0);
                    return;
                }
                if( !lp.haveShipperByID(para5)) {
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("发货方激活失败！");
                    JOptionPane.showMessageDialog(null,"该发货方未注册","无法激活",0);
                    return;
                }
                if( lp.findShipperByID(para5).isActivated()) {

                    result1JLabel.setText("请勿重复激活");
                    result2JLabel.setText("该发货方已经激活！");
                    JOptionPane.showMessageDialog(null,"该发货方已经激活","请勿重复激活",0);
                    return;
                }
                result1JLabel.setText("激活成功");
                result2JLabel.setText("该发货方审核通过！");
                JOptionPane.showMessageDialog(null,"激活成功","该发货方各项资格审查通过",1);
                lp.findShipperByID(para5).setActivated(true);
            }
        });
        jPanel.add(activation);
    }

    /**
     *  发货方注销
     */
    void initUseCase3(JPanel jPanel){
        JLabel caption = new JLabel("用例 发货方注销");
        JLabel caption2 = new JLabel("目前所有发货方信息如下");
        JButton refresh = new JButton("刷新");
        final JTextArea cardTextArea = new JTextArea("",12,22);
        initTextArea(cardTextArea,jPanel,65);
        JLabel caption3 = new JLabel("请输入要注销的发货方Id");
        final JTextField Id = new JTextField();
        JButton confirm = new JButton("确定");
        final JLabel result1JLabel = new JLabel("--------------------------");
        final JLabel result2JLabel = new JLabel();

        //组件位置设置
        caption.setBounds(90,10,100,30);
        caption2.setBounds(50,40,160,30);
        refresh.setBounds(200,40,50,30);
        caption3.setBounds(40,260,180,30);
        Id.setBounds(190,260,50,25);
        result1JLabel.setBounds(40,280,250,20);
        // result2JLabel.setBounds(10,290,250,20);
        confirm.setBounds(120,300,80,35);

        // 刷新按钮监听
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //刷新多行文本框的内容
                cardTextArea.setText("");
                cardTextArea.append("当前发货方共有"+lp.getShippersInPlatform().size()+"个 \n分别为：\n");
                for(int i = 0;i < lp.getShippersInPlatform().size();i++){
                    cardTextArea.append(lp.getShippersInPlatform().get(i).showCurrentStatus());
                }
            }
        });
        // 确认按钮监听
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //确认操作，显示相应的返回结果
                String ansID = Id.getText();
                int ansIndex = -1;
                boolean notFound = true;
                for(int i = 0;i < lp.getShippersInPlatform().size();i++){
                    if(lp.getShippersInPlatform().get(i).getID().equals(ansID)){
                        notFound = false;
                        ansIndex = i;
                    }
                }
                if(ansID.equals("")) {
                    JOptionPane.showMessageDialog(null,"ID不能为空","请重新设置",0);
                    return;
                }else if(notFound){
                    JOptionPane.showMessageDialog(null,"该ID发货方未注册","请重新设置",0);
                    return;
                }else if(lp.getShippersInPlatform().get(ansIndex).getCurrentCargoNum() == 0){
                    result1JLabel.setText("Id为" + ansID + "的发货方注销成功");
                    para6 = lp.getShippersInPlatform().get(ansIndex);
                    JOptionPane.showMessageDialog(null,"注销成功","成功",1);
                    testEvent = 3;
                    return;
                }else {
                    result1JLabel.setText("注销失败！该发货方仍有未完成订单");
                    JOptionPane.showMessageDialog(null,"注销失败","失败",0);

                }
            }
        });
        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(refresh);
        jPanel.add(caption3);
        jPanel.add(Id);
        jPanel.add(confirm);
        jPanel.add(result1JLabel);
        jPanel.add(result2JLabel);
    }

    /**
     *  收货方注册
     */
    void initUseCase4(JPanel jPanel){
        // 个人或公司名称；
        // 个人身份证号或公司社会统一代码（和生成的ID绑定）；
        // 行业类别；同意平台协议；备注；银行账号；
        // 手机号或座机号；地址；
        JLabel caption = new JLabel("用例 收货方注册");
        JLabel caption2 = new JLabel("请输入要进行注册的收货方信息");
        JLabel caption3 = new JLabel("（车牌号和公司名称请至少填写一项）");
        JLabel para1JLabel = new JLabel("个人名称");
        JLabel para10JLabel = new JLabel("公司名称");
        JLabel para11JLabel = new JLabel("个人身份证");
        JLabel para12JLabel = new JLabel("公司统一代码");
        JLabel para13JLabel = new JLabel("行业类别");
        JLabel para2JLabel = new JLabel("银行账号");
        JTextField t2 = new JTextField("");
        JLabel para21JLabel = new JLabel("地址");
        JLabel para22JLabel = new JLabel("手机/座机号");
        JLabel para23JLabel = new JLabel("备注");
        JLabel para32JLabel = new JLabel("本人已阅读并同意用户协议");
        final JTextField t1 = new JTextField(2);
        final JTextField t10 = new JTextField(2);
        final JTextField t11 = new JTextField(2);
        final JTextField t12 = new JTextField(2);
        final JTextField t13 = new JTextField(2);
        final JTextField t21 = new JTextField(2);
        final JTextField t22 = new JTextField(2);
        final JTextField t23 = new JTextField(2);
        final JLabel result1JLabel = new JLabel("---------------------------");
        final JLabel result2JLabel = new JLabel();

        //组件位置设置
        caption.setBounds(100,10,100,30);
        caption2.setBounds(60,40,200,30);
        caption3.setBounds(45,60,250,30);

        para1JLabel.setBounds(20,100,70,20);
        t1.setBounds(90,100,50,20);
        para10JLabel.setBounds(150,100,70,20);
        t10.setBounds(220,100,60,20);

        para11JLabel.setBounds(20,130,70,20);
        t11.setBounds(90,130,50,20);
        para12JLabel.setBounds(150,130,90,20);
        t12.setBounds(230,130,50,20);

        para13JLabel.setBounds(20,160,70,20);
        t13.setBounds(90,160,50,20);
        para2JLabel.setBounds(150,160,150,20);
        t2.setBounds(220,160,50,20);

        para21JLabel.setBounds(20,190,70,20);
        t21.setBounds(90,190,50,20);
        para22JLabel.setBounds(150,190,130,20);
        t22.setBounds(240,190,50,20);

        para23JLabel.setBounds(20,220,70,20);
        t23.setBounds(90,220,150,20);

        para32JLabel.setBounds(35,250,180,30);
        JCheckBox chkbox1=new JCheckBox(" ", false);    //创建指定文本和状态的复选框
        chkbox1.setBounds(10,250,150,30);

        result1JLabel.setBounds(50,275,280,20);
        result2JLabel.setBounds(50,295,150,20);

        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(caption3);
        jPanel.add(para1JLabel);
        jPanel.add(para10JLabel);
        jPanel.add(para11JLabel);
        jPanel.add(para12JLabel);
        jPanel.add(para13JLabel);
        jPanel.add(para2JLabel);
        jPanel.add(para21JLabel);
        jPanel.add(para22JLabel);
        jPanel.add(para23JLabel);
        jPanel.add(t2);
        jPanel.add( para32JLabel);
        jPanel.add(t1);
        jPanel.add(t10);
        jPanel.add(t11);
        jPanel.add(t12);
        jPanel.add(t13);
        jPanel.add(t21);
        jPanel.add(t22);
        jPanel.add(t23);
        jPanel.add(result1JLabel);
        jPanel.add(result2JLabel);
        jPanel.add(chkbox1);

        //添加查看协议按钮
        JButton checkseregulations = new JButton("查看协议");
        checkseregulations.setBounds(210,250,80,30);
        // 查看按钮监听
        checkseregulations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"用户协议的各项条例\n1....\n2....\n...","用户协议",1);
            }
        });
        jPanel.add(checkseregulations);

        //添加注册按钮
        JButton register = new JButton("注册");
        register.setBounds(50,310,100,35);
        // 注册按钮监听
        register.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(t10.getText().equals("")){
                    para7 = t1.getText();
                } else if(t1.getText().equals("")){
                    para7 = t10.getText();
                }else {
                    para7 = t10.getText()+"-"+t1.getText();
                }
                if(para7.equals("-")||para7.equals("")){
                    JOptionPane.showMessageDialog(null,"请至少输入个人名称或公司名称中的一个","请重新设置",0);
                    return;
                }
                if(!chkbox1.isSelected()){
                    JOptionPane.showMessageDialog(null,"请先阅读并同意用户协议","注册失败",0);
                    return;
                }
                if( t2.getText().equals("") ) {
                    JOptionPane.showMessageDialog(null,"参数不能为空","请重新设置",0);
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("收货方注册失败！");
                    return;
                }
                if(lp.haveReceiverByID(para7)) {
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("收货方注册失败！");
                    JOptionPane.showMessageDialog(null,"该收货方已经注册","请重新设置",0);
                    return;
                }
                result1JLabel.setText("收货方"+para7+"进行注册");
                result2JLabel.setText("收货方注册成功！");
                JOptionPane.showMessageDialog(null,"等待审查通过后激活！","注册成功！",1);
                testEvent = 4;
            }
        });
        jPanel.add(register);

        //添加激活按钮
        JButton activation = new JButton("激活");
        activation.setBounds(170,310,100,35);
        // 激活按钮监听
        activation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(t10.getText().equals("")){
                    para7 = t1.getText();
                } else if(t1.getText().equals("")){
                    para7 = t10.getText();
                }else {
                    para7 = t10.getText()+"-"+t1.getText();
                }

                if(para7.equals("-")||para7.equals("")){
                    JOptionPane.showMessageDialog(null,"请至少输入个人名称或公司名称中的一个","请重新设置",0);
                    return;
                }
                if(  t2.getText().equals("") ) {
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("收货方激活失败！");
                    JOptionPane.showMessageDialog(null,"参数不能为空","请重新设置",0);
                    return;
                }
                if( !lp.haveReceiverByID(para7)) {
                    result1JLabel.setText("信息无效");
                    result2JLabel.setText("收货方激活失败！");
                    JOptionPane.showMessageDialog(null,"该收货方未注册","无法激活",0);
                    return;
                }
                if( lp.findReceiverByID(para7).isActivated()) {
                    result1JLabel.setText("请勿重复激活");
                    result2JLabel.setText("该收货方已经激活！");
                    JOptionPane.showMessageDialog(null,"该收货方已经激活","请勿重复激活",0);
                    return;
                }
                result1JLabel.setText("激活成功");
                result2JLabel.setText("该收货方审核通过！");
                JOptionPane.showMessageDialog(null,"激活成功","该收货方各项资格审查通过",1);
                lp.findReceiverByID(para7).setActivated(true);
            }
        });
        jPanel.add(activation);
    }

    /**
     *  收货方注销
     */
    void initUseCase5(JPanel jPanel){
        JLabel caption = new JLabel("用例 收货方注销");
        JLabel caption2 = new JLabel("目前所有收货方信息如下");
        JButton refresh = new JButton("刷新");
        final JTextArea cardTextArea = new JTextArea("",12,22);
        initTextArea(cardTextArea,jPanel,65);
        JLabel caption3 = new JLabel("请输入要注销的收货方Id");
        final JTextField Id = new JTextField();
        JButton confirm = new JButton("确定");
        final JLabel result1JLabel = new JLabel("-------------------------");
        final JLabel result2JLabel = new JLabel();
        //组件位置设置
        caption.setBounds(90,10,120,30);
        caption2.setBounds(50,40,160,30);
        refresh.setBounds(200,40,50,30);
        caption3.setBounds(40,260,180,30);
        Id.setBounds(190,260,50,25);
        result1JLabel.setBounds(40,280,250,20);
        // result2JLabel.setBounds(10,290,250,20);
        confirm.setBounds(120,300,80,35);
        // 刷新按钮监听
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //刷新多行文本框的内容
                cardTextArea.setText("");
                cardTextArea.append("当前收货方共有"+lp.getReceiversInPlatform().size() +"个 \n分别为：\n");
                for(int i = 0;i < lp.getReceiversInPlatform().size();i++){
                    cardTextArea.append(lp.getReceiversInPlatform().get(i).showCurrentStatus());
                }
            }
        });
        // 确认按钮监听
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                //确认操作，显示相应的返回结果
                String ansID = Id.getText();
                int ansIndex = -1;
                boolean notFound = true;
                for(int i = 0;i < lp.getReceiversInPlatform().size();i++){
                    if(lp.getReceiversInPlatform().get(i).getID().equals(ansID)){
                        notFound = false;
                        ansIndex = i;
                    }
                }
                if(ansID.equals("")) {
                    JOptionPane.showMessageDialog(null,"ID不能为空","请重新设置",0);
                    return;
                }else if(notFound){
                    JOptionPane.showMessageDialog(null,"该ID收货方未注册","请重新设置",0);
                    return;
                }else if(lp.getReceiversInPlatform().get(ansIndex).getCurrentCargoNum() == 0){

                    result1JLabel.setText("Id为" + ansID + "的收货方注销成功");
                    para8 = lp.getReceiversInPlatform().get(ansIndex);
                    JOptionPane.showMessageDialog(null,"注销成功","成功",1);
                    testEvent = 5;
                    return;
                }else {
                    result1JLabel.setText("删除失败！该收货方仍有需要接收的订单");
                    JOptionPane.showMessageDialog(null,"注销失败","失败",1);

                }
            }
        });

        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(refresh);
        jPanel.add(caption3);
        jPanel.add(Id);
        jPanel.add(confirm);
        jPanel.add(result1JLabel);
        jPanel.add(result2JLabel);
    }


    /**
     *  订单提交
     */
    void initUseCase6(JPanel jPanel){
        //提交新订单，发货方ID 收货人ID  订单ID 运费 违约金 最迟到达时间 返回撤单成功
        //订单提交：货物类别；发货方/收货方的姓名/身份证号/地址/手机号；运费；备注车辆信息；违约金；最迟到达时间t
        JLabel caption = new JLabel("用例 订单提交");
        JLabel caption2 = new JLabel("请输入订单信息：");
        JLabel jLabel1 = new JLabel("发货方姓名：");
        final JTextField t1 = new JTextField();
        JLabel jLabel2 = new JLabel("收货方姓名");
        final JTextField t2 = new JTextField();
        JLabel jLabel3 = new JLabel("运费：");
        final JTextField t3 = new JTextField();
        JLabel jLabel4 = new JLabel("违约金：");
        final JTextField t4 = new JTextField();
        JLabel jLabel5 = new JLabel("最迟运达时间：");
        final JTextField t5 = new JTextField();
        JLabel jLabel6 = new JLabel("货物ID：");
        final JTextField t6 = new JTextField();
        JLabel n1 = new JLabel("发货人身份证");
        JLabel n2 = new JLabel("收货人身份证");
        JLabel n3 = new JLabel("发货人地址");
        JLabel n4 = new JLabel("收货人地址");
        JLabel n5 = new JLabel("发货人手机号");
        JLabel n6 = new JLabel("收货人手机号");
        JLabel n7 = new JLabel("车辆类型(1普通2冷链3危险品)：");
        JLabel n8 = new JLabel("备注");
        JTextField n1t = new JTextField("");
        JTextField n2t = new JTextField("");
        JTextField n3t = new JTextField("");
        JTextField n4t = new JTextField("");
        JTextField n5t = new JTextField("");
        JTextField n6t = new JTextField("");
        JTextField n7t = new JTextField("");
        JTextField n8t = new JTextField("");
        final JLabel result1 = new JLabel("-----------------------");
        JLabel result2 = new JLabel("");
        JButton confirm = new JButton("提交订单");

        //组件位置设置
        int interval = 25;
        caption.setBounds(110,10,120,30);
        caption2.setBounds(10,40 + 0 * interval,160,30);
        jLabel6.setBounds(10,40 + 1 * interval,160,30);
        jLabel1.setBounds(10,40 + 2 * interval,160,30);
        jLabel2.setBounds(10,40 + 3 * interval,160,30);
        jLabel3.setBounds(10,40 + 4 * interval,160,30);
        jLabel4.setBounds(10,40 + 5 * interval,160,30);
        jLabel5.setBounds(10,40 + 6 * interval,160,30);

        n1.setBounds(140,40 + 1 * interval,160,30);
        n2.setBounds(140,40 + 2 * interval,160,30);
        n3.setBounds(140,40 + 3 * interval,160,30);
        n4.setBounds(140,40 + 4 * interval,160,30);
        n5.setBounds(140,40 + 5 * interval,160,30);
        n6.setBounds(140,40 + 6 * interval,160,30);
        n7.setBounds(10,40 + 7 * interval,200,30);
        n8.setBounds(10,40 + 8 * interval,160,30);

        n1t.setBounds(230,40 + 1 * interval,60,30);
        n2t.setBounds(230,40 + 2 * interval,60,30);
        n3t.setBounds(230,40 + 3 * interval,60,30);
        n4t.setBounds(230,40 + 4 * interval,60,30);
        n5t.setBounds(230,40 + 5 * interval,60,30);
        n6t.setBounds(230,40 + 6 * interval,60,30);
        n7t.setBounds(200,40 + 7 * interval,60,30);
        n8t.setBounds(90,40 + 8 * interval,160,30);

        t1.setBounds(90,40 + 1 * interval,50,30);
        t2.setBounds(90,40 + 2 * interval,50,30);
        t3.setBounds(90,40 + 3 * interval,50,30);
        t4.setBounds(90,40 + 4 * interval,50,30);
        t5.setBounds(90,40 + 5 * interval,50,30);
        t6.setBounds(90,40 + 6 * interval,50,30);

        result1.setBounds(50,40 + 9 * interval,200,30);
        result2.setBounds(50,40 + 10 * interval,200,30);
        confirm.setBounds(115,40 + 11 * interval,80,35);

        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(jLabel1);
        jPanel.add(jLabel2);
        jPanel.add(jLabel3);
        jPanel.add(jLabel4);
        jPanel.add(jLabel5);
        jPanel.add(jLabel6);
        jPanel.add(t1);
        jPanel.add(t2);
        jPanel.add(t3);
        jPanel.add(t4);
        jPanel.add(t5);
        jPanel.add(t6);
        jPanel.add(result1);
        jPanel.add(result2);
        jPanel.add(confirm);
        jPanel.add(n1);
        jPanel.add(n2);
        jPanel.add(n3);
        jPanel.add(n4);
        jPanel.add(n5);
        jPanel.add(n6);
        jPanel.add(n7);
        jPanel.add(n8);
        jPanel.add(n1t);
        jPanel.add(n2t);
        jPanel.add(n3t);
        jPanel.add(n4t);
        jPanel.add(n5t);
        jPanel.add(n6t);
        jPanel.add(n7t);
        jPanel.add(n8t);

        // 确认按钮监听 检查并返回id是否存在，数值是否输入错误
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            //TODO
                if(t1.getText().equals("") || t2.getText().equals("")|| t3.getText() .equals("") ||
                        t4.getText() .equals("") || t5.getText() .equals("") ||t6.getText() .equals("")||
                        Integer.parseInt(t4.getText()) < 0 || Integer.parseInt(t5.getText()) < 0) {
                    JOptionPane.showMessageDialog(null,"设置不正确","请重新设置",0);
                    return;
                }else if(lp.haveShipperByID(t2.getText())  && lp.haveReceiverByID(t3.getText())) {

                    if(lp.haveCargoByID(t1.getText())){
                        result1.setText("发布失败！该ID的订单已经存在！");
                        JOptionPane.showMessageDialog(null,"订单ID重复","请重新设置",0);
                        return;
                    }
                    int i = Integer.parseInt(n7t.getText());

                    int begin = Integer.parseInt(n3t.getText());
                    int end = Integer.parseInt(n4t.getText());

                    String MAP_FILE = "/data/maps/leuven-simple.dot";
                    String fileName="/Users/yuzhongzhiheren/Desktop/LogisticsPlatform/example/src/main/java/com/github/rinde/rinsim/examples/LogisticsPlatform/map.txt";
                    String line1 ="";
                    String line2 ="";

                    double x1 = 0;
                    double y1 = 0;
                    double x2 = 0;
                    double y2 = 0;

                    try {
                        BufferedReader in=new BufferedReader(new FileReader(fileName));

                        for(int j = 1; j <=50; j++) {
                            line1 = in.readLine();
                            line2 = in.readLine();
                            if (j == (begin*2-1)) {
                                x1 = Double.parseDouble(line1);
                                y1 = Double.parseDouble(line2);

                            }
                            if(j == (end*2-1)) {
                                x2 = Double.parseDouble(line1);
                                y2 = Double.parseDouble(line2);
                            }
                        }
                        in.close();
                    } catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }

                    useCaseTest6Cargo = TruckLogistics.positionCargo(roadModel,x1,y1,x2,y2);
                    useCaseTest6Cargo.setNeededCarryType(i);
                    useCaseTest6Cargo.setID(t1.getText());
                    useCaseTest6Cargo.setShipper(lp.findShipperByID(t2.getText()));
                    useCaseTest6Cargo.setReceiver(lp.findReceiverByID(t3.getText()));
                    useCaseTest6Cargo.setFreightFare(Integer.parseInt(t4.getText()));
                    useCaseTest6Cargo.setOriginFreightFare(Integer.parseInt(t4.getText()));
                    useCaseTest6Cargo.setLiquidatedDamages(Integer.parseInt(t5.getText()));
                    useCaseTest6Cargo.setLimitedHour(Integer.parseInt(t6.getText()));
                    lp.findShipperByID(t2.getText()).getCargos().add(useCaseTest6Cargo);

                    //发货方资金不足则提示发货失败
                    if(bank.getAccount_DisposableMoneyMap().get(lp.findShipperByID(t2.getText())) < Integer.parseInt(t4.getText())){
                        result1.setText("     货物"+t1.getText()+" 资金不足，发布失败！");
                        JOptionPane.showMessageDialog(null,"该发货方资金不足","请重新设置",0);
                    }else{
                        JOptionPane.showMessageDialog(null,"订单发布成功","成功消息",1);
                        result1.setText("    货物"+useCaseTest6Cargo.getID()+"发布成功！");
                        testEvent = 6;
                        return;
                    }
                }else {
                    JOptionPane.showMessageDialog(null,"发货方或者收货方不存在","请重新设置",0);
                }
            }
        });
    }

    /**
     *  撤单
     */
    void initUseCase7(JPanel jPanel){
        //输入撤单的id ，根据订单状态确定能否撤单
        JLabel caption = new JLabel("用例 撤单");
        JLabel caption2 = new JLabel("请输入要进行撤单的订单ID");
        final JTextField t1 = new JTextField();
        final JLabel result1 = new JLabel("------------------------");
        final JLabel result2 = new JLabel("");
        final JLabel result3 = new JLabel("");
        JButton confirm = new JButton("确认撤单");
        //组件位置设置
        caption.setBounds(90,10,120,30);
        caption2.setBounds(60,80,160,30);
        t1.setBounds(110,125,60,30);
        result1.setBounds(50,160,200,30);
        result2.setBounds(50,200,200,30);
        result3.setBounds(50,240,200,30);
        confirm.setBounds(100,290,80,35);
        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(result1);
        jPanel.add(result2);
        jPanel.add(result3);
        jPanel.add(t1);
        jPanel.add(confirm);
        // 确认按钮监听 检查并返回id是否存在，根据状态决定能否撤单以及简单展示撤单处理结果
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(t1.getText().equals("")){
                    JOptionPane.showMessageDialog(null,"设置不正确","请重新设置",0);
                    return;
                }
                if(!lp.haveCargoByID(t1.getText()) && !lp.havePublishingCargoByID(t1.getText())){
                    JOptionPane.showMessageDialog(null,"该货物不存在","请重新设置",0);
                    return;
                }

                Cargo cargo = null;
                if(lp.haveCargoByID(t1.getText())){
                    cargo= lp.findCargoByID(t1.getText());
                }
                if(lp.havePublishingCargoByID(t1.getText())){
                    cargo= lp.findPublishingCargoByID(t1.getText());
                }

                useCaseTest7Cargo = cargo;
                int cargoStatus = cargo.getCargoStatus();
                if (cargoStatus == 1 ){

                    int choose = JOptionPane.showConfirmDialog(null,
                                    "\n订单编号 "+cargo.getID()
                                    +"由于该订单还没有被接单，发货方无需支付补偿" +"\n确定要撤单吗？",
                            "撤单提示",0);
                    if(choose == 0){
                        JOptionPane.showMessageDialog(null,"撤单成功","成功",1);
                        testEvent = 7;
                    }else {
                        JOptionPane.showMessageDialog(null,"撤单取消","取消",1);
                        return;
                    }
                    result1.setText("撤单成功！");
                    result2.setText("由于货物当前未被接单");
                    result3.setText("发货方可以直接撤单");
                }
                if (cargoStatus == 2 ){
                    result1.setText("撤单成功！");
                    result2.setText("由于货物当前为已接未运状态");
                    result3.setText("发货方补偿接单承运方一定运费");

                    int choose = JOptionPane.showConfirmDialog(null,"接单车辆 "
                            +cargo.getMyTruck().getID()+"\n订单编号 "+cargo.getID()+"\n订单剩余时间"+cargo.getRemainHour()
                            +"\n由于该订单已经被接单，发货方要支付补偿" + (int)(cargo.getFreightFare()* getWITHDRAWALFEERATIO())+"\n确定要撤单吗？",
                            "撤单提示",0);
                    if(choose == 0){
                        JOptionPane.showMessageDialog(null,"撤单成功","成功",1);
                        testEvent = 7;
                    }else {
                        JOptionPane.showMessageDialog(null,"撤单取消","取消",1);
                        return;
                    }
                }
                if (cargoStatus == 3) {
                    if(!bank.enoughAmountWith(cargo.getShipper(),cargo.getFreightFare())) {
                        //资金不足以撤单
                        result1.setText("撤单失败！");
                        result2.setText("货物已经在运，需要支付双倍运费");
                        result3.setText("该发货方资金不足以支付额外运费");
                        return;
                    }
                    if(!guarantor.enoughAmountWith(cargo.getMyTruck(),cargo.getLiquidatedDamages())) {
                        //接单车辆担保额不足以撤单
                        result1.setText("撤单失败！");
                        result2.setText("货物已经在运，需要支付双倍运费");
                        result3.setText("接单车辆担保额度无法满足额外的运输");
                        return;
                    }else {
                        int choose = JOptionPane.showConfirmDialog(null,"接单车辆 "
                                        +cargo.getMyTruck().getID()+"\n订单编号 "+cargo.getID()+"\n订单剩余时间"+cargo.getRemainHour()
                                        +"\n由于该订单已经在运，发货方要支付双倍运费" + (int)(cargo.getFreightFare()*2)+"\n确定要撤单吗？",
                                "撤单提示",0);
                        if(choose == 0){
                            JOptionPane.showMessageDialog(null,"撤单成功","成功",1);
                            testEvent = 7;
                        }else {
                            JOptionPane.showMessageDialog(null,"撤单取消","取消",1);
                        }
                    }
                }
                if (cargoStatus == 4){
                    JOptionPane.showMessageDialog(null,"该订单已经完成，无法撤单","撤单失败",0);
                    return;
                }

            }
        });
    }


    /**
     *  抢单    该用例不需要向主函数传递信息 ，暂时不使用
     *TODO:检查有问题，暂时不用，毕竟已经有了专门窗口
     */
    void initUseCase8(JPanel jPanel){
        //输入订单ID，展示抢单信息，（发货方改成 润廷公司）
        final JLabel caption = new JLabel("用例 抢单");
        JLabel caption2 = new JLabel("请输入要展示抢单的订单Id");
        final JTextField Id = new JTextField();
        JButton confirm = new JButton("确定");
        final JTextArea cardTextArea = new JTextArea("",14,18);
        initTextArea(cardTextArea,jPanel,65);
        //组件位置设置
        caption.setBounds(60,10,100,30);
        caption2.setBounds(20,40,160,30);
        Id.setBounds(30,300,80,35);
        confirm.setBounds(120,300,80,35);
        // 设置Timer 300ms实现一次动作 实际是一个线程   
        Timer timeAction = new Timer(300, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(timeSwitch){
                        timeCount++;
                        if(timeCount == 1){
                            cardTextArea.append(useCaseTest8Cargo.cargoDescriptionWithoutOrderPrice());
                        }
                        if(timeCount == 6){
                            cardTextArea.append("\n抢单开始!\n");
                        }
                        int n = useCaseTest8Cargo.getTempbid().size();

                        if(timeCount == 8){
                           Truck truck = lp.getTrucksInPlatform().get(3);
                           if(useCaseTest8Cargo.getTempbid().containsKey(truck)){
                               cardTextArea.append(truck.getID()+"出价"+useCaseTest8Cargo.getTempbid().get(truck)+"\n");
                           }
                        }
                        if(timeCount == 9){
                            Truck truck = lp.getTrucksInPlatform().get(5);
                            if(useCaseTest8Cargo.getTempbid().containsKey(truck)){
                                cardTextArea.append(truck.getID()+"出价"+useCaseTest8Cargo.getTempbid().get(truck)+"\n");
                            }
                        }
                        if(timeCount == 13){
                            Truck truck = lp.getTrucksInPlatform().get(0);
                            if(useCaseTest8Cargo.getTempbid().containsKey(truck)){
                                cardTextArea.append(truck.getID()+"出价"+useCaseTest8Cargo.getTempbid().get(truck)+"\n");
                            }
                        }
                        if(timeCount == 14){
                            Truck truck = lp.getTrucksInPlatform().get(7);
                            if(useCaseTest8Cargo.getTempbid().containsKey(truck)){
                                cardTextArea.append(truck.getID()+"出价"+useCaseTest8Cargo.getTempbid().get(truck)+"\n");
                            }
                        }
                        if(timeCount == 16){
                            Truck truck = lp.getTrucksInPlatform().get(6);
                            if(useCaseTest8Cargo.getTempbid().containsKey(truck)){
                                cardTextArea.append(truck.getID()+"出价"+useCaseTest8Cargo.getTempbid().get(truck)+"\n");
                            }
                        }
                        if(timeCount == 20){
                            Truck truck = lp.getTrucksInPlatform().get(2);
                            if(useCaseTest8Cargo.getTempbid().containsKey(truck)){
                                cardTextArea.append(truck.getID()+"出价"+useCaseTest8Cargo.getTempbid().get(truck)+"\n");
                            }
                        }
                        if(timeCount == 23){
                            Truck truck = lp.getTrucksInPlatform().get(4);
                            if(useCaseTest8Cargo.getTempbid().containsKey(truck)){
                                cardTextArea.append(truck.getID()+"出价"+useCaseTest8Cargo.getTempbid().get(truck)+"\n");
                            }
                        }
                        if(timeCount == 28){
                            Truck truck = lp.getTrucksInPlatform().get(1);
                            if(useCaseTest8Cargo.getTempbid().containsKey(truck)){
                                cardTextArea.append(truck.getID()+"出价"+useCaseTest8Cargo.getTempbid().get(truck)+"\n");
                            }
                        }
                        if(timeCount == 30){
                            cardTextArea.append("\n时间截止！\n");
                        }
                        if(timeCount == 33){
                            cardTextArea.append("有效出价中最低的承运方为：\n" + useCaseTest8Cargo.getMyTruck().getID() + "\n其出价为" + useCaseTest8Cargo.getOrderPrice());
                        }
                    }
                }
        });
        timeAction.start();
        // 确认按钮监听
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //确认操作，随时间依次展示要呈现的结果
                String id = Id.getText();
                if(!lp.haveCargoByID(id)){
                    cardTextArea.setText("");
                    cardTextArea.append("订单ID错误，请重新输入");
                    return;
                }else{
                    useCaseTest8Cargo = lp.findCargoByID(id);
                    // 随时间依次展示要呈现的结果
                    cardTextArea.setText("");
                    cardTextArea.append("润廷广播给承运方的信息如下：\n");
                    timeSwitch = true;
                    timeCount = 0;
                }
            }});
        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(Id);
        jPanel.add(confirm);
    }

    /**
     *  订单转手
     */
    void initUseCase9(JPanel jPanel){
        //演示的时候最好暂停，因为可能订单可能已经运完。输入正在执行的订单，展示订单信息，选择要转手给与的车辆ID
        //目前只处理已接未运的订单转手
        final JLabel caption = new JLabel("用例 订单转手");
        JLabel caption2 = new JLabel("请输入要进行转手的订单");
        final JTextField t1 = new JTextField();
        JButton showCargo = new JButton("确认");
        JLabel show1 = new JLabel("当前订单信息如下");
        final JLabel show2 = new JLabel("发货方:          货物类型:");
        final JLabel show3 = new JLabel("运费报价:         违约金:");
        JLabel show4 = new JLabel("出发地           目的地");
        final JLabel show5 = new JLabel("最迟天数：        备注：");
        final JLabel result1 = new JLabel("-----------------------");

        JLabel moneyOffer = new JLabel("转手报价");
        JTextField moneyOfferNum = new JTextField();

        JButton confirm = new JButton("订单转手");
        //组件位置设置
        int interval = 24;
        caption.setBounds(100,10,120,30);
        caption2.setBounds(60,40,160,30);
        t1.setBounds(80,40 + 1 * interval ,60,30);
        showCargo.setBounds(150,40 + 1 * interval ,70,30);
        show1.setBounds(60,40 + 2 * interval,260,30);
        show2.setBounds(60,40 + 3 * interval,260,30);
        show3.setBounds(60,40 + 4 * interval,260,30);
        show4.setBounds(60,40 + 5 * interval,260,30);
        show5.setBounds(60,40 + 6 * interval,260,30);
        result1.setBounds(60,40 + 7 * interval,260,30);

        moneyOffer.setBounds(80,40 + 10 * interval,80,30);
        moneyOfferNum.setBounds(140,40 + 10 * interval,80,30);
        confirm.setBounds(120,40 + 11 * interval,80,35);
        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(t1);
        jPanel.add(showCargo);
        jPanel.add(show1);
        jPanel.add(show2);
        jPanel.add(show3);
        jPanel.add(show4);
        jPanel.add(show5);
        jPanel.add(result1);
        jPanel.add(confirm);
        jPanel.add(moneyOffer);
        jPanel.add(moneyOfferNum);
        // 展示订单的按钮监听 检查并返回id是否存在，存在则在show中展示其信息
        showCargo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(t1.getText().equals("")){
                    JOptionPane.showMessageDialog(null,"设置不正确","请重新设置",0);
                    return;
                }else if(!lp.haveCargoByID(t1.getText())){
                    JOptionPane.showMessageDialog(null,"该货物不存在","请重新设置",0);
                }else {
                    useCaseTest9Cargo = lp.findCargoByID(t1.getText());
                    String s1 = "润廷";
                    String s2 = "未知";
                    int i2 = useCaseTest9Cargo.getNeededCarryType();
                    switch (i2){
                        case 1:
                            s2 = "常规";
                            break;
                        case 2:
                            s2 = "冷藏";
                            break;
                        case 3:
                            s2 = "危险品";
                            break;
                    }
                    int s3 = (int)useCaseTest9Cargo.getFreightFare();
                    int s4 = (int)useCaseTest9Cargo.getLiquidatedDamages();
                    int s5 = useCaseTest9Cargo.getRemainHour();
                    String s6 = useCaseTest9Cargo.getRemarks();
                    show2.setText("发货方:"+s1+ "    货物类型:"+s2);
                    show3.setText("运费报价:"+s3+"    违约金:"+s4);
                    if(s5 > 0) {
                        show5.setText("剩余时间:"+s5+ "    备注:"+s6);
                    } else {
                        show5.setText("订单已经超时:"+ (0-s5)+ "    备注:"+s6);
                    }
                    //不符合转手的订单则拒绝订单转手 :时间充足 ，当前已接未运
                    if(useCaseTest9Cargo.getRemainHour() > 5  && useCaseTest9Cargo.getCargoStatus() == 2){
                        orderCanbeChange = true;
                        result1.setText(useCaseTest9Cargo.getID()+"号订单符合转手条件");
                        JOptionPane.showMessageDialog(null,"该订单可以转手","条件满足",1);
                    }
                    else {
                        result1.setText(useCaseTest9Cargo.getID()+"号订单不符合转手条件");
                        JOptionPane.showMessageDialog(null,"该订单不可转手\n原因是订单剩余时间不足或者订单已经被运","条件不满足",0);
                    }
                }
            }
        });

        // 确认按钮监听 检查并返回id是否存在，对承运方先判断其能否接收该订单，展示订单转手完成
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if(orderCanbeChange){

                    if(moneyOfferNum.getText().equals("")) {
                        JOptionPane.showMessageDialog(null,"请输入转手报价","错误",0);
                        return;
                    }
                    int temp = Integer.parseInt(moneyOfferNum.getText());
                    if(temp < 0) {
                        JOptionPane.showMessageDialog(null,"请输入合理的报价","错误",0);
                        return;
                    }

                    testEvent = 9;
                    useCaseTest9Cargo = lp.findCargoByID(t1.getText());
                    useCaseTest9CargoOfferMoney = temp;
                    JOptionPane.showMessageDialog(null,"该订单已经发布到公告栏","订单转手开始",1);
                    orderCanbeChange = false;
                } else {
                    JOptionPane.showMessageDialog(null,"请先选择符合条件的转手订单","错误",0);
                }
            }
        });
    }

    /**
     *  订单完成    该用例不需要向主函数传递信息
     */
    void initUseCase10(JPanel jPanel){
        JLabel caption = new JLabel("用例 订单完成");
        JLabel caption2 = new JLabel("按时完成订单列表");
        JLabel caption3 = new JLabel("超时订单列表");
        //组件位置设置
        caption.setBounds(80,10,100,30);
        caption2.setBounds(20,50,160,30);
        caption3.setBounds(160,50,160,30);
        usecase10_intime.setLineWrap(true);
        usecase10_intime.setForeground(Color.BLACK);
        usecase10_intime.setFont(new Font("楷体",Font.ROMAN_BASELINE,12));
        usecase10_intime.setBackground(Color.lightGray);
        JScrollPane jsp1=new JScrollPane(usecase10_intime);
        jsp1.setBounds(10,90,130,240);
        usecase10_overtime.setLineWrap(true);
        usecase10_overtime.setForeground(Color.BLACK);
        usecase10_overtime.setFont(new Font("楷体",Font.ROMAN_BASELINE,12));
        usecase10_overtime.setBackground(Color.lightGray);
        JScrollPane jsp2=new JScrollPane(usecase10_overtime);
        jsp2.setBounds(150,90,140,240);
        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(caption3);
        jPanel.add(jsp1);
        jPanel.add(jsp2);
    }

    public void refreshUseCase10(){
        usecase10_intime.setText("");
        usecase10_overtime.setText("");
        int n = lp.getCargosInPlatform().size();
       for(int i = 0; i < n; i++) {
           Cargo cargo = lp.getCargosInPlatform().get(i);
           if(cargo.getCargoStatus() == 4){
               if(cargo.isOverTime()){
                   usecase10_overtime.append("订单"+ cargo.getID()+"超时"+(0-cargo.getRemainHour())+"小时\n");
               } else {
                   usecase10_intime.append("订单"+cargo.getID()+"按时完成\n");
               }
           }
       }
    }

    /**
     *  订单超时    该用例不需要向主函数传递信息
     */
    void initUseCase11(JPanel jPanel){
        //输入超时时长，返回超时的处理结果
        JLabel caption = new JLabel("用例 订单超时");
        JLabel caption2 = new JLabel("请输入假设超时的订单");
        final JTextField t1 = new JTextField();
        JLabel caption3 = new JLabel("请输入超时时长");
        final JTextField t2 = new JTextField();
        JLabel caption4 = new JLabel("订单超时处理结果如下：");
        JButton confirm = new JButton("确认");
        final JLabel result1 = new JLabel("--------------------");
        final JLabel result2 = new JLabel("-");
        final JLabel result3 = new JLabel("-");
        //组件位置设置
        int interval = 35;
        caption.setBounds(60,10,120,30);
        caption2.setBounds(20,10 + 1 * interval,160,30);
        t1.setBounds(20,10 + 2 * interval,60,30);
        caption3.setBounds(20,10 + 3 * interval,160,30);
        t2.setBounds(20,10 + 4 * interval,60,30);
        caption4.setBounds(20,10 + 5 * interval,160,30);
        result1.setBounds(20,10 + 6 * interval,260,30);
        result2.setBounds(20,10 + 7 * interval,260,30);
        result3.setBounds(20,10 + 8 * interval,260,30);
        confirm.setBounds(120,10 + 4 * interval,60,30);
        //添加进panel
        jPanel.add(caption);
        jPanel.add(caption2);
        jPanel.add(t1);
        jPanel.add(caption3);
        jPanel.add(t2);
        jPanel.add(caption4);
        jPanel.add(result1);
        jPanel.add(result2);
        jPanel.add(result3);
        jPanel.add(confirm);
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(t1.getText().equals("")|| t2.getText().equals("")){
                    JOptionPane.showMessageDialog(null,"设置不正确","请重新设置",0);
                    return;
                }else if(!lp.haveCargoByID(t1.getText())){
                    JOptionPane.showMessageDialog(null,"该货物不存在","请重新设置",0);
                }
                else if(Integer.parseInt(t2.getText())<0){
                    JOptionPane.showMessageDialog(null,"时间设置不能为空","请重新设置",0);
                }else {
                    Cargo cargo = lp.findCargoByID(t1.getText());
                    int over = Integer.parseInt(t2.getText());
                    result1.setText("订单" + cargo.getID()+"的接单运费为" +cargo.getOrderPrice());
                    result2.setText("每超过一个小时扣除运费5%");
                    result3.setText("其承运方"+cargo.getMyTruck().getID()+"需支付" + (int)(cargo.getOrderPrice()*0.05*over));
                }
            }
        });
    }

    public LogisticsPlatform getLp() {
        return lp;
    }

    public void setLp(LogisticsPlatform lp) {
        this.lp = lp;
    }

    public int getTestEvent() {
        return testEvent;
    }

    public void setTestEvent(int testEvent) {
        this.testEvent = testEvent;
    }

    public String[] getUseCaseNames() {
        return useCaseNames;
    }

    public void setUseCaseNames(String[] useCaseNames) {
        this.useCaseNames = useCaseNames;
    }

    public JButton[] getSelectedUseCaseButtons() {
        return selectedUseCaseButtons;
    }

    public void setSelectedUseCaseButtons(JButton[] selectedUseCaseButtons) {
        this.selectedUseCaseButtons = selectedUseCaseButtons;
    }

    public JLabel[] getUseCaseTitleLables() {
        return useCaseTitleLables;
    }

    public void setUseCaseTitleLables(JLabel[] useCaseTitleLables) {
        this.useCaseTitleLables = useCaseTitleLables;
    }

    public JComboBox[] getjComboBoxes() {
        return jComboBoxes;
    }

    public void setjComboBoxes(JComboBox[] jComboBoxes) {
        this.jComboBoxes = jComboBoxes;
    }

    public JTextArea[] getShowTextAreas() {
        return showTextAreas;
    }

    public void setShowTextAreas(JTextArea[] showTextAreas) {
        this.showTextAreas = showTextAreas;
    }

    public JPanel getCards() {
        return cards;
    }

    public CardLayout getCl() {
        return cl;
    }

    public JPanel[] getjPanels() {
        return jPanels;
    }

    public void setjPanels(JPanel[] jPanels) {
        this.jPanels = jPanels;
    }

    public RoadModel getRoadModel() {
        return roadModel;
    }

    public void setRoadModel(RoadModel roadModel) {
        this.roadModel = roadModel;
    }

    public RandomGenerator getRng() {
        return rng;
    }

    public void setRng(RandomGenerator rng) {
        this.rng = rng;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public Guarantor getGuarantor() {
        return guarantor;
    }

    public void setGuarantor(Guarantor guarantor) {
        this.guarantor = guarantor;
    }

    public boolean isHavaUpload() {
        return havaUpload;
    }

    public void setHavaUpload(boolean havaUpload) {
        this.havaUpload = havaUpload;
    }

    public String getPara1() {
        return para1;
    }

    public void setPara1(String para1) {
        this.para1 = para1;
    }

    public int getPara2() {
        return para2;
    }

    public void setPara2(int para2) {
        this.para2 = para2;
    }

    public int getPara3() {
        return para3;
    }

    public void setPara3(int para3) {
        this.para3 = para3;
    }

    public Truck getPara4() {
        return para4;
    }

    public void setPara4(Truck para4) {
        this.para4 = para4;
    }

    public String getPara5() {
        return para5;
    }

    public void setPara5(String para5) {
        this.para5 = para5;
    }

    public Shipper getPara6() {
        return para6;
    }

    public void setPara6(Shipper para6) {
        this.para6 = para6;
    }

    public String getPara7() {
        return para7;
    }

    public void setPara7(String para7) {
        this.para7 = para7;
    }

    public Receiver getPara8() {
        return para8;
    }

    public void setPara8(Receiver para8) {
        this.para8 = para8;
    }

    public Cargo getUseCaseTest6Cargo() {
        return useCaseTest6Cargo;
    }

    public void setUseCaseTest6Cargo(Cargo useCaseTest6Cargo) {
        this.useCaseTest6Cargo = useCaseTest6Cargo;
    }

    public Cargo getUseCaseTest7Cargo() {
        return useCaseTest7Cargo;
    }

    public void setUseCaseTest7Cargo(Cargo useCaseTest7Cargo) {
        this.useCaseTest7Cargo = useCaseTest7Cargo;
    }

    public Cargo getUseCaseTest8Cargo() {
        return useCaseTest8Cargo;
    }

    public void setUseCaseTest8Cargo(Cargo useCaseTest8Cargo) {
        this.useCaseTest8Cargo = useCaseTest8Cargo;
    }

    public boolean isTimeSwitch() {
        return timeSwitch;
    }

    public void setTimeSwitch(boolean timeSwitch) {
        this.timeSwitch = timeSwitch;
    }

    public int getTimeCount() {
        return timeCount;
    }

    public void setTimeCount(int timeCount) {
        this.timeCount = timeCount;
    }

    public Cargo getUseCaseTest9Cargo() {
        return useCaseTest9Cargo;
    }

    public void setUseCaseTest9Cargo(Cargo useCaseTest9Cargo) {
        this.useCaseTest9Cargo = useCaseTest9Cargo;
    }

    public boolean isOrderCanbeChange() {
        return orderCanbeChange;
    }

    public void setOrderCanbeChange(boolean orderCanbeChange) {
        this.orderCanbeChange = orderCanbeChange;
    }

    public Truck getUseCaseTest9Truck() {
        return useCaseTest9Truck;
    }

    public void setUseCaseTest9Truck(Truck useCaseTest9Truck) {
        this.useCaseTest9Truck = useCaseTest9Truck;
    }

    public JTextArea getUsecase10_intime() {
        return usecase10_intime;
    }

    public void setUsecase10_intime(JTextArea usecase10_intime) {
        this.usecase10_intime = usecase10_intime;
    }

    public JTextArea getUsecase10_overtime() {
        return usecase10_overtime;
    }

    public void setUsecase10_overtime(JTextArea usecase10_overtime) {
        this.usecase10_overtime = usecase10_overtime;
    }

    public JButton[] getBackButtons() {
        return backButtons;
    }

    public void setBackButtons(JButton[] backButtons) {
        this.backButtons = backButtons;
    }
}
