package cn.tycoding.entity.LogisticsPlatform; /**
 * 设置界面，程序运行开始的时候启动，设置完成后关闭
 *
 *
 *
 *  ~~~~~~~~~~~~~平台参数设置~~~~~~~~~~~~~
 * 利润分享比例：百分之多少     默认 33%
 *
 * 红包上限为运费的百分比：     默认 5%
 *
 * 抢单报价下限：承运方报价必须要高于这个值
 *
 * 展位费设置：
 *
 * 已接未运货物撤单收费百分比：  默认20%
 *
 *
 * ~~~~~~~~~~~~~~~~~模拟器设置~~~~~~~~~~
 * 货车数量:直接指定个数                 默认： 10
 * 货源:多 中 少                        默认：中
 * 货车抢单意愿:高 中 低                 默认: 中
 * 货车选择开启过滤器意愿：高 中 低        默认：中
 *
 *
 * ~~~~~~~~~~~~~~~功能设置~~~~~~~~~~~~~~~
 *
 * 承运方直接评级：可以指定给某些货车车牌号，直接对其评级进行设定。设定评级的车辆之后场合不再动态变化
 *
 *
 * ~~~~~~~~~~~~~~~~~可选设置~~~~~~~~~~~~~
 * 自动模式的开关
 * 发货方重新提交订单概率
 * 出现撤单概率
 * 已接未运货物撤单收费百分比
 * 货车担保额度
 * 非普通货物出现概率
 * 发货方重新提交订单增加运费的额度
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class JFrameSetup extends JFrame {
    private JPanel panelTop = new JPanel();
    private JPanel panelCenter = new JPanel();
    private JPanel panelDown = new JPanel();
    private JComboBox cmb = new JComboBox();

    boolean setUpImplemented = false;
    LogisticsPlatform lp;

    // 正上方的标题
    private JLabel caption = new JLabel("平台参数以及模拟器设置");
    // ~~~~~~~~~~~~~~~~~~功能设置~~~~~~~~~~~~~~~~~~
    private JLabel platformParameterSetting = new JLabel("~~~~~~~~~~~~~~~~~~平台参数设置~~~~~~~~~~~~~~~~~~");
    // 利润分享比例 默认 33%
    private JLabel profitShareRatio = new JLabel("利润分享百分比设置，请保证三者之和为100");

    private JLabel t11 = new JLabel("发货方");
    private JLabel t12 = new JLabel("承运方");
    private JLabel t13 = new JLabel("平台方");
    private JTextField t11t=new JTextField(2);
    private JTextField t12t=new JTextField(2);
    private JTextField t13t=new JTextField(2);
    // 红包上限为运费的百分比：     默认 5%
    private JLabel bonusMaxRatio = new JLabel("红包最多为运费的");
    private JTextField t2=new JTextField(2);
    // 抢单报价最低为原运费的：*%  默认30%
    private JLabel minOrderPrice = new JLabel("报价最低为原运费的");
    private JTextField t3=new JTextField(2);
    // 展位费设置： 默认1
    private JLabel exitbitionFee = new JLabel("展位费为");
    private JTextField t4=new JTextField(2);
    // 撤单时，已接未运收费比例：  默认10%
    private JLabel withdrawalFee = new JLabel("已接未运撤单收取原运费");
    private JTextField t5=new JTextField(2);

    // ~~~~~~~~~~~~~~~~~模拟器设置~~~~~~~~~~~~~~~~
    private JLabel simulatorSetting = new JLabel("~~~~~~~~~~~~~~~~~~模拟器设置~~~~~~~~~~~~~~~~~~");
    //货车数量:直接指定个数                 默认： 10
    private JLabel truckNum = new JLabel("货车数量为");
    private JTextField t6=new JTextField(2);
    // 货源:多 中 少                        默认：中
    private JLabel CargoAmount = new JLabel("货源: ");
    private ButtonGroup group1=new ButtonGroup();                 //JRadioButton 通常位于一个 ButtonGroup 按钮组中
    private JRadioButton group1Rb1=new JRadioButton("多");    //创建JRadioButton对象
    private JRadioButton group1Rb2=new JRadioButton("中");
    private JRadioButton group1Rb3=new JRadioButton("少");

    // 货车抢单意愿:高 中 低                 默认: 中
    private JLabel willingBidding = new JLabel("货车抢单意愿:");
    private ButtonGroup group2=new ButtonGroup();                 //JRadioButton 通常位于一个 ButtonGroup 按钮组中
    private JRadioButton group2Rb1=new JRadioButton("高");    //创建JRadioButton对象
    private JRadioButton group2Rb2=new JRadioButton("中");
    private JRadioButton group2Rb3=new JRadioButton("少");

    // 货车选择开启过滤器意愿：高 中 低        默认：中
    private JLabel chooseFilter = new JLabel("货车只抢附近订单意愿：");
    private ButtonGroup group3=new ButtonGroup();                 //JRadioButton 通常位于一个 ButtonGroup 按钮组中
    private JRadioButton group3Rb1=new JRadioButton("高");    //创建JRadioButton对象
    private JRadioButton group3Rb2=new JRadioButton("中");
    private JRadioButton group3Rb3=new JRadioButton("低");

    // ~~~~~~~~~~~~~~~功能设置~~~~~~~~~~~~~~~
    private JLabel functionSetting = new JLabel("~~~~~~~~~~~~~~~~~~~其他功能~~~~~~~~~~~~~~~~~~~");
    // 承运方直接评级：可以指定给某些货车车牌号，直接对其评级进行设定。设定评级的车辆之后场合不再动态变化
    private JLabel rankTruck = new JLabel("承运方直接评级(评级范围为1~10)");
    private JLabel rankTruck1 = new JLabel("输入车牌号和级别，直接令其评级固定为该值");
    private JTextField truckID = new JTextField("");
    private  JTextField ranking = new JTextField("");
    private JButton buttonReset = new JButton("恢复默认设置");
    private JButton buttonSubmit = new JButton("提交");

    public JFrameSetup(LogisticsPlatform lp) {
        this.lp = lp;
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(null);
        setTitle("基础属性设置");
        setBounds(200, 100, 380, 600);

        caption.setBounds(130,20,300,20);
        add(caption);

        platformParameterSetting.setBounds(10,50,380,25);
        add(platformParameterSetting);

        profitShareRatio.setBounds(50,80,300,25);
        add(profitShareRatio);

        t11.setBounds(50,110,50,25);
        t11t.setBounds(100,110,30,25);
        t12.setBounds(140,110,50,25);
        t12t.setBounds(190,110,30,25);
        t13.setBounds(230,110,50,25);
        t13t.setBounds(280,110,30,25);
        add(t11);
        add(t11t);
        add(t12);
        add(t12t);
        add(t13);
        add(t13t);

        bonusMaxRatio.setBounds(50,140,300,25);
        add(bonusMaxRatio);
        t2.setBounds(200,140,30,25);
        //t2.setText("5");
        add(t2);
        JLabel mark2 = new JLabel("%");
        mark2.setBounds(230,140,300,25);
        add(mark2);

        minOrderPrice.setBounds(50,170,300,25);
        add(minOrderPrice);
        t3.setBounds(200,170,30,25);
        //t3.setText("30");
        add(t3);
        JLabel mark3 = new JLabel("%");
        mark3.setBounds(230,170,300,25);
        add(mark3);

        exitbitionFee.setBounds(50,200,300,25);
        add(exitbitionFee);
        t4.setBounds(200,200,30,25);
        //t4.setText("1");
        add(t4);
        JLabel mark4 = new JLabel("元");
        mark4.setBounds(230,200,300,25);
        add(mark4);

        withdrawalFee.setBounds(50,230,300,25);
        add(withdrawalFee);
        t5.setBounds(200,230,30,25);
        //t5.setText("10");
        add(t5);
        JLabel mark5 = new JLabel("%");
        mark5.setBounds(230,230,300,25);
        add(mark5);

        simulatorSetting.setBounds(10,260,380,25);
        add(simulatorSetting);

        truckNum.setBounds(50,290,300,25);
        add(truckNum);
        t6.setBounds(200,290,30,25);
        //t6.setText("5");
        add(t6);
        JLabel mark6 = new JLabel("辆");
        mark6.setBounds(230,290,300,25);
        add(mark6);

        CargoAmount.setBounds(50,320,300,25);
        add(CargoAmount);
        group1Rb1.setBounds(200,320,50,25);
        group1Rb2.setBounds(250,320,50,25);
        group1Rb2.setSelected(true);
        group1Rb3.setBounds(300,320,50,25);
        group1.add(group1Rb1);
        group1.add(group1Rb2);
        group1.add(group1Rb3);
        add(group1Rb1);
        add(group1Rb2);
        add(group1Rb3);

        willingBidding.setBounds(50,350,300,25);
        add(willingBidding);
        group2Rb1.setBounds(200,350,50,25);
        group2Rb2.setBounds(250,350,50,25);
        group2Rb2.setSelected(true);
        group2Rb3.setBounds(300,350,50,25);
        group2.add(group2Rb1);
        group2.add(group2Rb2);
        group2.add(group2Rb3);
        add(group2Rb1);
        add(group2Rb2);
        add(group2Rb3);

        chooseFilter.setBounds(50,380,380,25);
        add(chooseFilter);
        group3Rb1.setBounds(200,380,50,25);
        group3Rb2.setBounds(250,380,50,25);
        group3Rb2.setSelected(true);
        group3Rb3.setBounds(300,380,50,25);
        group3.add(group3Rb1);
        group3.add(group3Rb2);
        group3.add(group3Rb3);
        add(group3Rb1);
        add(group3Rb2);
        add(group3Rb3);

        functionSetting.setBounds(10,410,380,25);
        add(functionSetting);

        rankTruck.setBounds(50,430,300,25);
        add(rankTruck);

        rankTruck1.setText("输入车牌号和级别，直接令其评级固定为该值");
        rankTruck1.setBounds(50,460,300,20);
        add(rankTruck1);

        //truckID.setText("沪 A1000");
        truckID.setBounds(100,480,80,30);
        add(truckID);

        //ranking.setText("10");
        ranking.setBounds(200,480,80,30);
        add(ranking);

        buttonSubmit.setText("提交");
        buttonSubmit.setBounds(205,520,150,40);
        add(buttonSubmit);
        buttonSubmit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //如果已经设置完成，则该按钮无任何实际效果
                if (setUpImplemented) {
                    JOptionPane.showMessageDialog(null,"本设置已经应用！\n" +
                            "如果需要 改变参数 请重启！","无法在运行中更改",0);
                    return;
                }

                //检查参数合理性，记录不合理的参数原因并展示
                String error = "提交失败！ 原因如下：\n";
                int errorline = 1;

                // 只要有一个参数不正确，submitFailed 就置为 true
                boolean submitfailed= false;

                //对参数设置入额`逐个检查并记录错误信息
                if (!isTwoNum(t11t.getText())) {
                    submitfailed = true;
                    error = error + "利润分享比例设置错误，请输入0~99的数字\n";
                    errorline++;
                }
                if (!isTwoNum(t2.getText())) {
                    submitfailed = true;
                    error = error + "红包上限设置错误，请输入0~99的数字\n";
                    errorline++;
                }
                if ((!isTwoNum(t3.getText())) || isZero(t3.getText())) {
                    submitfailed = true;
                    error = error + "抢单报价最低百分比设置错误，请输入1~99数字\n";
                }
                if ((!isTwoNum(t4.getText())) || isZero(t4.getText())) {
                    submitfailed = true;
                    error = error + "展位费设置错误，请输入1~99的数字\n";
                }
                if ((!isTwoNum(t5.getText())) || isZero(t5.getText())) {
                    submitfailed = true;
                    error = error + "已接未运撤单收取原运费必须为1%~99%\n";
                }
                if ((!isTwoNum(t6.getText()))) {
                    submitfailed = true;
                    error = error + "货车数量必须合理数字！\n";
                }

                // 保证 评级的等级在1~10
                if (!isTwoNum(ranking.getText())) {
                    submitfailed = true;
                    error = error + "评级的级别框中请输入1~10的数字！\n";
                } else {
                    int i = Integer.parseInt(ranking.getText());
                    if (i < 1 || i > 10) {
                        submitfailed = true;
                        error = error + "评级的级别框中请输入1~10的数字！\n";
                    }
                }

                // 参数出现错误则返回错误信息重新填写；没有错误则提示提交成功，向程序传递修改后的参数
                if (submitfailed) {
                    //展示提交出错窗口
                    System.out.println(error);
                    JOptionPane.showMessageDialog(null,error,"请重新设置",0);
                } else {
                    //展示提交成功窗口
                    //通知程序可以运行，使用新的参数进行模拟
                    JOptionPane.showMessageDialog(null,"设置成功 \n可以启动模拟器了！","成功",1);

                    //设置成功后，将界面设置为不可修改
                    t11t.setEditable(false);
                    t12t.setEditable(false);
                    t13t.setEditable(false);

                    t2.setEditable(false);
                    t3.setEditable(false);
                    t4.setEditable(false);
                    t5.setEditable(false);
                    t6.setEditable(false);
                    ranking.setEditable(false);
                    truckID.setEditable(false);
                    String fileName="/Users/yuzhongzhiheren/Desktop/LogisticsPlatform/example/src/main/java/com/github/rinde/rinsim/examples/LogisticsPlatform/config.txt";
                    String line="";

                    BufferedReader in= null;
                    try {
                        //
                        FileWriter writer = new FileWriter(fileName);
                        writer.write(t11t.getText()+"\n");
                        writer.write(t12t.getText()+"\n");
                        writer.write(t13t.getText()+"\n");
                        writer.write(t2.getText()+"\n");
                        writer.write(t3.getText()+"\n");
                        writer.write(t4.getText()+"\n");
                        writer.write(t5.getText()+"\n");
                        writer.write(t6.getText()+"\n");
                        writer.write(truckID.getText()+"\n");
                        writer.write(ranking.getText()+"\n");
                        writer.close();
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    setUpImplemented = true;
                }
            }
        });

        buttonReset.setText("设为默认值");
        buttonReset.setBounds(45,520,150,40);
        add(buttonReset);
        buttonReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String fileName="/Users/yuzhongzhiheren/Desktop/LogisticsPlatform/example/src/main/java/com/github/rinde/rinsim/examples/LogisticsPlatform/config.txt";
                String line="";
                 try {
                    BufferedReader in=new BufferedReader(new FileReader(fileName));
                    line=in.readLine();
                    t11t.setText(line);
                    line=in.readLine();
                    t12t.setText(line);
                    line=in.readLine();
                    t13t.setText(line);
                    line=in.readLine();
                    t2.setText(line);
                    line=in.readLine();
                    t3.setText(line);
                    line=in.readLine();
                    t4.setText(line);
                    line=in.readLine();
                    t5.setText(line);
                    line=in.readLine();
                    t6.setText(line);
                    line=in.readLine();
                    truckID.setText(line);
                    line=in.readLine();
                    ranking.setText(line);
                    in.close();
                } catch (IOException e1)
                {
                    e1.printStackTrace();
                }
                group1Rb1.setSelected(false);
                group1Rb2.setSelected(true);
                group1Rb3.setSelected(false);
                group2Rb1.setSelected(false);
                group2Rb2.setSelected(true);
                group2Rb3.setSelected(false);
                group3Rb1.setSelected(false);
                group3Rb2.setSelected(true);
                group3Rb3.setSelected(false);
            }
        });
    }

    // 判断是否是2位数的数字
    private boolean isTwoNum(String text) {
        if (text == null) {
            return false;
        } else if (text.length() == 0 ||text.length() > 2) {
            return false;
        } else if (text.length() == 2) {
            char firstChar =  text.charAt(0);
            char secondChar =  text.charAt(1);
            boolean true1 = firstChar <= 57 && firstChar >=48;
            boolean true2 = secondChar <= 57 && firstChar >=48;

            if (true1&&true2) {
                return true;
            } else {
                return false;
            }
        } else {
            //只有一个数字
            char firstChar =  text.charAt(0);
            if (firstChar <= 57 && firstChar >= 48) {
                return true;
            } else {
                return false;
            }
        }
    }
    private boolean isZero(String text) {
        int i = Integer.parseInt(text);
        if (i == 0) {
            return true;
        } else {
            return false;
        }
    }

    public JPanel getPanelTop() {
        return panelTop;
    }

    public void setPanelTop(JPanel panelTop) {
        this.panelTop = panelTop;
    }

    public JPanel getPanelCenter() {
        return panelCenter;
    }

    public void setPanelCenter(JPanel panelCenter) {
        this.panelCenter = panelCenter;
    }

    public JPanel getPanelDown() {
        return panelDown;
    }

    public void setPanelDown(JPanel panelDown) {
        this.panelDown = panelDown;
    }

    public JComboBox getCmb() {
        return cmb;
    }

    public void setCmb(JComboBox cmb) {
        this.cmb = cmb;
    }

    public boolean isSetUpImplemented() {
        return setUpImplemented;
    }

    public void setSetUpImplemented(boolean setUpImplemented) {
        this.setUpImplemented = setUpImplemented;
    }

    public LogisticsPlatform getLp() {
        return lp;
    }

    public void setLp(LogisticsPlatform lp) {
        this.lp = lp;
    }

    public JLabel getCaption() {
        return caption;
    }

    public void setCaption(JLabel caption) {
        this.caption = caption;
    }

    public JLabel getPlatformParameterSetting() {
        return platformParameterSetting;
    }

    public void setPlatformParameterSetting(JLabel platformParameterSetting) {
        this.platformParameterSetting = platformParameterSetting;
    }

    public JLabel getProfitShareRatio() {
        return profitShareRatio;
    }

    public void setProfitShareRatio(JLabel profitShareRatio) {
        this.profitShareRatio = profitShareRatio;
    }

    public JLabel getBonusMaxRatio() {
        return bonusMaxRatio;
    }

    public void setBonusMaxRatio(JLabel bonusMaxRatio) {
        this.bonusMaxRatio = bonusMaxRatio;
    }

    public JTextField getT2() {
        return t2;
    }

    public void setT2(JTextField t2) {
        this.t2 = t2;
    }

    public JLabel getMinOrderPrice() {
        return minOrderPrice;
    }

    public void setMinOrderPrice(JLabel minOrderPrice) {
        this.minOrderPrice = minOrderPrice;
    }

    public JTextField getT3() {
        return t3;
    }

    public void setT3(JTextField t3) {
        this.t3 = t3;
    }

    public JLabel getExitbitionFee() {
        return exitbitionFee;
    }

    public void setExitbitionFee(JLabel exitbitionFee) {
        this.exitbitionFee = exitbitionFee;
    }

    public JTextField getT4() {
        return t4;
    }

    public void setT4(JTextField t4) {
        this.t4 = t4;
    }

    public JLabel getWithdrawalFee() {
        return withdrawalFee;
    }

    public void setWithdrawalFee(JLabel withdrawalFee) {
        this.withdrawalFee = withdrawalFee;
    }

    public JTextField getT5() {
        return t5;
    }

    public void setT5(JTextField t5) {
        this.t5 = t5;
    }

    public JLabel getSimulatorSetting() {
        return simulatorSetting;
    }

    public void setSimulatorSetting(JLabel simulatorSetting) {
        this.simulatorSetting = simulatorSetting;
    }

    public JLabel getTruckNum() {
        return truckNum;
    }

    public void setTruckNum(JLabel truckNum) {
        this.truckNum = truckNum;
    }

    public JTextField getT6() {
        return t6;
    }

    public void setT6(JTextField t6) {
        this.t6 = t6;
    }

    public JLabel getCargoAmount() {
        return CargoAmount;
    }

    public void setCargoAmount(JLabel cargoAmount) {
        CargoAmount = cargoAmount;
    }

    public ButtonGroup getGroup1() {
        return group1;
    }

    public void setGroup1(ButtonGroup group1) {
        this.group1 = group1;
    }

    public JRadioButton getGroup1Rb1() {
        return group1Rb1;
    }

    public void setGroup1Rb1(JRadioButton group1Rb1) {
        this.group1Rb1 = group1Rb1;
    }

    public JRadioButton getGroup1Rb2() {
        return group1Rb2;
    }

    public void setGroup1Rb2(JRadioButton group1Rb2) {
        this.group1Rb2 = group1Rb2;
    }

    public JRadioButton getGroup1Rb3() {
        return group1Rb3;
    }

    public void setGroup1Rb3(JRadioButton group1Rb3) {
        this.group1Rb3 = group1Rb3;
    }

    public JLabel getWillingBidding() {
        return willingBidding;
    }

    public void setWillingBidding(JLabel willingBidding) {
        this.willingBidding = willingBidding;
    }

    public ButtonGroup getGroup2() {
        return group2;
    }

    public void setGroup2(ButtonGroup group2) {
        this.group2 = group2;
    }

    public JRadioButton getGroup2Rb1() {
        return group2Rb1;
    }

    public void setGroup2Rb1(JRadioButton group2Rb1) {
        this.group2Rb1 = group2Rb1;
    }

    public JRadioButton getGroup2Rb2() {
        return group2Rb2;
    }

    public void setGroup2Rb2(JRadioButton group2Rb2) {
        this.group2Rb2 = group2Rb2;
    }

    public JRadioButton getGroup2Rb3() {
        return group2Rb3;
    }

    public void setGroup2Rb3(JRadioButton group2Rb3) {
        this.group2Rb3 = group2Rb3;
    }

    public JLabel getChooseFilter() {
        return chooseFilter;
    }

    public void setChooseFilter(JLabel chooseFilter) {
        this.chooseFilter = chooseFilter;
    }

    public ButtonGroup getGroup3() {
        return group3;
    }

    public void setGroup3(ButtonGroup group3) {
        this.group3 = group3;
    }

    public JRadioButton getGroup3Rb1() {
        return group3Rb1;
    }

    public void setGroup3Rb1(JRadioButton group3Rb1) {
        this.group3Rb1 = group3Rb1;
    }

    public JRadioButton getGroup3Rb2() {
        return group3Rb2;
    }

    public void setGroup3Rb2(JRadioButton group3Rb2) {
        this.group3Rb2 = group3Rb2;
    }

    public JRadioButton getGroup3Rb3() {
        return group3Rb3;
    }

    public void setGroup3Rb3(JRadioButton group3Rb3) {
        this.group3Rb3 = group3Rb3;
    }

    public JLabel getFunctionSetting() {
        return functionSetting;
    }

    public void setFunctionSetting(JLabel functionSetting) {
        this.functionSetting = functionSetting;
    }

    public JLabel getRankTruck() {
        return rankTruck;
    }

    public void setRankTruck(JLabel rankTruck) {
        this.rankTruck = rankTruck;
    }

    public JLabel getRankTruck1() {
        return rankTruck1;
    }

    public void setRankTruck1(JLabel rankTruck1) {
        this.rankTruck1 = rankTruck1;
    }

    public JTextField getTruckID() {
        return truckID;
    }

    public void setTruckID(JTextField truckID) {
        this.truckID = truckID;
    }

    public JTextField getRanking() {
        return ranking;
    }

    public void setRanking(JTextField ranking) {
        this.ranking = ranking;
    }

    public JButton getButtonReset() {
        return buttonReset;
    }

    public void setButtonReset(JButton buttonReset) {
        this.buttonReset = buttonReset;
    }

    public JButton getButtonSubmit() {
        return buttonSubmit;
    }

    public void setButtonSubmit(JButton buttonSubmit) {
        this.buttonSubmit = buttonSubmit;
    }

    public JLabel getT11() {
        return t11;
    }

    public void setT11(JLabel t11) {
        this.t11 = t11;
    }

    public JLabel getT12() {
        return t12;
    }

    public void setT12(JLabel t12) {
        this.t12 = t12;
    }

    public JLabel getT13() {
        return t13;
    }

    public void setT13(JLabel t13) {
        this.t13 = t13;
    }

    public JTextField getT11t() {
        return t11t;
    }

    public void setT11t(JTextField t11t) {
        this.t11t = t11t;
    }

    public JTextField getT12t() {
        return t12t;
    }

    public void setT12t(JTextField t12t) {
        this.t12t = t12t;
    }

    public JTextField getT13t() {
        return t13t;
    }

    public void setT13t(JTextField t13t) {
        this.t13t = t13t;
    }
}

