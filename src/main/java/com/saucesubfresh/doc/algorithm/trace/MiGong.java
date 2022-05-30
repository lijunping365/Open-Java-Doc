package com.saucesubfresh.doc.algorithm.trace;

/**
 * 回溯算法解迷宫问题
 *
 * 以一个M×N的长方阵表示迷宫，0和1分别表示迷宫中的通路和障碍。设计程序，对任意设定的迷宫，求出从入口到出口的所有通路。
 *
 * 下面我们来详细讲一下迷宫问题的回溯算法。
 *
 * 该图是一个迷宫的图。1代表是墙不能走，0是可以走的路线。只能往上下左右走，直到从左上角到右下角出口。
 *
 * 做法是用一个二维数组来定义迷宫的初始状态，然后从左上角开始，不停的去试探所有可行的路线，碰到1就结束本次路径，
 *
 * 然后探索其他的方向，当然我们要标记一下已经走的路线，不能反复的在两个可行的格子之间来回走。直到走到出口为止，算找到了一个正确路径。
 *
 * 程序如下，具体做法看注释即可。
 *
 * 我把打印每一步路径判断的地方注释掉了，放开注释就能看到所有走的路径。
 *     程序执行效率是非常快，基本上是在3ms之内得到所有路径。
 *     原本只看图时我还以为只有3条路径，没想到程序打出来了8条。后来仔细看看，果然是有8条路径……
 *
 *     打印结果如下，5是用来标记路径的：
 *
 * 1458551044499
 * 得到一个解：
 * 5 5 1 0 0 0 1 0
 * 5 5 1 0 0 0 1 0
 * 5 0 1 0 1 1 0 1
 * 5 1 1 1 0 0 1 0
 * 5 5 5 1 5 5 5 0
 * 0 1 5 5 5 1 5 1
 * 0 1 1 1 1 0 5 1
 * 1 1 0 0 0 1 5 1
 * 1 1 0 0 0 0 5 0
 * 得到一个解：
 * 5 5 1 0 0 0 1 0
 * 5 5 1 0 0 0 1 0
 * 5 0 1 0 1 1 0 1
 * 5 1 1 1 5 5 1 0
 * 5 5 5 1 5 5 5 0
 * 0 1 5 5 5 1 5 1
 * 0 1 1 1 1 0 5 1
 * 1 1 0 0 0 1 5 1
 * 1 1 0 0 0 0 5 0
 * 得到一个解：
 * 5 5 1 0 0 0 1 0
 * 0 5 1 0 0 0 1 0
 * 5 5 1 0 1 1 0 1
 * 5 1 1 1 0 0 1 0
 * 5 5 5 1 5 5 5 0
 * 0 1 5 5 5 1 5 1
 * 0 1 1 1 1 0 5 1
 * 1 1 0 0 0 1 5 1
 * 1 1 0 0 0 0 5 0
 * 得到一个解：
 * 5 5 1 0 0 0 1 0
 * 0 5 1 0 0 0 1 0
 * 5 5 1 0 1 1 0 1
 * 5 1 1 1 5 5 1 0
 * 5 5 5 1 5 5 5 0
 * 0 1 5 5 5 1 5 1
 * 0 1 1 1 1 0 5 1
 * 1 1 0 0 0 1 5 1
 * 1 1 0 0 0 0 5 0
 * 得到一个解：
 * 5 0 1 0 0 0 1 0
 * 5 5 1 0 0 0 1 0
 * 5 5 1 0 1 1 0 1
 * 5 1 1 1 0 0 1 0
 * 5 5 5 1 5 5 5 0
 * 0 1 5 5 5 1 5 1
 * 0 1 1 1 1 0 5 1
 * 1 1 0 0 0 1 5 1
 * 1 1 0 0 0 0 5 0
 * 得到一个解：
 * 5 0 1 0 0 0 1 0
 * 5 5 1 0 0 0 1 0
 * 5 5 1 0 1 1 0 1
 * 5 1 1 1 5 5 1 0
 * 5 5 5 1 5 5 5 0
 * 0 1 5 5 5 1 5 1
 * 0 1 1 1 1 0 5 1
 * 1 1 0 0 0 1 5 1
 * 1 1 0 0 0 0 5 0
 * 得到一个解：
 * 5 0 1 0 0 0 1 0
 * 5 0 1 0 0 0 1 0
 * 5 0 1 0 1 1 0 1
 * 5 1 1 1 0 0 1 0
 * 5 5 5 1 5 5 5 0
 * 0 1 5 5 5 1 5 1
 * 0 1 1 1 1 0 5 1
 * 1 1 0 0 0 1 5 1
 * 1 1 0 0 0 0 5 0
 * 得到一个解：
 * 5 0 1 0 0 0 1 0
 * 5 0 1 0 0 0 1 0
 * 5 0 1 0 1 1 0 1
 * 5 1 1 1 5 5 1 0
 * 5 5 5 1 5 5 5 0
 * 0 1 5 5 5 1 5 1
 * 0 1 1 1 1 0 5 1
 * 1 1 0 0 0 1 5 1
 * 1 1 0 0 0 0 5 0
 * 1458551044503
 * ————————————————
 * 版权声明：本文为CSDN博主「天涯泪小武」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
 * 原文链接：https://blog.csdn.net/tianyaleixiaowu/article/details/50948031
 *
 *
 *
 * @author lijunping on 2022/5/30
 */
public class MiGong {

    /**
     * 定义迷宫数组
     */
    private int[][] array = {
            {0, 0, 1, 0, 0, 0, 1, 0},
            {0, 0, 1, 0, 0, 0, 1, 0},
            {0, 0, 1, 0, 1, 1, 0, 1},
            {0, 1, 1, 1, 0, 0, 1, 0},
            {0, 0, 0, 1, 0, 0, 0, 0},
            {0, 1, 0, 0, 0, 1, 0, 1},
            {0, 1, 1, 1, 1, 0, 0, 1},
            {1, 1, 0, 0, 0, 1, 0, 1},
            {1, 1, 0, 0, 0, 0, 0, 0}
    };

    private int maxLine = 8;
    private int maxRow = 9;

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        new MiGong().check(0, 0);
        System.out.println(System.currentTimeMillis());
    }

    private void check(int i, int j) {
        //如果到达右下角出口
        if (i == maxRow - 1 && j == maxLine - 1) {
            print();
            return;
        }

        //向右走
        if (canMove(i, j, i, j + 1)) {
            array[i][j] = 5;
            check(i, j + 1);
            array[i][j] = 0;
        }
        //向左走
        if (canMove(i, j, i, j - 1)) {
            array[i][j] = 5;
            check(i, j - 1);
            array[i][j] = 0;
        }
        //向下走
        if (canMove(i, j, i + 1, j)) {
            array[i][j] = 5;
            check(i + 1, j);
            array[i][j] = 0;
        }
        //向上走
        if (canMove(i, j, i - 1, j)) {
            array[i][j] = 5;
            check(i - 1, j);
            array[i][j] = 0;
        }
    }

    private boolean canMove(int i, int j, int targetI, int targetJ) {
//        System.out.println("从第" + (i + 1) + "行第" + (j + 1) + "列，走到第" + (targetI + 1) + "行第" + (targetJ + 1) + "列");
        if (targetI < 0 || targetJ < 0 || targetI >= maxRow || targetJ >= maxLine) {
//            System.out.println("到达最左边或最右边，失败了");
            return false;
        }
        if (array[targetI][targetJ] == 1) {
//            System.out.println("目标是墙，失败了");
            return false;
        }
        //避免在两个空格间来回走
        if (array[targetI][targetJ] == 5) {
//            System.out.println("来回走，失败了");
            return false;
        }

        return true;
    }

    private void print() {
        System.out.println("得到一个解：");
        for (int i = 0; i < maxRow; i++) {
            for (int j = 0; j < maxLine; j++) {
                System.out.print(array[i][j] + " ");
            }
            System.out.println();
        }
    }

}
