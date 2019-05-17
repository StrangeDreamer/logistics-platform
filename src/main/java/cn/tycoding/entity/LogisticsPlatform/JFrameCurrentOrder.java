package cn.tycoding.entity.LogisticsPlatform;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import static java.lang.Thread.sleep;
/**
 * 订单展示界面 已弃用
 */
public class JFrameCurrentOrder extends JFrame {

    // 出价上限，超出则认为出价无效
    double bidLimit = 100000;
    // 创建一个JPanel对象,展示报价
    JPanel jp = new JPanel();

    JLabel up1 = new JLabel("                     ");
    JLabel up2 = new JLabel("发货方支付运费：      ");
    JLabel up3 = new JLabel("订单信息如下：                             ");
    JLabel up4 = new JLabel("货物类型:   ;  时间要求:00");
    JLabel up5 = new JLabel("货物重量:000  ; 货物体积:000");
    JLabel up6 = new JLabel("出发地(10,10) ;目的地(10,10)");
    JLabel up7 = new JLabel("货物描述：无                     ");
    JLabel down1 = new JLabel("-----------------------------");
    JLabel down2 = new JLabel("出价列表(最低价前五名)");
    JLabel down3 = new JLabel("    1--00000 出价 ：000   ");
    JLabel down4 = new JLabel("    2--00000 出价 ：000   ");
    JLabel down5 = new JLabel("    3--00000 出价 ：000   ");
    JLabel down6 = new JLabel("    4--00000 出价 ：000   ");
    JLabel down7 = new JLabel("    5--00000 出价 ：000   ");
    JLabel down8 = new JLabel(" 00000 出价最低，获得订单！ ");
    JLabel down9 = new JLabel(" 抢单结果：                        ");

    JFrameCurrentOrder ()
    {
        setTitle("用例 抢单");
        setBounds(1150,50,200,375);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        up2.setFont(new Font("楷体",Font.BOLD,16));
        up3.setFont(new Font("楷体",Font.LAYOUT_NO_LIMIT_CONTEXT,11));
        down2.setFont(new Font("楷体",Font.CENTER_BASELINE,14));
        down9.setFont(new Font("楷体",Font.ITALIC,12));
        down8.setFont(new Font("楷体",Font.CENTER_BASELINE,13));
        jp.add(up1);
        jp.add(up2);
        jp.add(up3);
        jp.add(up4);
        jp.add(up5);
        jp.add(up6);
        jp.add(up7);
        jp.add(down1);
        jp.add(down2);
        jp.add(down3);
        jp.add(down4);
        jp.add(down5);
        jp.add(down6);
        jp.add(down7);
        jp.add(down9);
        jp.add(down8);

        // 为Frame窗口设置布局为BorderLayout
        setLayout(new BorderLayout());
        JButton buttonClear=new JButton("清空");
        buttonClear.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                up1.setText("                        ");
            }
        });

        add(jp,BorderLayout.CENTER);
       // add(buttonClear,BorderLayout.SOUTH);
    }

    public String haveNewOrder(Cargo cargo,LogisticsPlatform lp) {


        int truckN = lp.getTrucksInPlatform().size();
        cargo.getTempbid().size();
        up1.setText("      ***      ");
        try {
            sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        up1.setText("    *******    ");
        try {
            sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        up1.setText("***********");
        try {
            sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<Truck,Integer> map = cargo.getTempbid();
        List<Map.Entry<Truck, Integer>> list = new ArrayList<>();
        //对货物中保存的出价进行排序
        for(Map.Entry<Truck, Integer> entry : map.entrySet()){
            list.add(entry); //将map中的元素放入list中
        }

        list.sort(new Comparator<Map.Entry<Truck, Integer>>(){
            @Override
            public int compare(Map.Entry<Truck, Integer> o1, Map.Entry<Truck, Integer> o2) {
                return o1.getValue()-o2.getValue();}
            //正序 从小到大排列
        });

        String[] showRank = new String[5];

        for(int i = 0; i < 5; i++) {
            showRank[i] = "    "+(i+1) + " :-----                     ";
            if(list.size()>i){
                showRank[i] = "    "+(i+1)+" "+list.get(i).getKey().getID()+"出价 " + list.get(i).getValue() + "   ";
            }
        }

        up1.setText("润廷有新订单发布！ID为 "+cargo.getID());
        up2.setText("发货方支付运费:"+(int)cargo.getFreightFare());
        up4.setText("货物类型:" + MyTool.getTypeString(cargo.getNeededCarryType())  + "; 时间要求:" +cargo.getLimitedHour());
        up5.setText("货物重量:"+cargo.getWeight()+";  货物体积:"+cargo.getVolume());
        up6.setText("出发地(" +(int)MyTool.getCoordinates(cargo.getPickupLocation()).x + ","
                        +(int)MyTool.getCoordinates(cargo.getPickupLocation()).y
                        + ");目的地(" + (int)MyTool.getCoordinates(cargo.getDeliveryLocation()).x + ","
                        +(int)MyTool.getCoordinates(cargo.getDeliveryLocation()).y+")");

        down3.setText(showRank[0]);
        down4.setText(showRank[1]);
        down5.setText(showRank[2]);
        down6.setText(showRank[3]);
        down7.setText(showRank[4]);
        String showWinner = "" + cargo.getMyTruck().getID()+ "以"+(int)cargo.getOrderPrice()+"价格接单！";
        down8.setText(showWinner);

        String showBidding = showRank[0] + "\n" +showRank[1] + "\n" + showRank[2] + "\n" + showRank[3] + "\n" + showRank[4] + "\n";
        return showBidding;

    }
}