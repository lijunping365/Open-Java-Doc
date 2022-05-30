package com.saucesubfresh.doc.algorithm.trace;

/**
 * 八皇后问题，是一个古老而著名的问题，是回溯算法的典型案例。该问题是国际西洋棋棋手马克斯·贝瑟尔于1848年提出：在8×8格的国际象棋上摆放八个皇后，使其不能互相攻击，即任意两个皇后都不能处于同一行、同一列或同一斜线上，问有多少种摆法。
 *
 * 思路是按行来规定皇后，第一行放第一个皇后，第二行放第二个，然后通过遍历所有列，来判断下一个皇后能否放在该列。直到所有皇后都放完，或者放哪都不行。
 *
 * 详细一点说，第一个皇后先放第一行第一列，然后第二个皇后放在第二行第一列、然后判断是否OK，然后第二列、第三列、依次把所有列都放完，找到一个合适，继续第三个皇后，还是第一列、第二列……直到第8个皇后也能放在一个不冲突的位置，算是找到了一个正确解。然后回头继续第一个皇后放第二列，后面继续循环……
 * ————————————————
 *     版权声明：本文为CSDN博主「天涯泪小武」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
 *     原文链接：https://blog.csdn.net/tianyaleixiaowu/article/details/50945054
 *
 * 以上就是所有代码。逻辑还比较简单，逐行判断，依次遍历，直到找到该行合适列才进入下一行。
 *
 * @author lijunping on 2022/5/30
 */
public class WolfQueen {

    /**
     * 一共有多少个皇后（此时设置为8皇后在8X8棋盘，可以修改此值来设置N皇后问题）
     */
    int max = 8;
    /**
     * 该数组保存结果，第一个皇后摆在array[0]列，第二个摆在array[1]列
     */
    int[] array = new int[max];

    public static void main(String[] args) {
        new WolfQueen().check(0);
    }

    /**
     * n代表当前是第几个皇后
     * @param n
     * 皇后n在array[n]列
     */
    private void check(int n) {
        //终止条件是最后一行已经摆完，由于每摆一步都会校验是否有冲突，所以只要最后一行摆完，说明已经得到了一个正确解
        if (n == max) {
            print();
            return;
        }
        //从第一列开始放值，然后判断是否和本行本列本斜线有冲突，如果OK，就进入下一行的逻辑
        for (int i = 0; i < max; i++) {
            array[n] = i;
            if (judge(n)) {
                check(n + 1);
            }
        }
    }

    private boolean judge(int n) {
        for (int i = 0; i < n; i++) {
            if (array[i] == array[n] || Math.abs(n - i) == Math.abs(array[n] - array[i])) {
                return false;
            }
        }
        return true;
    }

    private void print()  {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + 1 + " ");
        }
        System.out.println("===========================");
    }


}
