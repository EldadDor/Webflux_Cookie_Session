package com.edx.reactive;

class s implements Runnable {
    int x, y;

    public void run() {
        for (int i = 0; i < 1000; i++)
            synchronized (this) {
                x = 12;
                y = 12;
            }
        System.out.println(x + " " + y + " ");
    }

    public static void main(String args[]) {
//        s run = new s();
//        Thread t1 = new Thread(run);
//        Thread t2 = new Thread(run);
//        t1.start();
//        t2.start();
//        int i = reverseInt(103);
//        System.out.println("i = " + i);
//        int i1 = reverseInt(203);
//        System.out.println("i1 = " + i1);
//        int i2 = reverseInt(1703);
//        System.out.println("i2 = " + i2);
//        int i3 = reverseInt(23);
//        System.out.println("i3 = " + i3);
//        int i4 = reverseInt(478);
//        System.out.println("i4 = " + i4);

        boolean b = checkPalindrome(2122);
        System.out.println("b = " + b);
        boolean b1 = checkPalindrome(1221);
        System.out.println("b1 = " + b1);
        boolean b2 = checkPalindrome(321123);
        System.out.println("b2 = " + b2);
        boolean b3 = checkPalindrome(211232);
        System.out.println("b3 = " + b3);
    }


    public static int reverseInt(int input) {
        StringBuilder sb = new StringBuilder();
        int temp = input, remainder;
        while (input > 0) {
            remainder = input % 10;
            sb.append(remainder);
            input = input / 10;
        }
        return Integer.parseInt(sb.toString());
    }

    public static boolean checkPalindrome(int input) {
        int temp = input;
        int remainder, sum = 0;
        while (input > 0) {
            remainder = input % 10;
            sum = (sum * 10) + remainder;
            input = input / 10;
        }
        return temp == sum;
    }

}
