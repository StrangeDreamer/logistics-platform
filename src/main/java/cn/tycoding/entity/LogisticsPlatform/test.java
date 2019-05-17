package cn.tycoding.entity.LogisticsPlatform;

import com.github.rinde.rinsim.geom.Point;


public class test {
    public static void main(String args[]) throws InterruptedException {



        LogisticsPlatform lp = new LogisticsPlatform(new Point(1,2));

//        //JFrameUseCaseTest jFrameUseCaseTest = new JFrameUseCaseTest(lp);
//
//
//        testCar car = new testCar(1,1);
//
//        testCar newCar;
//
//        newCar = car;
//
//        newCar.setCapacity(2);

//        System.out.println("car.capacit="+ car.getCapacity()+"   newCar.capacit="+ newCar.getCapacity());

        testJFrame testJF = new testJFrame(lp);

        int i = 0;
    }


    public void A(int i){
        i++;
    }

    public void B(Gvars i){
        i.setVar(i.getVar()+1);
    }

    public static class Gvars {
        int var = 0;

        public int getVar() {
            return var;
        }

        public void setVar(int var) {
            this.var = var;
        }
    }


}



