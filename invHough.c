//
//  invHough.c
//  Hough
//
//  Created by 澤村颯斗 on 2019/06/20.
//  Copyright © 2019年 澤村颯斗. All rights reserved.
//
/* Hough逆変換を行い、直線を抽出するプログラム invHough.c */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "Hough.h"

#define PI 3.141592653589   /* 円周率 */
#define MAX_THETA   360     /* θ軸のサイズ 1[deg]=1画素*/
#define MAX_RHO     720     /* rho軸のサイズ */

void swap_int(int *n1, int *n2)
/* int n1とint n2を入れ替える */
{
    int n;  /* 作業変数 */
    n = *n1;    *n1 = *n2;  *n2 = n;
}

void extract_line(int xs, int ys)
/* θ-rho平面上(image1上)の点(xs, ys)を画像上の直線上の直線に逆変換し、結果をimage2[y][x]に描く */
{
    double theta, rho, rho_max, _sin, _cos;     /* 作業変数 */
    int x,y;        /* 制御変数 */
    
    theta = 180.0 * xs / MAX_THETA;     /* [deg] */
    //_sin = sin()
}
