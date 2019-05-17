package cn.tycoding.entity.LogisticsPlatform; /**
 * 平台信息界面
 * 设置：setBounds(850,50,200,300);
 *
 * 收益展示
 * 今日累计收益 00 下面一行小字昨日收益
 * 本月累计收益 00 下面一行小字，上个月收益
 * 本年累计收益 00 下面一行小字，去年累计收益
 *
 * 计时开始，展示年月日
 *
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import static com.github.rinde.rinsim.examples.LogisticsPlatform.TruckLogistics.getBonusMaxRatio;
import static com.github.rinde.rinsim.examples.LogisticsPlatform.TruckLogistics.getProfitShareRatio1;
import static com.github.rinde.rinsim.examples.LogisticsPlatform.TruckLogistics.getProfitShareRatio2;

public class JFramePlatformProfit extends JFrame
{
    private DecimalFormat df = new DecimalFormat("0000.0");
    private final LogisticsPlatform  lp;
    private  Bank bank;
    private double BONUS_MAX_RATIO = 1;
    private double PROFIT_SHARE_RATIO1 = 1;
    private double PROFIT_SHARE_RATIO2 = 1;
    private double totalProfit;

    double todayProfit = 0;
    double yesterdayProfit = 0;
    double thisMonthProfit = 0;
    double lastMonthProfit = 0;
    double thisYearProfit = 0;

    // 收益基准在点击计时开始的时候 进行重新赋值。计时结束的时候进行清零
    // 收益基准，真正展示的收益计算是平台累计收益减去收益基准计算而得
    double profitBase = 0;
    // 今日收益基准
    double todayProfitBase = 0;
    // 本月收益基准
    double thisMonthProfitBase = 0;

    //计时开关
    boolean timeingSwitchOn = false;
    // 计时开始的时间
    double timeStart = -1;

    // 展示平台当前收益的地方
    JLabel showLineUp = new JLabel(  "*--------------------------------*") ;
    JLabel showPlatformProfitToday = new JLabel("平台今日累计收入：0000000");
    JLabel showPlatformProfitYestreday = new JLabel("昨日收入：000000");
    JLabel showPlatformProfitThisMonth = new JLabel("本月收入：000000");
    JLabel showPlatformProfitLastMonth = new JLabel("上个月收入：000000");
    JLabel showPlatformProfitThisYear = new JLabel("今年收入：000000");
    JLabel showLineDown = new JLabel("*--------------------------------*") ;
    JLabel showTime = new JLabel("模拟时间已经经过： 00年00月00日"); //加粗
    JButton buttonTimeStart = new JButton("计时开始");
    JButton buttonTimeEnd = new JButton("计时结束");

    // 展示最近结算的订单
    JLabel showFreightSettlement = new JLabel("最新结算的订单详情如下:                                       ");
    // 这2项在参数设置后要改变 //展示最近结算的订单
    JLabel showRatio = new JLabel();
    JTextArea newOrderSettlementRecord = new JTextArea("",11,23);
    JPanel jp = new JPanel();
    JButton buttonClear=new JButton("清空");

    JFramePlatformProfit(final LogisticsPlatform lp, Bank bank) {
        this.lp = lp;
        this.bank = bank;
        this.BONUS_MAX_RATIO = getBonusMaxRatio();
        this.PROFIT_SHARE_RATIO1 = getProfitShareRatio1();
        this.PROFIT_SHARE_RATIO2 = getProfitShareRatio2();

        setTitle("【平台】收益展示 及 结算记录");
        setBounds(850,50,280,450);
//        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        showPlatformProfitToday.setFont(new Font("楷体",Font.BOLD,18));
        showFreightSettlement.setFont(new Font("楷体",Font.LAYOUT_NO_LIMIT_CONTEXT,11));
        showRatio.setText("(利润分享比例为"+ PROFIT_SHARE_RATIO1 + "," + PROFIT_SHARE_RATIO2 +
                ",红包限额为原运费"+ BONUS_MAX_RATIO +")                       ");
        showRatio.setFont(new Font("楷体",Font.HANGING_BASELINE,9));
        jp.add(showLineUp );
        jp.add(showPlatformProfitToday);
        jp.add(showPlatformProfitYestreday);
        jp.add(showPlatformProfitThisMonth);
        jp.add(showPlatformProfitLastMonth);
        jp.add(showPlatformProfitThisYear);
        jp.add(showLineDown );
        jp.add(showTime);
        this.setTimer(showTime);
        jp.add(buttonTimeStart);
        jp.add(buttonTimeEnd);

        jp.add(showFreightSettlement);
        jp.add(showRatio);
        newOrderSettlementRecord.setLineWrap(true);
        newOrderSettlementRecord.setForeground(Color.BLACK);
        newOrderSettlementRecord.setFont(new Font("楷体",Font.ROMAN_BASELINE,12));
        newOrderSettlementRecord.setBackground(Color.lightGray);
        JScrollPane jsp=new JScrollPane(newOrderSettlementRecord);
        Dimension size=newOrderSettlementRecord.getPreferredSize();    //获得文本域的首选大小
        jsp.setBounds(110,90,size.width,size.height);
        jp.add(jsp);

        // 为Frame窗口设置布局为BorderLayout
        setLayout(new BorderLayout());
        add(jp,BorderLayout.CENTER);
        add(buttonClear,BorderLayout.SOUTH);

        timeingSwitchOn = true;
        timeStart = System.currentTimeMillis();
        // 把计时开始这一刻的平台利润作为基准，计算从此刻开始往后的各阶段收入
        profitBase = lp.getTrueProfit();

        // 计时开始按钮行为监听
        buttonTimeStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeingSwitchOn = true;
                timeStart = System.currentTimeMillis();
                // 把计时开始这一刻的平台利润作为基准，计算从此刻开始往后的各阶段收入
                profitBase = lp.getTrueProfit();

            }
        });

        //计时结束按钮行为监听
        buttonTimeEnd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeingSwitchOn = false;
                timeStart = 0;
                todayProfit = 0;
                yesterdayProfit = 0;
                thisMonthProfit = 0;
                lastMonthProfit = 0;
                thisYearProfit = 0;
                profitBase = 0;
                todayProfitBase = 0;
                thisMonthProfitBase = 0;
            }
        });

        //清空按钮监听
        buttonClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newOrderSettlementRecord.setText("");
            }
        });

    }

    // 设置Timer 1000ms实现一次动作 实际是一个线程   
    private void setTimer(JLabel time) {
        final JLabel varTime = time;
        Timer timeAction = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (timeingSwitchOn) {
                    //模拟时间的展示
                    int year;
                    int month;
                    int day;
                    int hour;
                    long currentTime = System.currentTimeMillis();
                    double duration = currentTime - timeStart;
                    int totalHour = (int)duration / 1000;
                    hour = totalHour % 24;
                    int totalDay = totalHour / 24;
                    day = totalDay % 30;
                    int totalMonth = totalDay / 30;
                    month = totalMonth % 12;
                    year = totalMonth / 12;
                    // 转换日期显示格式   
                    varTime.setText("模拟时间已经过：" + year + "年" + month + "月" + day + "天" + hour + "小时");

                    //模拟阶段收入的展示
                    double profit = lp.getTrueProfit() - profitBase;

                    //每小时更新今日收益表
                    todayProfit = profit - todayProfitBase;
                    //每小时更新本月收益表，也可以放在每天更新
                    thisMonthProfit = profit - thisMonthProfitBase;

                    //每24小时更新 今日收益表(清零) + 昨日收益表（保存原今日结算)   更新每日收入基准
                    if ((totalHour % 24 == 0) && (totalHour > 10)) {
                        yesterdayProfit = todayProfit;
                        todayProfitBase = profit;
                        todayProfit = 0;
                    }
                    //每24小时*30天更新 本月收益表 + 上个月收益表
                    if ((totalHour % (24 * 30) == 0) && (totalHour > 10)) {
                        lastMonthProfit = thisMonthProfit;
                        thisMonthProfitBase = profit;
                        thisMonthProfit = 0;
                    }
                    //年收入直接为总收入
                    thisYearProfit = profit;
                    //最终展示界面
                    showPlatformProfitToday.setText("平台今日收入："+ df.format(todayProfit));
                    showPlatformProfitYestreday.setText("昨日收入：" + df.format(yesterdayProfit));
                    showPlatformProfitThisMonth.setText("本月收入：" + df.format(thisMonthProfit));
                    showPlatformProfitLastMonth.setText("上月收入：" + df.format(lastMonthProfit));
                    showPlatformProfitThisYear.setText("今年收入：" + df.format(thisYearProfit));
                }

                if (timeStart == -1) {
                    varTime.setText("点击【计时开始】，进行平台收益模拟！");
                }
                if (timeStart == 0) {
                    varTime.setText("模拟结束！点击【计时开始】重新模拟收入！");
                }
             }
        });
        timeAction.start();
    }
    public void setPlatformProfit() {
        showPlatformProfitToday.setText("平台当前收益为:" + df.format(lp.getTrueProfit()));
        totalProfit = lp.getTrueProfit();
    }

    public void addNewOrderSettlementRecord(String str) {
        newOrderSettlementRecord.append("\n" + str);
    }
}